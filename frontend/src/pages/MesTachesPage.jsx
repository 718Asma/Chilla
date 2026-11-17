import { CalendarDays, ClipboardCheck } from 'lucide-react'
import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import Badge from '../components/ui/Badge'
import Chargement from '../components/ui/Chargement'
import EtatVide from '../components/ui/EtatVide'
import MessageErreur from '../components/ui/MessageErreur'
import { useMesTaches } from '../hooks/useMesTaches'
import { useProjets } from '../hooks/useProjets'
import { ETATS_TACHE } from '../utils/constantes'
import { estEnRetard, formaterDateCourte } from '../utils/dates'
import { codeTache } from '../utils/formatage'

const FILTRES = [{ id: 'actives', libelle: 'À traiter' }, { id: 'terminees', libelle: 'Terminées' }, { id: 'toutes', libelle: 'Toutes' }]

function MesTachesPage() {
  const { taches, chargement, erreur, recharger } = useMesTaches()
  const { projets } = useProjets({ intervalle: 0 })
  const [filtre, setFiltre] = useState('actives')

  const nomsProjets = useMemo(() => new Map(projets.map((p) => [p.id, p.libelle])), [projets])

  const visibles = useMemo(() => {
    const filtrees = taches.filter((t) =>
      filtre === 'actives' ? t.etat === 'NON_COMMENCE' || t.etat === 'EN_COURS' : filtre === 'terminees' ? t.etat === 'TERMINE' : true,
    )
    return filtrees.sort((a, b) => String(a.dateEcheance).localeCompare(String(b.dateEcheance)))
  }, [taches, filtre])

  if (chargement) return <Chargement message="Chargement de vos tâches…" />
  if (erreur) return <MessageErreur message={erreur} onReessayer={recharger} />

  return (
    <div className="page page--etroite">
      <section className="bandeau">
        <div className="bandeau__texte">
          <p className="fil-ariane">Mon espace</p>
          <h1>Mes tâches</h1>
          <p className="bandeau__sous-titre">Toutes les tâches qui vous sont assignées, triées par échéance.</p>
        </div>
      </section>

      <div className="pilules pilules--marge" role="tablist" aria-label="Filtrer mes tâches">
        {FILTRES.map((f) => (
          <button key={f.id} type="button" role="tab" aria-selected={filtre === f.id} className={`pilule${filtre === f.id ? ' pilule--active' : ''}`} onClick={() => setFiltre(f.id)}>
            {f.libelle}
          </button>
        ))}
      </div>

      {visibles.length === 0 ? (
        <EtatVide icone={ClipboardCheck} titre="Rien à afficher" texte="Aucune tâche ne correspond à ce filtre pour le moment." />
      ) : (
        <ul className="liste-taches">
          {visibles.map((t) => {
            const etat = ETATS_TACHE[t.etat]
            const retard = estEnRetard(t)
            return (
              <li key={t.id}>
                <Link to={`/projets/${t.idProjet}?onglet=taches`} className={`ligne-tache${retard ? ' ligne-tache--retard' : ''}`}>
                  <span className="ligne-tache__code">{codeTache(t.id)}</span>
                  <span className="ligne-tache__titre">
                    <strong>{t.libelle}</strong>
                    <small>{nomsProjets.get(t.idProjet) ?? `Projet n°${t.idProjet}`}</small>
                  </span>
                  <span className="ligne-tache__echeance">
                    <CalendarDays size={14} aria-hidden="true" /> {formaterDateCourte(t.dateEcheance)}
                    {retard && <Badge variante="danger">En retard</Badge>}
                  </span>
                  <Badge variante={etat.variante}>{etat.libelle}</Badge>
                </Link>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}

export default MesTachesPage
