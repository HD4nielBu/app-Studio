package io.github.hd4nielbu.lazystudio.application.ports.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Dificultad;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Intento;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Semaforo;

public interface PracticarEjercicios {

    Ejercicio agregarEjercicio(UUID conceptoId, String enunciado, String fuente, Dificultad dificultad, String respuestaFinal);

    List<Ejercicio> ejerciciosDe(UUID conceptoId);

    /** Empieza un intento, o devuelve el que quedó en curso para retomarlo donde quedó. */
    Intento empezar(UUID ejercicioId);

    Intento responder(UUID intentoId, String desarrollo, String respuestaFinal, int confianza);

    /** La respuesta final del ejercicio; solo se entrega en el rescate (R11). */
    String solucion(UUID intentoId);

    /** Semáforo de cada concepto del libro, en el orden del temario. */
    Map<UUID, Semaforo> semaforo(UUID libroId);
}
