import { ArrowRight, CalendarDays, Target } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useMembresDuProjet } from '../../hooks/useMembresDuProjet'
import { useTaches } from '../../hooks/useTaches'
import { formaterDateCourte } from '../../utils/dates'
import { codeProjet, pourcentage } from '../../utils/formatage'
import Avatar from '../ui/Avatar'
import Badge from '../ui/Badge'
import BarreProgression from '../ui/BarreProgression'

const MAX_AVATARS = 4

function ProjetCard({ projet }) {
  // Pas d'actualisation périodique par carte : elles se mettent à jour avec le temps réel.
  const { taches } = useTaches(projet.id, { intervalle: 0 })
  const { membres } = useMembresDuProjet(projet.id, { intervalle: 0 })

  const actives = taches.filter((t) => t.etat !== 'ANNULE')
  const terminees = actives.filter((t) => t.etat === 'TERMINE')
  const pointsTotal = actives.reduce((somme, t) => somme + (t.estimation || 0), 0)
  const pointsFaits = terminees.reduce((somme, t) => somme + (t.estimation || 0), 0)
  const avancement = pourcentage(terminees.length, actives.length)

  return (
    <article className={`carte-projet${projet.archive ? ' carte-projet--archive' : ''}`}>
      <header className="carte-projet__entete">
        <span className="carte-projet__code">{codeProjet(projet.id)}</span>
        {projet.archive ? (
          <Badge variante="gris">Archivé</Badge>
        ) : (
          <Badge variante="succes" point>
            Actif
          </Badge>
        )}
      </header>

      <h3 className="carte-projet__titre">
        <Link to={`/projets/${projet.id}`} className="carte-projet__lien">
          {projet.libelle}
        </Link>
      </h3>
      <p className="carte-projet__description">{projet.description || 'Aucune description.'}</p>

      <p className="carte-projet__dates">
        <CalendarDays size={15} aria-hidden="true" />
        {formaterDateCourte(projet.dateDebut)} → {formaterDateCourte(projet.dateFin)}
      </p>

      <div className="carte-projet__avancement">
        <div className="carte-projet__ligne">
          <span>
            <Target size={14} aria-hidden="true" /> {terminees.length}/{actives.length} tâches
          </span>
          <strong>{avancement}%</strong>
        </div>
        <BarreProgression valeur={avancement} variante={projet.archive ? 'gris' : 'primaire'} libelle="Avancement du projet" />
        <p className="carte-projet__points">
          {pointsFaits} / {pointsTotal} story points
        </p>
      </div>

      <footer className="carte-projet__pied">
        <div className="pile-avatars" aria-label={`${membres.length} membre(s)`}>
          {membres.slice(0, MAX_AVATARS).map((m) => (
            <Avatar key={m.idMembre} membre={m} taille="s" />
          ))}
          {membres.length > MAX_AVATARS && <span className="pile-avatars__reste">+{membres.length - MAX_AVATARS}</span>}
        </div>
        <span className="carte-projet__ouvrir">
          {projet.archive ? 'Consulter' : 'Ouvrir le projet'} <ArrowRight size={14} aria-hidden="true" />
        </span>
      </footer>
    </article>
  )
}

export default ProjetCard
