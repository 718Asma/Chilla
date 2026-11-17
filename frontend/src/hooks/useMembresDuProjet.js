import { membresApi } from '../api/membres'
import { projetsApi } from '../api/projets'
import { useAuth } from './useAuth'
import { useRessource } from './useRessource'

// Cache en mémoire des profils de membres (évite de refaire un GET /membres/{id} à chaque rechargement).
const DUREE_CACHE = 60000
const cache = new Map()

export function viderCacheMembres() {
  cache.clear()
}

async function chargerMembre(id) {
  const present = cache.get(id)
  if (present && Date.now() - present.date < DUREE_CACHE) {
    return present.membre
  }
  try {
    const membre = await membresApi.obtenir(id)
    cache.set(id, { membre, date: Date.now() })
    return membre
  } catch {
    return present?.membre ?? null
  }
}

// GET /projets/{id}/membres puis GET /membres/{idMembre} pour chacun (noms, emails).
// Renvoie aussi le rôle de l'utilisateur connecté : roleCourant / estChef.
export function useMembresDuProjet(idProjet, { intervalle } = {}) {
  const { membre: moi } = useAuth()

  const { donnees, chargement, erreur, recharger } = useRessource(
    async () => {
      const liens = await projetsApi.membres(idProjet)
      const profils = await Promise.all(liens.map((lien) => chargerMembre(lien.idMembre)))
      return liens.map((lien, i) => ({
        idMembre: lien.idMembre,
        idProjet: lien.idProjet,
        role: lien.role,
        nom: profils[i]?.nom ?? '',
        prenom: profils[i]?.prenom ?? `Membre n°${lien.idMembre}`,
        email: profils[i]?.email ?? '',
      }))
    },
    [idProjet],
    { initial: [], intervalle },
  )

  const roleCourant = donnees.find((m) => m.idMembre === moi?.id)?.role ?? null

  return {
    membres: donnees,
    chargement,
    erreur,
    recharger,
    roleCourant,
    estChef: roleCourant === 'CHEF_DE_PROJET',
  }
}
