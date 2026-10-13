import type { Metadata } from "next";
import Link from "next/link";
import { RootMark } from "@/components/RootMark";
import { Wizard } from "@/features/evaluacion/Wizard";
import styles from "./page.module.css";

export const metadata: Metadata = {
  title: "Evaluación | Raíz",
};

export default function EvaluacionPage() {
  return (
    <div className={styles.pagina}>
      <header>
        <Link href="/" className={styles.marca}>
          <RootMark size={24} />
          Raíz
        </Link>
      </header>
      <main>
        <Wizard />
      </main>
    </div>
  );
}
