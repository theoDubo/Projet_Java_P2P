Ce document est produit dans le cadre de la formation STRI � l'�cole d'ing�nieurs UPSSITECH de Toulouse en France. 
Avant d'utiliser le produit associ� il est n�cessaire de lire ce document dans son int�gralit� :

Ex�cution du code******************************************************************************

Afin de faire fonctionner notre syst�me, les �tapes suivantes doivent �tre ex�cut�es dans l'ordre :
    - lancer le code se trouvant dans serveurProxy_com.java (/src/communication/) : Un message signifiant que le serveur est en attente doit alors apparaitre
    - lancer le code se trouvant dans userPPP.java (/src/IHM/) : Il vous est alors demand� si vous voulez un autre dossier que celui automatiquement fournit
(/dossierServeur/). Pour cela le dossier doit au pr�alable �tre cr�er dans le dossier racine de fonctionnement (/). Il faut ensuite noter le chemin en entier
depuis le dossier racine (ex : nous avons un dossier Serveur nous tapons alors ./Serveur/). 
    - il est ensuite demand� le port sur lequel on souhaite se connecter. Les connexions se font en localHost mais avec quelques ajustements du code il est possible
de recevoir des demandes de machines distantes. 
    - r�ex�cuter un userPPP si vous souhaiter discuter avec une autre machine.
    - vous pouvez � partir de maintenant executer plusieurs demandes telles que get, ls, ou un changement de racine. 

L'avenir possible ******************************************************************************

A l'heure actuelle le code ne permet pas d'acc�der � des machines distantes, une future am�lioration peut �tre envisag�e. 
La mise �jour n'est pas effectu�e en cas d'ajout ou de suppression de fichiers sur les serveurs.

A noter ****************************************************************************************

Le proxy doit rester en fonction pendant toute l'utilisation du syst�me. Si celui-ci s'arr�te alors le syst�me n'est plus fonctionnel et doit �tre relanc�. 
Le projet pr�sent� a �t� effectu� dans un temps limit� avec des contraintes et ne peut donc rendre compte d'un travail pouss� et aboutit. Mais il peut n�anmoins
pr�senter un travail fonctionnel dans les limites pr�sent�es ci dessus. 
