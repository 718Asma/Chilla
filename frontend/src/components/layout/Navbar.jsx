import { Bell, CheckCheck, ListChecks, LogOut, Menu, User } from 'lucide-react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useInvitationsEnAttente } from '../../hooks/useInvitationsEnAttente'
import { useNotifications } from '../../hooks/useNotifications'
import { dateRelative } from '../../utils/dates'
import { nomComplet } from '../../utils/formatage'
import Avatar from '../ui/Avatar'
import Logo from '../ui/Logo'
import MenuDeroulant from '../ui/MenuDeroulant'
import SelecteurTheme from './SelecteurTheme'

function Compteur({ valeur }) {
  if (!valeur) return null
  return (
    <span className="compteur" aria-label={`${valeur} en attente`}>
      {valeur > 99 ? '99+' : valeur}
    </span>
  )
}

function Navbar({ onOuvrirMenu }) {
  const { membre, deconnexion } = useAuth()
  const { notifications, nonLues, marquerLue, toutMarquerLu } = useNotifications()
  const invitationsEnAttente = useInvitationsEnAttente()
  const navigate = useNavigate()

  const recentes = notifications.slice(0, 5)

  return (
    <header className="navbar">
      <button type="button" className="bouton-icone navbar__burger" onClick={onOuvrirMenu} aria-label="Ouvrir le menu">
        <Menu size={22} aria-hidden="true" />
      </button>

      <Link to="/projets" className="navbar__logo" aria-label="Chilla, accueil">
        <Logo sousTitre="Gestion agile" />
      </Link>

      <nav className="navbar__liens" aria-label="Navigation principale">
        <NavLink to="/projets" className="navbar__lien">
          Projets
        </NavLink>
        <NavLink to="/invitations" className="navbar__lien">
          Invitations
          <Compteur valeur={invitationsEnAttente} />
        </NavLink>
        <NavLink to="/notifications" className="navbar__lien">
          Notifications
          <Compteur valeur={nonLues} />
        </NavLink>
      </nav>

      <div className="navbar__actions">
        <SelecteurTheme />

        <MenuDeroulant
          largeur="360px"
          declencheur={({ basculer, props }) => (
            <button
              type="button"
              className="bouton-icone bouton-icone--cloche"
              onClick={basculer}
              aria-label={nonLues ? `Notifications, ${nonLues} non lues` : 'Notifications'}
              {...props}
            >
              <Bell size={20} aria-hidden="true" />
              {nonLues > 0 && <span className="cloche__pastille">{nonLues > 9 ? '9+' : nonLues}</span>}
            </button>
          )}
        >
          {(fermer) => (
            <div>
              <div className="menu__entete">
                <p className="menu__titre menu__titre--sans-marge">Notifications</p>
                {nonLues > 0 && (
                  <button type="button" className="lien-bouton" onClick={toutMarquerLu}>
                    <CheckCheck size={14} aria-hidden="true" /> Tout marquer lu
                  </button>
                )}
              </div>
              {recentes.length === 0 ? (
                <p className="menu__vide">Aucune notification pour le moment.</p>
              ) : (
                <ul className="liste-notifs-menu">
                  {recentes.map((n) => (
                    <li key={n.id}>
                      <button
                        type="button"
                        className={`notif-menu${n.lue ? '' : ' notif-menu--non-lue'}`}
                        onClick={() => {
                          marquerLue(n.id)
                          fermer()
                          navigate('/notifications')
                        }}
                      >
                        <span className="notif-menu__message">{n.message}</span>
                        <span className="notif-menu__date">{dateRelative(n.dateCreation)}</span>
                      </button>
                    </li>
                  ))}
                </ul>
              )}
              <Link to="/notifications" className="menu__pied" onClick={fermer}>
                Voir tout
              </Link>
            </div>
          )}
        </MenuDeroulant>

        <MenuDeroulant
          largeur="240px"
          declencheur={({ basculer, props }) => (
            <button type="button" className="navbar__profil" onClick={basculer} aria-label="Menu du profil" {...props}>
              <Avatar membre={membre} taille="s" />
              <span className="navbar__nom">
                <strong>{nomComplet(membre)}</strong>
                <small>{membre?.email}</small>
              </span>
            </button>
          )}
        >
          {(fermer) => (
            <div>
              <Link to="/profil" className="menu__element" onClick={fermer}>
                <User size={16} aria-hidden="true" /> Mon profil
              </Link>
              <Link to="/mes-taches" className="menu__element" onClick={fermer}>
                <ListChecks size={16} aria-hidden="true" /> Mes tâches
              </Link>
              <button type="button" className="menu__element menu__element--danger" onClick={() => deconnexion()}>
                <LogOut size={16} aria-hidden="true" /> Déconnexion
              </button>
            </div>
          )}
        </MenuDeroulant>
      </div>
    </header>
  )
}

export default Navbar
