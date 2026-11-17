import { Flag, TrendingUp } from 'lucide-react'
import { formaterDateCourte, joursAvant, pourcentageTempsEcoule } from '../../utils/dates'
import { nomComplet, pourcentage } from '../../utils/formatage'
import Avatar from '../ui/Avatar'

// Trois cartes sous le Kanban, calculées à partir des vraies données du sprint.
function ResumeSprint({ sprint, taches, membres }) {
  const actives = taches.filter((t) => t.etat !== 'ANNULE')
  const points = actives.reduce((s, t) => s + (t.estimation || 0), 0)
  const pointsFaits = actives.filter((t) => t.etat === 'TERMINE').reduce((s, t) => s + (t.estimation || 0), 0)
  const avancement = pourcentage(pointsFaits, points)
  const tempsEcoule = pourcentageTempsEcoule(sprint.dateDebut, sprint.dateFin)
  const joursRestants = joursAvant(sprint.dateFin)

  const charge = membres
    .map((m) => {
      const siennes = actives.filter((t) => t.idMembre === m.idMembre)
      return { membre: m, taches: siennes.length, points: siennes.reduce((s, t) => s + (t.estimation || 0), 0) }
    })
    .filter((c) => c.taches > 0)
    .sort((a, b) => b.points - a.points)
  const maxPoints = Math.max(1, ...charge.map((c) => c.points))

  const retard = avancement < tempsEcoule - 10

  return (
    <div className="resume-sprint">
      <section className="carte-resume">
        <p className="carte-resume__surtitre">Tendance du sprint</p>
        <h3>
          Avancement <TrendingUp size={16} aria-hidden="true" />
        </h3>
        <div className="jauge-double" aria-label={`Points terminés ${avancement}%, temps écoulé ${tempsEcoule}%`}>
          <div className="jauge-double__ligne">
            <span>Points livrés</span>
            <strong>{avancement}%</strong>
          </div>
          <div className="progression">
            <div className="progression__barre progression__barre--primaire" style={{ width: `${avancement}%` }} />
          </div>
          <div className="jauge-double__ligne">
            <span>Temps écoulé</span>
            <strong>{tempsEcoule}%</strong>
          </div>
          <div className="progression">
            <div className="progression__barre progression__barre--gris" style={{ width: `${tempsEcoule}%` }} />
          </div>
        </div>
        <p className={`carte-resume__pied${retard ? ' carte-resume__pied--alerte' : ''}`}>
          {pointsFaits} / {points} pts livrés — {retard ? 'en retard sur le calendrier' : 'dans les temps'}
        </p>
      </section>

      <section className="carte-resume">
        <p className="carte-resume__surtitre">Répartition de l'équipe</p>
        <h3>Charge d'équipe</h3>
        {charge.length === 0 ? (
          <p className="carte-resume__vide">Aucune tâche assignée dans ce sprint.</p>
        ) : (
          <ul className="charge">
            {charge.slice(0, 5).map(({ membre, taches: nombre, points: pts }) => (
              <li key={membre.idMembre}>
                <Avatar membre={membre} taille="s" />
                <span className="charge__nom">{nomComplet(membre)}</span>
                <span className="charge__valeur">
                  {pts} pt{pts > 1 ? 's' : ''} ({nombre} tâche{nombre > 1 ? 's' : ''})
                </span>
                <span className="charge__barre">
                  <span style={{ width: `${(pts / maxPoints) * 100}%` }} />
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="carte-resume carte-resume--objectif">
        <p className="carte-resume__surtitre">Objectif — {sprint.nom}</p>
        <h3>
          {sprint.objectif || 'Aucun objectif défini pour ce sprint.'} <Flag size={16} aria-hidden="true" />
        </h3>
        <p className="carte-resume__pied">
          {sprint.etat === 'EN_COURS' && joursRestants !== null
            ? joursRestants >= 0
              ? `Fin du sprint dans ${joursRestants} jour${joursRestants > 1 ? 's' : ''} (${formaterDateCourte(sprint.dateFin)})`
              : `Sprint dépassé de ${Math.abs(joursRestants)} jour${Math.abs(joursRestants) > 1 ? 's' : ''}`
            : `Du ${formaterDateCourte(sprint.dateDebut)} au ${formaterDateCourte(sprint.dateFin)}`}
        </p>
      </section>
    </div>
  )
}

export default ResumeSprint
