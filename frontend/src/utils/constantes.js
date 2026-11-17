export const LONGUEUR_MAX_TEXTE = 255
export const LONGUEUR_MIN_MOT_DE_PASSE = 8
export const INTERVALLE_ACTUALISATION = 30000 // filet de sécurité en plus du temps réel (ms)

// variante = couleur du badge (voir Badge.css)
export const ETATS_TACHE = {
  NON_COMMENCE: { libelle: 'À faire', variante: 'neutre', couleur: 'var(--c-primaire)' },
  EN_COURS: { libelle: 'En cours', variante: 'primaire', couleur: 'var(--c-alerte)' },
  TERMINE: { libelle: 'Terminé', variante: 'succes', couleur: 'var(--c-succes)' },
  ANNULE: { libelle: 'Annulé', variante: 'gris', couleur: 'var(--c-neutre)' },
}

export const ORDRE_ETATS_TACHE = ['NON_COMMENCE', 'EN_COURS', 'TERMINE', 'ANNULE']

export const ETATS_SPRINT = {
  NON_COMMENCE: { libelle: 'À venir', variante: 'neutre' },
  EN_COURS: { libelle: 'En cours', variante: 'primaire' },
  TERMINE: { libelle: 'Terminé', variante: 'succes' },
  ANNULE: { libelle: 'Annulé', variante: 'gris' },
}

// Transitions autorisées pour un sprint.
export const TRANSITIONS_SPRINT = {
  NON_COMMENCE: ['EN_COURS', 'ANNULE'],
  EN_COURS: ['TERMINE', 'ANNULE'],
  TERMINE: [],
  ANNULE: [],
}

export const ROLES = {
  CHEF_DE_PROJET: { libelle: 'Chef de projet', variante: 'primaire' },
  MEMBRE: { libelle: 'Membre', variante: 'neutre' },
}

export const STATUTS_INVITATION = {
  EN_ATTENTE: { libelle: 'En attente', variante: 'alerte' },
  ACCEPTEE: { libelle: 'Acceptée', variante: 'succes' },
  REFUSEE: { libelle: 'Refusée', variante: 'danger' },
}
