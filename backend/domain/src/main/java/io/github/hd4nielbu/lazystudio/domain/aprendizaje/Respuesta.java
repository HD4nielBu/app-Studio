package io.github.hd4nielbu.lazystudio.domain.aprendizaje;

import static io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException.exigir;
import static io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException.exigirTexto;

import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;

/** Una respuesta del alumno ya corregida. La confianza (1–3) es obligatoria y se da antes de corregir (R3). */
public record Respuesta(String desarrollo, String respuestaFinal, int confianza, Correccion correccion) {

    public Respuesta {
        desarrollo = desarrollo == null ? "" : desarrollo.strip();
        respuestaFinal = validarAntesDeCorregir(respuestaFinal, confianza);
        exigir(correccion, "la corrección");
    }

    /** Lo que el alumno debe dar antes de que se corrija: la respuesta final y su confianza (R3). */
    public static String validarAntesDeCorregir(String respuestaFinal, int confianza) {
        if (confianza < 1 || confianza > 3) {
            throw new ReglaVioladaException("Elige tu confianza (1, 2 o 3) antes de enviar.");
        }
        return exigirTexto(respuestaFinal, "La respuesta final");
    }

    /** Un fallo con confianza 3 es el más valioso de corregir (hipercorrección). */
    public boolean prioritaria() {
        return !correccion.correcta() && confianza == 3;
    }
}
