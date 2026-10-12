package io.github.hd4nielbu.lazystudio.application.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Intento;

public interface RepositorioDeIntentos {

    /** Guarda el intento con todas sus respuestas, en el momento (guardado continuo). */
    void guardar(Intento intento);

    Optional<Intento> buscarIntento(UUID id);

    Optional<Intento> enCurso(UUID ejercicioId);

    /** Todos los intentos del libro, en orden cronológico. */
    List<Intento> intentosDelLibro(UUID libroId);
}
