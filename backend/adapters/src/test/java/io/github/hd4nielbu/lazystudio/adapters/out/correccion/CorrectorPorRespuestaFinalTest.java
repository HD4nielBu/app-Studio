package io.github.hd4nielbu.lazystudio.adapters.out.correccion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Dificultad;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;

class CorrectorPorRespuestaFinalTest {

    private final Ejercicio ejercicio = Ejercicio.crear(UUID.randomUUID(), UUID.randomUUID(), "Simplifica",
            null, Dificultad.BASICA, "1 + 2x");

    @Test
    void aciertaSinPreguntarASympySiElTextoCoincide() {
        var corrector = new CorrectorPorRespuestaFinal((esperada, dada) -> {
            throw new AssertionError("no debía consultar a SymPy");
        });

        assertThat(corrector.corregir(ejercicio, "", "1+2X").correcta()).isTrue();
    }

    @Test
    void aciertaSiSympyDiceQueEsEquivalente() {
        var corrector = new CorrectorPorRespuestaFinal((esperada, dada) -> Optional.of(true));

        assertThat(corrector.corregir(ejercicio, "", "2*x + 1").correcta()).isTrue();
    }

    @Test
    void fallaSiSympyDiceQueNo() {
        var corrector = new CorrectorPorRespuestaFinal((esperada, dada) -> Optional.of(false));

        assertThat(corrector.corregir(ejercicio, "", "2x - 1").correcta()).isFalse();
    }

    @Test
    void fallaSiSympyNoPuedeDecidir() {
        var corrector = new CorrectorPorRespuestaFinal((esperada, dada) -> Optional.empty());

        assertThat(corrector.corregir(ejercicio, "", "2x - 1").correcta()).isFalse();
    }
}
