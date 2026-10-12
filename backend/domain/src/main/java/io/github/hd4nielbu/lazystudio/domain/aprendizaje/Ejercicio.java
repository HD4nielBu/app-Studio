package io.github.hd4nielbu.lazystudio.domain.aprendizaje;

import static io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException.exigir;
import static io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException.exigirTexto;

import java.util.UUID;

/**
 * Ejercicio del banco de un libro. {@code fuente} es libro, página y número; vacía significa que es propio.
 * La respuesta final no se muestra al alumno salvo en el rescate (R11).
 */
public record Ejercicio(UUID id, UUID libroId, UUID conceptoId, String enunciado, String fuente,
                        Dificultad dificultad, String respuestaFinal) {

    public Ejercicio {
        exigir(id, "el identificador");
        exigir(libroId, "el libro");
        exigir(conceptoId, "el concepto");
        enunciado = exigirTexto(enunciado, "El enunciado");
        fuente = fuente == null || fuente.isBlank() ? null : fuente.strip();
        exigir(dificultad, "la dificultad");
        respuestaFinal = exigirTexto(respuestaFinal, "La respuesta final");
    }

    public static Ejercicio crear(UUID libroId, UUID conceptoId, String enunciado, String fuente,
                                  Dificultad dificultad, String respuestaFinal) {
        return new Ejercicio(UUID.randomUUID(), libroId, conceptoId, enunciado, fuente, dificultad, respuestaFinal);
    }
}
