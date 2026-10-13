import styles from "./ChipGroup.module.css";

interface Option {
  id?: string;
  label?: string;
}

interface Props {
  name: string;
  options: Option[];
  value: string[];
  onChange: (value: string[]) => void;
  /** Opción que excluye a las demás, p. ej. "Nada lo alivia". */
  exclusive?: string;
  describedBy?: string;
}

/** Selección múltiple como chips. Son checkboxes reales: teclado, lector de pantalla y formularios funcionan. */
export function ChipGroup({ name, options, value, onChange, exclusive, describedBy }: Props) {
  const toggle = (id: string, checked: boolean) => {
    if (!checked) {
      onChange(value.filter((v) => v !== id));
    } else if (id === exclusive) {
      onChange([id]);
    } else {
      onChange([...value.filter((v) => v !== exclusive), id]);
    }
  };

  return (
    <div className={styles.chips} aria-describedby={describedBy}>
      {options.map(({ id = "", label }) => (
        <label key={id} className={styles.chip}>
          <input
            type="checkbox"
            name={name}
            value={id}
            checked={value.includes(id)}
            onChange={(event) => toggle(id, event.target.checked)}
          />
          <span>{label}</span>
        </label>
      ))}
    </div>
  );
}
