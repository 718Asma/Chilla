import { Loader2 } from 'lucide-react'

// variante : primaire | secondaire | fantome | danger ; taille : s | m | l
function Bouton({
  variante = 'primaire',
  taille = 'm',
  icone: Icone,
  chargement = false,
  disabled = false,
  pleineLargeur = false,
  type = 'button',
  className = '',
  children,
  ...reste
}) {
  const classes = ['bouton', `bouton--${variante}`, `bouton--${taille}`, pleineLargeur && 'bouton--plein', className]
    .filter(Boolean)
    .join(' ')
  return (
    <button type={type} className={classes} disabled={disabled || chargement} aria-busy={chargement || undefined} {...reste}>
      {chargement ? <Loader2 size={16} className="icone-rotation" aria-hidden="true" /> : Icone && <Icone size={16} aria-hidden="true" />}
      {children && <span>{children}</span>}
    </button>
  )
}

export default Bouton
