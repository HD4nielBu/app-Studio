package io.github.hd4nielbu.lazystudio.application.usecases;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import io.github.hd4nielbu.lazystudio.application.NoEncontradoException;
import io.github.hd4nielbu.lazystudio.application.ports.in.GestionarLibros;
import io.github.hd4nielbu.lazystudio.application.ports.in.GestionarTemario;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeLibros;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeTemas;
import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;
import io.github.hd4nielbu.lazystudio.domain.libro.Libro;
import io.github.hd4nielbu.lazystudio.domain.libro.Tema;
import io.github.hd4nielbu.lazystudio.domain.libro.Tono;
import io.github.hd4nielbu.lazystudio.domain.libro.Tratamiento;

public class ServicioLibros implements GestionarLibros, GestionarTemario {

    private final RepositorioDeLibros libros;
    private final RepositorioDeTemas temas;
    private final Clock reloj;

    public ServicioLibros(RepositorioDeLibros libros, RepositorioDeTemas temas, Clock reloj) {
        this.libros = libros;
        this.temas = temas;
        this.reloj = reloj;
    }

    @Override
    public Libro crear(String nombre, String materia, LocalDate fechaExamen, Tono tono, Tratamiento tratamiento) {
        var libro = Libro.crear(nombre, materia, fechaExamen, tono, tratamiento, LocalDate.now(reloj));
        libros.guardar(libro);
        return libro;
    }

    @Override
    public List<Libro> listar() {
        return libros.todos();
    }

    @Override
    public Libro obtener(UUID libroId) {
        return libros.buscar(libroId).orElseThrow(() -> new NoEncontradoException("No existe el libro " + libroId + "."));
    }

    @Override
    public Libro archivar(UUID libroId) {
        return guardarLibro(obtener(libroId).archivar());
    }

    @Override
    public Libro reactivar(UUID libroId) {
        return guardarLibro(obtener(libroId).reactivar());
    }

    @Override
    public void eliminar(UUID libroId) {
        obtener(libroId);
        libros.eliminar(libroId);
    }

    @Override
    public List<Tema> temasDe(UUID libroId) {
        obtener(libroId);
        return temas.delLibro(libroId);
    }

    @Override
    public Tema agregarTema(UUID libroId, String nombre) {
        var tema = Tema.crear(libroId, nombre);
        if (temasDe(libroId).stream().anyMatch(t -> t.nombre().equalsIgnoreCase(tema.nombre()))) {
            throw new ReglaVioladaException("El tema \"" + tema.nombre() + "\" ya está en este libro.");
        }
        temas.guardar(tema);
        return tema;
    }

    @Override
    public Tema definirConceptos(UUID temaId, List<String> conceptos) {
        return cambiarTema(temaId, t -> t.conConceptos(conceptos));
    }

    @Override
    public Tema quitarDelTemario(UUID temaId) {
        return cambiarTema(temaId, Tema::quitarDelTemario);
    }

    @Override
    public Tema restituir(UUID temaId) {
        return cambiarTema(temaId, Tema::restituir);
    }

    private Libro guardarLibro(Libro libro) {
        libros.guardar(libro);
        return libro;
    }

    private Tema cambiarTema(UUID temaId, UnaryOperator<Tema> cambio) {
        var tema = cambio.apply(temas.buscarTema(temaId)
                .orElseThrow(() -> new NoEncontradoException("No existe el tema " + temaId + ".")));
        temas.guardar(tema);
        return tema;
    }
}
