package io.github.hd4nielbu.lazystudio.adapters.out.correccion;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import io.github.hd4nielbu.lazystudio.application.ports.out.VerificadorMatematico;

/** Pregunta al servicio Python con SymPy (carpeta {@code verificador/}). Si no responde, no decide. */
@Component
class VerificadorSympy implements VerificadorMatematico {

    private final RestClient cliente;

    VerificadorSympy(@Value("${lazystudio.verificador.url}") String url) {
        var fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(1));
        // el servicio corta a los 5 s; un margen más y la corrección sigue sin SymPy
        fabrica.setReadTimeout(Duration.ofSeconds(6));
        // con buffer se envía Content-Length; sin él el cuerpo va por partes y el servidor Python lo lee vacío
        this.cliente = RestClient.builder().baseUrl(url)
                .requestFactory(new BufferingClientHttpRequestFactory(fabrica)).build();
    }

    @Override
    public Optional<Boolean> sonEquivalentes(String esperada, String dada) {
        try {
            var respuesta = cliente.post().uri("/equivalentes")
                    .body(Map.of("esperada", esperada, "dada", dada))
                    .retrieve().body(Respuesta.class);
            return Optional.ofNullable(respuesta).map(Respuesta::equivalentes);
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }

    private record Respuesta(Boolean equivalentes) {}
}
