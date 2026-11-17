import { projetsApi } from '../api/projets'
import { useRessource } from './useRessource'

export function useProjets({ intervalle } = {}) {
  const { donnees, chargement, erreur, recharger } = useRessource(() => projetsApi.lister(), [], {
    initial: [],
    intervalle,
  })
  return { projets: donnees, chargement, erreur, recharger }
}
