Client -> Interface et gestion interface

ClientUtils:
Établir une connexion au serveur.
Envoyer des messages (soit des messages de chat, soit des messages système comme une déconnexion ou une demande de contact).
Recevoir des messages envoyés par le serveur.
Gérer la fermeture des ressources réseau.

Modeles: format de message et utilisateurs

Server:
Écoute les connexions entrantes des clients sur un port prédéfini (12345).
Gère chaque client dans un thread séparé grâce à un pool de threads (ExecutorService).
Stocke les clients connectés dans une HashMap pour les identifier par leur nom d'utilisateur.
Relaye les messages entre clients et traite les messages système (déconnexion, demandes de contact, indicateurs de saisie).
