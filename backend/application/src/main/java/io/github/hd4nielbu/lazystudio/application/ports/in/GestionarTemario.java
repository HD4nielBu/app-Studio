package io.github.hd4nielbu.lazystudio.application.ports.in;

import java.util.List;
import java.util.UUID;

import io.github.hd4nielbu.lazystudio.domain.libro.Tema;

/** El temario lo elige el alumno: solo él agrega, quita o divide temas. */
public interface GestionarTemario {

    List<Tema> temasDe(UUID libroId);

    Tema agregarTema(UUID libroId, String nombre);

    Tema definirConceptos(UUID temaId, List<String> conceptos);

    Tema quitarDelTemario(UUID temaId);

    Tema restituir(UUID temaId);
}
