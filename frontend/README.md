# Chilla : frontend

React (Vite, JavaScript) + react-router-dom + axios + lucide-react.
Le flux temps réel (SSE) utilise `@microsoft/fetch-event-source` car `EventSource` ne permet pas d'envoyer l'en-tête `Authorization`.

## Lancer

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
```

Le backend doit tourner sur le port **8081** : Vite relaie `/api/...` vers `http://localhost:8081/...` (voir `vite.config.js`).
Pour viser un autre serveur, copiez `.env.example` en `.env` et renseignez `VITE_API_URL`.

## Architecture (guide de la professeure)

```
pages -> hooks -> api -> axios -> backend
```

- `src/api/` : seul endroit qui connaît l'URL et axios (`client.js` ajoute le jeton JWT et gère le 401).
- `src/hooks/` : chargement (`useRessource`), temps réel (`useTempsReel`), actions (`useAction`).
- `src/context/` : thème, authentification, toasts, notifications + flux SSE.
- `src/styles/` : toutes les couleurs sont dans `variables.css` (thèmes clair et sombre).

## Temps réel

Chaque notification reçue par le flux SSE, chaque action de l'utilisateur, chaque retour sur l'onglet
et un filet de sécurité toutes les 30 s déclenchent un rechargement silencieux des données affichées :
pas besoin de rafraîchir la page.

## Thème

Sélecteur dans la barre de navigation : clair, sombre ou système (suit l'appareil en direct).
