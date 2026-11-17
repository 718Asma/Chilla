// Formats reçus du backend : "2026-06-01" (LocalDate) ou "2026-06-01T09:00:00" (date-heure).

function enDate(valeur) {
  if (!valeur) return null
  const texte = String(valeur)
  const normalise = texte.includes('T') ? texte : texte.replace(' ', 'T')
  const date = new Date(normalise)
  return Number.isNaN(date.getTime()) ? null : date
}

export function formaterDate(valeur, options) {
  if (!valeur) return '-'
  const date = enDate(valeur)
  if (!date) return String(valeur)
  return date.toLocaleDateString('fr-FR', options)
}

export function formaterDateCourte(valeur) {
  return formaterDate(valeur, { day: '2-digit', month: '2-digit', year: 'numeric' })
}

export function formaterDateLongue(valeur) {
  return formaterDate(valeur, { day: 'numeric', month: 'long', year: 'numeric' })
}

// "il y a 5 min", "hier", sinon la date.
export function dateRelative(valeur) {
  const date = enDate(valeur)
  if (!date) return '-'
  const secondes = Math.round((Date.now() - date.getTime()) / 1000)
  if (secondes < 45) return "à l'instant"
  const minutes = Math.round(secondes / 60)
  if (minutes < 60) return `il y a ${minutes} min`
  const heures = Math.round(minutes / 60)
  if (heures < 24) return `il y a ${heures} h`
  const jours = Math.round(heures / 24)
  if (jours === 1) return 'hier'
  if (jours < 7) return `il y a ${jours} jours`
  return formaterDate(valeur, { day: 'numeric', month: 'short', year: 'numeric' })
}

// Date locale au format yyyy-MM-dd (jamais toISOString : décalage horaire).
export function versChampDate(date = new Date()) {
  const annee = date.getFullYear()
  const mois = String(date.getMonth() + 1).padStart(2, '0')
  const jour = String(date.getDate()).padStart(2, '0')
  return `${annee}-${mois}-${jour}`
}

export function aujourdhui() {
  return versChampDate(new Date())
}

// Nombre de jours (entiers) entre aujourd'hui et la date donnée ; négatif si passée.
export function joursAvant(valeur) {
  const date = enDate(valeur)
  if (!date) return null
  const debut = new Date()
  debut.setHours(0, 0, 0, 0)
  const cible = new Date(date)
  cible.setHours(0, 0, 0, 0)
  return Math.round((cible.getTime() - debut.getTime()) / 86400000)
}

// Une tâche non terminée dont l'échéance est passée.
export function estEnRetard(tache) {
  return (tache.etat === 'NON_COMMENCE' || tache.etat === 'EN_COURS') && (joursAvant(tache.dateEcheance) ?? 0) < 0
}

// Part du temps écoulé entre deux dates (0 à 100).
export function pourcentageTempsEcoule(debut, fin) {
  const d = enDate(debut)
  const f = enDate(fin)
  if (!d || !f || f <= d) return 0
  const part = ((Date.now() - d.getTime()) / (f.getTime() - d.getTime())) * 100
  return Math.max(0, Math.min(100, Math.round(part)))
}
