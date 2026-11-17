// Logo Chilla : « C » bleu arrondi et bulleux + « hilla », avec un halo doux.
function Logo({ taille = 36, texte = true, sousTitre }) {
  return (
    <span className="logo">
      <svg className="logo__marque" width={taille} height={taille} viewBox="0 0 48 48" role="img" aria-label="Chilla">
        <path
          d="M37 14.500C34.400 11.700 30.400 10 26.100 10 17.700 10 11 16.300 11 24s6.700 14 15.100 14c4.300 0 8.300-1.700 10.900-4.500"
          fill="none"
          stroke="currentColor"
          strokeWidth="9.500"
          strokeLinecap="round"
        />
        <path d="M17.500 19.500c1.500-3 4.200-4.800 7.600-4.800" fill="none" stroke="#fff" strokeWidth="2.200" strokeLinecap="round" opacity="0.55" />
        <circle cx="38" cy="24" r="3.200" fill="#fff" />
      </svg>
      {texte && (
        <span className="logo__texte">
          <span className="logo__nom">hilla</span>
          {sousTitre && <span className="logo__sous-titre">{sousTitre}</span>}
        </span>
      )}
    </span>
  )
}

export default Logo
