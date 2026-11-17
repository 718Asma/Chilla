import { createContext, useCallback, useEffect, useMemo, useState } from 'react'

export const ThemeContext = createContext(null)

const CLE_THEME = 'chilla_theme'
const MODES = ['clair', 'sombre', 'systeme']

function lireMode() {
  try {
    const mode = localStorage.getItem(CLE_THEME)
    return MODES.includes(mode) ? mode : 'systeme'
  } catch {
    return 'systeme'
  }
}

function systemeEstSombre() {
  return typeof window !== 'undefined' && window.matchMedia('(prefers-color-scheme: dark)').matches
}

// Thème clair / sombre / système (suit les réglages de l'appareil, en direct).
export function ThemeProvider({ children }) {
  const [mode, setMode] = useState(lireMode)
  const [sombreSysteme, setSombreSysteme] = useState(systemeEstSombre)

  useEffect(() => {
    const requete = window.matchMedia('(prefers-color-scheme: dark)')
    const surChangement = (e) => setSombreSysteme(e.matches)
    requete.addEventListener('change', surChangement)
    return () => requete.removeEventListener('change', surChangement)
  }, [])

  const themeApplique = mode === 'systeme' ? (sombreSysteme ? 'sombre' : 'clair') : mode

  useEffect(() => {
    document.documentElement.dataset.theme = themeApplique
  }, [themeApplique])

  const changerMode = useCallback((nouveau) => {
    if (!MODES.includes(nouveau)) return
    setMode(nouveau)
    try {
      localStorage.setItem(CLE_THEME, nouveau)
    } catch {
      // stockage indisponible : le choix reste valable pour la session
    }
  }, [])

  const valeur = useMemo(() => ({ mode, themeApplique, changerMode }), [mode, themeApplique, changerMode])

  return <ThemeContext.Provider value={valeur}>{children}</ThemeContext.Provider>
}
