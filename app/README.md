
# ReadyMapeo - Application Android

Voici une application mobile Android permettant de consulter, d'ajouter et de synchroniser une liste de clubs du site ReadyMapeo. Elle est conçue pour fonctionner même hors-ligne grâce à une base de données locale SQLite.

## Fonctionnalités Principales

* **Consultation hors-ligne :** Visualisation de la liste complète des clubs.
* **Synchronisation intelligente :** Mise à jour bidirectionnelle des données avec le serveur (récupération des nouveautés et envoi des données locales).
* **Indicateurs visuels :** Suivi de l'état des données locales avec des badges ("Non synchronisé", "En attente d'acceptation").

## Règles d'Ajout de Club (Important)

L'écosystème de l'application est protégé pour garantir l'intégrité des données :

1. **Authentification obligatoire :** Si la consultation de la liste est libre, **il est nécessaire de se connecter** (via l'onglet *Connexion*) avec un compte adhérent pour pouvoir envoyer l'ajout d'un nouveau club vers le serveur.
2. **Processus de validation :** Lorsqu'un utilisateur ajoute un club, celui-ci n'est pas immédiatement public. Il est stocké localement, envoyé à l'API lors de la synchronisation, puis **doit être impérativement validé par l'administrateur du site web** pour devenir officiel et apparaître de manière permanente dans la liste partagée.
