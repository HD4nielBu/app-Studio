package io.github.hd4nielbu.lazystudio.domain.aprendizaje;

import static io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException.exigir;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;

/**
 * La resolución de un ejercicio. Cada respuesta incorrecta da la siguiente pista; agotadas las pistas, rescate (R5, R6).
 * Se guardan todas las respuestas, también las correctas.
 */
public record Intento(UUID id, UUID ejercicioId, UUID conceptoId, Estado estado,
                      int pistasUsadas, int pistasMaximas, List<Respuesta> respuestas) {

    public enum Estado { EN_CURSO, RESUELTO, RESCATADO }

    // shortcut: fijo en 2 (valor por defecto de R5), hacerlo ajustable de 1 a 3 por libro cuando se agreguen sus ajustes.
    public static final int PISTAS_POR_DEFECTO = 2;

    public Intento {
        exigir(id, "el identificador");
        exigir(ejercicioId, "el ejercicio");
        exigir(conceptoId, "el concepto");
        exigir(estado, "el estado");
        respuestas = List.copyOf(respuestas);
    }

    public static Intento empezar(Ejercicio ejercicio) {
        return new Intento(UUID.randomUUID(), ejercicio.id(), ejercicio.conceptoId(), Estado.EN_CURSO,
                0, PISTAS_POR_DEFECTO, List.of());
    }

    public Intento registrar(Respuesta respuesta) {
        if (estado != Estado.EN_CURSO) {
            throw new ReglaVioladaException("Este ejercicio ya terminó; empieza otro intento.");
        }
        var todas = new ArrayList<>(respuestas);
        todas.add(respuesta);
        if (respuesta.correccion().correcta()) {
            return new Intento(id, ejercicioId, conceptoId, Estado.RESUELTO, pistasUsadas, pistasMaximas, todas);
        }
        if (pistasUsadas < pistasMaximas) {
            return new Intento(id, ejercicioId, conceptoId, Estado.EN_CURSO, pistasUsadas + 1, pistasMaximas, todas);
        }
        return new Intento(id, ejercicioId, conceptoId, Estado.RESCATADO, pistasUsadas, pistasMaximas, todas);
    }

    /** Criterio de 🟢 que decide el código: a la primera, sin pistas, y sin confianza 1 ("acertó, pero no lo sabe"). */
    public boolean cuentaParaDominio() {
        return estado == Estado.RESUELTO && respuestas.size() == 1 && respuestas.getFirst().confianza() >= 2;
    }
}
