import { useState, type FormEvent, type SyntheticEvent } from "react";
import type { Confianza, Ejercicio } from "../api/practica";
import { useIntento } from "../hooks/useIntento";

// R16 · autoría: sin pegar, soltar texto, autocompletar ni corrector en el cuadro de respuesta.
const bloquear = (e: SyntheticEvent) => e.preventDefault();
const sinAyudas = {
  onPaste: bloquear,
  onDrop: bloquear,
  autoComplete: "off",
  autoCorrect: "off",
  autoCapitalize: "off",
  spellCheck: false,
} as const;

const tiposDeFallo: Record<string, string> = {
  CONCEPTO: "concepto",
  PROCEDIMIENTO: "procedimiento",
  EJECUCION: "ejecución",
  LECTURA_DEL_ENUNCIADO: "lectura del enunciado",
  MEMORIA: "memoria",
  PRERREQUISITO: "prerrequisito",
};

const confianzas: { valor: Confianza; texto: string }[] = [
  { valor: 1, texto: "1 · Adiviné" },
  { valor: 2, texto: "2 · Creo que sí" },
  { valor: 3, texto: "3 · Estoy seguro" },
];

export function Resolver({ ejercicio, onTerminar }: { ejercicio: Ejercicio; onTerminar: () => void }) {
  const { intento, solucion, error, responder } = useIntento(ejercicio.id);
  const [desarrollo, setDesarrollo] = useState("");
  const [respuestaFinal, setRespuestaFinal] = useState("");
  const [confianza, setConfianza] = useState<Confianza | null>(null);
  const [enviando, setEnviando] = useState(false);

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (!confianza || enviando) return;
    setEnviando(true); // la IA puede tardar: un solo envío a la vez
    if (await responder(desarrollo, respuestaFinal, confianza)) setConfianza(null); // cada respuesta pide su confianza
    setEnviando(false);
  };

  if (!intento) return <p>{error ?? "Cargando…"}</p>;
  const ultima = intento.respuestas[intento.respuestas.length - 1];

  return (
    <section>
      <h3>{ejercicio.enunciado}</h3>
      {ejercicio.fuente && <p>Fuente: {ejercicio.fuente}</p>}
      {error && <p role="alert">{error}</p>}

      {intento.estado === "EN_CURSO" && (
        <>
          {ultima && !ultima.correccion.correcta && (
            <div role="status">
              <p>
                Incorrecto (pista {intento.pistasUsadas} de {intento.pistasMaximas})
                {ultima.correccion.tipoDeFallo && ` · fallo de ${tiposDeFallo[ultima.correccion.tipoDeFallo]}`}
                {ultima.correccion.lineaDelError && ` · línea ${ultima.correccion.lineaDelError} de tu desarrollo`}.
              </p>
              <p style={{ whiteSpace: "pre-line" }}>{ultima.correccion.explicacion}</p>
            </div>
          )}
          <form onSubmit={enviar}>
            <label>
              Desarrollo
              <textarea rows={6} value={desarrollo} onChange={(e) => setDesarrollo(e.target.value)} {...sinAyudas} />
            </label>
            <label>
              Respuesta final <input required value={respuestaFinal} onChange={(e) => setRespuestaFinal(e.target.value)} {...sinAyudas} />
            </label>
            <fieldset>
              <legend>¿Qué tan seguro estás?</legend>
              {confianzas.map((c) => (
                <label key={c.valor}>
                  <input type="radio" name="confianza" checked={confianza === c.valor} onChange={() => setConfianza(c.valor)} />
                  {c.texto}
                </label>
              ))}
            </fieldset>
            <button type="submit" disabled={!confianza || enviando}>{enviando ? "Corrigiendo…" : "Enviar respuesta"}</button>
          </form>
        </>
      )}

      {intento.estado === "RESUELTO" && (
        <p role="status">
          ¡Correcto!{intento.respuestas.length === 1 ? " A la primera y sin pistas." : ` Con ${intento.pistasUsadas} pista(s).`}
        </p>
      )}

      {intento.estado === "RESCATADO" && (
        <div role="status">
          <p>Rescate: se acabaron las pistas.</p>
          {ultima?.correccion.explicacion && <p style={{ whiteSpace: "pre-line" }}>{ultima.correccion.explicacion}</p>}
          <p>Esta es la respuesta final:</p>
          <p><strong>{solucion ?? "…"}</strong></p>
          <p>Ahora resuelve otro ejercicio de este concepto para comprobar que lo entendiste.</p>
        </div>
      )}

      {intento.estado !== "EN_CURSO" && <button onClick={onTerminar}>Elegir otro ejercicio</button>}
    </section>
  );
}
