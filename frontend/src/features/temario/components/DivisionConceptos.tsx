import { useState, type FormEvent } from "react";
import { MAX_CONCEPTOS, MIN_CONCEPTOS, type Concepto, type Semaforo, type Tema } from "../api/temario";

const colores: Record<Semaforo, string> = {
  SIN_VER: "⚪ sin ver",
  NO_LO_RESUELVE: "🔴 no lo resuelve",
  CON_AYUDA: "🟡 con ayuda",
  DOMINADO: "🟢 dominado",
};

type Props = {
  tema: Tema;
  semaforo: Record<string, Semaforo>;
  onGuardar: (conceptos: string[]) => Promise<boolean>;
  onPracticar: (concepto: Concepto) => void;
};

export function DivisionConceptos({ tema, semaforo, onGuardar, onPracticar }: Props) {
  const [texto, setTexto] = useState(tema.conceptos.map((c) => c.nombre).join("\n"));
  const [editando, setEditando] = useState(tema.conceptos.length === 0);

  const guardar = async (e: FormEvent) => {
    e.preventDefault();
    const conceptos = texto.split("\n").map((l) => l.trim()).filter(Boolean);
    if (await onGuardar(conceptos)) setEditando(false);
  };

  if (!editando) {
    return (
      <>
        <ol>
          {tema.conceptos.map((c) => (
            <li key={c.id}>
              {c.nombre} · {colores[semaforo[c.id] ?? "SIN_VER"]}{" "}
              {tema.enTemario && <button onClick={() => onPracticar(c)}>Practicar</button>}
            </li>
          ))}
        </ol>
        <button onClick={() => setEditando(true)}>Corregir conceptos</button>
      </>
    );
  }
  return (
    <form onSubmit={guardar}>
      <label>
        Conceptos de «{tema.nombre}», uno por línea (de {MIN_CONCEPTOS} a {MAX_CONCEPTOS})
        <textarea rows={MAX_CONCEPTOS} value={texto} onChange={(e) => setTexto(e.target.value)} />
      </label>
      <button type="submit">Guardar conceptos</button>
    </form>
  );
}
