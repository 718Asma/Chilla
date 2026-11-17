import { ArrowRight, Eye, EyeOff, Lock, Mail, User } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthLayout from '../components/layout/AuthLayout'
import Bouton from '../components/ui/Bouton'
import ChampFormulaire from '../components/ui/ChampFormulaire'
import MessageErreur from '../components/ui/MessageErreur'
import { useAuth } from '../hooks/useAuth'
import { erreurEmail, erreurMotDePasse, erreurObligatoire, nettoyerErreurs } from '../utils/validation'

function InscriptionPage() {
  const { inscription } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({ prenom: '', nom: '', email: '', motDePasse: '', confirmation: '' })
  const [afficher, setAfficher] = useState(false)
  const [erreurs, setErreurs] = useState({})
  const [erreurServeur, setErreurServeur] = useState(null)
  const [envoi, setEnvoi] = useState(false)

  const changer = (e) => {
    const { name, value } = e.target
    setForm((ancien) => ({ ...ancien, [name]: value }))
  }

  const soumettre = async (e) => {
    e.preventDefault()
    setErreurServeur(null)
    const invalides = nettoyerErreurs({
      prenom: erreurObligatoire(form.prenom, 'Le prénom'),
      nom: erreurObligatoire(form.nom, 'Le nom'),
      email: erreurEmail(form.email),
      motDePasse: erreurMotDePasse(form.motDePasse),
      confirmation: form.confirmation !== form.motDePasse ? 'Les deux mots de passe ne correspondent pas.' : null,
    })
    setErreurs(invalides)
    if (Object.keys(invalides).length > 0) return

    setEnvoi(true)
    try {
      await inscription({
        nom: form.nom.trim(),
        prenom: form.prenom.trim(),
        email: form.email.trim(),
        motDePasse: form.motDePasse,
      })
      navigate('/projets', { replace: true })
    } catch (err) {
      setErreurServeur(err.message)
      setEnvoi(false)
    }
  }

  return (
    <AuthLayout>
      <div className="auth__intro">
        <h1>Créer votre compte</h1>
        <p>Rejoignez Chilla et lancez votre premier projet en quelques minutes.</p>
      </div>

      <form onSubmit={soumettre} noValidate className="formulaire">
        <div className="formulaire__ligne">
          <ChampFormulaire
            label="Prénom"
            name="prenom"
            icone={User}
            autoComplete="given-name"
            value={form.prenom}
            onChange={changer}
            erreur={erreurs.prenom}
            required
            autoFocus
          />
          <ChampFormulaire
            label="Nom"
            name="nom"
            autoComplete="family-name"
            value={form.nom}
            onChange={changer}
            erreur={erreurs.nom}
            required
          />
        </div>
        <ChampFormulaire
          label="Adresse email"
          type="email"
          name="email"
          icone={Mail}
          autoComplete="email"
          placeholder="nom@exemple.com"
          value={form.email}
          onChange={changer}
          erreur={erreurs.email}
          required
        />
        <ChampFormulaire
          label="Mot de passe"
          type={afficher ? 'text' : 'password'}
          name="motDePasse"
          icone={Lock}
          autoComplete="new-password"
          aide="8 caractères minimum."
          value={form.motDePasse}
          onChange={changer}
          erreur={erreurs.motDePasse}
          required
          droite={
            <button
              type="button"
              className="bouton-icone bouton-icone--petit"
              onClick={() => setAfficher((a) => !a)}
              aria-label={afficher ? 'Masquer les mots de passe' : 'Afficher les mots de passe'}
              aria-pressed={afficher}
            >
              {afficher ? <EyeOff size={16} aria-hidden="true" /> : <Eye size={16} aria-hidden="true" />}
            </button>
          }
        />
        <ChampFormulaire
          label="Confirmer le mot de passe"
          type={afficher ? 'text' : 'password'}
          name="confirmation"
          icone={Lock}
          autoComplete="new-password"
          value={form.confirmation}
          onChange={changer}
          erreur={erreurs.confirmation}
          required
        />

        <MessageErreur message={erreurServeur} compact />

        <Bouton type="submit" taille="l" pleineLargeur chargement={envoi}>
          Créer mon compte
          {!envoi && <ArrowRight size={16} aria-hidden="true" />}
        </Bouton>
      </form>

      <p className="auth__bas">
        Déjà inscrit ? <Link to="/connexion">Se connecter</Link>
      </p>
    </AuthLayout>
  )
}

export default InscriptionPage
