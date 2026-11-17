import { api } from './client'

export const authApi = {
  inscrire: (donnees) => api.post('/auth/inscription', donnees),
  connecter: (donnees) => api.post('/auth/connexion', donnees),
  moi: () => api.get('/auth/moi'),
}
