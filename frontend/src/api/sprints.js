import { api } from './client'

export const sprintsApi = {
  listerDuProjet: (idProjet) => api.get(`/projets/${idProjet}/sprints`),
  // Renvoie un sprint, ou { message } quand aucun sprint n'est en cours.
  enCours: (idProjet) => api.get(`/projets/${idProjet}/sprints/en-cours`),
  creer: (idProjet, sprint) => api.post(`/projets/${idProjet}/sprints`, sprint),
  obtenir: (id) => api.get(`/sprints/${id}`),
  modifier: (id, sprint) => api.put(`/sprints/${id}`, sprint),
  supprimer: (id) => api.delete(`/sprints/${id}`),
  changerEtat: (id, etat) => api.put(`/sprints/${id}/etat`, { etat }),
  demarrer: (id) => api.post(`/sprints/${id}/demarrer`),
  terminer: (id) => api.post(`/sprints/${id}/terminer`),
  annuler: (id) => api.post(`/sprints/${id}/annuler`),
  taches: (id) => api.get(`/sprints/${id}/taches`),
}
