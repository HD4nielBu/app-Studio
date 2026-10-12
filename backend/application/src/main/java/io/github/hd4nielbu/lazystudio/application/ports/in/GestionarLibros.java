package io.github.hd4nielbu.lazystudio.application.ports.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import io.github.hd4nielbu.lazystudio.domain.libro.Libro;
import io.github.hd4nielbu.lazystudio.domain.libro.Tono;
import io.github.hd4nielbu.lazystudio.domain.libro.Tratamiento;

public interface GestionarLibros {

    Libro crear(String nombre, String materia, LocalDate fechaExamen, Tono tono, Tratamiento tratamiento);

    List<Libro> listar();

    Libro obtener(UUID libroId);

    Libro archivar(UUID libroId);

    Libro reactivar(UUID libroId);

    /** Borra el libro con todo su historial. Solo se llama cuando el alumno lo pide expresamente. */
    void eliminar(UUID libroId);
}
