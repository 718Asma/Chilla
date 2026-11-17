import { CalendarDays, Flag, Pencil, Play, Plus, Square, Trash2, XCircle, Zap } from 'lucide-react'
import { useState } from 'react'
import { sprintsApi } from '../../api/sprints'
import { useAction } from '../../hooks/useAction'
import { ETATS_SPRINT, TRANSITIONS_SPRINT } from '../../utils/constantes'
import { formaterDateCourte } from '../../utils/dates'
import { pluriel, pourcentage } from '../../utils/formatage'
import Badge from '../ui/Badge'
import BarreProgression from '../ui/BarreProgression'
import Bouton from '../ui/Bouton'
import EtatVide from '../ui/EtatVide'
import ModaleConfirmation from '../ui/ModaleConfirmation'
import SprintFormulaire from './SprintFormulaire'

function SprintsOnglet({ projet, sprints, taches, estChef, lectureSeule }) {
  const executer = useAction()
  const [formulaire, setFormulaire] = useState(null) // { sprint } ou null
  const [confirmation, setConfirmation] = useState(null) // { type, sprint }
  const peutGerer = estChef && !lectureSeule

  const lancer = (sprint) => executer(() => sprintsApi.demarrer(sprint.id), `« ${sprint.nom} » est démarré`)

  const confirmer = async () => {
    const { type, sprint } = confirmation
    if (type === 'terminer') await sprintsApi.terminer(sprint.id)
    if (type === 'annuler') await sprintsApi.annuler(sprint.id)
    if (type === 'supprimer') await sprintsApi.supprimer(sprint.id)
    executer(async () => {}, type === 'terminer' ? 'Sprint terminé' : type === 'annuler' ? 'Sprint annulé' : 'Sprint supprimé')
  }

  const textes = {
    terminer: { titre: 'Terminer le sprint ?', message: 'Un sprint terminé est définitif : il ne pourra plus être modifié.', bouton: 'Terminer le sprint', danger: false },
    annuler: { titre: 'Annuler le sprint ?', message: 'Un sprint annulé est définitif : il ne pourra plus être relancé.', bouton: 'Annuler le sprint', danger: true },
    supprimer: { titre: 'Supprimer le sprint ?', message: 'Le sprint et les tâches qu’il contient seront supprimés définitivement.', bouton: 'Supprimer', danger: true },
  }

  return (
    <section className="onglet-contenu" aria-labelledby="titre-sprints">
      <div className="section-entete">
        <div>
          <h2 id="titre-sprints">Sprints</h2>
          <p>Planifiez les itérations de l'équipe. Un seul sprint peut être en cours à la fois.</p>
        </div>
        {peutGerer && (
          <Bouton icone={Plus} onClick={() => setFormulaire({ sprint: null })}>
            Nouveau sprint
          </Bouton>
        )}
      </div>

      {sprints.length === 0 ? (
        <EtatVide
          icone={Zap}
          titre="Aucun sprint pour l'instant"
          texte={peutGerer ? 'Créez votre premier sprint pour organiser les tâches de l’équipe.' : 'Le chef de projet n’a pas encore planifié de sprint.'}
          action={
            peutGerer && (
              <Bouton icone={Plus} onClick={() => setFormulaire({ sprint: null })}>
                Créer un sprint
              </Bouton>
            )
          }
        />
      ) : (
        <ul className="liste-cartes">
          {sprints.map((sprint) => {
            const etat = ETATS_SPRINT[sprint.etat] ?? ETATS_SPRINT.NON_COMMENCE
            const duSprint = taches.filter((t) => t.idSprint === sprint.id && t.etat !== 'ANNULE')
            const faites = duSprint.filter((t) => t.etat === 'TERMINE').length
            const transitions = TRANSITIONS_SPRINT[sprint.etat] ?? []
            const modifiable = transitions.length > 0

            return (
              <li key={sprint.id} className={`carte-sprint${sprint.etat === 'EN_COURS' ? ' carte-sprint--active' : ''}`}>
                <div className="carte-sprint__entete">
                  <h3>{sprint.nom}</h3>
                  <Badge variante={etat.variante} point={sprint.etat === 'EN_COURS'}>
                    {etat.libelle}
                  </Badge>
                </div>
                {sprint.objectif && (
                  <p className="carte-sprint__objectif">
                    <Flag size={14} aria-hidden="true" /> {sprint.objectif}
                  </p>
                )}
                <p className="carte-sprint__dates">
                  <CalendarDays size={14} aria-hidden="true" /> {formaterDateCourte(sprint.dateDebut)} → {formaterDateCourte(sprint.dateFin)}
                </p>
                <div className="carte-sprint__avancement">
                  <span>
                    {faites}/{duSprint.length} {pluriel(duSprint.length, 'tâche')}
                  </span>
                  <BarreProgression valeur={pourcentage(faites, duSprint.length)} variante={sprint.etat === 'TERMINE' ? 'succes' : 'primaire'} libelle={`Avancement de ${sprint.nom}`} />
                </div>

                {peutGerer && modifiable && (
                  <div className="carte-sprint__actions">
                    {transitions.includes('EN_COURS') && (
                      <Bouton taille="s" icone={Play} onClick={() => lancer(sprint)}>
                        Démarrer
                      </Bouton>
                    )}
                    {transitions.includes('TERMINE') && (
                      <Bouton taille="s" icone={Square} onClick={() => setConfirmation({ type: 'terminer', sprint })}>
                        Terminer
                      </Bouton>
                    )}
                    {transitions.includes('ANNULE') && (
                      <Bouton taille="s" variante="secondaire" icone={XCircle} onClick={() => setConfirmation({ type: 'annuler', sprint })}>
                        Annuler
                      </Bouton>
                    )}
                    <Bouton taille="s" variante="fantome" icone={Pencil} onClick={() => setFormulaire({ sprint })} aria-label={`Modifier ${sprint.nom}`}>
                      Modifier
                    </Bouton>
                    <Bouton taille="s" variante="fantome" icone={Trash2} onClick={() => setConfirmation({ type: 'supprimer', sprint })} aria-label={`Supprimer ${sprint.nom}`} />
                  </div>
                )}
                {peutGerer && !modifiable && (
                  <div className="carte-sprint__actions">
                    <Bouton taille="s" variante="fantome" icone={Trash2} onClick={() => setConfirmation({ type: 'supprimer', sprint })}>
                      Supprimer
                    </Bouton>
                  </div>
                )}
              </li>
            )
          })}
        </ul>
      )}

      {formulaire && <SprintFormulaire ouverte projet={projet} sprint={formulaire.sprint} onFermer={() => setFormulaire(null)} />}

      <ModaleConfirmation
        ouverte={Boolean(confirmation)}
        titre={confirmation ? textes[confirmation.type].titre : ''}
        message={confirmation ? textes[confirmation.type].message : ''}
        libelleConfirmer={confirmation ? textes[confirmation.type].bouton : ''}
        danger={confirmation ? textes[confirmation.type].danger : false}
        onConfirmer={confirmer}
        onFermer={() => setConfirmation(null)}
      />
    </section>
  )
}

export default SprintsOnglet
