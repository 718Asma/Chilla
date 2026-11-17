import { useMemo } from 'react'
import { invitationsApi } from '../api/invitations'
import { membresApi } from '../api/membres'
import { projetsApi } from '../api/projets'
import { useRessource } from './useRessource'

// Invitations reçues, enrichies (quand c'est autorisé) du nom du projet et de l'émetteur.
// L'API renvoie 403 tant qu'on n'est pas membre du projet : ce n'est pas une erreur,
// on affiche alors simplement les numéros.
async function enrichir(invitation) {
  const [projet, emetteur] = await Promise.allSettled([
    projetsApi.obtenir(invitation.idProjet),
    membresApi.obtenir(invitation.idEmetteur),
  ])
  return {
    ...invitation,
    projet: projet.status === 'fulfilled' ? projet.value : null,
    emetteur: emetteur.status === 'fulfilled' ? emetteur.value : null,
  }
}

export function useInvitations() {
  const { donnees, chargement, erreur, recharger } = useRessource(
    async () => {
      const liste = await invitationsApi.recues()
      return Promise.all(liste.map(enrichir))
    },
    [],
    { initial: [] },
  )

  // En attente d'abord, puis les plus récentes.
  const invitations = useMemo(
    () =>
      [...donnees].sort((a, b) => {
        const attenteA = a.statut === 'EN_ATTENTE' ? 0 : 1
        const attenteB = b.statut === 'EN_ATTENTE' ? 0 : 1
        return attenteA - attenteB || String(b.dateEnvoi).localeCompare(String(a.dateEnvoi))
      }),
    [donnees],
  )
  const nombreEnAttente = useMemo(() => donnees.filter((i) => i.statut === 'EN_ATTENTE').length, [donnees])

  return { invitations, nombreEnAttente, chargement, erreur, recharger }
}
