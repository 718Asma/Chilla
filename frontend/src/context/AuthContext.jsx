import { createContext, useCallback, useEffect, useMemo, useState } from 'react'
import { authApi } from '../api/auth'
import { CLE_TOKEN, definirGestionnaireNonAutorise, lireToken } from '../api/client'

export const AuthContext = createContext(null)

const CLE_MEMBRE = 'chilla_membre'

function lireMembre() {
  try {
    const brut = localStorage.getItem(CLE_MEMBRE)
    return brut ? JSON.parse(brut) : null
  } catch {
    return null
  }
}

function ecrireSession(token, membre) {
  try {
    if (token) localStorage.setItem(CLE_TOKEN, token)
    else localStorage.removeItem(CLE_TOKEN)
    if (membre) localStorage.setItem(CLE_MEMBRE, JSON.stringify(membre))
    else localStorage.removeItem(CLE_MEMBRE)
  } catch {
    // stockage indisponible : la session ne survivra pas à un rechargement
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(lireToken)
  const [membre, setMembre] = useState(lireMembre)
  const [initialisation, setInitialisation] = useState(Boolean(lireToken()))
  const [sessionExpiree, setSessionExpiree] = useState(false)

  const deconnexion = useCallback((expiree = false) => {
    ecrireSession(null, null)
    setToken(null)
    setMembre(null)
    setSessionExpiree(expiree === true)
  }, [])

  // Un 401 sur une route protégée : le jeton a expiré ou le compte n'existe plus.
  useEffect(() => {
    definirGestionnaireNonAutorise(() => deconnexion(true))
    return () => definirGestionnaireNonAutorise(null)
  }, [deconnexion])

  // Au démarrage, on vérifie le jeton stocké.
  useEffect(() => {
    if (!token) {
      setInitialisation(false)
      return undefined
    }
    let annule = false
    authApi
      .moi()
      .then((courant) => {
        if (annule) return
        setMembre(courant)
        ecrireSession(token, courant)
      })
      .catch(() => {
        // 401 : géré par le gestionnaire. Autre erreur (réseau) : on garde la session locale.
      })
      .finally(() => {
        if (!annule) setInitialisation(false)
      })
    return () => {
      annule = true
    }
  }, [token])

  const ouvrirSession = useCallback((session) => {
    ecrireSession(session.token, session.membre)
    setToken(session.token)
    setMembre(session.membre)
    setSessionExpiree(false)
    return session.membre
  }, [])

  const connexion = useCallback(async (email, motDePasse) => ouvrirSession(await authApi.connecter({ email, motDePasse })), [ouvrirSession])

  const inscription = useCallback(async (donnees) => ouvrirSession(await authApi.inscrire(donnees)), [ouvrirSession])

  const mettreAJourMembre = useCallback(
    (nouveau) => {
      setMembre(nouveau)
      ecrireSession(token, nouveau)
    },
    [token],
  )

  const valeur = useMemo(
    () => ({ token, membre, initialisation, sessionExpiree, connexion, inscription, deconnexion, mettreAJourMembre }),
    [token, membre, initialisation, sessionExpiree, connexion, inscription, deconnexion, mettreAJourMembre],
  )

  return <AuthContext.Provider value={valeur}>{children}</AuthContext.Provider>
}
