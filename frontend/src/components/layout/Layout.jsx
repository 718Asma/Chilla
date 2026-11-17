import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { NotificationsProvider } from '../../context/NotificationsContext'
import Navbar from './Navbar'
import Sidebar from './Sidebar'

function Coque() {
  const [menuOuvert, setMenuOuvert] = useState(false)
  const { pathname } = useLocation()

  // Le menu mobile se referme à chaque changement de page.
  useEffect(() => {
    setMenuOuvert(false)
  }, [pathname])

  return (
    <div className="application">
      <a href="#contenu" className="lien-evitement">
        Aller au contenu
      </a>
      <Navbar onOuvrirMenu={() => setMenuOuvert(true)} />
      <Sidebar ouverte={menuOuvert} onFermer={() => setMenuOuvert(false)} />
      <main id="contenu" className="contenu" tabIndex={-1}>
        <Outlet />
      </main>
    </div>
  )
}

// Monté uniquement quand l'utilisateur est connecté (voir RoutePrivee) : le flux temps réel
// s'ouvre à la connexion et se ferme à la déconnexion.
function Layout() {
  return (
    <NotificationsProvider>
      <Coque />
    </NotificationsProvider>
  )
}

export default Layout
