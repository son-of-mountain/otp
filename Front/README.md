# Interface utilisateur: gestion de la table USER

Petite application front-end statique (HTML/CSS/JS) qui montre une liste d'utilisateurs et un formulaire de détail.

Fonctionnalités principales :
- Liste des utilisateurs (table)
- Bouton "+ Nouveau" pour créer un utilisateur
- Clic sur une ligne : affiche les détails en lecture
- Bouton "Modifier" pour rendre le formulaire éditable
- Sauvegarde persistée via `localStorage` (mock côté client)

Comment l'utiliser :
1. Ouvrir `index.html` dans votre navigateur (double-cliquer ou via un petit serveur statique).
2. Cliquer sur "+ Nouveau" pour créer un utilisateur.
3. Cliquer sur une ligne pour voir les détails. Cliquer sur "Modifier" pour éditer et "Enregistrer" pour sauvegarder.

Notes importantes :
- L'application fonctionne entièrement côté client : aucun backend n'est nécessaire. L'application démarre avec une liste vide et les utilisateurs que vous créez sont conservés localement dans le navigateur via `localStorage`.

- Pour vider ou réinitialiser les données stockées, ouvrez la console devtools et exécutez :
	`localStorage.removeItem('users_list_v1')`

Si vous souhaitez ultérieurement connecter cette interface à une API REST (Node/Express, Spring, etc.), je peux ajouter les appels réseau et un petit backend de test.
