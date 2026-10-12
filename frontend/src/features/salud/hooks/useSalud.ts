import { useEffect, useState } from "react";
import { obtenerSalud } from "../api/salud";

export type EstadoConexion = "comprobando" | "conectado" | "sin-base" | "sin-backend";

export function useSalud(): EstadoConexion {
  const [estado, setEstado] = useState<EstadoConexion>("comprobando");

  useEffect(() => {
    const control = new AbortController();
    obtenerSalud(control.signal)
      .then((s) => setEstado(s.status === "UP" ? "conectado" : "sin-base"))
      .catch(() => {
        if (!control.signal.aborted) setEstado("sin-backend");
      });
    return () => control.abort();
  }, []);

  return estado;
}
