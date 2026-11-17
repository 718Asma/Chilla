import { createContext, useCallback, useMemo, useRef, useState } from 'react'
import Toast from '../components/ui/Toast'

export const ToastContext = createContext(null)

const DUREE_PAR_DEFAUT = 5000

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])
  const compteur = useRef(0)

  const fermer = useCallback((id) => {
    setToasts((liste) => liste.filter((t) => t.id !== id))
  }, [])

  // afficherToast({ titre, message, type: 'info' | 'succes' | 'erreur' | 'notification', lien })
  const afficherToast = useCallback(
    (toast) => {
      compteur.current += 1
      const id = compteur.current
      setToasts((liste) => [...liste.slice(-3), { id, type: 'info', ...toast }])
      setTimeout(() => fermer(id), toast.duree ?? DUREE_PAR_DEFAUT)
      return id
    },
    [fermer],
  )

  const valeur = useMemo(() => ({ afficherToast, fermerToast: fermer }), [afficherToast, fermer])

  return (
    <ToastContext.Provider value={valeur}>
      {children}
      <div className="toasts" role="region" aria-label="Notifications temporaires" aria-live="polite">
        {toasts.map((toast) => (
          <Toast key={toast.id} {...toast} onFermer={() => fermer(toast.id)} />
        ))}
      </div>
    </ToastContext.Provider>
  )
}
