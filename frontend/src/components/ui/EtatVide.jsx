function EtatVide({ icone: Icone, titre, texte, action }) {
  return (
    <div className="etat-vide">
      {Icone && (
        <div className="etat-vide__icone">
          <Icone size={28} aria-hidden="true" />
        </div>
      )}
      <h3>{titre}</h3>
      {texte && <p>{texte}</p>}
      {action}
    </div>
  )
}

export default EtatVide
