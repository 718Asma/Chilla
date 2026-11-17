import { useState } from 'react'
import { tachesApi } from '../../api/taches'
import { useAuth } from '../../hooks/useAuth'
import { useNotifications } from '../../hooks/useNotifications'
import { useToast } from '../../hooks/useToast'
import { LONGUEUR_MAX_TEXTE } from '../../utils/constantes'
import { nomComplet } from '../../utils/formatage'
import { erreurLongueur, erreurObligatoire, nettoyerErreurs } from '../../utils/validation'
import Bouton from '../ui/Bouton'
import ChampFormulaire from '../ui/ChampFormulaire'
import MessageErreur from '../ui/MessageErreur'
import Modale from '../ui/Modale'

function Contenu({ projet, tache, sprints, membres, idSprintInitial, onFermer }) {
  const { membre: moi } = useAuth()
  const { signaler } = useNotifications()
  const { afficherToast } = useToast()
  const modification = Boolean(tache)

  // Seuls les sprints non terminés / non annulés peuvent recevoir des tâches.
  const sprintsDisponibles = sprints.filter((s) => s.etat === 'NON_COMMENCE' || s.etat === 'EN_COURS' || s.id === tache?.idSprint)

  const [form, setForm] = useState({
    libelle: tache?.libelle ?? '',
    description: tache?.description ?? '',
    dateEcheance: tache?.dateEcheance ?? '',
    estimation: String(tache?.estimation ?? 1),
    position: String(tache?.position ?? 0),
    idSprint: String(tache?.idSprint ?? idSprintInitial ?? sprintsDisponibles[0]?.id ?? ''),
    idMembreAssigne: String(tache?.idMembre ?? (membres.some((m) => m.idMembre === moi?.id) ? moi.id : (membres[0]?.idMembre ?? ''))),
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
    const estimation = Number(form.estimation)
    const position = Number(form.position)
    const invalides = nettoyerErreurs({
      libelle: erreurObligatoire(form.libelle, 'Le libellé') ?? erreurLongueur(form.libelle, 'Le libellé'),
      description: erreurLongueur(form.description, 'La description'),
      dateEcheance: erreurObligatoire(form.dateEcheance, "La date d'échéance"),
      estimation: Number.isInteger(estimation) && estimation >= 0 ? null : "L'estimation doit être un entier positif ou nul.",
      position: Number.isInteger(position) && position >= 0 ? null : 'La position doit être un entier positif ou nul.',
      idSprint: form.idSprint ? null : 'Choisissez un sprint.',
      idMembreAssigne: form.idMembreAssigne ? null : 'Choisissez un membre.',
    })
    setErreurs(invalides)
    if (Object.keys(invalides).length > 0) return

    setEnvoi(true)
    try {
      const corps = {
        idSprint: Number(form.idSprint),
        idMembreAssigne: Number(form.idMembreAssigne),
        libelle: form.libelle.trim(),
        description: form.description.trim(),
        dateEcheance: form.dateEcheance,
        estimation,
        position,
      }
      if (modification) await tachesApi.modifier(tache.id, corps)
      else await tachesApi.creer(projet.id, corps)
      afficherToast({ type: 'succes', titre: modification ? 'Tâche modifiée' : 'Tâche créée' })
      signaler()
      onFermer()
    } catch (err) {
      setErreurServeur(err.message)
      setEnvoi(false)
    }
  }

  if (sprintsDisponibles.length === 0) {
    return (
      <div className="formulaire">
        <p className="modale__message">Aucun sprint n'est disponible : créez ou planifiez un sprint (non terminé) avant d'ajouter une tâche.</p>
        <div className="formulaire__actions">
          <Bouton onClick={onFermer}>Compris</Bouton>
        </div>
      </div>
    )
  }

  return (
    <form onSubmit={soumettre} noValidate className="formulaire">
      <ChampFormulaire
        label="Libellé"
        name="libelle"
        value={form.libelle}
        onChange={changer}
        erreur={erreurs.libelle}
        compteur={`${form.libelle.length}/${LONGUEUR_MAX_TEXTE}`}
        placeholder="Ex. Intégration de la passerelle de paiement"
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
      />
      <div className="formulaire__ligne">
        <ChampFormulaire label="Sprint" as="select" name="idSprint" value={form.idSprint} onChange={changer} erreur={erreurs.idSprint} required>
          {sprintsDisponibles.map((s) => (
            <option key={s.id} value={s.id}>
              {s.nom}
            </option>
          ))}
        </ChampFormulaire>
        <ChampFormulaire label="Assignée à" as="select" name="idMembreAssigne" value={form.idMembreAssigne} onChange={changer} erreur={erreurs.idMembreAssigne} required>
          {membres.map((m) => (
            <option key={m.idMembre} value={m.idMembre}>
              {nomComplet(m)}
            </option>
          ))}
        </ChampFormulaire>
      </div>
      <div className="formulaire__ligne formulaire__ligne--trois">
        <ChampFormulaire label="Échéance" type="date" name="dateEcheance" value={form.dateEcheance} onChange={changer} erreur={erreurs.dateEcheance} required />
        <ChampFormulaire label="Estimation (pts)" type="number" min="0" step="1" name="estimation" value={form.estimation} onChange={changer} erreur={erreurs.estimation} />
        <ChampFormulaire label="Position" type="number" min="0" step="1" name="position" value={form.position} onChange={changer} erreur={erreurs.position} aide="0 = en fin de sprint" />
      </div>
      <MessageErreur message={erreurServeur} compact />
      <div className="formulaire__actions">
        <Bouton variante="fantome" onClick={onFermer} disabled={envoi}>
          Annuler
        </Bouton>
        <Bouton type="submit" chargement={envoi}>
          {modification ? 'Enregistrer' : 'Créer la tâche'}
        </Bouton>
      </div>
    </form>
  )
}

function TacheFormulaire({ ouverte, onFermer, ...reste }) {
  return (
    <Modale ouverte={ouverte} onFermer={onFermer} titre={reste.tache ? 'Modifier la tâche' : 'Nouvelle tâche'} taille="l">
      <Contenu {...reste} onFermer={onFermer} />
    </Modale>
  )
}

export default TacheFormulaire
