package io.github.hd4nielbu.lazystudio.domain.aprendizaje;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;

class IntentoTest {

    static final Ejercicio EJERCICIO = Ejercicio.crear(UUID.randomUUID(), UUID.randomUUID(),
            "y'' - 3y' + 2y = 0", "Zill, pág. 140, ej. 12", Dificultad.MEDIA, "y = C1 e^x + C2 e^(2x)");

    static Respuesta bien(int confianza) {
        return new Respuesta("", "r", confianza, Correccion.acierto());
    }

    static Respuesta mal(int confianza) {
        return new Respuesta("", "r", confianza, new Correccion(false, null, null, null));
    }

    @Test
    void aciertoALaPrimeraResuelveSinPistas() {
        var intento = Intento.empezar(EJERCICIO).registrar(bien(3));

        assertThat(intento.estado()).isEqualTo(Intento.Estado.RESUELTO);
        assertThat(intento.pistasUsadas()).isZero();
        assertThat(intento.cuentaParaDominio()).isTrue();
    }

    @Test
    void cadaFalloDaUnaPistaYAgotadasVieneElRescate() {
        var intento = Intento.empezar(EJERCICIO).registrar(mal(2));
        assertThat(intento.estado()).isEqualTo(Intento.Estado.EN_CURSO);
        assertThat(intento.pistasUsadas()).isEqualTo(1);

        intento = intento.registrar(mal(2));
        assertThat(intento.pistasUsadas()).isEqualTo(2);

        intento = intento.registrar(mal(1));
        assertThat(intento.estado()).isEqualTo(Intento.Estado.RESCATADO);
        assertThat(intento.respuestas()).hasSize(3);
    }

    @Test
    void conAyudaOConConfianzaUnoNoCuentaParaDominio() {
        assertThat(Intento.empezar(EJERCICIO).registrar(mal(2)).registrar(bien(3)).cuentaParaDominio()).isFalse();
        assertThat(Intento.empezar(EJERCICIO).registrar(bien(1)).cuentaParaDominio()).isFalse();
    }

    @Test
    void unIntentoTerminadoNoAceptaMasRespuestas() {
        var resuelto = Intento.empezar(EJERCICIO).registrar(bien(2));

        assertThatThrownBy(() -> resuelto.registrar(bien(2))).isInstanceOf(ReglaVioladaException.class);
    }

    @Test
    void laConfianzaEsObligatoriaYUnFalloConConfianzaTresEsPrioritario() {
        assertThatThrownBy(() -> bien(0)).isInstanceOf(ReglaVioladaException.class).hasMessageContaining("confianza");
        assertThatThrownBy(() -> bien(4)).isInstanceOf(ReglaVioladaException.class);
        assertThat(mal(3).prioritaria()).isTrue();
        assertThat(mal(2).prioritaria()).isFalse();
        assertThat(bien(3).prioritaria()).isFalse();
    }

    @Test
    void unaCorreccionCorrectaNoTieneTipoDeFallo() {
        assertThatThrownBy(() -> new Correccion(true, TipoDeFallo.EJECUCION, null, null)).isInstanceOf(ReglaVioladaException.class);
        assertThatThrownBy(() -> new Correccion(true, null, null, 2)).isInstanceOf(ReglaVioladaException.class);
        assertThatThrownBy(() -> new Correccion(false, null, null, 0)).isInstanceOf(ReglaVioladaException.class);
    }
}
