package io.github.hd4nielbu.lazystudio.domain.libro;

import static io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException.exigir;
import static io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException.exigirTexto;

import java.time.LocalDate;
import java.util.UUID;

import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;

/**
 * Unidad principal del estudio. Con fecha de examen es preparación de examen; sin fecha, aprendizaje libre.
 * Ciclo de vida: activo ⇄ archivado. Solo se elimina si el alumno lo pide.
 */
public record Libro(UUID id, String nombre, String materia, LocalDate fechaExamen,
                   Tono tono, Tratamiento tratamiento, boolean archivado) {

    public Libro {
        exigir(id, "el identificador");
        nombre = exigirTexto(nombre, "El nombre del libro");
        materia = exigirTexto(materia, "La materia");
        exigir(tono, "el tono");
        exigir(tratamiento, "el tratamiento");
    }

    public static Libro crear(String nombre, String materia, LocalDate fechaExamen,
                              Tono tono, Tratamiento tratamiento, LocalDate hoy) {
        if (fechaExamen != null && fechaExamen.isBefore(hoy)) {
            throw new ReglaVioladaException("La fecha del examen no puede estar en el pasado.");
        }
        return new Libro(UUID.randomUUID(), nombre, materia, fechaExamen, tono, tratamiento, false);
    }

    public Libro archivar() {
        return new Libro(id, nombre, materia, fechaExamen, tono, tratamiento, true);
    }

    public Libro reactivar() {
        return new Libro(id, nombre, materia, fechaExamen, tono, tratamiento, false);
    }
}
