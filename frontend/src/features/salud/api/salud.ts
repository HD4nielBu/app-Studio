import { getJson } from "../../../lib/http";

export type Salud = { status: "UP" | "DOWN" | "OUT_OF_SERVICE" | "UNKNOWN" };

export const obtenerSalud = (signal?: AbortSignal) => getJson<Salud>("/api/salud", signal);
