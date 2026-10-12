# src · Arquitectura hexagonal (obligatoria)

Toda la app de Lazy Studio vive en esta carpeta y sigue la **arquitectura hexagonal** (puertos y adaptadores). No es opcional. La lógica de negocio que se implementa aquí está definida en `../producto/logica-de-negocio.md`.

```
src/
├── domain/                 ← núcleo: reglas de negocio puras
├── application/
│   ├── ports/
│   │   ├── in/             ← puertos de entrada (lo que la app ofrece)
│   │   └── out/            ← puertos de salida (lo que la app necesita)
│   └── use-cases/          ← casos de uso (implementan ports/in, usan ports/out)
├── adapters/
│   ├── in/                 ← adaptadores de entrada: UI de escritorio, CLI…
│   └── out/                ← adaptadores de salida: persistencia, IA, CAS, PDF…
└── bootstrap/              ← raíz de composición: conecta adaptadores con puertos
```

## Regla de dependencias (la única que no se negocia)

Las dependencias apuntan **siempre hacia adentro**:

```
adapters  ──►  application  ──►  domain
bootstrap ──►  todo (solo para conectar)
```

| Capa | Puede importar | **Nunca** importa |
|---|---|---|
| `domain` | Nada fuera de sí misma (solo la librería estándar) | `application`, `adapters`, frameworks, BD, IA, UI |
| `application` | `domain` | `adapters`, frameworks, BD, IA, UI |
| `adapters` | `application` (sus puertos) y `domain` | Otros adaptadores |
| `bootstrap` | Todo | — (no contiene lógica) |

## Qué va en cada capa

**`domain/`**: entidades (Alumno, Examen, Concepto, Intento, Fallo, Sesión…), objetos de valor (Confianza 1–3, EstadoDominio…), reglas e invariantes (criterio de dominio, contador de pistas → rescate, transiciones de estado), eventos de dominio y errores de dominio. No hace E/S, no lee el reloj del sistema directamente y no llama a la red.

**`application/ports/in/`**: interfaces de los casos de uso, es decir, lo que un adaptador de entrada puede pedir. Por ejemplo: `IniciarSesion`, `ResponderEjercicio`, `PedirPista`, `CerrarSesion`.

**`application/ports/out/`**: interfaces de lo que la app necesita del mundo exterior, definidas **en términos del dominio** y no de la tecnología. Por ejemplo: `RepositorioDeProgreso`, `Tutor` (IA), `VerificadorMatematico` (CAS), `LectorDeMaterial`, `Reloj`, `Notificador`.

**`application/use-cases/`**: orquestan un caso de uso. Cargan con los puertos de salida, delegan las reglas al dominio, guardan y devuelven DTOs. Aquí no se decide ninguna regla pedagógica.

**`adapters/in/`**: traducen el mundo exterior (UI de escritorio, CLI, skill de Claude Code) a llamadas a `ports/in`. No contienen lógica de negocio.

**`adapters/out/`**: implementan `ports/out` con tecnología concreta, por ejemplo Markdown o SQLite, la API de Claude, SymPy o un lector de PDF. Cada tecnología puede cambiarse sin tocar `domain` ni `application`.

**`bootstrap/`**: crea los adaptadores, los inyecta en los casos de uso y arranca la app. Es el único lugar que conoce todas las piezas concretas.

## Reglas de trabajo
1. **La lógica de negocio va en `domain`.** Si una regla de `logica-de-negocio.md` aparece en un adaptador o en un caso de uso, está en el lugar equivocado.
2. **Los puertos de salida los define la aplicación**, con nombres del dominio (`Tutor`, no `ClaudeClient`).
3. **El dominio se prueba sin dobles de infraestructura.** Los casos de uso se prueban con adaptadores falsos en memoria.
4. **Primero el dominio, luego los casos de uso y al final los adaptadores.**
5. Si una carpeta crece mucho, se divide por **contexto** dentro de cada capa (p. ej. `domain/aprendizaje/`, `domain/planificacion/`, `domain/material/`), sin romper la regla de dependencias.
