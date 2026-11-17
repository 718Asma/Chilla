import { useContext } from 'react'
import { ThemeContext } from '../context/ThemeContext'

export function useTheme() {
  const contexte = useContext(ThemeContext)
  if (!contexte) throw new Error('useTheme doit être utilisé dans <ThemeProvider>')
  return contexte
}
