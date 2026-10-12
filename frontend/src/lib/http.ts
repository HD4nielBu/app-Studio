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

/** Pedido a la API. Si falla, lanza un Error con el mensaje del backend ("detail"), pensado para mostrarse al alumno. */
export async function pedir<T>(metodo: "GET" | "POST" | "PUT" | "DELETE", ruta: string, cuerpo?: unknown): Promise<T> {
  const respuesta = await fetch(BASE_URL + ruta, {
    method: metodo,
    headers: cuerpo === undefined ? undefined : { "Content-Type": "application/json" },
    body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
  });
  if (!respuesta.ok) {
    const problema = await respuesta.json().catch(() => null);
    throw new Error(problema?.detail ?? `No se pudo completar la operación (HTTP ${respuesta.status}).`);
  }
  return (respuesta.status === 204 ? undefined : await respuesta.json()) as T;
}
