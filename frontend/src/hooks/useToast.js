import { useContext } from 'react'
import { ToastContext } from '../context/ToastContext'

export function useToast() {
  const contexte = useContext(ToastContext)
  if (!contexte) throw new Error('useToast doit être utilisé dans <ToastProvider>')
  return contexte
}
