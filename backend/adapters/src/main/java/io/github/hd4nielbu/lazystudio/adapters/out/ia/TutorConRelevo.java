package io.github.hd4nielbu.lazystudio.adapters.out.ia;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import io.github.hd4nielbu.lazystudio.application.ports.out.Tutor;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.TipoDeFallo;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Tutor con varias IAs gratuitas en orden. Todas hablan el formato de chat de OpenAI, así que basta un adaptador:
 * cambia solo la URL, el modelo y la clave. Ante una cuota agotada (429) pausa ese proveedor y pasa al siguiente;
 * ante una caída, pasa al siguiente; una salida inválida se reintenta una sola vez con otro proveedor (parte 9).
 */
@Component
@EnableConfigurationProperties(TutorConRelevo.Config.class)
class TutorConRelevo implements Tutor {

    @ConfigurationProperties("lazystudio.ia")
    record Config(List<Proveedor> proveedores) {}

    /** Un proveedor sin clave se ignora: así la app funciona sin IA hasta que el alumno ponga las suyas. */
    record Proveedor(String nombre, String url, String modelo, String clave) {}

    static final int SALIDAS_INVALIDAS_PERMITIDAS = 2;
    static final Duration PAUSA_POR_DEFECTO = Duration.ofMinutes(1);
    private static final Logger log = LoggerFactory.getLogger(TutorConRelevo.class);
    private static final JsonMapper json = JsonMapper.shared();

    private final List<Proveedor> proveedores;
    private final Map<String, RestClient> clientes = new HashMap<>();
    private final Map<String, Instant> pausadoHasta = new HashMap<>();
    private final Clock reloj;

    TutorConRelevo(Config config, Clock reloj) {
        this.reloj = reloj;
        this.proveedores = config.proveedores() == null ? List.of()
                : config.proveedores().stream().filter(p -> p.clave() != null && !p.clave().isBlank()).toList();
        var fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(Duration.ofSeconds(40));
        for (var p : proveedores) {
            clientes.put(p.nombre(), RestClient.builder().baseUrl(p.url())
                    .defaultHeader("Authorization", "Bearer " + p.clave())
                    .requestFactory(new BufferingClientHttpRequestFactory(fabrica)).build());
        }
        log.info("IA: proveedores con clave: {}", proveedores.stream().map(Proveedor::nombre).toList());
    }

    @Override
    public Optional<Revision> revisarFallo(Ejercicio ejercicio, String desarrollo, String respuestaFinal, int pista) {
        var mensajes = List.of(
                Map.of("role", "system", "content", Indicaciones.SISTEMA),
                Map.of("role", "user", "content", Indicaciones.fallo(ejercicio, desarrollo, respuestaFinal, pista)));
        int invalidas = 0;
        for (var p : proveedores) {
            if (pausado(p)) continue;
            var texto = preguntar(p, mensajes);
            if (texto.isEmpty()) continue;
            var revision = validar(texto.get(), pista, ejercicio.respuestaFinal(), lineas(desarrollo));
            if (revision.isPresent()) return revision;
            log.warn("IA {}: salida inválida", p.nombre());
            if (++invalidas >= SALIDAS_INVALIDAS_PERMITIDAS) break;
        }
        return Optional.empty();
    }

    private synchronized boolean pausado(Proveedor p) {
        var hasta = pausadoHasta.get(p.nombre());
        return hasta != null && reloj.instant().isBefore(hasta);
    }

    private synchronized void pausar(Proveedor p, Duration cuanto) {
        pausadoHasta.put(p.nombre(), reloj.instant().plus(cuanto));
    }

    /** El texto de la respuesta, o vacío si el proveedor falló (y queda en pausa si fue por cuota). */
    private Optional<String> preguntar(Proveedor p, List<Map<String, String>> mensajes) {
        try {
            var respuesta = clientes.get(p.nombre()).post().uri("/chat/completions")
                    .body(Map.of("model", p.modelo(), "messages", mensajes, "temperature", 0.2,
                            "response_format", Map.of("type", "json_object")))
                    .retrieve().body(Chat.class);
            return Optional.ofNullable(respuesta).map(Chat::texto);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
                pausar(p, esperaIndicada(e));
                log.warn("IA {}: cuota agotada, en pausa", p.nombre());
            } else {
                log.warn("IA {}: error HTTP {}", p.nombre(), e.getStatusCode().value());
            }
        } catch (RestClientException e) {
            log.warn("IA {}: no responde ({})", p.nombre(), e.getMessage());
        }
        return Optional.empty();
    }

    private static Duration esperaIndicada(RestClientResponseException e) {
        try {
            var segundos = e.getResponseHeaders() == null ? null : e.getResponseHeaders().getFirst("Retry-After");
            return segundos == null ? PAUSA_POR_DEFECTO : Duration.ofSeconds(Long.parseLong(segundos.strip()));
        } catch (NumberFormatException ignorada) {
            return PAUSA_POR_DEFECTO;
        }
    }

    private static int lineas(String desarrollo) {
        return desarrollo == null || desarrollo.isBlank() ? 0 : (int) desarrollo.strip().lines().count();
    }

    /**
     * El código valida lo que dice la IA: formato, tipo de fallo entre los 6 y que la pista no regale la respuesta (R11).
     * Una línea que no existe en el desarrollo se descarta, pero no invalida el resto.
     */
    static Optional<Revision> validar(String texto, int pista, String respuestaEsperada, int lineasDelDesarrollo) {
        try {
            int desde = texto.indexOf('{'), hasta = texto.lastIndexOf('}'); // algunos modelos envuelven el JSON en ```
            if (desde < 0 || hasta < desde) return Optional.empty();
            var salida = json.readValue(texto.substring(desde, hasta + 1), Salida.class);
            if (salida.tipoDeFallo() == null || Arrays.stream(TipoDeFallo.values()).noneMatch(t -> t.name().equals(salida.tipoDeFallo()))
                    || vacio(salida.primerError())) {
                return Optional.empty();
            }
            if (pista > 0 && (vacio(salida.pista()) || regala(salida.pista(), respuestaEsperada))) {
                return Optional.empty();
            }
            if (regala(salida.primerError(), respuestaEsperada)) {
                return Optional.empty();
            }
            var linea = salida.linea() != null && salida.linea() >= 1 && salida.linea() <= lineasDelDesarrollo ? salida.linea() : null;
            return Optional.of(new Revision(TipoDeFallo.valueOf(salida.tipoDeFallo()), salida.primerError().strip(),
                    pista > 0 ? salida.pista().strip() : null, linea));
        } catch (JacksonException e) {
            return Optional.empty();
        }
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    // shortcut: respuestas de menos de 3 caracteres (p. ej. "2") no se revisan, porque aparecerían en casi cualquier texto.
    private static boolean regala(String texto, String respuestaEsperada) {
        var esperada = respuestaEsperada.replaceAll("\\s+", "").toLowerCase();
        return esperada.length() >= 3 && texto.replaceAll("\\s+", "").toLowerCase().contains(esperada);
    }

    record Salida(String tipoDeFallo, String primerError, String pista, Integer linea) {}

    record Chat(List<Opcion> choices) {
        record Opcion(Mensaje message) {}
        record Mensaje(String content) {}

        String texto() {
            return choices == null || choices.isEmpty() || choices.getFirst().message() == null
                    ? null : choices.getFirst().message().content();
        }
    }
}
