// variante : primaire | neutre | succes | alerte | danger | gris
function Badge({ variante = 'neutre', icone: Icone, point = false, children, className = '' }) {
  return (
    <span className={`badge badge--${variante} ${className}`.trim()}>
      {point && <span className="badge__point" aria-hidden="true" />}
      {Icone && <Icone size={12} aria-hidden="true" />}
      {children}
    </span>
  )
}

export default Badge
