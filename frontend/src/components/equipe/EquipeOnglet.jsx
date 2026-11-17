import { Mail, Send, UserMinus, Users } from 'lucide-react'
import { useState } from 'react'
import { projetsApi } from '../../api/projets'
import { useAction } from '../../hooks/useAction'
import { useAuth } from '../../hooks/useAuth'
import { ROLES } from '../../utils/constantes'
import { nomComplet } from '../../utils/formatage'
import { erreurEmail } from '../../utils/validation'
import Avatar from '../ui/Avatar'
import Badge from '../ui/Badge'
import Bouton from '../ui/Bouton'
import ChampFormulaire from '../ui/ChampFormulaire'
import EtatVide from '../ui/EtatVide'
import MessageErreur from '../ui/MessageErreur'
import ModaleConfirmation from '../ui/ModaleConfirmation'

function InvitationFormulaire({ projet }) {
  const [email, setEmail] = useState('')
  const [erreur, setErreur] = useState(null)
  const [erreurServeur, setErreurServeur] = useState(null)
  const [succes, setSucces] = useState(null)
  const [envoi, setEnvoi] = useState(false)

  const soumettre = async (e) => {
    e.preventDefault()
    setSucces(null)
    setErreurServeur(null)
    const invalide = erreurEmail(email)
    setErreur(invalide)
    if (invalide) return

    setEnvoi(true)
    try {
      await projetsApi.inviter(projet.id, { email: email.trim() })
      setSucces(`Invitation envoyée à ${email.trim()}. Elle apparaîtra aussitôt dans sa boîte d'invitations.`)
      setEmail('')
    } catch (err) {
      setErreurServeur(err.message)
    } finally {
      setEnvoi(false)
    }
  }

  return (
    <form className="invitation-form" onSubmit={soumettre} noValidate>
      <div className="invitation-form__champ">
        <ChampFormulaire
          label="Inviter par email"
          type="email"
          icone={Mail}
          placeholder="collegue@exemple.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          erreur={erreur}
          autoComplete="off"
        />
      </div>
      <Bouton type="submit" icone={Send} chargement={envoi}>
        Inviter
      </Bouton>
      {succes && (
        <p className="auth__info" role="status">
          {succes}
        </p>
      )}
      <MessageErreur message={erreurServeur} compact />
    </form>
  )
}

function EquipeOnglet({ projet, membres, estChef, lectureSeule }) {
  const { membre: moi } = useAuth()
  const executer = useAction()
  const [aRetirer, setARetirer] = useState(null)
  const peutGerer = estChef && !lectureSeule

  const changerRole = (membre, role) => {
    if (role === membre.role) return
    return executer(() => projetsApi.changerRole(projet.id, membre.idMembre, role), 'Rôle mis à jour')
  }

  return (
    <section className="onglet-contenu" aria-labelledby="titre-equipe">
      <div className="section-entete">
        <div>
          <h2 id="titre-equipe">Équipe</h2>
          <p>
            {membres.length} membre{membres.length > 1 ? 's' : ''} participent à ce projet.
          </p>
        </div>
      </div>

      {peutGerer && <InvitationFormulaire projet={projet} />}

      {membres.length === 0 ? (
        <EtatVide icone={Users} titre="Aucun membre" />
      ) : (
        <ul className="liste-equipe">
          {membres.map((m) => {
            const role = ROLES[m.role] ?? ROLES.MEMBRE
            const estMoi = m.idMembre === moi?.id
            return (
              <li key={m.idMembre} className="ligne-membre">
                <Avatar membre={m} />
                <div className="ligne-membre__identite">
                  <strong>
                    {nomComplet(m)} {estMoi && <span className="ligne-membre__vous">(vous)</span>}
                  </strong>
                  <small>{m.email || '—'}</small>
                </div>
                {peutGerer ? (
                  <>
                    <label className="sr-only" htmlFor={`role-${m.idMembre}`}>
                      Rôle de {nomComplet(m)}
                    </label>
                    <select id={`role-${m.idMembre}`} className="ligne-membre__role" value={m.role} onChange={(e) => changerRole(m, e.target.value)}>
                      {Object.entries(ROLES).map(([valeur, { libelle }]) => (
                        <option key={valeur} value={valeur}>
                          {libelle}
                        </option>
                      ))}
                    </select>
                    {!estMoi && (
                      <Bouton variante="fantome" taille="s" icone={UserMinus} onClick={() => setARetirer(m)} aria-label={`Retirer ${nomComplet(m)}`}>
                        Retirer
                      </Bouton>
                    )}
                  </>
                ) : (
                  <Badge variante={role.variante}>{role.libelle}</Badge>
                )}
              </li>
            )
          })}
        </ul>
      )}

      <ModaleConfirmation
        ouverte={Boolean(aRetirer)}
        titre="Retirer ce membre ?"
        message={aRetirer ? `${nomComplet(aRetirer)} ne pourra plus accéder au projet. Le retrait est refusé s'il lui reste des tâches en cours.` : ''}
        libelleConfirmer="Retirer du projet"
        danger
        onConfirmer={async () => {
          await projetsApi.retirerMembre(projet.id, aRetirer.idMembre)
          executer(async () => {}, 'Membre retiré du projet')
        }}
        onFermer={() => setARetirer(null)}
      />
    </section>
  )
}

export default EquipeOnglet
