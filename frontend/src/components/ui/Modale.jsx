import { X } from 'lucide-react'
import { useEffect, useId, useRef } from 'react'
import { createPortal } from 'react-dom'

const FOCALISABLES = 'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'

// Boîte de dialogue accessible : Échap et clic sur le fond ferment, le focus reste dans la modale.
function Modale({ ouverte, titre, description, onFermer, children, pied, taille = 'm' }) {
  const idTitre = useId()
  const panneau = useRef(null)

  useEffect(() => {
    if (!ouverte) return undefined
    const precedent = document.activeElement
    const defilementAvant = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    const premier = panneau.current?.querySelector('input:not([disabled]), select, textarea') ?? panneau.current
    premier?.focus()

    const surTouche = (e) => {
      if (e.key === 'Escape') {
        e.stopPropagation()
        onFermer()
      } else if (e.key === 'Tab' && panneau.current) {
        const elements = [...panneau.current.querySelectorAll(FOCALISABLES)]
        if (elements.length === 0) return
        const debut = elements[0]
        const fin = elements[elements.length - 1]
        if (e.shiftKey && document.activeElement === debut) {
          e.preventDefault()
          fin.focus()
        } else if (!e.shiftKey && document.activeElement === fin) {
          e.preventDefault()
          debut.focus()
        }
      }
    }
    document.addEventListener('keydown', surTouche)
    return () => {
      document.removeEventListener('keydown', surTouche)
      document.body.style.overflow = defilementAvant
      precedent?.focus?.()
    }
  }, [ouverte, onFermer])

  if (!ouverte) return null

  return createPortal(
    <div className="modale-fond" onMouseDown={(e) => e.target === e.currentTarget && onFermer()}>
      <div ref={panneau} className={`modale modale--${taille}`} role="dialog" aria-modal="true" aria-labelledby={idTitre} tabIndex={-1}>
        <header className="modale__entete">
          <div>
            <h2 id={idTitre}>{titre}</h2>
            {description && <p className="modale__description">{description}</p>}
          </div>
          <button type="button" className="bouton-icone" onClick={onFermer} aria-label="Fermer">
            <X size={18} aria-hidden="true" />
          </button>
        </header>
        <div className="modale__corps">{children}</div>
        {pied && <footer className="modale__pied">{pied}</footer>}
      </div>
    </div>,
    document.body,
  )
}

export default Modale
