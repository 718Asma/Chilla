import { initiales, nomComplet, teinteAvatar } from '../../utils/formatage'

// membre : { id | idMembre, nom, prenom }
function Avatar({ membre, taille = 'm', className = '' }) {
  const id = membre?.id ?? membre?.idMembre
  return (
    <span
      className={`avatar avatar--${taille} avatar--t${teinteAvatar(id)} ${className}`.trim()}
      title={nomComplet(membre)}
      role="img"
      aria-label={nomComplet(membre)}
    >
      {initiales(membre)}
    </span>
  )
}

export default Avatar
