import { useSalud, type EstadoConexion as Estado } from "../hooks/useSalud";

const textos: Record<Estado, string> = {
  comprobando: "Comprobando conexión…",
  conectado: "Conectado",
  "sin-base": "El backend responde, pero la base de datos no está disponible",
  "sin-backend": "No se pudo conectar con el backend",
};

export function EstadoConexion() {
  return <p role="status">{textos[useSalud()]}</p>;
}
