import { tachesApi } from '../api/taches'
import { useRessource } from './useRessource'

export function useTaches(idProjet, { intervalle } = {}) {
  const { donnees, chargement, erreur, recharger, modifierLocalement } = useRessource(
    () => tachesApi.listerDuProjet(idProjet),
    [idProjet],
    { initial: [], intervalle },
  )
  return { taches: donnees, chargement, erreur, recharger, modifierLocalement }
}
