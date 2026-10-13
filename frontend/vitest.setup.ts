import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach } from "vitest";

afterEach(() => {
  cleanup();
  // Las pruebas con entorno node (p. ej. el proxy) no tienen sessionStorage.
  globalThis.sessionStorage?.clear();
});
