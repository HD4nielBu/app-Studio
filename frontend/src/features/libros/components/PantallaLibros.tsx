import { useState } from "react";
import type { Libro } from "../api/libros";
import { useLibros } from "../hooks/useLibros";
import { FormLibro } from "./FormLibro";

type Props = { onAbrir: (libro: Libro) => void };

export function PantallaLibros({ onAbrir }: Props) {
  const { libros, error, crear, archivar, reactivar, eliminar } = useLibros();
  const [porEliminar, setPorEliminar] = useState<string | null>(null);

  const fila = (libro: Libro) => (
    <li key={libro.id}>
      <strong>{libro.nombre}</strong> · {libro.materia} ·{" "}
      {libro.fechaExamen ? `examen el ${libro.fechaExamen}` : "aprendizaje libre"}{" "}
      <button onClick={() => onAbrir(libro)}>Abrir</button>{" "}
      {libro.archivado
        ? <button onClick={() => reactivar(libro.id)}>Reactivar</button>
        : <button onClick={() => archivar(libro.id)}>Archivar</button>}{" "}
      {porEliminar === libro.id ? (
        <span role="alert">
          Se borrará el libro con todo su historial.{" "}
          <button onClick={() => eliminar(libro.id).then(() => setPorEliminar(null))}>Sí, eliminar</button>{" "}
          <button onClick={() => setPorEliminar(null)}>Cancelar</button>
        </span>
      ) : (
        <button onClick={() => setPorEliminar(libro.id)}>Eliminar</button>
      )}
    </li>
  );

  const activos = libros.filter((l) => !l.archivado);
  const archivados = libros.filter((l) => l.archivado);

  return (
    <section>
      <h2>Mis libros</h2>
      {error && <p role="alert">{error}</p>}
      {activos.length ? <ul>{activos.map(fila)}</ul> : <p>Todavía no tienes libros activos.</p>}
      {archivados.length > 0 && (
        <details>
          <summary>Archivados ({archivados.length})</summary>
          <ul>{archivados.map(fila)}</ul>
        </details>
      )}
      <FormLibro onCrear={crear} />
    </section>
  );
}
