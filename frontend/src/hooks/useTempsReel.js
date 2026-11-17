import { useEffect, useRef } from 'react'
import { INTERVALLE_ACTUALISATION } from '../utils/constantes'
import { useNotifications } from './useNotifications'

// Recharge des données en silence (sans clignotement) quand :
//  - une notification arrive par le flux temps réel,
//  - l'utilisateur fait une action (signaler()),
//  - le flux se reconnecte, l'onglet redevient visible ou la fenêtre reprend le focus,
//  - toutes les 30 s (filet de sécurité pour les changements qui ne génèrent pas de notification).
export function useTempsReel(recharger, { intervalle = INTERVALLE_ACTUALISATION, actif = true } = {}) {
  const { abonner } = useNotifications()
  const rechargerRef = useRef(recharger)

  useEffect(() => {
    rechargerRef.current = recharger
  }, [recharger])

  useEffect(() => {
    if (!actif) return undefined
    let minuteur = null
    const lancer = () => rechargerRef.current({ silencieux: true })

    const declencher = (type) => {
      clearTimeout(minuteur)
      // Une action de l'utilisateur est répercutée tout de suite ; le reste est regroupé.
      minuteur = setTimeout(lancer, type === 'local' ? 0 : 300)
    }

    const desabonner = abonner(declencher)
    const surRetour = () => {
      if (document.visibilityState === 'visible') declencher('focus')
    }
    document.addEventListener('visibilitychange', surRetour)
    window.addEventListener('focus', surRetour)
    const horloge = intervalle
      ? setInterval(() => {
          if (document.visibilityState === 'visible') lancer()
        }, intervalle)
      : null

    return () => {
      clearTimeout(minuteur)
      desabonner()
      document.removeEventListener('visibilitychange', surRetour)
      window.removeEventListener('focus', surRetour)
      if (horloge) clearInterval(horloge)
    }
  }, [abonner, intervalle, actif])
}
