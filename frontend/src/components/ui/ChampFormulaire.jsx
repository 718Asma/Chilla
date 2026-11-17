import { useId } from 'react'

// Champ avec label, aide et erreur. as : 'input' (défaut) | 'textarea' | 'select' (options en children).
// icone : icône Lucide à gauche ; droite : élément à droite (ex. bouton « afficher le mot de passe »).
function ChampFormulaire({ label, as: Balise = 'input', erreur, aide, icone: Icone, droite, compteur, className = '', children, ...reste }) {
  const id = useId()
  const idMessage = `${id}-msg`
  const message = erreur || aide

  return (
    <div className={`champ${erreur ? ' champ--erreur' : ''} ${className}`.trim()}>
      <div className="champ__ligne-label">
        <label htmlFor={id}>
          {label}
          {reste.required && (
            <span className="champ__requis" aria-hidden="true">
              {' '}
              *
            </span>
          )}
        </label>
        {compteur && <span className="champ__compteur">{compteur}</span>}
      </div>
      <div className={`champ__controle${Icone ? ' champ__controle--icone' : ''}`}>
        {Icone && <Icone size={18} className="champ__icone" aria-hidden="true" />}
        <Balise id={id} aria-invalid={erreur ? true : undefined} aria-describedby={message ? idMessage : undefined} {...reste}>
          {children}
        </Balise>
        {droite && <div className="champ__droite">{droite}</div>}
      </div>
      {message && (
        <p id={idMessage} className={erreur ? 'champ__erreur' : 'champ__aide'} role={erreur ? 'alert' : undefined}>
          {message}
        </p>
      )}
    </div>
  )
}

export default ChampFormulaire
