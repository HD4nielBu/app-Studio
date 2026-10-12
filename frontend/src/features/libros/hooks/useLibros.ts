import { useCallback, useEffect, useState } from "react";
import * as api from "../api/libros";

export function useLibros() {
  const [libros, setLibros] = useState<api.Libro[]>([]);
  const [error, setError] = useState<string | null>(null);

  const recargar = useCallback(() => api.listarLibros().then(setLibros, (e: Error) => setError(e.message)), []);

  useEffect(() => {
    recargar();
  }, [recargar]);

  /** Ejecuta una operación, recarga la lista y devuelve si salió bien. */
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
    libros,
    error,
    crear: (libro: api.NuevoLibro) => hacer(() => api.crearLibro(libro)),
    archivar: (id: string) => hacer(() => api.archivarLibro(id)),
    reactivar: (id: string) => hacer(() => api.reactivarLibro(id)),
    eliminar: (id: string) => hacer(() => api.eliminarLibro(id)),
  };
}
