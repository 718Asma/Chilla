import { LONGUEUR_MAX_TEXTE, LONGUEUR_MIN_MOT_DE_PASSE } from './constantes'

const MOTIF_EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/

export function emailValide(valeur) {
  return MOTIF_EMAIL.test((valeur ?? '').trim())
}

export function erreurEmail(valeur) {
  if (!valeur?.trim()) return "L'adresse email est obligatoire."
  if (!emailValide(valeur)) return "Saisissez une adresse email valide (ex. prenom@exemple.com)."
  return null
}

export function erreurMotDePasse(valeur) {
  if (!valeur) return 'Le mot de passe est obligatoire.'
  if (valeur.length < LONGUEUR_MIN_MOT_DE_PASSE) {
    return `Le mot de passe doit contenir au moins ${LONGUEUR_MIN_MOT_DE_PASSE} caractères.`
  }
  return null
}

export function erreurObligatoire(valeur, nom) {
  return valeur?.trim() ? null : `${nom} est obligatoire.`
}

export function erreurLongueur(valeur, nom) {
  return (valeur ?? '').length > LONGUEUR_MAX_TEXTE ? `${nom} ne doit pas dépasser ${LONGUEUR_MAX_TEXTE} caractères.` : null
}

// Retire les clés sans erreur : un objet vide signifie « formulaire valide ».
export function nettoyerErreurs(erreurs) {
  return Object.fromEntries(Object.entries(erreurs).filter(([, message]) => message))
}
