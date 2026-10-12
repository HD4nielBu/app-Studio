import { useCallback, useEffect, useState } from "react";
import * as api from "../api/temario";

export function useTemario(libroId: string) {
  const [temas, setTemas] = useState<api.Tema[]>([]);
  const [semaforo, setSemaforo] = useState<Record<string, api.Semaforo>>({});
  const [error, setError] = useState<string | null>(null);

  const recargar = useCallback(
    () => Promise.all([api.temasDe(libroId), api.semaforo(libroId)])
      .then(([t, s]) => { setTemas(t); setSemaforo(s); }, (e: Error) => setError(e.message)),
    [libroId],
  );

  useEffect(() => {
    recargar();
  }, [recargar]);

  const hacer = async (operacion: () => Promise<unknown>) => {
    try {
      await operacion();
      setError(null);
      await recargar();
      return true;
    } catch (e) {
      setError((e as Error).message);
      return false;
    }
  };

  return {
    temas,
    semaforo,
    error,
    agregar: (nombre: string) => hacer(() => api.agregarTema(libroId, nombre)),
    definir: (temaId: string, conceptos: string[]) => hacer(() => api.definirConceptos(temaId, conceptos)),
    quitar: (temaId: string) => hacer(() => api.quitarDelTemario(temaId)),
    restituir: (temaId: string) => hacer(() => api.restituir(temaId)),
  };
}
