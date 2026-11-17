import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import Chargement from '../ui/Chargement'

// Redirige vers /connexion si l'utilisateur n'est pas connecté.
function RoutePrivee() {
  const { token, initialisation } = useAuth()
  const location = useLocation()

  if (initialisation) return <Chargement pleinePage message="Vérification de votre session…" />
  if (!token) return <Navigate to="/connexion" replace state={{ depuis: location.pathname }} />
  return <Outlet />
}

export default RoutePrivee
