import { ArrowRight, Eye, EyeOff, Lock, Mail } from 'lucide-react'
import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import AuthLayout from '../components/layout/AuthLayout'
import Bouton from '../components/ui/Bouton'
import ChampFormulaire from '../components/ui/ChampFormulaire'
import MessageErreur from '../components/ui/MessageErreur'
import { useAuth } from '../hooks/useAuth'
import { erreurEmail, erreurObligatoire, nettoyerErreurs } from '../utils/validation'

function ConnexionPage() {
  const { connexion, sessionExpiree } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [email, setEmail] = useState('')
  const [motDePasse, setMotDePasse] = useState('')
  const [afficher, setAfficher] = useState(false)
  const [erreurs, setErreurs] = useState({})
  const [erreurServeur, setErreurServeur] = useState(null)
  const [envoi, setEnvoi] = useState(false)

  const soumettre = async (e) => {
    e.preventDefault()
    setErreurServeur(null)
    const invalides = nettoyerErreurs({
      email: erreurEmail(email),
      motDePasse: erreurObligatoire(motDePasse, 'Le mot de passe'),
    })
    setErreurs(invalides)
    if (Object.keys(invalides).length > 0) return

    setEnvoi(true)
    try {
      await connexion(email.trim(), motDePasse)
      navigate(location.state?.depuis ?? '/projets', { replace: true })
    } catch (err) {
      setErreurServeur(err.message)
      setEnvoi(false)
    }
  }

  return (
    <AuthLayout>
      <div className="auth__intro">
        <h1>Connexion à Chilla</h1>
        <p>Gérez vos projets agiles simplement avec votre équipe.</p>
      </div>

      {sessionExpiree && !erreurServeur && (
        <p className="auth__info" role="status">
          Votre session a expiré. Reconnectez-vous pour continuer.
        </p>
      )}

      <form onSubmit={soumettre} noValidate className="formulaire">
        <ChampFormulaire
          label="Adresse email"
          type="email"
          name="email"
          icone={Mail}
          autoComplete="email"
          placeholder="nom@exemple.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          erreur={erreurs.email}
          required
          autoFocus
        />
        <ChampFormulaire
          label="Mot de passe"
          type={afficher ? 'text' : 'password'}
          name="motDePasse"
          icone={Lock}
          autoComplete="current-password"
          placeholder="••••••••"
          value={motDePasse}
          onChange={(e) => setMotDePasse(e.target.value)}
          erreur={erreurs.motDePasse}
          required
          droite={
            <button
              type="button"
              className="bouton-icone bouton-icone--petit"
              onClick={() => setAfficher((a) => !a)}
              aria-label={afficher ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
              aria-pressed={afficher}
            >
              {afficher ? <EyeOff size={16} aria-hidden="true" /> : <Eye size={16} aria-hidden="true" />}
            </button>
          }
        />

        <MessageErreur message={erreurServeur} compact />

        <Bouton type="submit" taille="l" pleineLargeur chargement={envoi}>
          Se connecter
          {!envoi && <ArrowRight size={16} aria-hidden="true" />}
        </Bouton>
      </form>

      <p className="auth__bas">
        Pas encore de compte ? <Link to="/inscription">Créer un compte</Link>
      </p>
    </AuthLayout>
  )
}

export default ConnexionPage
