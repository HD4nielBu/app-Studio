import { pedir } from "../../../lib/http";

// shortcut: tipos escritos a mano, generarlos desde OpenAPI (types/) cuando la API crezca.
export type Dificultad = "BASICA" | "MEDIA" | "ALTA";
export type Ejercicio = { id: string; conceptoId: string; enunciado: string; fuente: string | null; dificultad: Dificultad };
export type NuevoEjercicio = { enunciado: string; fuente: string; dificultad: Dificultad; respuestaFinal: string };
export type Confianza = 1 | 2 | 3;

export type Respuesta = {
  desarrollo: string;
  respuestaFinal: string;
  confianza: Confianza;
  correccion: { correcta: boolean; tipoDeFallo: string | null; explicacion: string | null };
};

export type Intento = {
  id: string;
  ejercicioId: string;
  estado: "EN_CURSO" | "RESUELTO" | "RESCATADO";
  pistasUsadas: number;
  pistasMaximas: number;
  respuestas: Respuesta[];
};

export const ejerciciosDe = (conceptoId: string) => pedir<Ejercicio[]>("GET", `/api/conceptos/${conceptoId}/ejercicios`);
export const agregarEjercicio = (conceptoId: string, e: NuevoEjercicio) =>
  pedir<Ejercicio>("POST", `/api/conceptos/${conceptoId}/ejercicios`, e);
export const empezar = (ejercicioId: string) => pedir<Intento>("POST", `/api/ejercicios/${ejercicioId}/intentos`);
export const responder = (intentoId: string, desarrollo: string, respuestaFinal: string, confianza: Confianza) =>
  pedir<Intento>("POST", `/api/intentos/${intentoId}/respuestas`, { desarrollo, respuestaFinal, confianza });
export const solucion = (intentoId: string) =>
  pedir<{ respuestaFinal: string }>("GET", `/api/intentos/${intentoId}/solucion`).then((s) => s.respuestaFinal);
