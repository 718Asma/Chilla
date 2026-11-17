import { fetchEventSource } from '@microsoft/fetch-event-source'
import { createContext, useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { API_URL } from '../api/client'
import { normaliserNotification, notificationsApi } from '../api/notifications'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'

export const NotificationsContext = createContext(null)

class ErreurFatale extends Error {}

// Notifications + bus d'événements temps réel.
// - Le flux SSE pousse les nouvelles notifications (cloche, toasts).
// - Chaque événement (notification, reconnexion, action locale) est aussi diffusé aux hooks de
//   données (useTempsReel) qui se rechargent en silence : l'interface reste à jour sans F5.
export function NotificationsProvider({ children }) {
  const { token, deconnexion } = useAuth()
  const { afficherToast } = useToast()

  const [notifications, setNotifications] = useState([])
  const [nonLues, setNonLues] = useState(0)
  const [chargement, setChargement] = useState(true)
  const [erreur, setErreur] = useState(null)
  const [connecte, setConnecte] = useState(false)

  const ecouteurs = useRef(new Set())
  const dejaConnecte = useRef(false)

  const abonner = useCallback((fonction) => {
    ecouteurs.current.add(fonction)
    return () => ecouteurs.current.delete(fonction)
  }, [])

  const diffuser = useCallback((type, donnees) => {
    ecouteurs.current.forEach((fonction) => fonction(type, donnees))
  }, [])

  // À appeler après une action de l'utilisateur : tout ce qui est affiché se met à jour.
  const signaler = useCallback(() => diffuser('local'), [diffuser])

  const charger = useCallback(async (silencieux = false) => {
    try {
      if (!silencieux) setChargement(true)
      const liste = await notificationsApi.lister()
      setNotifications(liste)
      setNonLues(liste.filter((n) => !n.lue).length)
      setErreur(null)
    } catch (e) {
      if (!silencieux) setErreur(e.message)
    } finally {
      setChargement(false)
    }
  }, [])

  // Historique au chargement, remis à zéro à la déconnexion.
  useEffect(() => {
    if (token) {
      charger()
    } else {
      setNotifications([])
      setNonLues(0)
    }
  }, [token, charger])

  // Flux temps réel (Server-Sent Events) : un seul flux par session, fermé à la déconnexion.
  useEffect(() => {
    if (!token) return undefined
    const controleur = new AbortController()
    dejaConnecte.current = false

    fetchEventSource(`${API_URL}/notifications/flux`, {
      signal: controleur.signal,
      headers: { Authorization: `Bearer ${token}` },
      openWhenHidden: true,

      async onopen(reponse) {
        const type = reponse.headers.get('content-type') ?? ''
        if (reponse.ok && type.includes('text/event-stream')) {
          setConnecte(true)
          return
        }
        if (reponse.status === 401) {
          throw new ErreurFatale('Session expirée')
        }
        if (reponse.status >= 400 && reponse.status < 500 && reponse.status !== 429) {
          throw new ErreurFatale(`Flux refusé (${reponse.status})`)
        }
        throw new Error(`Flux indisponible (${reponse.status})`)
      },

      onmessage(message) {
        if (message.event === 'connecte') {
          try {
            setNonLues(JSON.parse(message.data).nonLues ?? 0)
          } catch {
            // donnée illisible : le compteur sera recalculé au prochain chargement
          }
          // Après une coupure, on rattrape ce qui a été manqué.
          if (dejaConnecte.current) {
            charger(true)
            diffuser('connecte')
          }
          dejaConnecte.current = true
        } else if (message.event === 'notification') {
          let nouvelle = null
          try {
            nouvelle = normaliserNotification(JSON.parse(message.data))
          } catch {
            return
          }
          if (!nouvelle) return
          setNotifications((liste) => (liste.some((n) => n.id === nouvelle.id) ? liste : [nouvelle, ...liste]))
          if (!nouvelle.lue) setNonLues((n) => n + 1)
          afficherToast({ type: 'notification', titre: 'Nouvelle notification', message: nouvelle.message, lien: '/notifications' })
          diffuser('notification', nouvelle)
        }
      },

      onclose() {
        setConnecte(false)
        throw new Error('Flux fermé') // on laisse fetchEventSource se reconnecter
      },

      onerror(e) {
        setConnecte(false)
        if (e instanceof ErreurFatale) throw e
        return 3000 // nouvel essai dans 3 s
      },
    }).catch((e) => {
      if (e instanceof ErreurFatale && e.message === 'Session expirée') {
        deconnexion(true)
      }
    })

    return () => {
      controleur.abort()
      setConnecte(false)
    }
  }, [token, afficherToast, charger, diffuser, deconnexion])

  const marquerLue = useCallback(
    async (id) => {
      const cible = notifications.find((n) => n.id === id)
      if (!cible || cible.lue) return
      setNotifications((liste) => liste.map((n) => (n.id === id ? { ...n, lue: true } : n)))
      setNonLues((n) => Math.max(0, n - 1))
      try {
        await notificationsApi.marquerLue(id)
      } catch (e) {
        afficherToast({ type: 'erreur', titre: 'Action impossible', message: e.message })
        charger(true)
      }
    },
    [notifications, afficherToast, charger],
  )

  const toutMarquerLu = useCallback(async () => {
    setNotifications((liste) => liste.map((n) => ({ ...n, lue: true })))
    setNonLues(0)
    try {
      await notificationsApi.toutMarquerLu()
    } catch (e) {
      afficherToast({ type: 'erreur', titre: 'Action impossible', message: e.message })
      charger(true)
    }
  }, [afficherToast, charger])

  const supprimer = useCallback(
    async (id) => {
      const cible = notifications.find((n) => n.id === id)
      setNotifications((liste) => liste.filter((n) => n.id !== id))
      if (cible && !cible.lue) setNonLues((n) => Math.max(0, n - 1))
      try {
        await notificationsApi.supprimer(id)
      } catch (e) {
        afficherToast({ type: 'erreur', titre: 'Suppression impossible', message: e.message })
        charger(true)
      }
    },
    [notifications, afficherToast, charger],
  )

  const valeur = useMemo(
    () => ({
      notifications,
      nonLues,
      chargement,
      erreur,
      connecte,
      recharger: charger,
      marquerLue,
      toutMarquerLu,
      supprimer,
      abonner,
      signaler,
    }),
    [notifications, nonLues, chargement, erreur, connecte, charger, marquerLue, toutMarquerLu, supprimer, abonner, signaler],
  )

  return <NotificationsContext.Provider value={valeur}>{children}</NotificationsContext.Provider>
}
