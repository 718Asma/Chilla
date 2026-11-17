import { Bell, BellOff, Check, CheckCheck, Trash2 } from 'lucide-react'
import { useState } from 'react'
import Bouton from '../components/ui/Bouton'
import Chargement from '../components/ui/Chargement'
import EtatVide from '../components/ui/EtatVide'
import MessageErreur from '../components/ui/MessageErreur'
import { useNotifications } from '../hooks/useNotifications'
import { dateRelative, formaterDate } from '../utils/dates'

function NotificationsPage() {
  const { notifications, nonLues, chargement, erreur, recharger, marquerLue, toutMarquerLu, supprimer } = useNotifications()
  const [filtre, setFiltre] = useState('toutes')

  const visibles = filtre === 'non-lues' ? notifications.filter((n) => !n.lue) : notifications

  if (chargement && notifications.length === 0) return <Chargement message="Chargement des notifications…" />
  if (erreur && notifications.length === 0) return <MessageErreur message={erreur} onReessayer={() => recharger()} />

  return (
    <div className="page page--etroite">
      <section className="bandeau">
        <div className="bandeau__texte">
          <p className="fil-ariane">Activité</p>
          <h1>Notifications</h1>
          <p className="bandeau__sous-titre">
            {nonLues > 0 ? `${nonLues} notification${nonLues > 1 ? 's' : ''} non lue${nonLues > 1 ? 's' : ''}.` : 'Vous êtes à jour.'} Les nouvelles notifications apparaissent ici en temps réel.
          </p>
        </div>
        <Bouton variante="secondaire" icone={CheckCheck} disabled={nonLues === 0} onClick={toutMarquerLu}>
          Tout marquer comme lu
        </Bouton>
      </section>

      <div className="pilules pilules--marge" role="tablist" aria-label="Filtrer les notifications">
        {[
          { id: 'toutes', libelle: 'Toutes', nombre: notifications.length },
          { id: 'non-lues', libelle: 'Non lues', nombre: nonLues },
        ].map((f) => (
          <button key={f.id} type="button" role="tab" aria-selected={filtre === f.id} className={`pilule${filtre === f.id ? ' pilule--active' : ''}`} onClick={() => setFiltre(f.id)}>
            {f.libelle} <span className="pilule__compteur">{f.nombre}</span>
          </button>
        ))}
      </div>

      {visibles.length === 0 ? (
        <EtatVide
          icone={filtre === 'non-lues' ? BellOff : Bell}
          titre={filtre === 'non-lues' ? 'Tout est lu' : 'Aucune notification'}
          texte={filtre === 'non-lues' ? 'Aucune notification en attente de lecture.' : 'Les assignations, invitations et changements de sprint apparaîtront ici.'}
        />
      ) : (
        <ul className="liste-notifications">
          {visibles.map((n) => (
            <li key={n.id} className={`notification${n.lue ? '' : ' notification--non-lue'}`}>
              <span className="notification__pastille" aria-hidden="true" />
              <div className="notification__corps">
                <p>{n.message}</p>
                <time dateTime={n.dateCreation} title={formaterDate(n.dateCreation, { dateStyle: 'long' })}>
                  {dateRelative(n.dateCreation)}
                </time>
              </div>
              <div className="notification__actions">
                {!n.lue && (
                  <Bouton variante="fantome" taille="s" icone={Check} onClick={() => marquerLue(n.id)} aria-label="Marquer comme lue">
                    Lue
                  </Bouton>
                )}
                <Bouton variante="fantome" taille="s" icone={Trash2} onClick={() => supprimer(n.id)} aria-label="Supprimer la notification" />
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default NotificationsPage
