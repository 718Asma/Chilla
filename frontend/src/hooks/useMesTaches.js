import { membresApi } from '../api/membres'
import { useAuth } from './useAuth'
import { useRessource } from './useRessource'

export function useMesTaches() {
  const { membre } = useAuth()
  const { donnees, chargement, erreur, recharger } = useRessource(() => membresApi.taches(membre.id), [membre?.id], {
    initial: [],
    actif: Boolean(membre),
  })
  return { taches: donnees, chargement, erreur, recharger }
}
