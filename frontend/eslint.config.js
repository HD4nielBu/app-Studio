import js from "@eslint/js";
import tseslint from "typescript-eslint";
import reactHooks from "eslint-plugin-react-hooks";
import { importX, createNodeResolver } from "eslint-plugin-import-x";
import globals from "globals";
import { readdirSync } from "node:fs";

// Regla de dependencias del frontend (como ArchUnit en el backend):
// components, lib, types, platform/ports → features → app. Una feature no importa de otra.
const features = readdirSync("./src/features", { withFileTypes: true })
  .filter((d) => d.isDirectory())
  .map((d) => d.name);
const compartido = ["./src/components", "./src/lib", "./src/types", "./src/platform/ports.ts"];
const implementaciones = ["./src/platform/tauri-desktop", "./src/platform/tauri-mobile"];

const zonas = [
  ...compartido.map((target) => ({
    target,
    from: ["./src/features", "./src/app", ...implementaciones],
    message: "Lo compartido no conoce features, app ni implementaciones de plataforma.",
  })),
  {
    target: "./src/features",
    from: ["./src/app", ...implementaciones],
    message: "Una feature usa platform/ports.ts; solo app/ elige la implementación.",
  },
  ...implementaciones.map((target) => ({
    target,
    from: ["./src/features", "./src/app"],
    message: "Una implementación de plataforma no conoce features ni app.",
  })),
  ...features.map((f) => ({
    target: `./src/features/${f}`,
    from: "./src/features",
    except: [`./${f}`],
    message: "Una feature no importa de otra feature.",
  })),
];

export default tseslint.config(
  { ignores: ["dist", "src-tauri"] },
  js.configs.recommended,
  tseslint.configs.recommended,
  reactHooks.configs.flat.recommended,
  {
    files: ["src/**/*.{ts,tsx}"],
    languageOptions: { globals: globals.browser },
    plugins: { "import-x": importX },
    settings: {
      "import-x/resolver-next": [createNodeResolver({ extensions: [".ts", ".tsx", ".js"] })],
    },
    rules: {
      "import-x/no-restricted-paths": ["error", { zones: zonas }],
    },
  },
);
