import { Inbox, Plus } from 'lucide-react'
import { useState } from 'react'
import { ETATS_TACHE } from '../../utils/constantes'

// Une colonne du Kanban : zone de dépôt pour le glisser-déposer.
function ColonneKanban({ etat, taches, peutDeposer, onDeposer, onAjouter, children }) {
  const [survol, setSurvol] = useState(false)
  const infos = ETATS_TACHE[etat]

  return (
    <section
      className={`colonne${survol && peutDeposer ? ' colonne--survol' : ''}`}
      aria-label={`${infos.libelle}, ${taches.length} tâche${taches.length > 1 ? 's' : ''}`}
      onDragOver={(e) => {
        if (!peutDeposer) return
        e.preventDefault()
        e.dataTransfer.dropEffect = 'move'
        setSurvol(true)
      }}
      onDragLeave={(e) => {
        if (!e.currentTarget.contains(e.relatedTarget)) setSurvol(false)
      }}
      onDrop={(e) => {
        e.preventDefault()
        setSurvol(false)
        onDeposer(etat)
      }}
    >
      <header className="colonne__entete">
        <h3>
          <span className="point-etat" style={{ background: infos.couleur }} aria-hidden="true" />
          {infos.libelle}
          <span className="colonne__compteur">{taches.length}</span>
        </h3>
      </header>
      <div className="colonne__cartes">
        {children}
        {taches.length === 0 && (
          <p className="colonne__vide">
            <Inbox size={18} aria-hidden="true" />
            {peutDeposer ? 'Glissez une tâche ici' : 'Aucune tâche'}
          </p>
        )}
      </div>
      {onAjouter && (
        <button type="button" className="colonne__ajout" onClick={onAjouter}>
          <Plus size={14} aria-hidden="true" /> Ajouter une tâche
        </button>
      )}
    </section>
  )
}

export default ColonneKanban
