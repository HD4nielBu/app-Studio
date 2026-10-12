package io.github.hd4nielbu.lazystudio.adapters.out.ia;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Dificultad;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.TipoDeFallo;

/** El relevo contra proveedores falsos que hablan el formato de chat de OpenAI. */
class TutorConRelevoTest {

    static final String VALIDA = "{\"tipoDeFallo\": \"EJECUCION\", \"primerError\": \"En la línea 2 restaste mal.\", \"pista\": \"Revisa el signo.\"}";

    private final List<HttpServer> servidores = new ArrayList<>();
    private final Ejercicio ejercicio = Ejercicio.crear(UUID.randomUUID(), UUID.randomUUID(), "Simplifica 3x - x + 1",
            null, Dificultad.BASICA, "1 + 2x");

    /** Un proveedor falso que responde siempre lo mismo; cuenta cuántas veces lo llamaron. */
    private TutorConRelevo.Proveedor proveedor(String nombre, int estado, String contenido, AtomicInteger llamadas) throws IOException {
        var servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/chat/completions", intercambio -> {
            llamadas.incrementAndGet();
            var cuerpo = estado == 200 ? "{\"choices\":[{\"message\":{\"content\":\"" + escapar(contenido) + "\"}}]}" : "{\"error\":\"x\"}";
            var bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(estado, bytes.length);
            intercambio.getResponseBody().write(bytes);
            intercambio.close();
        });
        servidor.start();
        servidores.add(servidor);
        return new TutorConRelevo.Proveedor(nombre, "http://127.0.0.1:" + servidor.getAddress().getPort(), "modelo", "clave");
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static TutorConRelevo tutor(TutorConRelevo.Proveedor... proveedores) {
        return new TutorConRelevo(new TutorConRelevo.Config(List.of(proveedores)),
                Clock.fixed(Instant.parse("2026-10-12T12:00:00Z"), ZoneOffset.UTC));
    }

    @AfterEach
    void apagar() {
        servidores.forEach(s -> s.stop(0));
    }

    @Test
    void usaElPrimerProveedorSiResponde() throws IOException {
        var segundo = new AtomicInteger();
        var tutor = tutor(proveedor("a", 200, VALIDA, new AtomicInteger()), proveedor("b", 200, VALIDA, segundo));

        var revision = tutor.revisarFallo(ejercicio, "3x - x = 4x", "4x + 1", 1).orElseThrow();

        assertThat(revision.tipoDeFallo()).isEqualTo(TipoDeFallo.EJECUCION);
        assertThat(revision.pista()).isEqualTo("Revisa el signo.");
        assertThat(segundo).hasValue(0);
    }

    @Test
    void conCuotaAgotadaPasaAlSiguienteYDejaElAgotadoEnPausa() throws IOException {
        var agotado = new AtomicInteger();
        var tutor = tutor(proveedor("a", 429, "", agotado), proveedor("b", 200, VALIDA, new AtomicInteger()));

        assertThat(tutor.revisarFallo(ejercicio, "", "4x + 1", 1)).isPresent();
        assertThat(tutor.revisarFallo(ejercicio, "", "4x + 1", 1)).isPresent();
        assertThat(agotado).as("en pausa, no se le vuelve a preguntar").hasValue(1);
    }

    @Test
    void conUnaCaidaPasaAlSiguiente() throws IOException {
        var tutor = tutor(proveedor("a", 500, "", new AtomicInteger()), proveedor("b", 200, VALIDA, new AtomicInteger()));

        assertThat(tutor.revisarFallo(ejercicio, "", "4x + 1", 1)).isPresent();
    }

    @Test
    void reintentaUnaSolaVezAnteSalidasInvalidas() throws IOException {
        var tercero = new AtomicInteger();
        var tutor = tutor(proveedor("a", 200, "no es JSON", new AtomicInteger()),
                proveedor("b", 200, "{\"tipoDeFallo\": \"OTRO\", \"primerError\": \"x\"}", new AtomicInteger()),
                proveedor("c", 200, VALIDA, tercero));

        assertThat(tutor.revisarFallo(ejercicio, "", "4x + 1", 1)).isEmpty();
        assertThat(tercero).hasValue(0);
    }

    @Test
    void sinProveedoresConClaveNoHayRevision() {
        var tutor = tutor(new TutorConRelevo.Proveedor("a", "http://127.0.0.1:1", "m", ""));

        assertThat(tutor.revisarFallo(ejercicio, "", "4x + 1", 1)).isEmpty();
    }

    @Test
    void validaLaSalida() {
        assertThat(TutorConRelevo.validar("```json\n" + VALIDA + "\n```", 1, "1 + 2x")).as("acepta JSON envuelto").isPresent();
        assertThat(TutorConRelevo.validar(VALIDA.replace("\"Revisa el signo.\"", "null"), 1, "1 + 2x"))
                .as("si se pidió pista, debe venir").isEmpty();
        assertThat(TutorConRelevo.validar(VALIDA.replace("\"Revisa el signo.\"", "null"), 0, "1 + 2x"))
                .as("en el rescate no hace falta pista").isPresent();
        assertThat(TutorConRelevo.validar(VALIDA.replace("Revisa el signo.", "La respuesta es 1+2x"), 1, "1 + 2x"))
                .as("la pista no puede regalar la respuesta (R11)").isEmpty();
    }
}
