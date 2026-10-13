package io.github.hd4nielbu.lazystudio.adapters.out.correccion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.hd4nielbu.lazystudio.application.ports.out.Tutor;
import io.github.hd4nielbu.lazystudio.application.ports.out.VerificadorMatematico;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Dificultad;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.TipoDeFallo;

class CorrectorPorRespuestaFinalTest {

    private final Ejercicio ejercicio = Ejercicio.crear(UUID.randomUUID(), UUID.randomUUID(), "Simplifica",
            null, Dificultad.BASICA, "1 + 2x");
    private final Tutor sinIa = (ejercicio, desarrollo, respuesta, pista) -> Optional.empty();

    private CorrectorPorRespuestaFinal corrector(VerificadorMatematico verificador) {
        return new CorrectorPorRespuestaFinal(verificador, sinIa);
    }

    @Test
    void aciertaSinPreguntarASympySiElTextoCoincide() {
        var corrector = corrector((esperada, dada) -> {
            throw new AssertionError("no debía consultar a SymPy");
        });

        assertThat(corrector.corregir(ejercicio, "", "1+2X", 1).correcta()).isTrue();
    }

    @Test
    void aciertaSiSympyDiceQueEsEquivalente() {
        var corrector = corrector((esperada, dada) -> Optional.of(true));

        assertThat(corrector.corregir(ejercicio, "", "2*x + 1", 1).correcta()).isTrue();
    }

    @Test
    void fallaSiSympyDiceQueNo() {
        var corrector = corrector((esperada, dada) -> Optional.of(false));

        assertThat(corrector.corregir(ejercicio, "", "2x - 1", 1).correcta()).isFalse();
    }

    @Test
    void fallaSiSympyNoPuedeDecidir() {
        var corrector = corrector((esperada, dada) -> Optional.empty());

        assertThat(corrector.corregir(ejercicio, "", "2x - 1", 1).correcta()).isFalse();
    }

    @Test
    void siFallaLaIaExplicaElErrorYDaLaPista() {
        Tutor ia = (ejercicio, desarrollo, respuesta, pista) ->
                Optional.of(new Tutor.Revision(TipoDeFallo.EJECUCION, "Restaste mal en la línea 2.", "Revisa el signo.", 2));
        var corrector = new CorrectorPorRespuestaFinal((esperada, dada) -> Optional.of(false), ia);

        var correccion = corrector.corregir(ejercicio, "", "2x - 1", 1);

        assertThat(correccion.tipoDeFallo()).isEqualTo(TipoDeFallo.EJECUCION);
        assertThat(correccion.lineaDelError()).isEqualTo(2);
        assertThat(correccion.explicacion()).isEqualTo("Restaste mal en la línea 2.\n\nPista: Revisa el signo.");
    }

    @Test
    void sinIaElFalloQuedaSinEvaluar() {
        var correccion = corrector((esperada, dada) -> Optional.of(false)).corregir(ejercicio, "", "2x - 1", 1);

        assertThat(correccion.tipoDeFallo()).isNull();
        assertThat(correccion.explicacion()).isEqualTo(CorrectorPorRespuestaFinal.SIN_EVALUAR);
    }
}
