import { AlertTriangle, CalendarDays, CheckCircle2, MoreHorizontal, Pencil, Trash2, UserCog } from 'lucide-react'
import { ETATS_TACHE, ORDRE_ETATS_TACHE } from '../../utils/constantes'
import { estEnRetard, formaterDateCourte } from '../../utils/dates'
import { codeTache, nomComplet } from '../../utils/formatage'
import Avatar from '../ui/Avatar'
import MenuDeroulant from '../ui/MenuDeroulant'

function CarteTache({ tache, responsable, peutChangerEtat, peutModifier, estChef, glissee, onChangerEtat, onModifier, onAssigner, onSupprimer, onDebutGlisse, onFinGlisse }) {
  const enRetard = estEnRetard(tache)
  const termine = tache.etat === 'TERMINE'
  const annule = tache.etat === 'ANNULE'
  const aDesActions = peutChangerEtat || peutModifier || estChef

  return (
    <article
      className={`carte-tache${enRetard ? ' carte-tache--retard' : ''}${annule ? ' carte-tache--annulee' : ''}${glissee ? ' carte-tache--glissee' : ''}${peutChangerEtat ? ' carte-tache--deplacable' : ''}`}
      draggable={peutChangerEtat}
      onDragStart={(e) => {
        e.dataTransfer.effectAllowed = 'move'
        e.dataTransfer.setData('text/plain', String(tache.id))
        onDebutGlisse(tache)
      }}
      onDragEnd={onFinGlisse}
    >
      <header className="carte-tache__entete">
        <span className="carte-tache__code">{codeTache(tache.id)}</span>
        <span className="carte-tache__droite">
          <span className="carte-tache__points">
            {tache.estimation} pt{tache.estimation > 1 ? 's' : ''}
          </span>
          {aDesActions && (
            <MenuDeroulant
              largeur="220px"
              declencheur={({ basculer, props }) => (
                <button type="button" className="bouton-icone bouton-icone--petit" onClick={basculer} aria-label={`Actions pour ${tache.libelle}`} {...props}>
                  <MoreHorizontal size={16} aria-hidden="true" />
                </button>
              )}
            >
              {(fermer) => (
                <div>
                  {peutChangerEtat && (
                    <>
                      <p className="menu__titre">Déplacer vers</p>
                      {ORDRE_ETATS_TACHE.filter((e) => e !== tache.etat).map((etat) => (
                        <button
                          key={etat}
                          type="button"
                          className="menu__element"
                          onClick={() => {
                            fermer()
                            onChangerEtat(tache, etat)
                          }}
                        >
                          <span className="point-etat" style={{ background: ETATS_TACHE[etat].couleur }} aria-hidden="true" />
                          {ETATS_TACHE[etat].libelle}
                        </button>
                      ))}
                      <div className="menu__separateur" />
                    </>
                  )}
                  {peutModifier && (
                    <button type="button" className="menu__element" onClick={() => { fermer(); onModifier(tache) }}>
                      <Pencil size={16} aria-hidden="true" /> Modifier
                    </button>
                  )}
                  {estChef && (
                    <>
                      <button type="button" className="menu__element" onClick={() => { fermer(); onAssigner(tache) }}>
                        <UserCog size={16} aria-hidden="true" /> Réassigner
                      </button>
                      <button type="button" className="menu__element menu__element--danger" onClick={() => { fermer(); onSupprimer(tache) }}>
                        <Trash2 size={16} aria-hidden="true" /> Supprimer
                      </button>
                    </>
                  )}
                </div>
              )}
            </MenuDeroulant>
          )}
        </span>
      </header>

      <h4 className={`carte-tache__titre${termine || annule ? ' carte-tache__titre--barre' : ''}`}>{tache.libelle}</h4>
      {tache.description && <p className="carte-tache__description">{tache.description}</p>}

      {enRetard && (
        <p className="carte-tache__alerte">
          <AlertTriangle size={14} aria-hidden="true" /> Échéance dépassée
        </p>
      )}

      <footer className="carte-tache__pied">
        <span className="carte-tache__echeance">
          {termine ? <CheckCircle2 size={14} aria-hidden="true" /> : <CalendarDays size={14} aria-hidden="true" />}
          {formaterDateCourte(tache.dateEcheance)}
        </span>
        <span className="carte-tache__responsable">
          <span className="carte-tache__nom">{nomComplet(responsable)}</span>
          <Avatar membre={responsable ?? { id: tache.idMembre }} taille="s" />
        </span>
      </footer>
    </article>
  )
}

export default CarteTache
