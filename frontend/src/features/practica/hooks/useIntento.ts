import { useEffect, useState } from "react";
import * as api from "../api/practica";

/** Empieza (o retoma) el intento del ejercicio. La solución solo llega si el intento terminó en rescate. */
export function useIntento(ejercicioId: string) {
  const [intento, setIntento] = useState<api.Intento | null>(null);
  const [solucion, setSolucion] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.empezar(ejercicioId).then(setIntento, (e: Error) => setError(e.message));
  }, [ejercicioId]);

  useEffect(() => {
    if (intento?.estado === "RESCATADO") api.solucion(intento.id).then(setSolucion, (e: Error) => setError(e.message));
  }, [intento]);

  const responder = async (desarrollo: string, respuestaFinal: string, confianza: api.Confianza) => {
    if (!intento) return false;
    try {
      setIntento(await api.responder(intento.id, desarrollo, respuestaFinal, confianza));
      setError(null);
      return true;
    } catch (e) {
      setError((e as Error).message);
      return false;
    }
  };

  return { intento, solucion, error, responder };
}
