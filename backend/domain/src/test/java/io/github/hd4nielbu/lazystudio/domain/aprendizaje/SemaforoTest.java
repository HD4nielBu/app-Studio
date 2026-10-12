package io.github.hd4nielbu.lazystudio.domain.aprendizaje;

import static io.github.hd4nielbu.lazystudio.domain.aprendizaje.IntentoTest.bien;
import static io.github.hd4nielbu.lazystudio.domain.aprendizaje.IntentoTest.mal;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class SemaforoTest {

    static final UUID CONCEPTO = UUID.randomUUID();

    static Ejercicio ejercicio() {
        return Ejercicio.crear(UUID.randomUUID(), CONCEPTO, "e", null, Dificultad.BASICA, "r");
    }

    static Intento aLaPrimera(Ejercicio e) {
        return Intento.empezar(e).registrar(bien(2));
    }

    static Intento rescatado() {
        return Intento.empezar(ejercicio()).registrar(mal(2)).registrar(mal(2)).registrar(mal(2));
    }

    @Test
    void sinIntentosTerminadosEstaSinVer() {
        assertThat(Semaforo.de(List.of(), true)).isEqualTo(Semaforo.SIN_VER);
        assertThat(Semaforo.de(List.of(Intento.empezar(ejercicio())), true)).isEqualTo(Semaforo.SIN_VER);
    }

    @Test
    void siElUltimoTerminoEnRescateNoLoResuelve() {
        assertThat(Semaforo.de(List.of(aLaPrimera(ejercicio()), rescatado()), false)).isEqualTo(Semaforo.NO_LO_RESUELVE);
    }

    @Test
    void resueltoPeroSinTodosLosCriteriosEstaConAyuda() {
        var a = ejercicio();
        var b = ejercicio();
        assertThat(Semaforo.de(List.of(rescatado(), aLaPrimera(a)), true)).isEqualTo(Semaforo.CON_AYUDA);
        // dos a la primera, pero sin el juicio de la IA
        assertThat(Semaforo.de(List.of(aLaPrimera(a), aLaPrimera(b)), false)).isEqualTo(Semaforo.CON_AYUDA);
        // el juicio de la IA, pero el mismo ejercicio dos veces no es "un caso distinto"
        assertThat(Semaforo.de(List.of(aLaPrimera(a), aLaPrimera(a)), true)).isEqualTo(Semaforo.CON_AYUDA);
    }

    @Test
    void dominadoConLosCuatroCriterios() {
        assertThat(Semaforo.de(List.of(aLaPrimera(ejercicio()), aLaPrimera(ejercicio())), true)).isEqualTo(Semaforo.DOMINADO);
    }
}
