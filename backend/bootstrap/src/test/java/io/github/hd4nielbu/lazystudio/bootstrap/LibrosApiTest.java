package io.github.hd4nielbu.lazystudio.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

/** Porción 1 de punta a punta: HTTP → casos de uso → PostgreSQL (con la migración V1 de Flyway). */
class LibrosApiTest extends ApiDePrueba {

    @Test
    void creaArchivaYReactivaUnLibro() {
        var id = crearLibro();

        assertThat(enviar("GET", "/libros/" + id, null).getBody()).containsEntry("fechaExamen", "2099-11-03")
                .containsEntry("archivado", false);
        assertThat(enviar("POST", "/libros/" + id + "/archivar", null).getBody()).containsEntry("archivado", true);
        assertThat(enviar("POST", "/libros/" + id + "/reactivar", null).getBody()).containsEntry("archivado", false);
        assertThat(lista("/libros").getBody()).extracting(l -> l.get("id")).contains(id);
    }

    @Test
    void rechazaDatosInvalidosConUnMensajeParaElAlumno() {
        var vacio = enviar("POST", "/libros", Map.of("nombre", " ", "materia", "M", "tono", "NEUTRO", "tratamiento", "TU"));
        assertThat(vacio.getStatusCode().value()).isEqualTo(400);
        assertThat(vacio.getBody()).containsEntry("detail", "El nombre del libro no puede estar vacío.");

        var tonoRaro = enviar("POST", "/libros", Map.of("nombre", "L", "materia", "M", "tono", "GRITON", "tratamiento", "TU"));
        assertThat(tonoRaro.getStatusCode().value()).isEqualTo(400);

        assertThat(enviar("GET", "/libros/00000000-0000-0000-0000-000000000000", null).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void elTemarioSeDivideSeCorrigeYSeQuitaSinPerderNada() {
        var libro = crearLibro();
        var tema = (String) enviar("POST", "/libros/" + libro + "/temas", Map.of("nombre", "EDO homogéneas")).getBody().get("id");
        enviar("POST", "/libros/" + libro + "/temas", Map.of("nombre", "Wronskiano"));

        assertThat(enviar("POST", "/libros/" + libro + "/temas", Map.of("nombre", "edo HOMOGÉNEAS")).getStatusCode().value())
                .isEqualTo(400);
        assertThat(enviar("PUT", "/temas/" + tema + "/conceptos", Map.of("conceptos", List.of("H1", "H2"))).getStatusCode().value())
                .isEqualTo(400);

        var antes = conceptos(enviar("PUT", "/temas/" + tema + "/conceptos", Map.of("conceptos", List.of("H1", "H2", "H3"))));
        var despues = conceptos(enviar("PUT", "/temas/" + tema + "/conceptos", Map.of("conceptos", List.of("H3", "H1", "H4"))));
        assertThat(despues).extracting(c -> c.get("nombre")).containsExactly("H3", "H1", "H4");
        assertThat(despues.get(0).get("id")).isEqualTo(antes.get(2).get("id"));
        assertThat(despues.get(1).get("id")).isEqualTo(antes.get(0).get("id"));

        enviar("POST", "/temas/" + tema + "/quitar", null);
        var temas = lista("/libros/" + libro + "/temas").getBody();
        assertThat(temas).extracting(t -> t.get("nombre")).containsExactly("EDO homogéneas", "Wronskiano");
        assertThat(temas.get(0)).containsEntry("enTemario", false);
        assertThat(conceptos(temas.get(0))).hasSize(3);
    }

    @Test
    void eliminarBorraElLibroConSuTemario() {
        var libro = crearLibro();
        var tema = (String) enviar("POST", "/libros/" + libro + "/temas", Map.of("nombre", "T")).getBody().get("id");
        enviar("PUT", "/temas/" + tema + "/conceptos", Map.of("conceptos", List.of("a", "b", "c")));

        assertThat(enviar("DELETE", "/libros/" + libro, null).getStatusCode().value()).isEqualTo(204);
        assertThat(enviar("GET", "/libros/" + libro, null).getStatusCode().value()).isEqualTo(404);
        assertThat(enviar("POST", "/temas/" + tema + "/restituir", null).getStatusCode().value()).isEqualTo(404);
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> conceptos(Object tema) {
        var cuerpo = tema instanceof ResponseEntity<?> r ? r.getBody() : tema;
        return (List<Map<String, Object>>) ((Map<String, Object>) cuerpo).get("conceptos");
    }
}
