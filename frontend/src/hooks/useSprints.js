import { useMemo } from 'react'
import { sprintsApi } from '../api/sprints'
import { useRessource } from './useRessource'

export function useSprints(idProjet) {
  const { donnees, chargement, erreur, recharger } = useRessource(() => sprintsApi.listerDuProjet(idProjet), [idProjet], {
    initial: [],
  })

  const sprints = useMemo(
    () => [...donnees].sort((a, b) => String(a.dateDebut).localeCompare(String(b.dateDebut)) || a.id - b.id),
    [donnees],
  )
  const sprintEnCours = useMemo(() => sprints.find((s) => s.etat === 'EN_COURS') ?? null, [sprints])

  return { sprints, sprintEnCours, chargement, erreur, recharger }
}
