import { api } from './client'

export const projetsApi = {
  lister: () => api.get('/projets'),
  obtenir: (id) => api.get(`/projets/${id}`),
  creer: (projet) => api.post('/projets', projet),
  modifier: (id, projet) => api.put(`/projets/${id}`, projet),
  supprimer: (id) => api.delete(`/projets/${id}`),
  archiver: (id) => api.post(`/projets/${id}/archiver`),
  desarchiver: (id) => api.post(`/projets/${id}/desarchiver`),
  quitter: (id) => api.post(`/projets/${id}/quitter`),

  membres: (id) => api.get(`/projets/${id}/membres`),
  role: (id, idMembre) => api.get(`/projets/${id}/membres/${idMembre}/role`),
  changerRole: (id, idMembre, role) => api.put(`/projets/${id}/membres/${idMembre}/role`, { role }),
  retirerMembre: (id, idMembre) => api.delete(`/projets/${id}/membres/${idMembre}`),

  inviter: (id, cible) => api.post(`/projets/${id}/invitations`, cible),
}
