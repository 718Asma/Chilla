import { ArrowRight, Bell, Check, Clock, Mail, MailOpen, Radio, X } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { invitationsApi } from '../api/invitations'
import Badge from '../components/ui/Badge'
import Bouton from '../components/ui/Bouton'
import Chargement from '../components/ui/Chargement'
import EtatVide from '../components/ui/EtatVide'
import MessageErreur from '../components/ui/MessageErreur'
import { useAction } from '../hooks/useAction'
import { useInvitations } from '../hooks/useInvitations'
import { useNotifications } from '../hooks/useNotifications'
import { STATUTS_INVITATION } from '../utils/constantes'
import { dateRelative, formaterDateCourte } from '../utils/dates'
import { nomComplet } from '../utils/formatage'

const FILTRES = [
  { id: 'toutes', libelle: 'Toutes' },
  { id: 'attente', libelle: 'En attente' },
  { id: 'traitees', libelle: 'Traitées' },
]

function CarteInvitation({ invitation }) {
  const executer = useAction()
  const [enCours, setEnCours] = useState(null)
  const statut = STATUTS_INVITATION[invitation.statut] ?? STATUTS_INVITATION.EN_ATTENTE
  const enAttente = invitation.statut === 'EN_ATTENTE'
  const titre = invitation.projet?.libelle ?? `Invitation au projet n°${invitation.idProjet}`
  const emetteur = invitation.emetteur ? nomComplet(invitation.emetteur) : `le membre n°${invitation.idEmetteur}`

  const repondre = async (action) => {
    setEnCours(action)
    await executer(
      () => (action === 'accepter' ? invitationsApi.accepter(invitation.id) : invitationsApi.refuser(invitation.id)),
      action === 'accepter' ? 'Invitation acceptée : bienvenue dans le projet !' : 'Invitation refusée',
    )
    setEnCours(null)
  }

  return (
    <article className={`carte-invitation${enAttente ? ' carte-invitation--attente' : ''}`}>
      <div className="carte-invitation__marque" aria-hidden="true">
        {titre.slice(0, 2).toUpperCase()}
      </div>
      <div className="carte-invitation__corps">
        <header className="carte-invitation__entete">
          <div>
            <p className="carte-invitation__surtitre">Invitation à rejoindre un projet</p>
            <h3>{titre}</h3>
          </div>
          <Badge variante={statut.variante} point={enAttente}>
            {statut.libelle}
          </Badge>
        </header>
        <p className="carte-invitation__meta">
          <span>
            <Mail size={14} aria-hidden="true" /> Envoyée par {emetteur}
          </span>
          <span>
            <Clock size={14} aria-hidden="true" /> {formaterDateCourte(invitation.dateEnvoi)} · {dateRelative(invitation.dateEnvoi)}
          </span>
        </p>

        {enAttente && (
          <div className="carte-invitation__actions">
            <Bouton variante="danger" icone={X} chargement={enCours === 'refuser'} disabled={enCours === 'accepter'} onClick={() => repondre('refuser')}>
              Refuser
            </Bouton>
            <Bouton icone={Check} chargement={enCours === 'accepter'} disabled={enCours === 'refuser'} onClick={() => repondre('accepter')}>
              Accepter
            </Bouton>
          </div>
        )}
        {invitation.statut === 'ACCEPTEE' && (
          <div className="carte-invitation__actions">
            <Link to={`/projets/${invitation.idProjet}`} className="bouton bouton--secondaire bouton--m">
              Accéder au projet <ArrowRight size={16} aria-hidden="true" />
            </Link>
          </div>
        )}
      </div>
    </article>
  )
}

function InvitationsPage() {
  const { invitations, nombreEnAttente, chargement, erreur, recharger } = useInvitations()
  const { notifications, connecte } = useNotifications()
  const [filtre, setFiltre] = useState('toutes')

  const visibles = invitations.filter((i) => (filtre === 'attente' ? i.statut === 'EN_ATTENTE' : filtre === 'traitees' ? i.statut !== 'EN_ATTENTE' : true))
  const compteurs = { toutes: invitations.length, attente: nombreEnAttente, traitees: invitations.length - nombreEnAttente }

  if (chargement) return <Chargement message="Chargement des invitations…" />
  if (erreur) return <MessageErreur message={erreur} onReessayer={recharger} />

  return (
    <div className="page">
      <section className="bandeau">
        <div className="bandeau__texte">
          <p className="fil-ariane">Espace collaboration</p>
          <h1>Mes invitations</h1>
          <p className="bandeau__sous-titre">Consultez les invitations reçues pour collaborer sur des projets agiles et rejoindre de nouvelles équipes.</p>
        </div>
        <div className={`pastille-action${nombreEnAttente ? ' pastille-action--active' : ''}`}>
          <span className="pastille-action__nombre">{nombreEnAttente}</span>
          <span>
            <small>Action requise</small>
            <strong>
              {nombreEnAttente} demande{nombreEnAttente > 1 ? 's' : ''} en attente
            </strong>
          </span>
        </div>
      </section>

      <div className="deux-colonnes">
        <div>
          <div className="pilules pilules--marge" role="tablist" aria-label="Filtrer les invitations">
            {FILTRES.map((f) => (
              <button key={f.id} type="button" role="tab" aria-selected={filtre === f.id} className={`pilule${filtre === f.id ? ' pilule--active' : ''}`} onClick={() => setFiltre(f.id)}>
                {f.libelle} <span className="pilule__compteur">{compteurs[f.id]}</span>
              </button>
            ))}
          </div>

          {visibles.length === 0 ? (
            <EtatVide
              icone={MailOpen}
              titre={invitations.length === 0 ? 'Aucune invitation' : 'Rien dans cette catégorie'}
              texte={invitations.length === 0 ? "Quand un chef de projet vous invitera, l'invitation apparaîtra ici instantanément." : 'Changez de filtre pour voir les autres invitations.'}
            />
          ) : (
            <div className="liste-invitations">
              {visibles.map((invitation) => (
                <CarteInvitation key={invitation.id} invitation={invitation} />
              ))}
            </div>
          )}
        </div>

        <aside className="panneau-lateral" aria-label="Flux direct">
          <section className="carte-flux">
            <header>
              <h2>
                <Radio size={16} aria-hidden="true" /> Flux direct
              </h2>
              <Link to="/notifications">Tout voir</Link>
            </header>
            <p className={`flux__etat${connecte ? ' flux__etat--ok' : ''}`}>{connecte ? 'Connecté en temps réel' : 'Reconnexion en cours…'}</p>
            {notifications.length === 0 ? (
              <p className="carte-resume__vide">Aucune activité récente.</p>
            ) : (
              <ul>
                {notifications.slice(0, 3).map((n) => (
                  <li key={n.id} className={n.lue ? '' : 'flux__non-lue'}>
                    <Bell size={14} aria-hidden="true" />
                    <span>{n.message}</span>
                    <time>{dateRelative(n.dateCreation)}</time>
                  </li>
                ))}
              </ul>
            )}
            <Link to="/notifications" className="flux__lien">
              Voir toutes les notifications <ArrowRight size={14} aria-hidden="true" />
            </Link>
          </section>

          <section className="carte-astuce">
            <p className="carte-resume__surtitre">Conseil agile</p>
            <h2>Une charge mentale sous contrôle</h2>
            <p>Avant d'accepter, vérifiez votre disponibilité sur le sprint en cours : un engagement réaliste garde l'équipe sereine.</p>
          </section>
        </aside>
      </div>
    </div>
  )
}

export default InvitationsPage
