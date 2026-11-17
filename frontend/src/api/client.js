import axios from 'axios'

// Seul fichier qui connaît l'URL du backend et la manière d'envoyer une requête.
export const API_URL = import.meta.env.VITE_API_URL || '/api'
export const CLE_TOKEN = 'chilla_token'

let gestionnaireNonAutorise = null

// AuthContext enregistre ici la fonction appelée quand le jeton est refusé (401).
export function definirGestionnaireNonAutorise(fn) {
  gestionnaireNonAutorise = fn
}

export function lireToken() {
  try {
    return localStorage.getItem(CLE_TOKEN)
  } catch {
    return null
  }
}

const instance = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
})

instance.interceptors.request.use((config) => {
  const token = lireToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  (reponse) => reponse,
  (error) => {
    const statut = error.response?.status
    let message
    if (error.response) {
      const donnees = error.response.data
      message = donnees?.erreur ?? (typeof donnees === 'string' && donnees ? donnees : `Erreur HTTP ${statut}`)
    } else if (error.code === 'ECONNABORTED') {
      message = 'Le serveur met trop de temps à répondre. Réessayez dans un instant.'
    } else {
      message = 'Impossible de joindre le serveur. Vérifiez votre connexion et que le backend est démarré.'
    }

    const erreur = new Error(message)
    erreur.status = statut

    const url = error.config?.url ?? ''
    const routePublique = url.startsWith('/auth/connexion') || url.startsWith('/auth/inscription')
    if (statut === 401 && !routePublique && gestionnaireNonAutorise) {
      gestionnaireNonAutorise()
    }
    return Promise.reject(erreur)
  },
)

async function requete(config) {
  const reponse = await instance.request(config)
  if (reponse.status === 204 || reponse.data === '' || reponse.data === undefined) {
    return null
  }
  return reponse.data
}

export const api = {
  get: (path) => requete({ method: 'get', url: path }),
  post: (path, body) => requete({ method: 'post', url: path, data: body }),
  put: (path, body) => requete({ method: 'put', url: path, data: body }),
  // Le corps est optionnel (nécessaire pour supprimer un compte avec son mot de passe).
  delete: (path, body) => requete({ method: 'delete', url: path, data: body }),
}
