import { api } from './client'

export const tachesApi = {
  listerDuProjet: (idProjet) => api.get(`/projets/${idProjet}/taches`),
  creer: (idProjet, tache) => api.post(`/projets/${idProjet}/taches`, tache),
  obtenir: (id) => api.get(`/taches/${id}`),
  modifier: (id, tache) => api.put(`/taches/${id}`, tache),
  supprimer: (id) => api.delete(`/taches/${id}`),
  changerEtat: (id, etat) => api.put(`/taches/${id}/etat`, { etat }),
  assigner: (id, idMembre) => api.put(`/taches/${id}/assignation`, { idMembre }),
}
