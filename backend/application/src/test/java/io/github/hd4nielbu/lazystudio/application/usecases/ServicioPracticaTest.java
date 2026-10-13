package io.github.hd4nielbu.lazystudio.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.hd4nielbu.lazystudio.application.NoEncontradoException;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeEjercicios;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeIntentos;
import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Correccion;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Dificultad;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Intento;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Semaforo;
import io.github.hd4nielbu.lazystudio.domain.libro.Tono;
import io.github.hd4nielbu.lazystudio.domain.libro.Tratamiento;

class ServicioPracticaTest {

    final ServicioLibrosTest.EnMemoria libros = new ServicioLibrosTest.EnMemoria();
    final EnMemoria repo = new EnMemoria();
    // Corrector falso: acierta si la respuesta es "bien".
    final ServicioPractica servicio = new ServicioPractica(libros, repo, repo,
            (ejercicio, desarrollo, respuesta, pista) -> respuesta.equals("bien") ? Correccion.acierto() : new Correccion(false, null, null, null));

    UUID libroId;
    UUID h1;
    UUID h2;
    UUID h3;

    @BeforeEach
    void temario() {
        var gestion = new ServicioLibros(libros, libros, Clock.systemUTC());
        libroId = gestion.crear("L", "M", null, Tono.NEUTRO, Tratamiento.TU).id();
        var tema = gestion.definirConceptos(gestion.agregarTema(libroId, "T").id(), List.of("H1", "H2", "H3"));
        h1 = tema.conceptos().get(0).id();
        h2 = tema.conceptos().get(1).id();
        h3 = tema.conceptos().get(2).id();
    }

    UUID ejercicio(UUID concepto) {
        return servicio.agregarEjercicio(concepto, "enunciado", null, Dificultad.MEDIA, "y = 1").id();
    }

    @Test
    void elEjercicioQuedaEnElLibroDeSuConcepto() {
        var ejercicio = repo.ejercicios.get(ejercicio(h1));

        assertThat(ejercicio.libroId()).isEqualTo(libroId);
        assertThat(servicio.ejerciciosDe(h1)).containsExactly(ejercicio);
        assertThatThrownBy(() -> ejercicio(UUID.randomUUID())).isInstanceOf(NoEncontradoException.class);
    }

    @Test
    void empezarRetomaElIntentoEnCurso() {
        var ejercicio = ejercicio(h1);
        var intento = servicio.empezar(ejercicio);
        servicio.responder(intento.id(), "", "mal", 2);

        var retomado = servicio.empezar(ejercicio);

        assertThat(retomado.id()).isEqualTo(intento.id());
        assertThat(retomado.pistasUsadas()).isEqualTo(1);
    }

    @Test
    void cadaRespuestaSeGuardaEnElMomento() {
        var intento = servicio.empezar(ejercicio(h1));

        servicio.responder(intento.id(), "desarrollo", "mal", 3);

        assertThat(repo.intentos.get(intento.id()).respuestas()).singleElement()
                .satisfies(r -> assertThat(r.prioritaria()).isTrue());
    }

    @Test
    void sinConfianzaNoSeCorrige() {
        var intento = servicio.empezar(ejercicio(h1));

        assertThatThrownBy(() -> servicio.responder(intento.id(), "", "bien", 0)).isInstanceOf(ReglaVioladaException.class);
        assertThat(repo.intentos.get(intento.id()).respuestas()).isEmpty();
    }

    @Test
    void laSolucionSoloSeEntregaEnElRescate() {
        var intento = servicio.empezar(ejercicio(h1)).id();
        servicio.responder(intento, "", "mal", 2);
        assertThatThrownBy(() -> servicio.solucion(intento)).isInstanceOf(ReglaVioladaException.class);

        servicio.responder(intento, "", "mal", 2);
        servicio.responder(intento, "", "mal", 2);

        assertThat(servicio.solucion(intento)).isEqualTo("y = 1");
    }

    @Test
    void semaforoDeCadaConceptoDelLibro() {
        servicio.responder(servicio.empezar(ejercicio(h1)).id(), "", "bien", 2);
        var rescatado = servicio.empezar(ejercicio(h2)).id();
        for (int i = 0; i < 3; i++) servicio.responder(rescatado, "", "mal", 2);

        assertThat(servicio.semaforo(libroId)).containsExactly(
                Map.entry(h1, Semaforo.CON_AYUDA), Map.entry(h2, Semaforo.NO_LO_RESUELVE),
                Map.entry(h3, Semaforo.SIN_VER));
    }

    static class EnMemoria implements RepositorioDeEjercicios, RepositorioDeIntentos {
        final Map<UUID, Ejercicio> ejercicios = new LinkedHashMap<>();
        final Map<UUID, Intento> intentos = new LinkedHashMap<>();

        public void guardar(Ejercicio e) { ejercicios.put(e.id(), e); }
        public Optional<Ejercicio> buscarEjercicio(UUID id) { return Optional.ofNullable(ejercicios.get(id)); }
        public List<Ejercicio> delConcepto(UUID c) { return ejercicios.values().stream().filter(e -> e.conceptoId().equals(c)).toList(); }

        public void guardar(Intento i) { intentos.put(i.id(), i); }
        public Optional<Intento> buscarIntento(UUID id) { return Optional.ofNullable(intentos.get(id)); }
        public Optional<Intento> enCurso(UUID ejercicioId) {
            return intentos.values().stream().filter(i -> i.ejercicioId().equals(ejercicioId) && i.estado() == Intento.Estado.EN_CURSO).findFirst();
        }
        public List<Intento> intentosDelLibro(UUID libroId) {
            return new ArrayList<>(intentos.values().stream().filter(i -> ejercicios.get(i.ejercicioId()).libroId().equals(libroId)).toList());
        }
    }
}
