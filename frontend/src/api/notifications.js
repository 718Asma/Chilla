import { api } from './client'

// Le flux temps réel envoie l'objet brut du modèle Java (IDnotif, datecreanotif...).
// On le ramène à la même forme que les notifications de GET /notifications.
export function normaliserNotification(brute) {
  if (!brute) return null
  if (brute.id !== undefined) return brute
  return {
    id: brute.IDnotif,
    message: brute.message,
    dateCreation: brute.datecreanotif ?? brute.dateCreaNotif ?? null,
    lue: Boolean(brute.estlue),
    idMembre: brute.IDmem,
  }
}

export const notificationsApi = {
  lister: () => api.get('/notifications'),
  nonLues: () => api.get('/notifications/non-lues'),
  compterNonLues: () => api.get('/notifications/non-lues/nombre'),
  toutMarquerLu: () => api.post('/notifications/lues'),
  marquerLue: (id) => api.put(`/notifications/${id}/lue`),
  supprimer: (id) => api.delete(`/notifications/${id}`),
}
