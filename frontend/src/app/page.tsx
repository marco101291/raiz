import Link from "next/link";
import styles from "./page.module.css";
import { RootMark } from "@/components/RootMark";

export default function Home() {
  return (
    <main className={styles.page}>
      <p className={styles.marca}>
        <RootMark />
        Raíz
      </p>

      <h1 className={styles.titulo}>¿El dolor de espalda o de cuello te baja por un brazo o una pierna?</h1>

      <div className={styles.intro}>
        <p>
          Responde unas preguntas sobre tu dolor y marca en un mapa del cuerpo dónde lo sientes. Te
          explicamos qué raíces nerviosas son compatibles con tus síntomas y a quién conviene ver.
        </p>
        <p>
          Es una guía educativa: no diagnostica ni sustituye una consulta. Toma unos 5 minutos y no
          guardamos tus respuestas.
        </p>
      </div>

      <Link href="/evaluacion" className={styles.empezar}>
        Empezar la evaluación
      </Link>

      <p className={styles.urgente}>
        <strong>Si tienes adormecida la zona entre las piernas, no puedes controlar la orina o pierdes fuerza
        rápidamente, acude a urgencias ahora.</strong>{" "}
        No necesitas llenar este formulario.
      </p>
    </main>
  );
}
