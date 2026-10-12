package io.github.hd4nielbu.lazystudio.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

/** Esqueleto que camina: la app arranca, se conecta a un PostgreSQL real y /api/salud lo informa. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresDePrueba.class)
class SaludTest {

    @LocalServerPort
    int puerto;

    @Test
    void saludInformaUpConLaBaseConectada() throws Exception {
        var respuesta = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/salud"))
                        .header("Origin", "http://localhost:1420")
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(respuesta.statusCode()).isEqualTo(200);
        assertThat(respuesta.body()).contains("\"status\":\"UP\"");
        assertThat(respuesta.headers().firstValue("Access-Control-Allow-Origin")).hasValue("http://localhost:1420");
    }
}
