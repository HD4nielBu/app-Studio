import { useState, type FormEvent } from "react";
import type { Concepto } from "../api/temario";
import { useTemario } from "../hooks/useTemario";
import { DivisionConceptos } from "./DivisionConceptos";

type Props = { libroId: string; nombreLibro: string; onVolver: () => void; onPracticar: (concepto: Concepto) => void };

export function PantallaTemario({ libroId, nombreLibro, onVolver, onPracticar }: Props) {
  const { temas, semaforo, error, agregar, definir, quitar, restituir } = useTemario(libroId);
  const [nombre, setNombre] = useState("");

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (await agregar(nombre)) setNombre("");
  };

  const elegidos = temas.filter((t) => t.enTemario);
  const quitados = temas.filter((t) => !t.enTemario);

  return (
    <section>
      <button onClick={onVolver}>← Mis libros</button>
      <h2>Temario de {nombreLibro}</h2>
      {error && <p role="alert">{error}</p>}
      {elegidos.length === 0 && <p>Agrega los temas que entran en tu examen.</p>}
      {elegidos.map((tema) => (
        <article key={tema.id}>
          <h3>{tema.nombre}</h3>
          <DivisionConceptos tema={tema} semaforo={semaforo} onGuardar={(c) => definir(tema.id, c)} onPracticar={onPracticar} />{" "}
          <button onClick={() => quitar(tema.id)}>Quitar del temario</button>
        </article>
      ))}
      <form onSubmit={enviar}>
        <label>
          Nuevo tema <input required value={nombre} onChange={(e) => setNombre(e.target.value)} />
        </label>
        <button type="submit">Agregar tema</button>
      </form>
      {quitados.length > 0 && (
        <details>
          <summary>Quitados del temario ({quitados.length}) · su historial se conserva</summary>
          <ul>
            {quitados.map((t) => (
              <li key={t.id}>
                {t.nombre} <button onClick={() => restituir(t.id)}>Volver a agregar</button>
              </li>
            ))}
          </ul>
        </details>
      )}
    </section>
  );
}
