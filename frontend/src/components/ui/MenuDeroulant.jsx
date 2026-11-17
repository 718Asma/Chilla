import { useEffect, useRef, useState } from 'react'

// Menu flottant : declencheur({ ouvert, basculer, props }) rend le bouton ;
// children(fermer) rend le contenu. Se ferme avec Échap ou un clic à l'extérieur.
function MenuDeroulant({ declencheur, aligne = 'droite', largeur, children, className = '' }) {
  const [ouvert, setOuvert] = useState(false)
  const racine = useRef(null)

  useEffect(() => {
    if (!ouvert) return undefined
    const surClic = (e) => {
      if (!racine.current?.contains(e.target)) setOuvert(false)
    }
    const surTouche = (e) => {
      if (e.key === 'Escape') {
        setOuvert(false)
        racine.current?.querySelector('button')?.focus()
      }
    }
    document.addEventListener('mousedown', surClic)
    document.addEventListener('keydown', surTouche)
    return () => {
      document.removeEventListener('mousedown', surClic)
      document.removeEventListener('keydown', surTouche)
    }
  }, [ouvert])

  const fermer = () => setOuvert(false)

  return (
    <div className={`menu ${className}`.trim()} ref={racine}>
      {declencheur({
        ouvert,
        basculer: () => setOuvert((o) => !o),
        props: { 'aria-haspopup': 'true', 'aria-expanded': ouvert },
      })}
      {ouvert && (
        <div className={`menu__panneau menu__panneau--${aligne}`} style={largeur ? { width: largeur } : undefined}>
          {typeof children === 'function' ? children(fermer) : children}
        </div>
      )}
    </div>
  )
}

export default MenuDeroulant
