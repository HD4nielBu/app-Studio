package io.github.hd4nielbu.lazystudio.adapters.out.correccion;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

/** Contra un servidor HTTP real que, como el de Python, exige Content-Length. */
class VerificadorSympyTest {

    private HttpServer servidor;

    private String levantar(int estado, String respuesta) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/equivalentes", intercambio -> {
            boolean conLargo = intercambio.getRequestHeaders().getFirst("Content-Length") != null;
            var cuerpo = (conLargo ? respuesta : "{\"error\":\"sin Content-Length\"}").getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(conLargo ? estado : 400, cuerpo.length);
            intercambio.getResponseBody().write(cuerpo);
            intercambio.close();
        });
        servidor.start();
        return "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    @AfterEach
    void apagar() {
        if (servidor != null) servidor.stop(0);
    }

    @Test
    void leeLaRespuestaDelServicio() throws IOException {
        var verificador = new VerificadorSympy(levantar(200, "{\"equivalentes\": true}"));

        assertThat(verificador.sonEquivalentes("1 + 2x", "2*x+1")).contains(true);
    }

    @Test
    void noDecideSiElServicioNoSabe() throws IOException {
        var verificador = new VerificadorSympy(levantar(200, "{\"equivalentes\": null}"));

        assertThat(verificador.sonEquivalentes("1 + 2x", "2x+")).isEmpty();
    }

    @Test
    void noDecideSiElServicioNoResponde() {
        assertThat(new VerificadorSympy("http://127.0.0.1:1").sonEquivalentes("x", "x")).isEmpty();
    }
}
