package io.github.hd4nielbu.lazystudio.application.usecases;

import static java.util.stream.Collectors.groupingBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.github.hd4nielbu.lazystudio.application.NoEncontradoException;
import io.github.hd4nielbu.lazystudio.application.ports.in.PracticarEjercicios;
import io.github.hd4nielbu.lazystudio.application.ports.out.Corrector;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeEjercicios;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeIntentos;
import io.github.hd4nielbu.lazystudio.application.ports.out.RepositorioDeTemas;
import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Dificultad;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Intento;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Respuesta;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Semaforo;
import io.github.hd4nielbu.lazystudio.domain.libro.Concepto;

public class ServicioPractica implements PracticarEjercicios {

    private final RepositorioDeTemas temas;
    private final RepositorioDeEjercicios ejercicios;
    private final RepositorioDeIntentos intentos;
    private final Corrector corrector;

    public ServicioPractica(RepositorioDeTemas temas, RepositorioDeEjercicios ejercicios,
                            RepositorioDeIntentos intentos, Corrector corrector) {
        this.temas = temas;
        this.ejercicios = ejercicios;
        this.intentos = intentos;
        this.corrector = corrector;
    }

    @Override
    public Ejercicio agregarEjercicio(UUID conceptoId, String enunciado, String fuente, Dificultad dificultad, String respuestaFinal) {
        var tema = temas.temaDelConcepto(conceptoId).orElseThrow(() -> noExiste("el concepto", conceptoId));
        var ejercicio = Ejercicio.crear(tema.libroId(), conceptoId, enunciado, fuente, dificultad, respuestaFinal);
        ejercicios.guardar(ejercicio);
        return ejercicio;
    }

    @Override
    public List<Ejercicio> ejerciciosDe(UUID conceptoId) {
        temas.temaDelConcepto(conceptoId).orElseThrow(() -> noExiste("el concepto", conceptoId));
        return ejercicios.delConcepto(conceptoId);
    }

    @Override
    public Intento empezar(UUID ejercicioId) {
        var ejercicio = ejercicio(ejercicioId);
        return intentos.enCurso(ejercicioId).orElseGet(() -> {
            var nuevo = Intento.empezar(ejercicio);
            intentos.guardar(nuevo);
            return nuevo;
        });
    }

    @Override
    public Intento responder(UUID intentoId, String desarrollo, String respuestaFinal, int confianza) {
        var intento = intento(intentoId);
        if (intento.estado() != Intento.Estado.EN_CURSO) {
            throw new ReglaVioladaException("Este ejercicio ya terminó; empieza otro intento.");
        }
        Respuesta.validarAntesDeCorregir(respuestaFinal, confianza);
        var correccion = corrector.corregir(ejercicio(intento.ejercicioId()), desarrollo, respuestaFinal);
        var actualizado = intento.registrar(new Respuesta(desarrollo, respuestaFinal, confianza, correccion));
        intentos.guardar(actualizado);
        return actualizado;
    }

    @Override
    public String solucion(UUID intentoId) {
        var intento = intento(intentoId);
        if (intento.estado() != Intento.Estado.RESCATADO) {
            throw new ReglaVioladaException("La solución se muestra solo en el rescate, después de usar las pistas.");
        }
        return ejercicio(intento.ejercicioId()).respuestaFinal();
    }

    @Override
    public Map<UUID, Semaforo> semaforo(UUID libroId) {
        var porConcepto = intentos.intentosDelLibro(libroId).stream().collect(groupingBy(Intento::conceptoId));
        var semaforo = new LinkedHashMap<UUID, Semaforo>();
        // shortcut: el juicio de la IA (explica el porqué y lo distingue) llega en la porción 3; hasta entonces no hay 🟢.
        temas.delLibro(libroId).stream().flatMap(t -> t.conceptos().stream()).map(Concepto::id)
                .forEach(c -> semaforo.put(c, Semaforo.de(porConcepto.getOrDefault(c, List.of()), false)));
        return semaforo;
    }

    private Ejercicio ejercicio(UUID id) {
        return ejercicios.buscarEjercicio(id).orElseThrow(() -> noExiste("el ejercicio", id));
    }

    private Intento intento(UUID id) {
        return intentos.buscarIntento(id).orElseThrow(() -> noExiste("el intento", id));
    }

    private static NoEncontradoException noExiste(String que, UUID id) {
        return new NoEncontradoException("No existe " + que + " " + id + ".");
    }
}
