// La URL del backend vive solo aquí. shortcut: fija en localhost, pasar a variable de entorno al tener servidor (Fase 3).
const BASE_URL = "http://localhost:8080";

export async function getJson<T>(ruta: string, signal?: AbortSignal): Promise<T> {
  const respuesta = await fetch(BASE_URL + ruta, { signal });
  // Actuator responde 503 con el cuerpo {"status":"DOWN"} cuando la base no está disponible.
  if (!respuesta.ok && respuesta.status !== 503) {
    throw new Error(`HTTP ${respuesta.status} en ${ruta}`);
  }
  return respuesta.json() as Promise<T>;
}
