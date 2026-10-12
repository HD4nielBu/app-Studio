package io.github.hd4nielbu.lazystudio.application.ports.out;

import java.util.Optional;

/** Decide si dos expresiones matemáticas son equivalentes aunque estén escritas distinto. */
public interface VerificadorMatematico {

    /** Vacío si no se puede decidir: no entiende la expresión, tarda demasiado o el servicio no responde. */
    Optional<Boolean> sonEquivalentes(String esperada, String dada);
}
