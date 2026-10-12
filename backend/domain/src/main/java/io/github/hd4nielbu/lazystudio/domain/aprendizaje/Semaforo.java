package io.github.hd4nielbu.lazystudio.domain.aprendizaje;

import java.util.List;

/** Estado de dominio de un concepto. Lo calcula solo el código, a partir del historial; no se guarda un porcentaje. */
public enum Semaforo {
    SIN_VER, NO_LO_RESUELVE, CON_AYUDA, DOMINADO;

    /** Ejercicios distintos que hay que resolver a la primera: criterio "a la primera" más criterio "caso distinto". */
    static final int EJERCICIOS_A_LA_PRIMERA = 2;

    /**
     * @param intentos       los del concepto, en orden cronológico
     * @param juicioDeLaIa   la IA juzgó que explica el porqué y lo distingue de conceptos parecidos
     */
    // shortcut: las bajadas por repasos fallidos (🟢→🟡, dos fallos→🔴) llegan con el repaso espaciado (porción 6).
    public static Semaforo de(List<Intento> intentos, boolean juicioDeLaIa) {
        var terminados = intentos.stream().filter(i -> i.estado() != Intento.Estado.EN_CURSO).toList();
        if (terminados.isEmpty()) {
            return SIN_VER;
        }
        long resueltosALaPrimera = terminados.stream().filter(Intento::cuentaParaDominio)
                .map(Intento::ejercicioId).distinct().count();
        if (resueltosALaPrimera >= EJERCICIOS_A_LA_PRIMERA && juicioDeLaIa) {
            return DOMINADO;
        }
        return terminados.getLast().estado() == Intento.Estado.RESCATADO ? NO_LO_RESUELVE : CON_AYUDA;
    }
}
