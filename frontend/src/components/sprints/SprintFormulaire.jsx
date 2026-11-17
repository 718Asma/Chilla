import { useState } from 'react'
import { sprintsApi } from '../../api/sprints'
import { useNotifications } from '../../hooks/useNotifications'
import { useToast } from '../../hooks/useToast'
import { LONGUEUR_MAX_TEXTE } from '../../utils/constantes'
import { erreurLongueur, erreurObligatoire, nettoyerErreurs } from '../../utils/validation'
import Bouton from '../ui/Bouton'
import ChampFormulaire from '../ui/ChampFormulaire'
import MessageErreur from '../ui/MessageErreur'
import Modale from '../ui/Modale'

function Contenu({ projet, sprint, onFermer }) {
  const { signaler } = useNotifications()
  const { afficherToast } = useToast()
  const modification = Boolean(sprint)

  const [form, setForm] = useState({
    nom: sprint?.nom ?? '',
    objectif: sprint?.objectif ?? '',
    dateDebut: sprint?.dateDebut ?? projet.dateDebut,
    dateFin: sprint?.dateFin ?? '',
  })
  const [erreurs, setErreurs] = useState({})
  const [erreurServeur, setErreurServeur] = useState(null)
  const [envoi, setEnvoi] = useState(false)

  const changer = (e) => {
    const { name, value } = e.target
    setForm((ancien) => ({ ...ancien, [name]: value }))
  }

  const dansLeProjet = (date) => date >= projet.dateDebut && date <= projet.dateFin

  const soumettre = async (e) => {
    e.preventDefault()
    setErreurServeur(null)
    const invalides = nettoyerErreurs({
      nom: erreurObligatoire(form.nom, 'Le nom du sprint') ?? erreurLongueur(form.nom, 'Le nom'),
      objectif: erreurLongueur(form.objectif, "L'objectif"),
      dateDebut:
        erreurObligatoire(form.dateDebut, 'La date de début') ??
        (!dansLeProjet(form.dateDebut) ? 'Le sprint doit rester dans les dates du projet.' : null),
      dateFin:
        erreurObligatoire(form.dateFin, 'La date de fin') ??
        (form.dateFin < form.dateDebut
          ? 'La date de fin doit être postérieure ou égale au début.'
          : !dansLeProjet(form.dateFin)
            ? 'Le sprint doit rester dans les dates du projet.'
            : null),
    })
    setErreurs(invalides)
    if (Object.keys(invalides).length > 0) return

    setEnvoi(true)
    try {
      const corps = { ...form, nom: form.nom.trim(), objectif: form.objectif.trim() }
      if (modification) await sprintsApi.modifier(sprint.id, corps)
      else await sprintsApi.creer(projet.id, corps)
      afficherToast({ type: 'succes', titre: modification ? 'Sprint modifié' : 'Sprint créé' })
      signaler()
      onFermer()
    } catch (err) {
      setErreurServeur(err.message)
      setEnvoi(false)
    }
  }

  return (
    <form onSubmit={soumettre} noValidate className="formulaire">
      <ChampFormulaire
        label="Nom du sprint"
        name="nom"
        value={form.nom}
        onChange={changer}
        erreur={erreurs.nom}
        placeholder="Ex. Sprint 1 - Tunnel d'achat"
        compteur={`${form.nom.length}/${LONGUEUR_MAX_TEXTE}`}
        required
      />
      <ChampFormulaire
        label="Objectif"
        as="textarea"
        rows={3}
        name="objectif"
        value={form.objectif}
        onChange={changer}
        erreur={erreurs.objectif}
        placeholder="Que doit accomplir l'équipe pendant ce sprint ?"
        compteur={`${form.objectif.length}/${LONGUEUR_MAX_TEXTE}`}
      />
      <div className="formulaire__ligne">
        <ChampFormulaire label="Début" type="date" name="dateDebut" min={projet.dateDebut} max={projet.dateFin} value={form.dateDebut} onChange={changer} erreur={erreurs.dateDebut} required />
        <ChampFormulaire label="Fin" type="date" name="dateFin" min={form.dateDebut || projet.dateDebut} max={projet.dateFin} value={form.dateFin} onChange={changer} erreur={erreurs.dateFin} required />
      </div>
      <MessageErreur message={erreurServeur} compact />
      <div className="formulaire__actions">
        <Bouton variante="fantome" onClick={onFermer} disabled={envoi}>
          Annuler
        </Bouton>
        <Bouton type="submit" chargement={envoi}>
          {modification ? 'Enregistrer' : 'Créer le sprint'}
        </Bouton>
      </div>
    </form>
  )
}

function SprintFormulaire({ ouverte, projet, sprint, onFermer }) {
  return (
    <Modale
      ouverte={ouverte}
      onFermer={onFermer}
      titre={sprint ? 'Modifier le sprint' : 'Nouveau sprint'}
      description={`Période du projet : du ${projet.dateDebut} au ${projet.dateFin}`}
    >
      <Contenu projet={projet} sprint={sprint} onFermer={onFermer} />
    </Modale>
  )
}

export default SprintFormulaire
