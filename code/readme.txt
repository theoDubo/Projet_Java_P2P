Ce document est produit dans le cadre de la formation STRI à l'école d'ingénieur UPSSITECH de Toulouse en France. 
Avant d'utiliser le produit associé il est nécessaire de lire ce document dans son intégralité :

Exécution du code******************************************************************************

Afin de faire fonctionner notre système les étapes suivantes doivent être exécutés dans l'ordre :
    - lancer le code se trouvant dans serveurProxy_com.java (/src/communication/) : Un message signifiant que le serveur est en attente doit alors apparaitre
    - lancer le code se trouvant dans userPPP.java (/src/IHM/) : Il vous est alors demandé si vous voulez un autre dossier que celui automatiquement fournie
(/dossierServeur/). Pour cela le dossier doit au préalable être créer dans le dossier racine de fonctionnement (/). Il faut ensuite noter le chemin en entier
depuis le dossier racine (ex : nous avons un dossier Serveur nous tapons alors ./Serveur/). 
    - il est ensuite demander le port sur lequel on souhaite se connecter. Les connexions se font en localHost mais avec quelques ajustements du code il est possible
de recevoir des demandes de machines distantes. 
    - réexécuter un userPPP si vous souhaiter discuter avec une autre machine.
    - vous pouvez à partir de là exécuter plusieurs demandes tel que get, ls, ou un changement de racine. 

L'avenir possible ******************************************************************************

À l'heure actuelle le code ne permet pas d'accéder à des machines distantes une future amélioration peut être envisagée. 
La mise à jour n'est pas effectuée en cas d'ajout ou de suppression de fichier sur les serveurs.

À noter ****************************************************************************************

Le proxy doit rester en fonction pendant toute l'utilisation du système. Si celui-ci s'arrête alors le système n'est plus fonctionnel et doit être relancé. 
Le projet présenté a été effectué dans un temps limité avec des contraintes et ne peut donc rendre compte d'un travail poussé et aboutit. Mais il peut néanmoins
présenter un travail fonctionnel dans les limites présentées ci dessus. 
