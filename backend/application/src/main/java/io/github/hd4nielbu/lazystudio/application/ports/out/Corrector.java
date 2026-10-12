package io.github.hd4nielbu.lazystudio.application.ports.out;

import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Correccion;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;

/** Corrige una respuesta: el código decide si es correcta y la IA explica el fallo (parte 9). */
public interface Corrector {

    /** @param pista la pista que corresponde si falla (1, 2, ...), o 0 si toca el rescate */
    Correccion corregir(Ejercicio ejercicio, String desarrollo, String respuestaFinal, int pista);
}
