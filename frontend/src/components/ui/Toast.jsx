import { AlertTriangle, Bell, CheckCircle2, Info, X } from 'lucide-react'
import { Link } from 'react-router-dom'

const ICONES = { info: Info, succes: CheckCircle2, erreur: AlertTriangle, notification: Bell }

function Toast({ type = 'info', titre, message, lien, onFermer }) {
  const Icone = ICONES[type] ?? Info
  const contenu = (
    <>
      {titre && <strong>{titre}</strong>}
      {message && <span>{message}</span>}
    </>
  )
  return (
    <div className={`toast toast--${type}`} role={type === 'erreur' ? 'alert' : 'status'}>
      <Icone size={20} className="toast__icone" aria-hidden="true" />
      {lien ? (
        <Link to={lien} className="toast__texte" onClick={onFermer}>
          {contenu}
        </Link>
      ) : (
        <div className="toast__texte">{contenu}</div>
      )}
      <button type="button" className="bouton-icone bouton-icone--petit" onClick={onFermer} aria-label="Fermer la notification">
        <X size={14} aria-hidden="true" />
      </button>
    </div>
  )
}

export default Toast
