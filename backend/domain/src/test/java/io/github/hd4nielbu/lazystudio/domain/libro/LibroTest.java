package io.github.hd4nielbu.lazystudio.domain.libro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import io.github.hd4nielbu.lazystudio.domain.ReglaVioladaException;

class LibroTest {

    static final LocalDate HOY = LocalDate.of(2026, 10, 12);

    @Test
    void creaActivoYSinFechaEsAprendizajeLibre() {
        var libro = Libro.crear(" Cálculo 3 · 2.º parcial ", "Cálculo 3", null, Tono.NEUTRO, Tratamiento.VOS, HOY);

        assertThat(libro.nombre()).isEqualTo("Cálculo 3 · 2.º parcial");
        assertThat(libro.fechaExamen()).isNull();
        assertThat(libro.archivado()).isFalse();
    }

    @Test
    void aceptaExamenHoyPeroNoEnElPasado() {
        assertThat(Libro.crear("L", "M", HOY, Tono.NEUTRO, Tratamiento.TU, HOY).fechaExamen()).isEqualTo(HOY);
        assertThatThrownBy(() -> Libro.crear("L", "M", HOY.minusDays(1), Tono.NEUTRO, Tratamiento.TU, HOY))
                .isInstanceOf(ReglaVioladaException.class).hasMessageContaining("pasado");
    }

    @Test
    void exigeNombreMateriaTonoYTratamiento() {
        assertThatThrownBy(() -> Libro.crear("  ", "M", null, Tono.NEUTRO, Tratamiento.TU, HOY))
                .isInstanceOf(ReglaVioladaException.class);
        assertThatThrownBy(() -> Libro.crear("L", null, null, Tono.NEUTRO, Tratamiento.TU, HOY))
                .isInstanceOf(ReglaVioladaException.class);
        assertThatThrownBy(() -> Libro.crear("L", "M", null, null, Tratamiento.TU, HOY))
                .isInstanceOf(ReglaVioladaException.class);
    }

    @Test
    void archivaYReactiva() {
        var libro = Libro.crear("L", "M", null, Tono.CERCANO, Tratamiento.USTED, HOY);

        assertThat(libro.archivar().archivado()).isTrue();
        assertThat(libro.archivar().reactivar()).isEqualTo(libro);
    }
}
