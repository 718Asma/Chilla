import { Loader2 } from 'lucide-react'

function Chargement({ message = 'Chargement…', pleinePage = false }) {
  return (
    <div className={`chargement${pleinePage ? ' chargement--page' : ''}`} role="status" aria-live="polite">
      <Loader2 size={28} className="icone-rotation" aria-hidden="true" />
      <span>{message}</span>
    </div>
  )
}

export default Chargement
