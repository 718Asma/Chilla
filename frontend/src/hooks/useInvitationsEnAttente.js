import { invitationsApi } from '../api/invitations'
import { useRessource } from './useRessource'

// Compteur léger pour le badge de la barre de navigation.
export function useInvitationsEnAttente() {
  const { donnees } = useRessource(() => invitationsApi.enAttente(), [], { initial: [] })
  return donnees.length
}
