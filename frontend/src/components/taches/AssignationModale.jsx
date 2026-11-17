import { useState } from 'react'
import { tachesApi } from '../../api/taches'
import { nomComplet } from '../../utils/formatage'
import ChampFormulaire from '../ui/ChampFormulaire'
import ModaleConfirmation from '../ui/ModaleConfirmation'

function Interne({ tache, membres, onFermer, onAssignee }) {
  const [idMembre, setIdMembre] = useState(String(tache.idMembre ?? membres[0]?.idMembre ?? ''))

  return (
    <ModaleConfirmation
      ouverte
      titre="Réassigner la tâche"
      libelleConfirmer="Réassigner"
      onFermer={onFermer}
      onConfirmer={async () => {
        await tachesApi.assigner(tache.id, Number(idMembre))
        onAssignee()
      }}
    >
      <p className="modale__message">« {tache.libelle} »</p>
      <ChampFormulaire label="Nouveau responsable" as="select" value={idMembre} onChange={(e) => setIdMembre(e.target.value)}>
        {membres.map((m) => (
          <option key={m.idMembre} value={m.idMembre}>
            {nomComplet(m)}
          </option>
        ))}
      </ChampFormulaire>
    </ModaleConfirmation>
  )
}

// Réassignation d'une tâche (réservée au chef de projet).
function AssignationModale({ tache, ...reste }) {
  return tache ? <Interne key={tache.id} tache={tache} {...reste} /> : null
}

export default AssignationModale
