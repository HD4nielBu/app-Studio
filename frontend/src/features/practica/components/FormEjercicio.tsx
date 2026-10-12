import { useState, type FormEvent } from "react";
import type { Dificultad, NuevoEjercicio } from "../api/practica";

const vacio: NuevoEjercicio = { enunciado: "", fuente: "", dificultad: "MEDIA", respuestaFinal: "" };

export function FormEjercicio({ onAgregar }: { onAgregar: (e: NuevoEjercicio) => Promise<boolean> }) {
  const [ejercicio, setEjercicio] = useState(vacio);

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (await onAgregar(ejercicio)) setEjercicio(vacio);
  };

  return (
    <form onSubmit={enviar}>
      <h3>Agregar ejercicio</h3>
      <label>
        Enunciado
        <textarea required rows={3} value={ejercicio.enunciado}
          onChange={(e) => setEjercicio({ ...ejercicio, enunciado: e.target.value })} />
      </label>
      <label>
        Fuente (libro, página y número; vacía si es propio){" "}
        <input value={ejercicio.fuente} placeholder="Zill, pág. 140, ej. 12"
          onChange={(e) => setEjercicio({ ...ejercicio, fuente: e.target.value })} />
      </label>
      <label>
        Dificultad{" "}
        <select value={ejercicio.dificultad} onChange={(e) => setEjercicio({ ...ejercicio, dificultad: e.target.value as Dificultad })}>
          <option value="BASICA">Básica</option>
          <option value="MEDIA">Media</option>
          <option value="ALTA">Alta</option>
        </select>
      </label>
      <label>
        Respuesta final (no se te mostrará al resolver, salvo en el rescate){" "}
        <input required value={ejercicio.respuestaFinal}
          onChange={(e) => setEjercicio({ ...ejercicio, respuestaFinal: e.target.value })} />
      </label>
      <button type="submit">Agregar ejercicio</button>
    </form>
  );
}
