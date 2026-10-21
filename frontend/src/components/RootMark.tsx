/** Raíz logo: a nerve trunk splitting into roots, in the yellow of anatomy atlases. */
export function RootMark({ size = 28 }: { size?: number }) {
  return (
    <svg width={size} height={size} viewBox="0 0 32 32" aria-hidden="true" focusable="false">
      <path
        d="M16 3v11M16 14c0 5-6 6-8 14M16 14c0 5 6 6 8 14M16 14v14"
        fill="none"
        stroke="var(--tinta)"
        strokeWidth="5"
        strokeLinecap="round"
      />
      <path
        d="M16 3v11M16 14c0 5-6 6-8 14M16 14c0 5 6 6 8 14M16 14v14"
        fill="none"
        stroke="var(--nervio)"
        strokeWidth="2.5"
        strokeLinecap="round"
      />
    </svg>
  );
}
