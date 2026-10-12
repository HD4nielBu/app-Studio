package io.github.hd4nielbu.lazystudio.domain.libro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;

class TemaTest {

    final Tema tema = Tema.crear(UUID.randomUUID(), "EDO de orden superior");

    @Test
    void naceEnElTemarioYSinDividir() {
        assertThat(tema.enTemario()).isTrue();
        assertThat(tema.conceptos()).isEmpty();
    }

    @Test
    void seDivideEnTresASeisConceptos() {
        assertThat(tema.conConceptos(List.of("H1", "H2", "H3")).conceptos()).extracting(Concepto::nombre)
                .containsExactly("H1", "H2", "H3");
        assertThat(tema.conConceptos(List.of("a", "b", "c", "d", "e", "f")).conceptos()).hasSize(6);

        assertThatThrownBy(() -> tema.conConceptos(List.of("a", "b"))).isInstanceOf(ReglaVioladaException.class);
        assertThatThrownBy(() -> tema.conConceptos(List.of("a", "b", "c", "d", "e", "f", "g")))
                .isInstanceOf(ReglaVioladaException.class);
    }

    @Test
    void rechazaConceptosVaciosORepetidos() {
        assertThatThrownBy(() -> tema.conConceptos(List.of("a", " ", "c"))).isInstanceOf(ReglaVioladaException.class);
        assertThatThrownBy(() -> tema.conConceptos(List.of("Wronskiano", "b", "wronskiano ")))
                .isInstanceOf(ReglaVioladaException.class).hasMessageContaining("repetido");
    }

    @Test
    void alCorregirLaDivisionLosConceptosQueQuedanConservanSuIdentidad() {
        var antes = tema.conConceptos(List.of("H1", "H2", "H3"));
        var despues = antes.conConceptos(List.of("H1", "H3", "H4"));

        assertThat(despues.conceptos().get(0).id()).isEqualTo(antes.conceptos().get(0).id());
        assertThat(despues.conceptos().get(1).id()).isEqualTo(antes.conceptos().get(2).id());
        assertThat(despues.conceptos().get(2).id()).isNotIn(antes.conceptos().stream().map(Concepto::id).toList());
    }

    @Test
    void quitarDelTemarioConservaLosConceptos() {
        var dividido = tema.conConceptos(List.of("H1", "H2", "H3"));

        var quitado = dividido.quitarDelTemario();

        assertThat(quitado.enTemario()).isFalse();
        assertThat(quitado.conceptos()).isEqualTo(dividido.conceptos());
        assertThat(quitado.restituir()).isEqualTo(dividido);
    }
}
