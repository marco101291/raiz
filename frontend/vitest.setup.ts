import "@testing-library/jest-dom/vitest";
import { cleanup } from "@testing-library/react";
import { afterEach } from "vitest";

// jsdom does not implement scrolling and logs an error on every call; the wizard scrolls on each step.
if (typeof window !== "undefined") {
  window.scrollTo = () => {};
}

afterEach(() => {
  cleanup();
  // Tests with the node environment (e.g. the proxy) have no sessionStorage.
  globalThis.sessionStorage?.clear();
});
