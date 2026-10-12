package io.github.hd4nielbu.lazystudio.adapters.out.correccion;

import org.springframework.stereotype.Component;

import io.github.hd4nielbu.lazystudio.application.ports.out.Corrector;
import io.github.hd4nielbu.lazystudio.application.ports.out.Tutor;
import io.github.hd4nielbu.lazystudio.application.ports.out.VerificadorMatematico;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Correccion;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;

/**
 * Decide si la respuesta final es correcta comparando el texto y, si no coincide, con SymPy: gana siempre el CAS.
 * Si es incorrecta, la IA explica el primer error, clasifica el fallo y redacta la pista.
 */
// shortcut: no reconoce constantes renombradas (C1 por C2) y, si SymPy no decide, no consulta a un segundo modelo
// ni marca el ejercicio "sin verificar" (parte 9); agregarlo cuando se guarde ese estado.
@Component
class CorrectorPorRespuestaFinal implements Corrector {

    static final String SIN_EVALUAR = "La respuesta final no coincide con la esperada.";

    private final VerificadorMatematico verificador;
    private final Tutor tutor;

    CorrectorPorRespuestaFinal(VerificadorMatematico verificador, Tutor tutor) {
        this.verificador = verificador;
        this.tutor = tutor;
    }

    @Override
    public Correccion corregir(Ejercicio ejercicio, String desarrollo, String respuestaFinal, int pista) {
        var esperada = ejercicio.respuestaFinal();
        boolean correcta = normalizar(respuestaFinal).equals(normalizar(esperada))
                || verificador.sonEquivalentes(esperada, respuestaFinal).orElse(false);
        if (correcta) {
            return Correccion.acierto();
        }
        return tutor.revisarFallo(ejercicio, desarrollo, respuestaFinal, pista)
                .map(r -> new Correccion(false, r.tipoDeFallo(),
                        r.pista() == null ? r.primerError() : r.primerError() + "\n\nPista: " + r.pista()))
                .orElse(new Correccion(false, null, SIN_EVALUAR));
    }

    static String normalizar(String texto) {
        return texto.replaceAll("\\s+", "").toLowerCase();
    }
}
