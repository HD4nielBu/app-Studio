package io.github.hd4nielbu.lazystudio.domain.aprendizaje;

import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;

/**
 * Resultado de corregir una respuesta. Un fallo puede quedar sin tipo ("sin clasificar") mientras no lo clasifique la IA.
 * {@code explicacion} señala el primer error (R4) y {@code lineaDelError} su línea en el desarrollo (1 = la primera),
 * o null si no se sabe.
 */
public record Correccion(boolean correcta, TipoDeFallo tipoDeFallo, String explicacion, Integer lineaDelError) {

    public Correccion {
        if (correcta && tipoDeFallo != null) {
            throw new ReglaVioladaException("Una respuesta correcta no tiene tipo de fallo.");
        }
        if (lineaDelError != null && (correcta || lineaDelError < 1)) {
            throw new ReglaVioladaException("La línea del error es de una respuesta incorrecta y empieza en 1.");
        }
    }

    public static Correccion acierto() {
        return new Correccion(true, null, null, null);
    }
}
