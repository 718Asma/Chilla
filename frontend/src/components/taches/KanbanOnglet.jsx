import { Plus, Zap } from 'lucide-react'
import { useMemo, useState } from 'react'
import { tachesApi } from '../../api/taches'
import { useAction } from '../../hooks/useAction'
import { useAuth } from '../../hooks/useAuth'
import { useNotifications } from '../../hooks/useNotifications'
import { useToast } from '../../hooks/useToast'
import { ETATS_SPRINT, ORDRE_ETATS_TACHE } from '../../utils/constantes'
import Badge from '../ui/Badge'
import Bouton from '../ui/Bouton'
import EtatVide from '../ui/EtatVide'
import ModaleConfirmation from '../ui/ModaleConfirmation'
import AssignationModale from './AssignationModale'
import CarteTache from './CarteTache'
import ColonneKanban from './ColonneKanban'
import ResumeSprint from './ResumeSprint'
import TacheFormulaire from './TacheFormulaire'

function KanbanOnglet({ projet, sprints, sprintEnCours, taches, modifierLocalement, recharger, membres, estChef, lectureSeule, onAllerSprints }) {
  const { membre: moi } = useAuth()
  const { signaler } = useNotifications()
  const { afficherToast } = useToast()
  const executer = useAction()

  const [choix, setChoix] = useState(null)
  const [formulaire, setFormulaire] = useState(null) // { tache }
  const [aReassigner, setAReassigner] = useState(null)
  const [aSupprimer, setASupprimer] = useState(null)
  const [glissee, setGlissee] = useState(null)

  // Sprint affiché : le choix de l'utilisateur, sinon le sprint en cours, sinon le premier non terminé.
  const sprintAffiche = useMemo(() => {
    return (
      sprints.find((s) => s.id === choix) ??
      sprintEnCours ??
      sprints.find((s) => s.etat === 'NON_COMMENCE') ??
      sprints[sprints.length - 1] ??
      null
    )
  }, [sprints, choix, sprintEnCours])

  const membresParId = useMemo(() => new Map(membres.map((m) => [m.idMembre, m])), [membres])

  const tachesDuSprint = useMemo(
    () => (sprintAffiche ? taches.filter((t) => t.idSprint === sprintAffiche.id).sort((a, b) => a.position - b.position || a.id - b.id) : []),
    [taches, sprintAffiche],
  )

  const actives = tachesDuSprint.filter((t) => t.etat !== 'ANNULE')
  const chargeTotale = actives.reduce((s, t) => s + (t.estimation || 0), 0)
  const pointsFaits = actives.filter((t) => t.etat === 'TERMINE').reduce((s, t) => s + (t.estimation || 0), 0)

  const peutChangerEtat = (tache) => !lectureSeule && (estChef || tache.idMembre === moi?.id)

  // Mise à jour optimiste : la carte bouge tout de suite, on annule si le serveur refuse.
  const deplacer = async (tache, etat) => {
    if (!tache || tache.etat === etat) return
    modifierLocalement((liste) => liste.map((t) => (t.id === tache.id ? { ...t, etat } : t)))
    try {
      await tachesApi.changerEtat(tache.id, etat)
      signaler()
    } catch (e) {
      afficherToast({ type: 'erreur', titre: 'Déplacement impossible', message: e.message, duree: 7000 })
      recharger({ silencieux: true })
    }
  }

  const deposer = (etat) => {
    if (glissee) deplacer(glissee, etat)
    setGlissee(null)
  }

  if (sprints.length === 0) {
    return (
      <EtatVide
        icone={Zap}
        titre="Aucun sprint à afficher"
        texte="Les tâches s'organisent par sprint. Créez d'abord un sprint pour démarrer le tableau Kanban."
        action={
          <Bouton variante="secondaire" onClick={onAllerSprints}>
            Aller aux sprints
          </Bouton>
        }
      />
    )
  }

  const etatSprint = ETATS_SPRINT[sprintAffiche.etat]

  return (
    <section className="onglet-contenu" aria-label="Tableau Kanban">
      <div className="barre-sprint">
        <div className="barre-sprint__choix">
          <span className="barre-sprint__surtitre">Sprint affiché</span>
          <div className="barre-sprint__ligne">
            <label className="sr-only" htmlFor="choix-sprint">
              Choisir un sprint
            </label>
            <select id="choix-sprint" value={sprintAffiche.id} onChange={(e) => setChoix(Number(e.target.value))}>
              {sprints.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.nom}
                </option>
              ))}
            </select>
            <Badge variante={etatSprint.variante} point={sprintAffiche.etat === 'EN_COURS'}>
              {etatSprint.libelle}
            </Badge>
          </div>
          <p className="barre-sprint__stats">
            <span>
              Charge totale : <strong>{chargeTotale} pts</strong>
            </span>
            <span className="barre-sprint__succes">
              <strong>{pointsFaits} pts</strong> complétés
            </span>
          </p>
        </div>
        {!lectureSeule && (
          <Bouton icone={Plus} taille="l" onClick={() => setFormulaire({ tache: null })}>
            Nouvelle tâche
          </Bouton>
        )}
      </div>

      <div className="kanban">
        {ORDRE_ETATS_TACHE.map((etat) => {
          const colonne = tachesDuSprint.filter((t) => t.etat === etat)
          return (
            <ColonneKanban
              key={etat}
              etat={etat}
              taches={colonne}
              peutDeposer={Boolean(glissee) && glissee.etat !== etat}
              onDeposer={deposer}
              onAjouter={etat === 'NON_COMMENCE' && !lectureSeule ? () => setFormulaire({ tache: null }) : undefined}
            >
              {colonne.map((tache) => (
                <CarteTache
                  key={tache.id}
                  tache={tache}
                  responsable={membresParId.get(tache.idMembre)}
                  peutChangerEtat={peutChangerEtat(tache)}
                  peutModifier={!lectureSeule}
                  estChef={estChef && !lectureSeule}
                  glissee={glissee?.id === tache.id}
                  onChangerEtat={deplacer}
                  onModifier={(t) => setFormulaire({ tache: t })}
                  onAssigner={setAReassigner}
                  onSupprimer={setASupprimer}
                  onDebutGlisse={setGlissee}
                  onFinGlisse={() => setGlissee(null)}
                />
              ))}
            </ColonneKanban>
          )
        })}
      </div>

      <ResumeSprint sprint={sprintAffiche} taches={tachesDuSprint} membres={membres} />

      {formulaire && (
        <TacheFormulaire
          ouverte
          projet={projet}
          tache={formulaire.tache}
          sprints={sprints}
          membres={membres}
          idSprintInitial={sprintAffiche.id}
          onFermer={() => setFormulaire(null)}
        />
      )}

      <AssignationModale
        tache={aReassigner}
        membres={membres}
        onFermer={() => setAReassigner(null)}
        onAssignee={() => executer(async () => {}, 'Tâche réassignée')}
      />

      <ModaleConfirmation
        ouverte={Boolean(aSupprimer)}
        titre="Supprimer la tâche ?"
        message={aSupprimer ? `« ${aSupprimer.libelle} » sera supprimée définitivement.` : ''}
        libelleConfirmer="Supprimer"
        danger
        onConfirmer={async () => {
          await tachesApi.supprimer(aSupprimer.id)
          executer(async () => {}, 'Tâche supprimée')
        }}
        onFermer={() => setASupprimer(null)}
      />
    </section>
  )
}

export default KanbanOnglet
