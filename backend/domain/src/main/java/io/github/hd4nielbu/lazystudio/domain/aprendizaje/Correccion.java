package io.github.hd4nielbu.lazystudio.domain.aprendizaje;

import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;

/**
 * Resultado de corregir una respuesta. Un fallo puede quedar sin tipo ("sin clasificar") mientras no lo clasifique la IA.
 * {@code explicacion} señala el primer error (R4).
 */
public record Correccion(boolean correcta, TipoDeFallo tipoDeFallo, String explicacion) {

    public Correccion {
        if (correcta && tipoDeFallo != null) {
            throw new ReglaVioladaException("Una respuesta correcta no tiene tipo de fallo.");
        }
    }

    public static Correccion acierto() {
        return new Correccion(true, null, null);
    }
}
