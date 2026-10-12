package io.github.hd4nielbu.lazystudio.application.ports.out;

import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Correccion;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;

/** Corrige una respuesta. Hoy compara la respuesta final; en la porción 3 lo hace la IA. */
public interface Corrector {

    Correccion corregir(Ejercicio ejercicio, String desarrollo, String respuestaFinal);
}
