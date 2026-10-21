import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach } from "vitest";

afterEach(() => {
  cleanup();
  // Tests with the node environment (e.g. the proxy) have no sessionStorage.
  globalThis.sessionStorage?.clear();
});
