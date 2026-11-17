import { Archive, ArchiveRestore, CalendarDays, Clock, Flag, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { projetsApi } from '../../api/projets'
import { useAction } from '../../hooks/useAction'
import { ETATS_TACHE, ORDRE_ETATS_TACHE } from '../../utils/constantes'
import { formaterDateLongue, joursAvant } from '../../utils/dates'
import { pourcentage } from '../../utils/formatage'
import Badge from '../ui/Badge'
import BarreProgression from '../ui/BarreProgression'
import Bouton from '../ui/Bouton'
import ModaleConfirmation from '../ui/ModaleConfirmation'

function VueEnsemble({ projet, sprintEnCours, taches, estChef, onAllerSprints }) {
  const navigate = useNavigate()
  const executer = useAction()
  const [suppression, setSuppression] = useState(false)

  const compteurs = ORDRE_ETATS_TACHE.map((etat) => ({ etat, nombre: taches.filter((t) => t.etat === etat).length }))
  const suivies = taches.filter((t) => t.etat !== 'ANNULE')
  const terminees = taches.filter((t) => t.etat === 'TERMINE').length
  const joursRestants = sprintEnCours ? joursAvant(sprintEnCours.dateFin) : null
  const tachesSprint = sprintEnCours ? taches.filter((t) => t.idSprint === sprintEnCours.id && t.etat !== 'ANNULE') : []
  const faitesSprint = tachesSprint.filter((t) => t.etat === 'TERMINE').length

  return (
    <section className="onglet-contenu" aria-label="Vue d'ensemble">
      <div className="grille-ensemble">
        <article className="carte-info">
          <h2>À propos du projet</h2>
          <p className="carte-info__texte">{projet.description || 'Aucune description.'}</p>
          <dl className="liste-definition">
            <div>
              <dt>
                <CalendarDays size={15} aria-hidden="true" /> Début
              </dt>
              <dd>{formaterDateLongue(projet.dateDebut)}</dd>
            </div>
            <div>
              <dt>
                <Flag size={15} aria-hidden="true" /> Fin prévue
              </dt>
              <dd>{formaterDateLongue(projet.dateFin)}</dd>
            </div>
            <div>
              <dt>
                <Clock size={15} aria-hidden="true" /> Avancement global
              </dt>
              <dd>
                {terminees}/{suivies.length} tâches ({pourcentage(terminees, suivies.length)}%)
              </dd>
            </div>
          </dl>
          <BarreProgression valeur={pourcentage(terminees, suivies.length)} libelle="Avancement du projet" />
        </article>

        <article className="carte-info">
          <h2>Sprint en cours</h2>
          {sprintEnCours ? (
            <>
              <p className="carte-info__titre-sprint">
                {sprintEnCours.nom} <Badge variante="primaire" point>En cours</Badge>
              </p>
              {sprintEnCours.objectif && <p className="carte-info__texte">{sprintEnCours.objectif}</p>}
              <p className="carte-info__texte">
                {joursRestants !== null && joursRestants >= 0
                  ? `Fin dans ${joursRestants} jour${joursRestants > 1 ? 's' : ''} (${formaterDateLongue(sprintEnCours.dateFin)})`
                  : `Échéance dépassée depuis ${Math.abs(joursRestants ?? 0)} jour(s)`}
              </p>
              <BarreProgression valeur={pourcentage(faitesSprint, tachesSprint.length)} libelle="Avancement du sprint" />
              <p className="carte-info__detail">
                {faitesSprint}/{tachesSprint.length} tâches terminées
              </p>
            </>
          ) : (
            <>
              <p className="carte-info__texte">Aucun sprint n'est en cours actuellement.</p>
              <Bouton variante="secondaire" taille="s" onClick={onAllerSprints}>
                Voir les sprints
              </Bouton>
            </>
          )}
        </article>
      </div>

      <h2 className="titre-section">Tâches par état</h2>
      <div className="compteurs-taches">
        {compteurs.map(({ etat, nombre }) => (
          <div key={etat} className="compteur-tache">
            <span className="point-etat" style={{ background: ETATS_TACHE[etat].couleur }} aria-hidden="true" />
            <strong>{nombre}</strong>
            <span>{ETATS_TACHE[etat].libelle}</span>
          </div>
        ))}
      </div>

      {estChef && (
        <article className="carte-info carte-info--sensible">
          <h2>Gestion du projet</h2>
          <p className="carte-info__texte">
            {projet.archive
              ? 'Ce projet est archivé (lecture seule). Désarchivez-le pour reprendre le travail.'
              : "Archiver un projet le passe en lecture seule sans rien supprimer. La suppression, elle, est définitive."}
          </p>
          <div className="carte-info__actions">
            {projet.archive ? (
              <Bouton variante="secondaire" icone={ArchiveRestore} onClick={() => executer(() => projetsApi.desarchiver(projet.id), 'Projet désarchivé')}>
                Désarchiver
              </Bouton>
            ) : (
              <Bouton variante="secondaire" icone={Archive} onClick={() => executer(() => projetsApi.archiver(projet.id), 'Projet archivé')}>
                Archiver
              </Bouton>
            )}
            <Bouton variante="danger" icone={Trash2} onClick={() => setSuppression(true)}>
              Supprimer le projet
            </Bouton>
          </div>
        </article>
      )}

      <ModaleConfirmation
        ouverte={suppression}
        titre="Supprimer ce projet ?"
        message={`« ${projet.libelle} » sera supprimé définitivement, avec ses sprints, ses tâches et ses invitations.`}
        libelleConfirmer="Supprimer définitivement"
        danger
        onConfirmer={async () => {
          await projetsApi.supprimer(projet.id)
          executer(async () => {}, 'Projet supprimé')
          navigate('/projets', { replace: true })
        }}
        onFermer={() => setSuppression(false)}
      />
    </section>
  )
}

export default VueEnsemble
