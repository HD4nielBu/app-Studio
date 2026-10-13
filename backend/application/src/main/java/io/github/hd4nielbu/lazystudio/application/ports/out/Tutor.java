package io.github.hd4nielbu.lazystudio.application.ports.out;

import java.util.Optional;

import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.TipoDeFallo;

/** La IA: hace solo lo que es lenguaje (parte 9). Nunca decide si una respuesta es correcta. */
public interface Tutor {

    /**
     * Revisa una respuesta que el código ya dio por incorrecta: ubica el primer error (R4), clasifica el fallo y,
     * si {@code pista} es mayor que 0, redacta esa pista sin dar la solución (R5, R11).
     * Vacío si ninguna IA dio una salida válida: el fallo queda "sin evaluar" y el progreso no cambia.
     */
    Optional<Revision> revisarFallo(Ejercicio ejercicio, String desarrollo, String respuestaFinal, int pista);

    /** {@code pista} es null cuando no se pidió (rescate); {@code linea}, la del desarrollo con el error, o null. */
    record Revision(TipoDeFallo tipoDeFallo, String primerError, String pista, Integer linea) {}
}
