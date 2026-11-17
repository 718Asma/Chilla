import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/layout/Layout'
import RoutePrivee from './components/layout/RoutePrivee'
import RoutePublique from './components/layout/RoutePublique'
import { AuthProvider } from './context/AuthContext'
import { ThemeProvider } from './context/ThemeContext'
import { ToastProvider } from './context/ToastContext'
import ConnexionPage from './pages/ConnexionPage'
import InscriptionPage from './pages/InscriptionPage'
import InvitationsPage from './pages/InvitationsPage'
import MesTachesPage from './pages/MesTachesPage'
import NotFoundPage from './pages/NotFoundPage'
import NotificationsPage from './pages/NotificationsPage'
import ProfilPage from './pages/ProfilPage'
import ProjetDetailPage from './pages/ProjetDetailPage'
import ProjetsPage from './pages/ProjetsPage'

function App() {
  return (
    <ThemeProvider>
      <BrowserRouter>
        <ToastProvider>
          <AuthProvider>
            <Routes>
              <Route element={<RoutePublique />}>
                <Route path="connexion" element={<ConnexionPage />} />
                <Route path="inscription" element={<InscriptionPage />} />
              </Route>

              <Route element={<RoutePrivee />}>
                <Route element={<Layout />}>
                  <Route index element={<Navigate to="/projets" replace />} />
                  <Route path="projets" element={<ProjetsPage />} />
                  <Route path="projets/:id" element={<ProjetDetailPage />} />
                  <Route path="invitations" element={<InvitationsPage />} />
                  <Route path="notifications" element={<NotificationsPage />} />
                  <Route path="mes-taches" element={<MesTachesPage />} />
                  <Route path="profil" element={<ProfilPage />} />
                  <Route path="*" element={<NotFoundPage />} />
                </Route>
              </Route>
            </Routes>
          </AuthProvider>
        </ToastProvider>
      </BrowserRouter>
    </ThemeProvider>
  )
}

export default App
