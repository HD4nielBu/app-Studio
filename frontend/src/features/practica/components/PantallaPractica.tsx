import { useState } from "react";
import type { Ejercicio } from "../api/practica";
import { useEjercicios } from "../hooks/useEjercicios";
import { FormEjercicio } from "./FormEjercicio";
import { Resolver } from "./Resolver";

type Props = { conceptoId: string; nombreConcepto: string; onVolver: () => void };

const dificultades = { BASICA: "básica", MEDIA: "media", ALTA: "alta" };

export function PantallaPractica({ conceptoId, nombreConcepto, onVolver }: Props) {
  const { ejercicios, error, agregar } = useEjercicios(conceptoId);
  const [elegido, setElegido] = useState<Ejercicio | null>(null);

  return (
    <section>
      <button onClick={onVolver}>← Temario</button>
      <h2>Practicar: {nombreConcepto}</h2>
      {error && <p role="alert">{error}</p>}
      {elegido ? (
        <Resolver key={elegido.id} ejercicio={elegido} onTerminar={() => setElegido(null)} />
      ) : (
        <>
          {ejercicios.length ? (
            <ol>
              {ejercicios.map((e) => (
                <li key={e.id}>
                  {e.enunciado} · {dificultades[e.dificultad]} {e.fuente ? `· ${e.fuente}` : "· propio"}{" "}
                  <button onClick={() => setElegido(e)}>Resolver</button>
                </li>
              ))}
            </ol>
          ) : (
            <p>Este concepto todavía no tiene ejercicios.</p>
          )}
          <FormEjercicio onAgregar={agregar} />
        </>
      )}
    </section>
  );
}
