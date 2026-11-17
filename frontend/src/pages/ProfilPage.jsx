import { KeyRound, ListChecks, Mail, Save, Trash2, User } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { membresApi } from '../api/membres'
import Avatar from '../components/ui/Avatar'
import Bouton from '../components/ui/Bouton'
import ChampFormulaire from '../components/ui/ChampFormulaire'
import MessageErreur from '../components/ui/MessageErreur'
import ModaleConfirmation from '../components/ui/ModaleConfirmation'
import { useAuth } from '../hooks/useAuth'
import { viderCacheMembres } from '../hooks/useMembresDuProjet'
import { useNotifications } from '../hooks/useNotifications'
import { useToast } from '../hooks/useToast'
import { formaterDateLongue } from '../utils/dates'
import { nomComplet } from '../utils/formatage'
import { erreurEmail, erreurMotDePasse, erreurObligatoire, nettoyerErreurs } from '../utils/validation'

function FormulaireProfil() {
  const { membre, mettreAJourMembre } = useAuth()
  const { signaler } = useNotifications()
  const { afficherToast } = useToast()
  const [form, setForm] = useState({ prenom: membre.prenom, nom: membre.nom, email: membre.email })
  const [erreurs, setErreurs] = useState({})
  const [erreurServeur, setErreurServeur] = useState(null)
  const [envoi, setEnvoi] = useState(false)

  const modifie = form.prenom !== membre.prenom || form.nom !== membre.nom || form.email !== membre.email

  const changer = (e) => setForm((ancien) => ({ ...ancien, [e.target.name]: e.target.value }))

  const soumettre = async (e) => {
    e.preventDefault()
    setErreurServeur(null)
    const invalides = nettoyerErreurs({
      prenom: erreurObligatoire(form.prenom, 'Le prénom'),
      nom: erreurObligatoire(form.nom, 'Le nom'),
      email: erreurEmail(form.email),
    })
    setErreurs(invalides)
    if (Object.keys(invalides).length > 0) return

    setEnvoi(true)
    try {
      const misAJour = await membresApi.modifier(membre.id, { nom: form.nom.trim(), prenom: form.prenom.trim(), email: form.email.trim() })
      mettreAJourMembre(misAJour)
      viderCacheMembres()
      afficherToast({ type: 'succes', titre: 'Profil mis à jour' })
      signaler()
    } catch (err) {
      setErreurServeur(err.message)
    } finally {
      setEnvoi(false)
    }
  }

  return (
    <form className="carte-formulaire formulaire" onSubmit={soumettre} noValidate>
      <h2>
        <User size={18} aria-hidden="true" /> Informations personnelles
      </h2>
      <div className="formulaire__ligne">
        <ChampFormulaire label="Prénom" name="prenom" value={form.prenom} onChange={changer} erreur={erreurs.prenom} required />
        <ChampFormulaire label="Nom" name="nom" value={form.nom} onChange={changer} erreur={erreurs.nom} required />
      </div>
      <ChampFormulaire label="Adresse email" type="email" name="email" icone={Mail} value={form.email} onChange={changer} erreur={erreurs.email} required />
      <MessageErreur message={erreurServeur} compact />
      <div className="formulaire__actions">
        <Bouton type="submit" icone={Save} chargement={envoi} disabled={!modifie}>
          Enregistrer
        </Bouton>
      </div>
    </form>
  )
}

function FormulaireMotDePasse() {
  const { membre } = useAuth()
  const { afficherToast } = useToast()
  const vide = { ancienMotDePasse: '', nouveauMotDePasse: '', confirmation: '' }
  const [form, setForm] = useState(vide)
  const [erreurs, setErreurs] = useState({})
  const [erreurServeur, setErreurServeur] = useState(null)
  const [envoi, setEnvoi] = useState(false)

  const changer = (e) => setForm((ancien) => ({ ...ancien, [e.target.name]: e.target.value }))

  const soumettre = async (e) => {
    e.preventDefault()
    setErreurServeur(null)
    const invalides = nettoyerErreurs({
      ancienMotDePasse: erreurObligatoire(form.ancienMotDePasse, "L'ancien mot de passe"),
      nouveauMotDePasse: erreurMotDePasse(form.nouveauMotDePasse),
      confirmation: form.confirmation !== form.nouveauMotDePasse ? 'Les deux mots de passe ne correspondent pas.' : null,
    })
    setErreurs(invalides)
    if (Object.keys(invalides).length > 0) return

    setEnvoi(true)
    try {
      await membresApi.changerMotDePasse(membre.id, { ancienMotDePasse: form.ancienMotDePasse, nouveauMotDePasse: form.nouveauMotDePasse })
      afficherToast({ type: 'succes', titre: 'Mot de passe modifié' })
      setForm(vide)
    } catch (err) {
      setErreurServeur(err.message)
    } finally {
      setEnvoi(false)
    }
  }

  return (
    <form className="carte-formulaire formulaire" onSubmit={soumettre} noValidate>
      <h2>
        <KeyRound size={18} aria-hidden="true" /> Mot de passe
      </h2>
      <ChampFormulaire label="Ancien mot de passe" type="password" name="ancienMotDePasse" autoComplete="current-password" value={form.ancienMotDePasse} onChange={changer} erreur={erreurs.ancienMotDePasse} required />
      <div className="formulaire__ligne">
        <ChampFormulaire label="Nouveau mot de passe" type="password" name="nouveauMotDePasse" autoComplete="new-password" aide="8 caractères minimum." value={form.nouveauMotDePasse} onChange={changer} erreur={erreurs.nouveauMotDePasse} required />
        <ChampFormulaire label="Confirmation" type="password" name="confirmation" autoComplete="new-password" value={form.confirmation} onChange={changer} erreur={erreurs.confirmation} required />
      </div>
      <MessageErreur message={erreurServeur} compact />
      <div className="formulaire__actions">
        <Bouton type="submit" variante="secondaire" icone={KeyRound} chargement={envoi}>
          Changer le mot de passe
        </Bouton>
      </div>
    </form>
  )
}

function ProfilPage() {
  const { membre, deconnexion } = useAuth()
  const [suppression, setSuppression] = useState(false)
  const [motDePasse, setMotDePasse] = useState('')
  const [erreurMdp, setErreurMdp] = useState(null)

  return (
    <div className="page page--etroite">
      <section className="bandeau bandeau--profil">
        <Avatar membre={membre} taille="xl" />
        <div className="bandeau__texte">
          <p className="fil-ariane">Mon profil</p>
          <h1>{nomComplet(membre)}</h1>
          <p className="bandeau__sous-titre">
            {membre.email} · Membre depuis le {formaterDateLongue(membre.dateCreation)}
          </p>
        </div>
        <Link to="/mes-taches" className="bouton bouton--secondaire bouton--m">
          <ListChecks size={16} aria-hidden="true" /> <span>Mes tâches</span>
        </Link>
      </section>

      <FormulaireProfil key={`${membre.prenom}|${membre.nom}|${membre.email}`} />
      <FormulaireMotDePasse />

      <section className="carte-formulaire carte-formulaire--danger">
        <h2>
          <Trash2 size={18} aria-hidden="true" /> Zone dangereuse
        </h2>
        <p>La suppression de votre compte est définitive. Elle est refusée s'il vous reste des tâches assignées ou si vous êtes le seul chef d'un projet.</p>
        <Bouton variante="danger" icone={Trash2} onClick={() => setSuppression(true)}>
          Supprimer mon compte
        </Bouton>
      </section>

      <ModaleConfirmation
        ouverte={suppression}
        titre="Supprimer mon compte ?"
        message="Cette action est irréversible. Saisissez votre mot de passe pour confirmer."
        libelleConfirmer="Supprimer définitivement"
        danger
        onConfirmer={async () => {
          if (!motDePasse) {
            setErreurMdp('Le mot de passe est obligatoire.')
            throw new Error('Saisissez votre mot de passe pour confirmer.')
          }
          setErreurMdp(null)
          await membresApi.supprimer(membre.id, motDePasse)
          deconnexion()
        }}
        onFermer={() => {
          setSuppression(false)
          setMotDePasse('')
          setErreurMdp(null)
        }}
      >
        <ChampFormulaire label="Mot de passe" type="password" autoComplete="current-password" value={motDePasse} onChange={(e) => setMotDePasse(e.target.value)} erreur={erreurMdp} />
      </ModaleConfirmation>
    </div>
  )
}

export default ProfilPage
