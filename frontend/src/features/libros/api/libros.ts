import { pedir } from "../../../lib/http";

// shortcut: tipos escritos a mano, generarlos desde OpenAPI (types/) cuando la API crezca.
export type Tono = "ESTRICTO" | "NEUTRO" | "CERCANO";
export type Tratamiento = "TU" | "VOS" | "USTED";

export type Libro = {
  id: string;
  nombre: string;
  materia: string;
  fechaExamen: string | null;
  tono: Tono;
  tratamiento: Tratamiento;
  archivado: boolean;
};

export type NuevoLibro = Omit<Libro, "id" | "archivado">;

export const listarLibros = () => pedir<Libro[]>("GET", "/api/libros");
export const crearLibro = (libro: NuevoLibro) => pedir<Libro>("POST", "/api/libros", libro);
export const archivarLibro = (id: string) => pedir<Libro>("POST", `/api/libros/${id}/archivar`);
export const reactivarLibro = (id: string) => pedir<Libro>("POST", `/api/libros/${id}/reactivar`);
export const eliminarLibro = (id: string) => pedir<void>("DELETE", `/api/libros/${id}`);
