import { useState, type FormEvent } from "react";
import type { NuevoLibro, Tono, Tratamiento } from "../api/libros";

const vacio: NuevoLibro = { nombre: "", materia: "", fechaExamen: null, tono: "NEUTRO", tratamiento: "TU" };

export function FormLibro({ onCrear }: { onCrear: (libro: NuevoLibro) => Promise<boolean> }) {
  const [libro, setLibro] = useState(vacio);

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (await onCrear(libro)) setLibro(vacio);
  };

  return (
    <form onSubmit={enviar}>
      <h2>Nuevo libro</h2>
      <label>
        Nombre <input required value={libro.nombre} onChange={(e) => setLibro({ ...libro, nombre: e.target.value })}
          placeholder="Cálculo 3 · 2.º parcial" />
      </label>
      <label>
        Materia <input required value={libro.materia} onChange={(e) => setLibro({ ...libro, materia: e.target.value })} />
      </label>
      <label>
        Fecha del examen (vacía = aprendizaje libre){" "}
        <input type="date" value={libro.fechaExamen ?? ""}
          onChange={(e) => setLibro({ ...libro, fechaExamen: e.target.value || null })} />
      </label>
      <label>
        Tono{" "}
        <select value={libro.tono} onChange={(e) => setLibro({ ...libro, tono: e.target.value as Tono })}>
          <option value="ESTRICTO">Estricto</option>
          <option value="NEUTRO">Neutro</option>
          <option value="CERCANO">Cercano</option>
        </select>
      </label>
      <label>
        Tratamiento{" "}
        <select value={libro.tratamiento} onChange={(e) => setLibro({ ...libro, tratamiento: e.target.value as Tratamiento })}>
          <option value="TU">Tú</option>
          <option value="VOS">Vos</option>
          <option value="USTED">Usted</option>
        </select>
      </label>
      <button type="submit">Crear libro</button>
    </form>
  );
}
