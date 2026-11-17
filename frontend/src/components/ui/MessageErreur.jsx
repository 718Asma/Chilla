import { AlertCircle, RefreshCw } from 'lucide-react'
import Bouton from './Bouton'

function MessageErreur({ message, onReessayer, compact = false }) {
  if (!message) return null
  return (
    <div className={`message-erreur${compact ? ' message-erreur--compact' : ''}`} role="alert">
      <AlertCircle size={18} aria-hidden="true" />
      <p>{message}</p>
      {onReessayer && (
        <Bouton variante="secondaire" taille="s" icone={RefreshCw} onClick={() => onReessayer()}>
          Réessayer
        </Bouton>
      )}
    </div>
  )
}

export default MessageErreur
