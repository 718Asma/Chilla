import { useCallback, useEffect, useRef, useState } from 'react'
import { useTempsReel } from './useTempsReel'

// Brique commune des hooks de chargement : { donnees, chargement, erreur, recharger }.
// - chargement n'est vrai que pour le premier chargement (ou un rechargement explicite) ;
// - recharger({ silencieux: true }) met à jour sans afficher de spinner ni d'erreur ;
// - les données ne changent (donc rien ne se redessine) que si le contenu a vraiment changé ;
// - modifierLocalement() permet les mises à jour optimistes (glisser-déposer, etc.).
export function useRessource(
  chargeur,
  dependances = [],
  { initial = null, actif = true, tempsReel = true, intervalle } = {},
) {
  const [donnees, setDonnees] = useState(initial)
  const [chargement, setChargement] = useState(actif)
  const [erreur, setErreur] = useState(null)

  const chargeurRef = useRef(chargeur)
  const compteur = useRef(0)

  useEffect(() => {
    chargeurRef.current = chargeur
  })

  const recharger = useCallback(async ({ silencieux = false } = {}) => {
    compteur.current += 1
    const monNumero = compteur.current
    try {
      if (!silencieux) setChargement(true)
      const resultat = await chargeurRef.current()
      if (monNumero !== compteur.current) return
      setDonnees((avant) => (JSON.stringify(avant) === JSON.stringify(resultat) ? avant : resultat))
      setErreur(null)
    } catch (e) {
      if (monNumero !== compteur.current) return
      if (!silencieux) setErreur(e.message)
    } finally {
      if (monNumero === compteur.current) setChargement(false)
    }
  }, [])

  const cle = JSON.stringify(dependances)
  useEffect(() => {
    if (actif) {
      recharger()
    } else {
      setChargement(false)
    }
  }, [actif, cle, recharger])

  const rechargerTempsReel = useCallback((options) => (actif ? recharger(options) : undefined), [actif, recharger])
  useTempsReel(rechargerTempsReel, { actif: actif && tempsReel, ...(intervalle !== undefined && { intervalle }) })

  const modifierLocalement = useCallback((transformation) => setDonnees((avant) => transformation(avant)), [])

  return { donnees, chargement, erreur, recharger, modifierLocalement }
}
