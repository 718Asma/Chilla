import { ArrowLeft, CalendarDays, Gauge, Info, KanbanSquare, LayoutDashboard, LogOut, Pencil, ShieldCheck, Users, Zap } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { projetsApi } from '../api/projets'
import EquipeOnglet from '../components/equipe/EquipeOnglet'
import ProjetFormulaire from '../components/projets/ProjetFormulaire'
import VueEnsemble from '../components/projets/VueEnsemble'
import SprintsOnglet from '../components/sprints/SprintsOnglet'
import KanbanOnglet from '../components/taches/KanbanOnglet'
import Badge from '../components/ui/Badge'
import Bouton from '../components/ui/Bouton'
import Chargement from '../components/ui/Chargement'
import MessageErreur from '../components/ui/MessageErreur'
import ModaleConfirmation from '../components/ui/ModaleConfirmation'
import Onglets from '../components/ui/Onglets'
import { useAction } from '../hooks/useAction'
import { useMembresDuProjet } from '../hooks/useMembresDuProjet'
import { useProjet } from '../hooks/useProjet'
import { useSprints } from '../hooks/useSprints'
import { useTaches } from '../hooks/useTaches'
import { formaterDateCourte } from '../utils/dates'
import { codeProjet, pourcentage } from '../utils/formatage'

const ONGLETS_VALIDES = ['ensemble', 'sprints', 'taches', 'equipe']

function ProjetDetailPage() {
  const { id } = useParams()
  const idProjet = Number(id)
  const navigate = useNavigate()
  const executer = useAction()
  const [parametres, setParametres] = useSearchParams()

  const { projet, chargement, erreur, recharger } = useProjet(idProjet)
  const { membres, estChef, roleCourant, chargement: chargementMembres } = useMembresDuProjet(idProjet)
  const { sprints, sprintEnCours } = useSprints(idProjet)
  const { taches, modifierLocalement, recharger: rechargerTaches } = useTaches(idProjet)

  const [edition, setEdition] = useState(false)
  const [quitter, setQuitter] = useState(false)

  // L'onglet actif est dans l'URL : il survit au rafraîchissement et au bouton « retour ».
  const demande = parametres.get('onglet')
  const onglet = ONGLETS_VALIDES.includes(demande) ? demande : 'ensemble'
  const changerOnglet = (nouveau) => setParametres({ onglet: nouveau }, { replace: true })

  if (chargement || (chargementMembres && !projet)) return <Chargement message="Chargement du projet…" />
  if (erreur || !projet) {
    return (
      <div className="page">
        <MessageErreur message={erreur ?? 'Projet introuvable.'} onReessayer={recharger} />
        <Link to="/projets" className="lien-retour">
          <ArrowLeft size={16} aria-hidden="true" /> Retour aux projets
        </Link>
      </div>
    )
  }

  const lectureSeule = projet.archive
  const suivies = taches.filter((t) => t.etat !== 'ANNULE')
  const terminees = suivies.filter((t) => t.etat === 'TERMINE').length

  const onglets = [
    { id: 'ensemble', libelle: "Vue d'ensemble", icone: LayoutDashboard },
    { id: 'sprints', libelle: 'Sprints', icone: Zap, compteur: sprints.length },
    { id: 'taches', libelle: 'Tâches & Kanban', icone: KanbanSquare, compteur: taches.length },
    { id: 'equipe', libelle: 'Équipe', icone: Users, compteur: membres.length },
  ]

  return (
    <div className="page">
      <header className="projet-entete">
        <nav className="fil-ariane fil-ariane--lien" aria-label="Fil d'Ariane">
          <Link to="/projets">
            <ArrowLeft size={14} aria-hidden="true" /> Tous les projets
          </Link>
          <span aria-hidden="true">/</span>
          <span className="fil-ariane__code">{codeProjet(projet.id)}</span>
        </nav>

        <div className="projet-entete__ligne">
          <div className="projet-entete__texte">
            <h1>
              {projet.libelle}
              <span className="projet-entete__badges">
                {roleCourant === 'CHEF_DE_PROJET' && (
                  <Badge variante="primaire" icone={ShieldCheck}>
                    Chef de projet
                  </Badge>
                )}
                {lectureSeule ? <Badge variante="gris">Archivé</Badge> : <Badge variante="succes" point>Actif</Badge>}
              </span>
            </h1>
            {projet.description && <p className="projet-entete__description">{projet.description}</p>}
            <p className="projet-entete__meta">
              <span>
                <CalendarDays size={14} aria-hidden="true" /> Période : {formaterDateCourte(projet.dateDebut)} → {formaterDateCourte(projet.dateFin)}
              </span>
              <span>
                <Gauge size={14} aria-hidden="true" /> {pourcentage(terminees, suivies.length)}% terminé ({terminees}/{suivies.length} tâches)
              </span>
              {sprintEnCours && (
                <span>
                  <Zap size={14} aria-hidden="true" /> {sprintEnCours.nom}
                </span>
              )}
            </p>
          </div>
          <div className="projet-entete__actions">
            {estChef && !lectureSeule && (
              <Bouton variante="secondaire" icone={Pencil} onClick={() => setEdition(true)}>
                Modifier le projet
              </Bouton>
            )}
            <Bouton variante="fantome" icone={LogOut} onClick={() => setQuitter(true)}>
              Quitter
            </Bouton>
          </div>
        </div>

        {lectureSeule && (
          <p className="bandeau-archive" role="status">
            <Info size={16} aria-hidden="true" /> Projet archivé : lecture seule.
            {estChef && ' Désarchivez-le depuis la vue d\'ensemble pour reprendre le travail.'}
          </p>
        )}

        <Onglets onglets={onglets} actif={onglet} onChange={changerOnglet} libelle="Sections du projet" />
      </header>

      <div role="tabpanel" id={`panneau-${onglet}`} aria-labelledby={`onglet-${onglet}`}>
        {onglet === 'ensemble' && (
          <VueEnsemble projet={projet} sprintEnCours={sprintEnCours} taches={taches} estChef={estChef} onAllerSprints={() => changerOnglet('sprints')} />
        )}
        {onglet === 'sprints' && <SprintsOnglet projet={projet} sprints={sprints} taches={taches} estChef={estChef} lectureSeule={lectureSeule} />}
        {onglet === 'taches' && (
          <KanbanOnglet
            projet={projet}
            sprints={sprints}
            sprintEnCours={sprintEnCours}
            taches={taches}
            modifierLocalement={modifierLocalement}
            recharger={rechargerTaches}
            membres={membres}
            estChef={estChef}
            lectureSeule={lectureSeule}
            onAllerSprints={() => changerOnglet('sprints')}
          />
        )}
        {onglet === 'equipe' && <EquipeOnglet projet={projet} membres={membres} estChef={estChef} lectureSeule={lectureSeule} />}
      </div>

      {edition && <ProjetFormulaire ouverte projet={projet} onFermer={() => setEdition(false)} />}

      <ModaleConfirmation
        ouverte={quitter}
        titre="Quitter ce projet ?"
        message="Vous n'aurez plus accès à ce projet. Le départ est refusé si vous êtes le dernier chef de projet ou s'il vous reste des tâches en cours."
        libelleConfirmer="Quitter le projet"
        danger
        onConfirmer={async () => {
          await projetsApi.quitter(projet.id)
          executer(async () => {}, 'Vous avez quitté le projet')
          navigate('/projets', { replace: true })
        }}
        onFermer={() => setQuitter(false)}
      />
    </div>
  )
}

export default ProjetDetailPage
