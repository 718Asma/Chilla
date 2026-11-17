import { Bell, FolderKanban, ListChecks, Mail, User, Wifi, WifiOff, X } from 'lucide-react'
import { NavLink } from 'react-router-dom'
import { useNotifications } from '../../hooks/useNotifications'
import { useProjets } from '../../hooks/useProjets'
import { codeProjet } from '../../utils/formatage'

function Sidebar({ ouverte, onFermer }) {
  const { connecte } = useNotifications()
  const { projets } = useProjets({ intervalle: 60000 })
  const actifs = projets.filter((p) => !p.archive).slice(0, 8)

  return (
    <>
      {ouverte && <div className="sidebar-voile" onClick={onFermer} aria-hidden="true" />}
      <aside className={`sidebar${ouverte ? ' sidebar--ouverte' : ''}`} aria-label="Barre latérale">
        <button type="button" className="bouton-icone sidebar__fermer" onClick={onFermer} aria-label="Fermer le menu">
          <X size={20} aria-hidden="true" />
        </button>

        <nav className="sidebar__bloc sidebar__bloc--mobile" aria-label="Navigation principale">
          <p className="sidebar__titre">Navigation</p>
          <NavLink to="/projets" className="sidebar__lien">
            <FolderKanban size={18} aria-hidden="true" /> <span>Projets</span>
          </NavLink>
          <NavLink to="/invitations" className="sidebar__lien">
            <Mail size={18} aria-hidden="true" /> <span>Invitations</span>
          </NavLink>
          <NavLink to="/notifications" className="sidebar__lien">
            <Bell size={18} aria-hidden="true" /> <span>Notifications</span>
          </NavLink>
        </nav>

        <nav className="sidebar__bloc" aria-label="Raccourcis">
          <p className="sidebar__titre">Mon espace</p>
          <NavLink to="/mes-taches" className="sidebar__lien">
            <ListChecks size={18} aria-hidden="true" /> <span>Mes tâches</span>
          </NavLink>
          <NavLink to="/profil" className="sidebar__lien">
            <User size={18} aria-hidden="true" /> <span>Mon profil</span>
          </NavLink>
        </nav>

        <nav className="sidebar__bloc sidebar__bloc--projets" aria-label="Projets actifs">
          <p className="sidebar__titre">Projets actifs</p>
          {actifs.length === 0 ? (
            <p className="sidebar__vide">Aucun projet actif</p>
          ) : (
            actifs.map((projet) => (
              <NavLink key={projet.id} to={`/projets/${projet.id}`} className="sidebar__lien sidebar__lien--projet" title={projet.libelle}>
                <span className="sidebar__code">{codeProjet(projet.id).slice(4)}</span>
                <span className="sidebar__libelle">{projet.libelle}</span>
              </NavLink>
            ))
          )}
        </nav>

        <div className={`sidebar__statut${connecte ? ' sidebar__statut--ok' : ''}`} role="status">
          {connecte ? <Wifi size={16} aria-hidden="true" /> : <WifiOff size={16} aria-hidden="true" />}
          <span>
            <strong>{connecte ? 'Temps réel actif' : 'Reconnexion…'}</strong>
            <small>{connecte ? 'Tout se met à jour automatiquement' : 'Les données restent actualisées toutes les 30 s'}</small>
          </span>
        </div>
      </aside>
    </>
  )
}

export default Sidebar
