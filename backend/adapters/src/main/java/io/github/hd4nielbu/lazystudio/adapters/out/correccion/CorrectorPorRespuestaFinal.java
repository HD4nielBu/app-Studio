package io.github.hd4nielbu.lazystudio.adapters.out.correccion;

import org.springframework.stereotype.Component;

import io.github.hd4nielbu.lazystudio.application.ports.out.Corrector;
import io.github.hd4nielbu.lazystudio.application.ports.out.VerificadorMatematico;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Correccion;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;

/**
 * Corrector provisional: compara la respuesta final sin espacios ni mayúsculas y, si no coincide, pregunta a SymPy
 * si es equivalente. No mira el desarrollo ni clasifica el fallo (queda "sin clasificar").
 */
// shortcut: no mira el desarrollo ni reconoce constantes renombradas (C1 por C2); se reemplaza por la IA (porción 3).
@Component
class CorrectorPorRespuestaFinal implements Corrector {

    private final VerificadorMatematico verificador;

    CorrectorPorRespuestaFinal(VerificadorMatematico verificador) {
        this.verificador = verificador;
    }

    @Override
    public Correccion corregir(Ejercicio ejercicio, String desarrollo, String respuestaFinal) {
        var esperada = ejercicio.respuestaFinal();
        boolean correcta = normalizar(respuestaFinal).equals(normalizar(esperada))
                || verificador.sonEquivalentes(esperada, respuestaFinal).orElse(false);
        return correcta
                ? Correccion.acierto()
                : new Correccion(false, null, "La respuesta final no coincide con la esperada.");
    }

    private static String normalizar(String texto) {
        return texto.replaceAll("\\s+", "").toLowerCase();
    }
}
