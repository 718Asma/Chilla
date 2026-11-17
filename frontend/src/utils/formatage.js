export function nomComplet(membre) {
  if (!membre) return 'Membre inconnu'
  return `${membre.prenom ?? ''} ${membre.nom ?? ''}`.trim() || 'Membre inconnu'
}

export function initiales(membre) {
  if (!membre) return '?'
  const p = (membre.prenom ?? '').trim()[0] ?? ''
  const n = (membre.nom ?? '').trim()[0] ?? ''
  return (p + n).toUpperCase() || '?'
}

// Numéro de couleur d'avatar stable (1 à 6) pour un identifiant.
export function teinteAvatar(id) {
  const nombre = Number(id) || 0
  return (Math.abs(nombre) % 6) + 1
}

export function codeProjet(id) {
  return `PRJ-${String(id).padStart(2, '0')}`
}

export function codeTache(id) {
  return `TSK-${String(id).padStart(3, '0')}`
}

export function pluriel(nombre, singulier, plurielForme) {
  return nombre > 1 ? (plurielForme ?? `${singulier}s`) : singulier
}

export function pourcentage(partie, total) {
  if (!total) return 0
  return Math.round((partie / total) * 100)
}
