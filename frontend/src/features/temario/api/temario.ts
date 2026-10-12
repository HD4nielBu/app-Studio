import { pedir } from "../../../lib/http";

// shortcut: tipos escritos a mano, generarlos desde OpenAPI (types/) cuando la API crezca.
export type Concepto = { id: string; nombre: string };
export type Tema = { id: string; libroId: string; nombre: string; enTemario: boolean; conceptos: Concepto[] };

export type Semaforo = "SIN_VER" | "NO_LO_RESUELVE" | "CON_AYUDA" | "DOMINADO";

export const MIN_CONCEPTOS = 3;
export const MAX_CONCEPTOS = 6;

export const temasDe = (libroId: string) => pedir<Tema[]>("GET", `/api/libros/${libroId}/temas`);
export const agregarTema = (libroId: string, nombre: string) => pedir<Tema>("POST", `/api/libros/${libroId}/temas`, { nombre });
export const definirConceptos = (temaId: string, conceptos: string[]) =>
  pedir<Tema>("PUT", `/api/temas/${temaId}/conceptos`, { conceptos });
export const quitarDelTemario = (temaId: string) => pedir<Tema>("POST", `/api/temas/${temaId}/quitar`);
export const restituir = (temaId: string) => pedir<Tema>("POST", `/api/temas/${temaId}/restituir`);
export const semaforo = (libroId: string) => pedir<Record<string, Semaforo>>("GET", `/api/libros/${libroId}/semaforo`);
