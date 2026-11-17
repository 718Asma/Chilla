import { useState } from 'react'
import { projetsApi } from '../../api/projets'
import { useNotifications } from '../../hooks/useNotifications'
import { useToast } from '../../hooks/useToast'
import { LONGUEUR_MAX_TEXTE } from '../../utils/constantes'
import { aujourdhui } from '../../utils/dates'
import { erreurLongueur, erreurObligatoire, nettoyerErreurs } from '../../utils/validation'
import Bouton from '../ui/Bouton'
import ChampFormulaire from '../ui/ChampFormulaire'
import MessageErreur from '../ui/MessageErreur'
import Modale from '../ui/Modale'

function Contenu({ projet, onFermer, onEnregistre }) {
  const { signaler } = useNotifications()
  const { afficherToast } = useToast()
  const modification = Boolean(projet)

  const [form, setForm] = useState({
    libelle: projet?.libelle ?? '',
    description: projet?.description ?? '',
    dateDebut: projet?.dateDebut ?? aujourdhui(),
    dateFin: projet?.dateFin ?? '',
  })
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
      libelle: erreurObligatoire(form.libelle, 'Le nom du projet') ?? erreurLongueur(form.libelle, 'Le nom'),
      description: erreurLongueur(form.description, 'La description'),
      dateDebut: erreurObligatoire(form.dateDebut, 'La date de début'),
      dateFin:
        erreurObligatoire(form.dateFin, 'La date de fin') ??
        (form.dateDebut && form.dateFin < form.dateDebut ? 'La date de fin doit être postérieure ou égale au début.' : null),
    })
    setErreurs(invalides)
    if (Object.keys(invalides).length > 0) return

    setEnvoi(true)
    try {
      const corps = { ...form, libelle: form.libelle.trim(), description: form.description.trim() }
      const resultat = modification ? await projetsApi.modifier(projet.id, corps) : await projetsApi.creer(corps)
      afficherToast({ type: 'succes', titre: modification ? 'Projet modifié' : 'Projet créé', message: `« ${corps.libelle} » est à jour.` })
      signaler()
      onEnregistre?.(resultat)
      onFermer()
    } catch (err) {
      setErreurServeur(err.message)
      setEnvoi(false)
    }
  }

  return (
    <form onSubmit={soumettre} noValidate className="formulaire">
      <ChampFormulaire
        label="Nom du projet"
        name="libelle"
        value={form.libelle}
        onChange={changer}
        erreur={erreurs.libelle}
        compteur={`${form.libelle.length}/${LONGUEUR_MAX_TEXTE}`}
        placeholder="Ex. Plateforme e-commerce"
        required
      />
      <ChampFormulaire
        label="Description"
        as="textarea"
        rows={3}
        name="description"
        value={form.description}
        onChange={changer}
        erreur={erreurs.description}
        compteur={`${form.description.length}/${LONGUEUR_MAX_TEXTE}`}
        placeholder="Objectif et périmètre du projet"
      />
      <div className="formulaire__ligne">
        <ChampFormulaire label="Date de début" type="date" name="dateDebut" value={form.dateDebut} onChange={changer} erreur={erreurs.dateDebut} required />
        <ChampFormulaire label="Date de fin" type="date" name="dateFin" min={form.dateDebut} value={form.dateFin} onChange={changer} erreur={erreurs.dateFin} required />
      </div>

      <MessageErreur message={erreurServeur} compact />

      <div className="formulaire__actions">
        <Bouton variante="fantome" onClick={onFermer} disabled={envoi}>
          Annuler
        </Bouton>
        <Bouton type="submit" chargement={envoi}>
          {modification ? 'Enregistrer' : 'Créer le projet'}
        </Bouton>
      </div>
    </form>
  )
}

// Création (projet absent) ou modification (projet fourni) d'un projet.
function ProjetFormulaire({ ouverte, projet, onFermer, onEnregistre }) {
  return (
    <Modale
      ouverte={ouverte}
      onFermer={onFermer}
      titre={projet ? 'Modifier le projet' : 'Nouveau projet'}
      description={projet ? undefined : 'Donnez un cadre à votre équipe : un nom, une période, un objectif.'}
    >
      <Contenu projet={projet} onFermer={onFermer} onEnregistre={onEnregistre} />
    </Modale>
  )
}

export default ProjetFormulaire
