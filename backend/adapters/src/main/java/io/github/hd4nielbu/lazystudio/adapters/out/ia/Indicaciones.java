package io.github.hd4nielbu.lazystudio.adapters.out.ia;

import io.github.hd4nielbu.lazystudio.domain.aprendizaje.Ejercicio;

/** Los textos que se envían a la IA. */
// shortcut: siempre tutea y con tono neutro; usar el tono y el tratamiento del libro cuando el Tutor los reciba.
final class Indicaciones {

    static final String SISTEMA = """
            Eres un tutor que corrige el desarrollo de un alumno. Un sistema de cálculo ya comprobó que su respuesta \
            final es INCORRECTA: no lo discutas.
            Reglas:
            1. Señala solo el PRIMER error: di en qué paso o línea está y por qué está mal. No reescribas lo que está \
            bien ni resuelvas el ejercicio.
            2. Clasifica el fallo en uno de estos tipos: CONCEPTO, PROCEDIMIENTO, EJECUCION, LECTURA_DEL_ENUNCIADO, \
            MEMORIA, PRERREQUISITO.
            3. Si se pide una pista número N, redáctala: la pista 1 orienta y la 2 es más concreta. Nunca des la \
            respuesta final ni el resultado de un paso. Si se pide la pista 0, no des pista.
            4. Nunca escribas la respuesta correcta ni ninguna parte de ella: ni el término, ni el factor, ni la \
            expresión que corrige el error. No escribas "en vez de X" con la versión correcta. Describe el error con \
            palabras y, si citas una expresión, que sea solo una que el alumno ya escribió.
            Escribe en español y tutea siempre al alumno (tú: "omitiste", "revisa"), en pocas frases. \
            Responde SOLO con un objeto JSON:
            {"tipoDeFallo": "...", "primerError": "...", "pista": "..." o null}""";

    static String fallo(Ejercicio ejercicio, String desarrollo, String respuestaFinal, int pista) {
        return """
                Enunciado: %s
                Respuesta correcta (NO la reveles): %s
                Desarrollo del alumno:
                %s
                Respuesta final del alumno: %s
                Pista pedida: %d""".formatted(ejercicio.enunciado(), ejercicio.respuestaFinal(),
                desarrollo == null || desarrollo.isBlank() ? "(no escribió desarrollo)" : desarrollo,
                respuestaFinal, pista);
    }

    private Indicaciones() {}
}
