import type { Metadata, Viewport } from "next";
import { Atkinson_Hyperlegible_Next } from "next/font/google";
import "./globals.css";

// Diseñada por el Braille Institute para baja visión: una sola familia para toda la interfaz.
const atkinson = Atkinson_Hyperlegible_Next({
  subsets: ["latin", "latin-ext"],
  display: "swap",
  // Next no tiene métricas de esta fuente para ajustar el respaldo; usamos la del sistema.
  adjustFontFallback: false,
  fallback: ["system-ui", "sans-serif"],
});

export const metadata: Metadata = {
  title: "Raíz",
  description:
    "Orientación educativa sobre dolor de espalda y cuello que baja a un brazo o una pierna. No diagnostica: te dice a quién ver y con qué prisa.",
};

export const viewport: Viewport = {
  themeColor: "#eef1ef",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="es-MX" className={atkinson.className}>
      <body>{children}</body>
    </html>
  );
}
