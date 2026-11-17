import { api } from './client'

export const membresApi = {
  obtenir: (id) => api.get(`/membres/${id}`),
  modifier: (id, profil) => api.put(`/membres/${id}`, profil),
  changerMotDePasse: (id, donnees) => api.put(`/membres/${id}/mot-de-passe`, donnees),
  supprimer: (id, motDePasse) => api.delete(`/membres/${id}`, { motDePasse }),
  projets: (id) => api.get(`/membres/${id}/projets`),
  projetsActifs: (id) => api.get(`/membres/${id}/projets/actifs`),
  taches: (id) => api.get(`/membres/${id}/taches`),
}
