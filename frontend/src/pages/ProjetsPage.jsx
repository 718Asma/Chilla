import { AlarmClock, CheckCircle2, FolderKanban, FolderPlus, Plus, Rocket, Search, Timer } from 'lucide-react'
import { useMemo, useState } from 'react'
import ProjetCard from '../components/projets/ProjetCard'
import ProjetFormulaire from '../components/projets/ProjetFormulaire'
import Badge from '../components/ui/Badge'
import Bouton from '../components/ui/Bouton'
import Chargement from '../components/ui/Chargement'
import EtatVide from '../components/ui/EtatVide'
import MessageErreur from '../components/ui/MessageErreur'
import { useMesTaches } from '../hooks/useMesTaches'
import { useProjets } from '../hooks/useProjets'
import { joursAvant } from '../utils/dates'
import { pourcentage } from '../utils/formatage'

const FILTRES = [
  { id: 'tous', libelle: 'Tous' },
  { id: 'actifs', libelle: 'Actifs' },
  { id: 'archives', libelle: 'Archivés' },
]

const TRIS = {
  recents: { libelle: 'Plus récents', comparer: (a, b) => b.id - a.id },
  nom: { libelle: 'Nom (A → Z)', comparer: (a, b) => a.libelle.localeCompare(b.libelle, 'fr') },
  echeance: { libelle: 'Date de fin', comparer: (a, b) => String(a.dateFin).localeCompare(String(b.dateFin)) },
}

function Statistique({ icone: Icone, libelle, valeur, detail, ton = 'primaire' }) {
  return (
    <div className="statistique">
      <span className={`statistique__icone statistique__icone--${ton}`}>
        <Icone size={20} aria-hidden="true" />
      </span>
      <div>
        <p className="statistique__libelle">{libelle}</p>
        <p className="statistique__valeur">{valeur}</p>
        {detail && <p className="statistique__detail">{detail}</p>}
      </div>
    </div>
  )
}

function ProjetsPage() {
  const { projets, chargement, erreur, recharger } = useProjets()
  const { taches: mesTaches } = useMesTaches()

  const [filtre, setFiltre] = useState('tous')
  const [recherche, setRecherche] = useState('')
  const [tri, setTri] = useState('recents')
  const [creation, setCreation] = useState(false)

  const actifs = projets.filter((p) => !p.archive)
  const archives = projets.filter((p) => p.archive)
  const compteurs = { tous: projets.length, actifs: actifs.length, archives: archives.length }

  const visibles = useMemo(() => {
    const terme = recherche.trim().toLowerCase()
    return projets
      .filter((p) => (filtre === 'actifs' ? !p.archive : filtre === 'archives' ? p.archive : true))
      .filter((p) => !terme || `${p.libelle} ${p.description ?? ''}`.toLowerCase().includes(terme))
      .sort(TRIS[tri].comparer)
  }, [projets, filtre, recherche, tri])

  // Statistiques personnelles calculées à partir des tâches de l'utilisateur.
  const enCours = mesTaches.filter((t) => t.etat === 'EN_COURS').length
  const suivies = mesTaches.filter((t) => t.etat !== 'ANNULE')
  const terminees = suivies.filter((t) => t.etat === 'TERMINE').length
  const enRetard = mesTaches.filter((t) => (t.etat === 'NON_COMMENCE' || t.etat === 'EN_COURS') && joursAvant(t.dateEcheance) < 0).length

  if (chargement) return <Chargement message="Chargement de vos projets…" />
  if (erreur) return <MessageErreur message={erreur} onReessayer={recharger} />

  return (
    <div className="page">
      <section className="bandeau">
        <div className="bandeau__texte">
          <p className="fil-ariane">Espace de travail</p>
          <h1>
            Mes projets agiles{' '}
            <Badge variante="primaire" point>
              {actifs.length} actif{actifs.length > 1 ? 's' : ''} / {projets.length} au total
            </Badge>
          </h1>
          <p className="bandeau__sous-titre">Supervisez le cycle de vie, la vélocité et l'avancement de chaque projet de votre équipe.</p>
        </div>
        <Bouton icone={Plus} taille="l" onClick={() => setCreation(true)}>
          Nouveau projet
        </Bouton>
      </section>

      <section className="statistiques" aria-label="Mes indicateurs">
        <Statistique icone={FolderKanban} libelle="Projets actifs" valeur={actifs.length} detail={`${archives.length} archivé${archives.length > 1 ? 's' : ''}`} />
        <Statistique icone={Timer} libelle="Mes tâches en cours" valeur={enCours} detail={`${mesTaches.length} assignée${mesTaches.length > 1 ? 's' : ''} au total`} ton="alerte" />
        <Statistique icone={CheckCircle2} libelle="Taux de livraison" valeur={`${pourcentage(terminees, suivies.length)}%`} detail={`${terminees} tâche${terminees > 1 ? 's' : ''} terminée${terminees > 1 ? 's' : ''}`} ton="succes" />
        <Statistique icone={AlarmClock} libelle="Tâches en retard" valeur={enRetard} detail={enRetard ? 'À traiter en priorité' : 'Tout est dans les temps'} ton={enRetard ? 'danger' : 'succes'} />
      </section>

      <section className="barre-outils" aria-label="Filtres">
        <div className="pilules" role="tablist" aria-label="Filtrer les projets">
          {FILTRES.map((f) => (
            <button
              key={f.id}
              type="button"
              role="tab"
              aria-selected={filtre === f.id}
              className={`pilule${filtre === f.id ? ' pilule--active' : ''}`}
              onClick={() => setFiltre(f.id)}
            >
              {f.libelle} <span className="pilule__compteur">{compteurs[f.id]}</span>
            </button>
          ))}
        </div>
        <div className="barre-outils__droite">
          <label className="recherche">
            <Search size={16} aria-hidden="true" />
            <span className="sr-only">Rechercher un projet</span>
            <input type="search" placeholder="Rechercher un projet…" value={recherche} onChange={(e) => setRecherche(e.target.value)} />
          </label>
          <label className="tri">
            <span className="sr-only">Trier par</span>
            <select value={tri} onChange={(e) => setTri(e.target.value)}>
              {Object.entries(TRIS).map(([cle, { libelle }]) => (
                <option key={cle} value={cle}>
                  Tri : {libelle}
                </option>
              ))}
            </select>
          </label>
        </div>
      </section>

      {projets.length === 0 ? (
        <EtatVide
          icone={Rocket}
          titre="Bienvenue sur Chilla !"
          texte="Vous ne participez à aucun projet pour le moment. Créez le vôtre ou acceptez une invitation reçue."
          action={
            <Bouton icone={FolderPlus} onClick={() => setCreation(true)}>
              Créer mon premier projet
            </Bouton>
          }
        />
      ) : visibles.length === 0 ? (
        <EtatVide icone={Search} titre="Aucun projet ne correspond" texte="Modifiez votre recherche ou changez de filtre pour retrouver vos projets." />
      ) : (
        <div className="grille-projets">
          {visibles.map((projet) => (
            <ProjetCard key={projet.id} projet={projet} />
          ))}
          <button type="button" className="carte-nouveau" onClick={() => setCreation(true)}>
            <span className="carte-nouveau__icone">
              <Plus size={22} aria-hidden="true" />
            </span>
            <strong>Créer un nouveau projet</strong>
            <small>Définissez un cadre, invitez votre équipe et planifiez vos sprints.</small>
          </button>
        </div>
      )}

      <ProjetFormulaire ouverte={creation} onFermer={() => setCreation(false)} />
    </div>
  )
}

export default ProjetsPage
