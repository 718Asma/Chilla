import { Compass } from 'lucide-react'
import { Link } from 'react-router-dom'
import EtatVide from '../components/ui/EtatVide'

function NotFoundPage() {
  return (
    <div className="page">
      <EtatVide
        icone={Compass}
        titre="Page introuvable"
        texte="La page que vous cherchez n'existe pas ou a été déplacée."
        action={
          <Link to="/projets" className="bouton bouton--primaire bouton--m">
            <span>Retour aux projets</span>
          </Link>
        }
      />
    </div>
  )
}

export default NotFoundPage
