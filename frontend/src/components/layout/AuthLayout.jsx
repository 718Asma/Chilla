import { Activity, LayoutList, Zap } from 'lucide-react'
import Logo from '../ui/Logo'
import SelecteurTheme from './SelecteurTheme'

const ARGUMENTS = [
  { icone: Activity, titre: 'Sprints interactifs', texte: 'Pilotez vos itérations avec des tableaux de bord clairs et des tâches fluides.' },
  { icone: LayoutList, titre: 'Backlog agile structuré', texte: 'Priorisez vos user stories et mesurez la charge de chaque membre.' },
  { icone: Zap, titre: 'Notifications temps réel', texte: 'Restez synchronisé : assignations et invitations arrivent instantanément.' },
]

// Mise en page partagée par les pages de connexion et d'inscription.
function AuthLayout({ children }) {
  return (
    <div className="auth">
      <div className="auth__theme">
        <SelecteurTheme />
      </div>
      <div className="auth__carte">
        <section className="auth__formulaire">
          <Logo taille={40} />
          {children}
        </section>
        <aside className="auth__promo" aria-label="Présentation de Chilla">
          <span className="auth__pastille">
            <span className="badge__point" aria-hidden="true" /> Plateforme agile collaborative
          </span>
          <h2>La sérénité d'un suivi agile pensé pour les équipes modernes.</h2>
          <ul>
            {ARGUMENTS.map(({ icone: Icone, titre, texte }) => (
              <li key={titre}>
                <span className="auth__icone">
                  <Icone size={18} aria-hidden="true" />
                </span>
                <span>
                  <strong>{titre}</strong>
                  <small>{texte}</small>
                </span>
              </li>
            ))}
          </ul>
        </aside>
      </div>
    </div>
  )
}

export default AuthLayout
