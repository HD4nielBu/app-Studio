package io.github.hd4nielbu.lazystudio.adapters.in.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.hd4nielbu.lazystudio.application.ports.in.GestionarLibros;
import io.github.hd4nielbu.lazystudio.application.ports.in.GestionarTemario;
import io.github.hd4nielbu.lazystudio.domain.libro.Libro;
import io.github.hd4nielbu.lazystudio.domain.libro.Tema;
import io.github.hd4nielbu.lazystudio.domain.libro.Tono;
import io.github.hd4nielbu.lazystudio.domain.libro.Tratamiento;

@RestController
@RequestMapping("/api")
class LibrosController {

    record NuevoLibro(String nombre, String materia, LocalDate fechaExamen, Tono tono, Tratamiento tratamiento) {
    }

    record NuevoTema(String nombre) {
    }

    record Division(List<String> conceptos) {
    }

    private final GestionarLibros libros;
    private final GestionarTemario temario;

    LibrosController(GestionarLibros libros, GestionarTemario temario) {
        this.libros = libros;
        this.temario = temario;
    }

    @GetMapping("/libros")
    List<Libro> listar() {
        return libros.listar();
    }

    @PostMapping("/libros")
    @ResponseStatus(HttpStatus.CREATED)
    Libro crear(@RequestBody NuevoLibro p) {
        return libros.crear(p.nombre(), p.materia(), p.fechaExamen(), p.tono(), p.tratamiento());
    }

    @GetMapping("/libros/{id}")
    Libro obtener(@PathVariable UUID id) {
        return libros.obtener(id);
    }

    @PostMapping("/libros/{id}/archivar")
    Libro archivar(@PathVariable UUID id) {
        return libros.archivar(id);
    }

    @PostMapping("/libros/{id}/reactivar")
    Libro reactivar(@PathVariable UUID id) {
        return libros.reactivar(id);
    }

    @DeleteMapping("/libros/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void eliminar(@PathVariable UUID id) {
        libros.eliminar(id);
    }

    @GetMapping("/libros/{id}/temas")
    List<Tema> temas(@PathVariable UUID id) {
        return temario.temasDe(id);
    }

    @PostMapping("/libros/{id}/temas")
    @ResponseStatus(HttpStatus.CREATED)
    Tema agregarTema(@PathVariable UUID id, @RequestBody NuevoTema p) {
        return temario.agregarTema(id, p.nombre());
    }

    @PutMapping("/temas/{id}/conceptos")
    Tema definirConceptos(@PathVariable UUID id, @RequestBody Division p) {
        return temario.definirConceptos(id, p.conceptos());
    }

    @PostMapping("/temas/{id}/quitar")
    Tema quitar(@PathVariable UUID id) {
        return temario.quitarDelTemario(id);
    }

    @PostMapping("/temas/{id}/restituir")
    Tema restituir(@PathVariable UUID id) {
        return temario.restituir(id);
    }
}
