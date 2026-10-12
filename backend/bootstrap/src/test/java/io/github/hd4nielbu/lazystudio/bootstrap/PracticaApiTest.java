package io.github.hd4nielbu.lazystudio.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Porción 2a de punta a punta: ejercicio → intento con pistas y rescate → semáforo, con la migración V2. */
class PracticaApiTest extends ApiDePrueba {

    String libro;
    String tema;
    List<String> conceptos;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void temario() {
        libro = crearLibro();
        tema = id(enviar("POST", "/libros/" + libro + "/temas", Map.of("nombre", "EDO homogéneas")));
        var dividido = enviar("PUT", "/temas/" + tema + "/conceptos", Map.of("conceptos", List.of("H1", "H2", "H3")));
        conceptos = ((List<Map<String, Object>>) dividido.getBody().get("conceptos")).stream().map(c -> (String) c.get("id")).toList();
    }

    String ejercicio(String concepto) {
        var r = enviar("POST", "/conceptos/" + concepto + "/ejercicios", Map.of("enunciado", "y'' - 3y' + 2y = 0",
                "fuente", "Zill, pág. 140, ej. 12", "dificultad", "MEDIA", "respuestaFinal", "y = C1 e^x + C2 e^(2x)"));
        assertThat(r.getStatusCode().value()).isEqualTo(201);
        assertThat(r.getBody()).doesNotContainKey("respuestaFinal"); // R11
        return id(r);
    }

    Map<String, Object> responder(String intento, String respuesta, int confianza) {
        return enviar("POST", "/intentos/" + intento + "/respuestas",
                Map.of("desarrollo", "r = 1, r = 2", "respuestaFinal", respuesta, "confianza", confianza)).getBody();
    }

    @Test
    void pistasRescateYSolucionSoloAlFinal() {
        var intento = id(enviar("POST", "/ejercicios/" + ejercicio(conceptos.get(0)) + "/intentos", null));

        assertThat(responder(intento, "y = e^x", 3)).containsEntry("estado", "EN_CURSO").containsEntry("pistasUsadas", 1)
                .containsEntry("pistasMaximas", 2);
        assertThat(enviar("GET", "/intentos/" + intento + "/solucion", null).getStatusCode().value()).isEqualTo(400);

        responder(intento, "y = e^x", 2);
        assertThat(responder(intento, "y = e^(2x)", 1)).containsEntry("estado", "RESCATADO");
        assertThat(enviar("GET", "/intentos/" + intento + "/solucion", null).getBody())
                .containsEntry("respuestaFinal", "y = C1 e^x + C2 e^(2x)");
        assertThat(enviar("POST", "/intentos/" + intento + "/respuestas",
                Map.of("respuestaFinal", "x", "confianza", 2)).getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void retomaElIntentoYCorrigeSinImportarEspaciosNiMayusculas() {
        var ejercicio = ejercicio(conceptos.get(0));
        var intento = id(enviar("POST", "/ejercicios/" + ejercicio + "/intentos", null));
        responder(intento, "y = e^x", 2);

        assertThat(id(enviar("POST", "/ejercicios/" + ejercicio + "/intentos", null))).isEqualTo(intento);
        var resuelto = responder(intento, "Y=c1e^x+c2E^(2x)", 2);
        assertThat(resuelto).containsEntry("estado", "RESUELTO");
        assertThat((List<?>) resuelto.get("respuestas")).hasSize(2);
    }

    @Test
    void sinConfianzaNoSeEnvia() {
        var intento = id(enviar("POST", "/ejercicios/" + ejercicio(conceptos.get(0)) + "/intentos", null));

        var r = enviar("POST", "/intentos/" + intento + "/respuestas", Map.of("respuestaFinal", "y = 1"));

        assertThat(r.getStatusCode().value()).isEqualTo(400);
        assertThat(r.getBody().get("detail")).asString().contains("confianza");
    }

    @Test
    void semaforoDelLibro() {
        var bien = id(enviar("POST", "/ejercicios/" + ejercicio(conceptos.get(0)) + "/intentos", null));
        responder(bien, "y = C1 e^x + C2 e^(2x)", 2);
        var mal = id(enviar("POST", "/ejercicios/" + ejercicio(conceptos.get(1)) + "/intentos", null));
        for (int i = 0; i < 3; i++) responder(mal, "no sé", 2);

        assertThat(enviar("GET", "/libros/" + libro + "/semaforo", null).getBody()).containsExactly( // en el orden del temario
                Map.entry(conceptos.get(0), "CON_AYUDA"), Map.entry(conceptos.get(1), "NO_LO_RESUELVE"),
                Map.entry(conceptos.get(2), "SIN_VER"));
    }

    @Test
    void unConceptoConEjerciciosNoSeQuitaPeroElLibroSeBorraEntero() {
        ejercicio(conceptos.get(0));
        responder(id(enviar("POST", "/ejercicios/" + ejercicio(conceptos.get(0)) + "/intentos", null)), "x", 2);

        var r = enviar("PUT", "/temas/" + tema + "/conceptos", Map.of("conceptos", List.of("H2", "H3", "H4")));
        assertThat(r.getStatusCode().value()).isEqualTo(400);
        assertThat(lista("/libros/" + libro + "/temas").getBody().getFirst().get("conceptos")).asList().hasSize(3);

        assertThat(enviar("DELETE", "/libros/" + libro, null).getStatusCode().value()).isEqualTo(204);
    }
}
