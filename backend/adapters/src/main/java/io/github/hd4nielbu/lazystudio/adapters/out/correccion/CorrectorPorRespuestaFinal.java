package io.github.hd4nielbu.lazystudio.adapters.out.correccion;

import org.springframework.stereotype.Component;

import io.github.hd4nielbu.lazystudio.application.ports.out.Corrector;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Correccion;
import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;

/**
 * Corrector provisional: compara la respuesta final sin espacios ni mayúsculas. No mira el desarrollo
 * ni clasifica el fallo (queda "sin clasificar").
 */
// shortcut: no reconoce respuestas equivalentes escritas distinto; se reemplaza por la IA (porción 3) y SymPy (2b).
@Component
class CorrectorPorRespuestaFinal implements Corrector {

    @Override
    public Correccion corregir(Ejercicio ejercicio, String desarrollo, String respuestaFinal) {
        return normalizar(respuestaFinal).equals(normalizar(ejercicio.respuestaFinal()))
                ? Correccion.acierto()
                : new Correccion(false, null, "La respuesta final no coincide con la esperada.");
    }

    private static String normalizar(String texto) {
        return texto.replaceAll("\\s+", "").toLowerCase();
    }
}
