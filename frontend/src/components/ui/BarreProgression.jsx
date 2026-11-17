function BarreProgression({ valeur, variante = 'primaire', libelle }) {
  const borne = Math.max(0, Math.min(100, valeur || 0))
  return (
    <div
      className="progression"
      role="progressbar"
      aria-valuenow={borne}
      aria-valuemin={0}
      aria-valuemax={100}
      aria-label={libelle}
    >
      <div className={`progression__barre progression__barre--${variante}`} style={{ width: `${borne}%` }} />
    </div>
  )
}

export default BarreProgression
