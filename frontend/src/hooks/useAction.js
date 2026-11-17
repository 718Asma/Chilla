import { useCallback } from 'react'
import { useNotifications } from './useNotifications'
import { useToast } from './useToast'

// Exécute une action de l'utilisateur (appel API) puis met tout l'affichage à jour.
// - succès : toast de confirmation (optionnel) + signaler() => tous les hooks se rechargent ;
// - échec : toast d'erreur avec le message du backend.
// Renvoie true si l'action a réussi.
export function useAction() {
  const { afficherToast } = useToast()
  const { signaler } = useNotifications()

  return useCallback(
    async (fonction, messageSucces) => {
      try {
        await fonction()
        if (messageSucces) afficherToast({ type: 'succes', titre: messageSucces })
        signaler()
        return true
      } catch (e) {
        afficherToast({ type: 'erreur', titre: 'Action impossible', message: e.message, duree: 7000 })
        return false
      }
    },
    [afficherToast, signaler],
  )
}
