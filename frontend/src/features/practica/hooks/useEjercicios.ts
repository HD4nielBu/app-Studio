import { useCallback, useEffect, useState } from "react";
import * as api from "../api/practica";

export function useEjercicios(conceptoId: string) {
  const [ejercicios, setEjercicios] = useState<api.Ejercicio[]>([]);
  const [error, setError] = useState<string | null>(null);

  const recargar = useCallback(
    () => api.ejerciciosDe(conceptoId).then(setEjercicios, (e: Error) => setError(e.message)),
    [conceptoId],
  );

  useEffect(() => {
    recargar();
  }, [recargar]);

  const agregar = async (ejercicio: api.NuevoEjercicio) => {
    try {
      await api.agregarEjercicio(conceptoId, ejercicio);
      setError(null);
      await recargar();
      return true;
    } catch (e) {
      setError((e as Error).message);
      return false;
    }
  };

  return { ejercicios, error, agregar };
}
