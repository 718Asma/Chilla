import { useEffect, useState } from 'react'
import Bouton from './Bouton'
import MessageErreur from './MessageErreur'
import Modale from './Modale'

// Confirmation avant une action sensible. onConfirmer peut lancer une erreur : son message
// (celui du backend) s'affiche dans la fenêtre sans la fermer.
function ModaleConfirmation({ ouverte, titre, message, libelleConfirmer = 'Confirmer', danger = false, onConfirmer, onFermer, children }) {
  const [envoi, setEnvoi] = useState(false)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    if (ouverte) {
      setEnvoi(false)
      setErreur(null)
    }
  }, [ouverte])

  const confirmer = async () => {
    setEnvoi(true)
    setErreur(null)
    try {
      await onConfirmer()
      onFermer()
    } catch (e) {
      setErreur(e.message)
      setEnvoi(false)
    }
  }

  return (
    <Modale
      ouverte={ouverte}
      onFermer={onFermer}
      titre={titre}
      taille="s"
      pied={
        <>
          <Bouton variante="fantome" onClick={onFermer} disabled={envoi}>
            Annuler
          </Bouton>
          <Bouton variante={danger ? 'danger' : 'primaire'} onClick={confirmer} chargement={envoi}>
            {libelleConfirmer}
          </Bouton>
        </>
      }
    >
      {message && <p className="modale__message">{message}</p>}
      {children}
      <MessageErreur message={erreur} compact />
    </Modale>
  )
}

export default ModaleConfirmation
