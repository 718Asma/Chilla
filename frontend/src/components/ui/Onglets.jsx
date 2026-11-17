// onglets : [{ id, libelle, icone, compteur }]
function Onglets({ onglets, actif, onChange, libelle = 'Sections' }) {
  const surTouche = (e, index) => {
    if (e.key !== 'ArrowRight' && e.key !== 'ArrowLeft') return
    e.preventDefault()
    const decalage = e.key === 'ArrowRight' ? 1 : -1
    const suivant = onglets[(index + decalage + onglets.length) % onglets.length]
    onChange(suivant.id)
    document.getElementById(`onglet-${suivant.id}`)?.focus()
  }

  return (
    <div className="onglets" role="tablist" aria-label={libelle}>
      {onglets.map((onglet, index) => {
        const Icone = onglet.icone
        const selectionne = onglet.id === actif
        return (
          <button
            key={onglet.id}
            id={`onglet-${onglet.id}`}
            type="button"
            role="tab"
            aria-selected={selectionne}
            aria-controls={`panneau-${onglet.id}`}
            tabIndex={selectionne ? 0 : -1}
            className={`onglet${selectionne ? ' onglet--actif' : ''}`}
            onClick={() => onChange(onglet.id)}
            onKeyDown={(e) => surTouche(e, index)}
          >
            {Icone && <Icone size={16} aria-hidden="true" />}
            <span>{onglet.libelle}</span>
            {onglet.compteur !== undefined && <span className="onglet__compteur">{onglet.compteur}</span>}
          </button>
        )
      })}
    </div>
  )
}

export default Onglets
