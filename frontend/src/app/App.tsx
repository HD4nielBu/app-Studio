import { useState } from "react";
import { PantallaLibros, type Libro } from "../features/libros";
import { PantallaTemario, type Concepto } from "../features/temario";
import { PantallaPractica } from "../features/practica";
import { EstadoConexion } from "../features/salud";

export default function App() {
  const [libro, setLibro] = useState<Libro | null>(null);
  const [concepto, setConcepto] = useState<Concepto | null>(null);

  const pantalla = !libro ? (
    <PantallaLibros onAbrir={setLibro} />
  ) : !concepto ? (
    <PantallaTemario libroId={libro.id} nombreLibro={libro.nombre} onVolver={() => setLibro(null)} onPracticar={setConcepto} />
  ) : (
    <PantallaPractica conceptoId={concepto.id} nombreConcepto={concepto.nombre} onVolver={() => setConcepto(null)} />
  );

  return (
    <main>
      <h1>Lazy Studio</h1>
      <EstadoConexion />
      {pantalla}
    </main>
  );
}
