import { api } from './client'

export const invitationsApi = {
  recues: () => api.get('/invitations'),
  enAttente: () => api.get('/invitations/en-attente'),
  obtenir: (id) => api.get(`/invitations/${id}`),
  annuler: (id) => api.delete(`/invitations/${id}`),
  accepter: (id) => api.post(`/invitations/${id}/accepter`),
  refuser: (id) => api.post(`/invitations/${id}/refuser`),
}
