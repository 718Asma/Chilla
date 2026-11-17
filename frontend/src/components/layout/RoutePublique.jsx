import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import Chargement from '../ui/Chargement'

// Un utilisateur déjà connecté qui ouvre /connexion ou /inscription est renvoyé vers /projets.
function RoutePublique() {
  const { token, initialisation } = useAuth()

  if (initialisation) return <Chargement pleinePage message="Vérification de votre session…" />
  if (token) return <Navigate to="/projets" replace />
  return <Outlet />
}

export default RoutePublique
