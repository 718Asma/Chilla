import { projetsApi } from '../api/projets'
import { useRessource } from './useRessource'

export function useProjet(idProjet) {
  const { donnees, chargement, erreur, recharger } = useRessource(() => projetsApi.obtenir(idProjet), [idProjet])
  return { projet: donnees, chargement, erreur, recharger }
}
