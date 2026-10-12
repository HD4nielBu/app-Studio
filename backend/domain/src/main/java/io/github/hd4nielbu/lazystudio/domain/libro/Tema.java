package io.github.hd4nielbu.lazystudio.domain.libro;

import static io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException.exigir;
import static io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException.exigirTexto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;

/**
 * Bloque del temario que eligió el alumno. Quitarlo del temario no lo borra: su historial se conserva.
 * Se divide en 3 a 6 conceptos; mientras no se divide, no tiene conceptos.
 */
public record Tema(UUID id, UUID libroId, String nombre, boolean enTemario, List<Concepto> conceptos) {

    public static final int MIN_CONCEPTOS = 3;
    public static final int MAX_CONCEPTOS = 6;

    public Tema {
        exigir(id, "el identificador");
        exigir(libroId, "el libro");
        nombre = exigirTexto(nombre, "El nombre del tema");
        conceptos = List.copyOf(conceptos);
    }

    public static Tema crear(UUID libroId, String nombre) {
        return new Tema(UUID.randomUUID(), libroId, nombre, true, List.of());
    }

    /** Reemplaza la división en conceptos. Un concepto que se mantiene con el mismo nombre conserva su identidad. */
    public Tema conConceptos(List<String> nombres) {
        exigir(nombres, "la lista de conceptos");
        if (nombres.size() < MIN_CONCEPTOS || nombres.size() > MAX_CONCEPTOS) {
            throw new ReglaVioladaException(
                    "Un tema se divide en " + MIN_CONCEPTOS + " a " + MAX_CONCEPTOS + " conceptos; se recibieron " + nombres.size() + ".");
        }
        var vistos = new HashSet<String>();
        var nuevos = new ArrayList<Concepto>();
        for (var crudo : nombres) {
            var nombre = exigirTexto(crudo, "El nombre de un concepto");
            if (!vistos.add(nombre.toLowerCase())) {
                throw new ReglaVioladaException("El concepto \"" + nombre + "\" está repetido.");
            }
            var id = conceptos.stream().filter(c -> c.nombre().equals(nombre)).map(Concepto::id)
                    .findFirst().orElseGet(UUID::randomUUID);
            nuevos.add(new Concepto(id, nombre));
        }
        return new Tema(id, libroId, nombre, enTemario, nuevos);
    }

    public Tema quitarDelTemario() {
        return new Tema(id, libroId, nombre, false, conceptos);
    }

    public Tema restituir() {
        return new Tema(id, libroId, nombre, true, conceptos);
    }
}
