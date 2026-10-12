package io.github.hd4nielbu.lazystudio.adapters.in.rest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.hd4nielbu.lazystudio.application.ports.in.PracticarEjercicios;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Dificultad;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Intento;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Semaforo;

@RestController
@RequestMapping("/api")
class PracticaController {

    record NuevoEjercicio(String enunciado, String fuente, Dificultad dificultad, String respuestaFinal) {
    }

    /** El ejercicio sin su respuesta final: el alumno no la ve salvo en el rescate (R11). */
    record EjercicioVista(UUID id, UUID conceptoId, String enunciado, String fuente, Dificultad dificultad) {
        static EjercicioVista de(Ejercicio e) {
            return new EjercicioVista(e.id(), e.conceptoId(), e.enunciado(), e.fuente(), e.dificultad());
        }
    }

    /** Confianza nula = no elegida: la rechaza la regla R3 del dominio, con su mensaje. */
    record NuevaRespuesta(String desarrollo, String respuestaFinal, Integer confianza) {
    }

    private final PracticarEjercicios practica;

    PracticaController(PracticarEjercicios practica) {
        this.practica = practica;
    }

    @GetMapping("/conceptos/{id}/ejercicios")
    List<EjercicioVista> ejercicios(@PathVariable UUID id) {
        return practica.ejerciciosDe(id).stream().map(EjercicioVista::de).toList();
    }

    @PostMapping("/conceptos/{id}/ejercicios")
    @ResponseStatus(HttpStatus.CREATED)
    EjercicioVista agregar(@PathVariable UUID id, @RequestBody NuevoEjercicio p) {
        return EjercicioVista.de(practica.agregarEjercicio(id, p.enunciado(), p.fuente(), p.dificultad(), p.respuestaFinal()));
    }

    @PostMapping("/ejercicios/{id}/intentos")
    Intento empezar(@PathVariable UUID id) {
        return practica.empezar(id);
    }

    @PostMapping("/intentos/{id}/respuestas")
    Intento responder(@PathVariable UUID id, @RequestBody NuevaRespuesta p) {
        return practica.responder(id, p.desarrollo(), p.respuestaFinal(), p.confianza() == null ? 0 : p.confianza());
    }

    @GetMapping("/intentos/{id}/solucion")
    Map<String, String> solucion(@PathVariable UUID id) {
        return Map.of("respuestaFinal", practica.solucion(id));
    }

    @GetMapping("/libros/{id}/semaforo")
    Map<UUID, Semaforo> semaforo(@PathVariable UUID id) {
        return practica.semaforo(id);
    }
}
