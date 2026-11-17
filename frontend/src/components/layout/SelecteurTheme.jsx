import { Monitor, Moon, Sun } from 'lucide-react'
import { useTheme } from '../../hooks/useTheme'
import MenuDeroulant from '../ui/MenuDeroulant'

const CHOIX = [
  { mode: 'clair', libelle: 'Clair', icone: Sun },
  { mode: 'sombre', libelle: 'Sombre', icone: Moon },
  { mode: 'systeme', libelle: 'Système', icone: Monitor },
]

function SelecteurTheme() {
  const { mode, themeApplique, changerMode } = useTheme()
  const IconeCourante = themeApplique === 'sombre' ? Moon : Sun

  return (
    <MenuDeroulant
      largeur="190px"
      declencheur={({ basculer, props }) => (
        <button type="button" className="bouton-icone" onClick={basculer} aria-label="Changer de thème" title="Thème" {...props}>
          <IconeCourante size={20} aria-hidden="true" />
        </button>
      )}
    >
      {(fermer) => (
        <div role="radiogroup" aria-label="Thème de l'interface">
          <p className="menu__titre">Apparence</p>
          {CHOIX.map(({ mode: valeur, libelle, icone: Icone }) => (
            <button
              key={valeur}
              type="button"
              role="radio"
              aria-checked={mode === valeur}
              className={`menu__element${mode === valeur ? ' menu__element--actif' : ''}`}
              onClick={() => {
                changerMode(valeur)
                fermer()
              }}
            >
              <Icone size={16} aria-hidden="true" />
              {libelle}
              {valeur === 'systeme' && <span className="menu__indice">auto</span>}
            </button>
          ))}
        </div>
      )}
    </MenuDeroulant>
  )
}

export default SelecteurTheme
