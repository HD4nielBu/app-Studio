package io.github.hd4nielbu.lazystudio.domain;

/** Una regla de negocio no se cumple. El mensaje se muestra al alumno tal cual. */
public class ReglaVioladaException extends RuntimeException {

    public ReglaVioladaException(String mensaje) {
        super(mensaje);
    }

    public static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaVioladaException(campo + " no puede estar vacío.");
        }
        return valor.strip();
    }

    public static <T> T exigir(T valor, String campo) {
        if (valor == null) {
            throw new ReglaVioladaException("Falta " + campo + ".");
        }
        return valor;
    }
}
