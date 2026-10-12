package io.github.hd4nielbu.lazystudio.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.hd4nielbu.lazystudio.application.NoEncontradoException;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeLibros;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeTemas;
import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;
import io.github.hd4nielbu.lazystudio.domain.libro.Libro;
import io.github.hd4nielbu.lazystudio.domain.libro.Tema;
import io.github.hd4nielbu.lazystudio.domain.libro.Tono;
import io.github.hd4nielbu.lazystudio.domain.libro.Tratamiento;

class ServicioLibrosTest {

    final EnMemoria repo = new EnMemoria();
    final ServicioLibros servicio = new ServicioLibros(repo, repo, Clock.fixed(Instant.parse("2026-10-12T15:00:00Z"), ZoneOffset.UTC));

    Libro libro() {
        return servicio.crear("Cálculo 3 · 2.º parcial", "Cálculo 3", LocalDate.of(2026, 11, 3), Tono.NEUTRO, Tratamiento.VOS);
    }

    @Test
    void creaYArchivaUnLibro() {
        var libro = libro();

        assertThat(servicio.listar()).containsExactly(libro);
        assertThat(servicio.archivar(libro.id()).archivado()).isTrue();
        assertThat(servicio.obtener(libro.id()).archivado()).isTrue();
    }

    @Test
    void usaElRelojParaRechazarFechasPasadas() {
        assertThatThrownBy(() -> servicio.crear("L", "M", LocalDate.of(2026, 10, 11), Tono.NEUTRO, Tratamiento.TU))
                .isInstanceOf(ReglaVioladaException.class);
    }

    @Test
    void noRepiteTemasEnUnMismoLibro() {
        var libro = libro();
        servicio.agregarTema(libro.id(), "Wronskiano");

        assertThatThrownBy(() -> servicio.agregarTema(libro.id(), "  WRONSKIANO "))
                .isInstanceOf(ReglaVioladaException.class).hasMessageContaining("ya está");
        assertThat(servicio.agregarTema(libro().id(), "Wronskiano")).isNotNull(); // en otro libro sí
    }

    @Test
    void divideYQuitaTemasGuardandoCadaCambio() {
        var tema = servicio.agregarTema(libro().id(), "EDO homogéneas");

        servicio.definirConceptos(tema.id(), List.of("H1", "H2", "H3"));
        servicio.quitarDelTemario(tema.id());

        var guardado = repo.buscarTema(tema.id()).orElseThrow();
        assertThat(guardado.enTemario()).isFalse();
        assertThat(guardado.conceptos()).hasSize(3);
    }

    @Test
    void avisaSiElLibroOElTemaNoExisten() {
        assertThatThrownBy(() -> servicio.obtener(UUID.randomUUID())).isInstanceOf(NoEncontradoException.class);
        assertThatThrownBy(() -> servicio.agregarTema(UUID.randomUUID(), "T")).isInstanceOf(NoEncontradoException.class);
        assertThatThrownBy(() -> servicio.restituir(UUID.randomUUID())).isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void eliminarBorraElLibroYSusTemas() {
        var libro = libro();
        servicio.agregarTema(libro.id(), "T");

        servicio.eliminar(libro.id());

        assertThat(servicio.listar()).isEmpty();
        assertThat(repo.temas).isEmpty();
    }

    public static class EnMemoria implements RepositorioDeLibros, RepositorioDeTemas {
        final Map<UUID, Libro> libros = new LinkedHashMap<>();
        final Map<UUID, Tema> temas = new LinkedHashMap<>();

        public void guardar(Libro libro) { libros.put(libro.id(), libro); }
        public Optional<Libro> buscar(UUID id) { return Optional.ofNullable(libros.get(id)); }
        public List<Libro> todos() { return new ArrayList<>(libros.values()); }
        public void eliminar(UUID id) { libros.remove(id); temas.values().removeIf(t -> t.libroId().equals(id)); }

        public void guardar(Tema tema) { temas.put(tema.id(), tema); }
        public Optional<Tema> buscarTema(UUID id) { return Optional.ofNullable(temas.get(id)); }
        public Optional<Tema> temaDelConcepto(UUID conceptoId) {
            return temas.values().stream().filter(t -> t.conceptos().stream().anyMatch(c -> c.id().equals(conceptoId))).findFirst();
        }
        public List<Tema> delLibro(UUID libroId) { return temas.values().stream().filter(t -> t.libroId().equals(libroId)).toList(); }
    }
}
