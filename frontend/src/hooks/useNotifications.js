import { useContext } from 'react'
import { NotificationsContext } from '../context/NotificationsContext'

export function useNotifications() {
  const contexte = useContext(NotificationsContext)
  if (!contexte) throw new Error('useNotifications doit être utilisé dans <NotificationsProvider>')
  return contexte
}
