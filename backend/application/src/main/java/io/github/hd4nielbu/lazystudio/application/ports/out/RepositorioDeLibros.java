package io.github.hd4nielbu.lazystudio.application.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.github.hd4nielbu.lazystudio.domain.libro.Libro;

public interface RepositorioDeLibros {

    void guardar(Libro libro);

    Optional<Libro> buscar(UUID id);

    List<Libro> todos();

    /** Borra también sus temas y conceptos. */
    void eliminar(UUID id);
}
