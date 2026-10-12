package io.github.hd4nielbu.lazystudio.bootstrap;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/** Base de las pruebas de punta a punta: la app completa contra un PostgreSQL real, llamada por HTTP. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(PostgresDePrueba.class)
abstract class ApiDePrueba {

    @LocalServerPort
    int puerto;

    RestClient http;

    @BeforeEach
    void cliente() {
        http = RestClient.builder().baseUrl("http://localhost:" + puerto + "/api")
                .defaultStatusHandler(HttpStatusCode::isError, (pedido, respuesta) -> { }) // los errores se comprueban en cada prueba
                .build();
    }

    ResponseEntity<Map<String, Object>> enviar(String metodo, String ruta, Object cuerpo) {
        var pedido = http.method(HttpMethod.valueOf(metodo)).uri(ruta);
        if (cuerpo != null) pedido.body(cuerpo);
        return pedido.retrieve().toEntity(new ParameterizedTypeReference<>() { });
    }

    ResponseEntity<List<Map<String, Object>>> lista(String ruta) {
        return http.get().uri(ruta).retrieve().toEntity(new ParameterizedTypeReference<>() { });
    }

    String id(ResponseEntity<Map<String, Object>> respuesta) {
        return (String) respuesta.getBody().get("id");
    }

    String crearLibro() {
        return id(enviar("POST", "/libros", Map.of("nombre", "Cálculo 3 · 2.º parcial", "materia", "Cálculo 3",
                "fechaExamen", "2099-11-03", "tono", "NEUTRO", "tratamiento", "VOS")));
    }
}
