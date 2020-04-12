package fonctionnement;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import communication.Serveur_ProxyCom;

public class ServeurProxy implements Runnable {
	// Declaration et initialisation des variables
	Socket socket;
	Serveur_ProxyCom serveur;
	private int portServer;

	// Constructeur prenant en parametres un socket, un serveur et un entier
	public ServeurProxy(Socket socket, Serveur_ProxyCom serveur,int s) {
		super();
		this.socket = socket;
		this.serveur = serveur;
		this.portServer=s;
	}

	// Methode permettant de verifier si le serveur telecharge plus qu'il n'envoie, en dessous d'un tiers on bloc le telechargement
	public boolean ratiosComputation(int socketPort) {
		// Declaration du booleen et recuperation des informations cles
		boolean ckeck = true;
		int upload=serveur.tracker.get(socketPort).getNbDestination();
		int download=serveur.tracker.get(socketPort).getNbEmission();
		int total = upload+download;
		// Pour que le calcul se fasse, il est interessant que celui-ci ait lieu sur au moins 8 echanges
		// Cela permet d'avoir une reelle idee quant a l'activite d'une application
		if(upload+download>=8) {
			if(upload/total<=0.33) {
				ckeck=false;
			}
		}
		return ckeck;
	}

	// Methode qui permet de gerer les demandes get au niveau du tracker pour les demandes de paquets
	public Map <Integer, Map <String,  ArrayList<Integer>>> handleTheGet(int socketPort, String fileName, String packetNumber,String portDestination) {
		// Une ecriture a la fois
		synchronized(serveur.registry) {
			// Si le nom du fichier correspond deja a un port du socket
			if(serveur.registry.get(socketPort).containsKey(fileName)) {
				// On ajoute alors le numero du paquet dans l'arraylist
				serveur.registry.get(socketPort).get(fileName).add(Integer.parseInt(packetNumber));
			}else {
				// Sinon on ajoute une nouvelle entree
				Map <String, ArrayList<Integer>> serverregistry = new HashMap<String, ArrayList<Integer>>();
				serverregistry.put(fileName, new ArrayList<Integer>());
				serveur.registry.get(socketPort).putAll(serverregistry);
			}
			// Appel a une methode permettant de mettre les valeurs a jour
			serveur.updateTrackerValues(String.valueOf(socketPort),portDestination);
			return serveur.registry;
		}
	}

	// Methode qui permet de creer la liste des serveurs avec les fichiers et leur blocs
	public void creationListeFichier(int port,String nom,String bloc) throws IOException {
		// Declaration et initialisation des variables
		ArrayList<Integer> block=creationChaine(bloc);

		// Si le socket est deja connu dans la liste 
		if (serveur.registry.containsKey(port)) {
			if (serveur.registry.get(port).containsKey(nom)) { 
				// Si le registre connait deja le serveur et le fichier alors on ajoute les numeros de blocs
				serveur.registry.get(port).get(nom).addAll(block);
				// On enleve les potentiels doublons
				Set<Integer> set = new HashSet<Integer>() ;
				set.addAll(serveur.registry.get(port).get(nom)) ;
				serveur.registry.get(port).get(nom).clear();
				serveur.registry.get(port).get(nom).addAll(set);
			}
			else {
				// Si le registre connait deja le serveur mais pas le fichier on ajoute une nouvelle entree de map
				serveur.registry.get(port).put(nom,block);
			}
		} else { 
			// Si le registre ne connait ni le serveur ni le fichier alors on cree une entree de map pour le serveur
			serveur.registry.put(port, new HashMap<String,ArrayList<Integer>>());

			// On cree une entree de map pour le fichier avec les bon numeros de blocs
			serveur.registry.get(port).put(nom,block);
		}
	}

	// Methode qui retourne le nombre de blocs sous format [1,2,x,x]
	private ArrayList<Integer> creationChaine(String Bloc) {
		// Declaration et initialisation des variables
		ArrayList<Integer> nbBlockBonFormat=new ArrayList<Integer>();
		// On parcourt et on ajoute chaque bloc dans la l'arraylist
		for(int i =1; i<Integer.parseInt(Bloc)+1;i++) {
			nbBlockBonFormat.add(i);
		}
		return nbBlockBonFormat;
	}

	// Methode qui permet de gerer les demandes get au niveau du tracker pour les demandes de plage de paquets
	public Map <Integer, Map <String,  ArrayList<Integer>>> handleTheGet(int socketPort, String fileName, String packetNumber1, String packetNumber2,String portdestination) {
		// Une ecriture a la fois
		synchronized(serveur.registry) { 
			// Declaration et initialisation des variables
			int packet1 = Integer.parseInt(packetNumber1);
			int packet2 = Integer.parseInt(packetNumber2);

			// Si le nom du fichier correspond deja a un port du socket
			if(serveur.registry.get(socketPort).containsKey(fileName)) {
				for(int i = packet1; i<= packet2;i++) {
					// On lui ajoute les blocs de la plages qui lui correspondent
					serveur.registry.get(socketPort).get(fileName).add(i);
				}		
			}else {
				// Sinon on cree une entree
				Map <String, ArrayList<Integer>> serverregistry = new HashMap<String, ArrayList<Integer>>();
				serverregistry.put(fileName, new ArrayList<Integer>());
				serveur.registry.get(socketPort).putAll(serverregistry);		
			}
			// Appel a la methode de mise a jour des informations
			serveur.updateTrackerValues(String.valueOf(socketPort),portdestination);
			return serveur.registry;
		}
	}

	// Methode de creation du menu
	private void menu(Socket sss) throws Exception { 
		// Declaration et initialisation des variables
		BufferedReader entreeSocket;
		entreeSocket = new BufferedReader(new InputStreamReader(sss.getInputStream()));
		String chaine="";
		String[] var;
		do {
			// Lecture d'une chaine envoyee par le client
			while((chaine=entreeSocket.readLine())==null);
			System.out.println("message recu : "+chaine);
			// Le format du get attendu est le suivant : "get nomFichier 1" ou "get nomFichier 1 4"
			// Si ce n'est pas la fin desiree par l'utilisateur alors on separe la commande suivant les espaces et on analyse le premier mot
			if (!chaine.equals("FIN")) {
				if (chaine.contains(" "))var=chaine.split(" ");
				else {
					var=new String[1];
					var[0]=chaine;
				}
				switch(var[0]) {
				// Dans le cas d'un get
				case "get":
					if (var.length==3) {
						handleTheGet(portServer,var[1],var[2],var[3]);
					}
					if (var.length==4) {
						handleTheGet(portServer,var[1],var[2],var[3],var[4]);
					}
					break;
				// Dans le cas d'un search
				case "search":
					if(var.length>=3)throw new Exception("invalid parameter number");
					search(var[1]);
					break;
				// Dans le cas d'un isFichier
				case "isFichier" :
					if (var.length>2) throw new Exception("invalid parameter number");
					isFichier(var[1],sss);
					break;
				// Dans le cas d'un fichierDispo
				case "fichierDispo":
					if (var.length>3)throw new Exception("invalid parameter number");
					creationListeFichier(sss.getPort(),var[1],var[2]);
					break;
				// Dans le cas d'un maj
				case "maj":
					serveur.ServerArray(new DataOutputStream(sss.getOutputStream()));
					break;
				// Dans le cas d'un ls
				case "ls":
					ls();
					break;
				// Dans le cas d'un ChangeRoot
				case "ChangeRoot":
					eraseMemoryOfFile(var[1]);
					break;
				}
				var=null;
			}
		}while (!chaine.equals("FIN"));
		// On se deconnecte
		serveur.deconnexion(portServer);
	}

	// Methode qui efface la memoire du fichier
	private void eraseMemoryOfFile(String port) {
		// Si on trouve une correspondance alors on ecraser par une nouvelle hashmap par dessus l'ancienne
		for (Map.Entry<Integer, Map<String,ArrayList<Integer>>> entry : serveur.registry.entrySet()) {
			if (entry.getKey()==Integer.parseInt(port))serveur.registry.get(entry.getKey()).clear();
		}
	}

	// Methode permettant d'affichier 
	private void ls() throws IOException {
		// Declaration et initialisation des variables
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		ArrayList<String> check = new ArrayList<String>();	
		// Pour tous les serveurs
		for (Map.Entry<Integer, Map<String,ArrayList<Integer>>> entry : serveur.registry.entrySet()) {	
			// Pour tous les fichiers references dans le serveur
			for (Map.Entry<String,ArrayList<Integer>> entry2 : entry.getValue().entrySet()) {
				if (!check.contains(entry2.getKey()))check.add(entry2.getKey());
			}
		}
		// On fait appel a la methode decrite ci-dessous
		writeArrayList(os,check);
	}

	// Methode ecrivant le cotenu de l'arraylist passer en parametre dans le flux de donnees en sortie
	public void writeArrayList(DataOutputStream os,ArrayList ar) throws IOException {
		// Declaration et initialisation des variables
		String chaine="";
		String pass = ar.toString();
		String pass2=pass.substring(1, pass.length()-1);
		String[] pass3;
		// On separe le cotenu de l'arraylist apres un toString()
		if (pass2.contains(","))pass3=pass2.split(",");
		else {
			pass3=new String[1];
			pass3[0]=pass2;
		}
		for (String s : pass3)chaine+=s+" ";
		System.out.println(chaine);
		// Envoi dans le stream
		os.writeUTF(chaine);
	}

	// Methode qui recherche dans le registre quel serveur detient le fichier demande et renvoi la liste avec les paquets que chacun des serveurs detient
	private void search(String string) throws IOException {
		// Declaration et initialisation des variables
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		// Si les ratios sont mauvais alors on ecrit -2
		if(!ratiosComputation(portServer)){
			os.writeInt(-2);
		}else {
			// Sinon pour tous les serveurs
			for (Map.Entry<Integer, Map<String,ArrayList<Integer>>> entry : serveur.registry.entrySet()) {
				// Et pour tous les fichiers references dans le serveur
				for (Map.Entry<String,ArrayList<Integer>> entry2 : entry.getValue().entrySet()) {
					// Si le nom du fichier correspond alors on envoie le num�ro associ� au serveur et les blocs contenus
					if (entry2.getKey().matches(string)) {
						os.writeInt(entry.getKey());
						writeArrayList(os,entry2.getValue());
						System.out.println("j'ai envoye "+entry.getKey()+" "+entry2.getValue());
					}
				}

			}
		}
		// Si aucun serveur n'a le fichier on le signal en envoyant -1 au serveur qui demande
		os.writeInt(-1);
	}

	// Methode permettant de savoir si le fichier existe deja dans la liste
	public void isFichier(String nomF, Socket sss) throws IOException{
		// Declaration et initialisation des variables
		DataOutputStream os = new DataOutputStream(sss.getOutputStream());
		// Pour tous les serveurs
		for (Map.Entry<Integer, Map<String,ArrayList<Integer>>> entry : serveur.registry.entrySet()) {
			// Pour tous les fichiers references dans le serveur
			for (Map.Entry<String,ArrayList<Integer>> entry2 : entry.getValue().entrySet()) {
				// Si on a une correspondance on envoie 1 sinon 0
				if (entry2.getKey().matches(nomF)) {
					os.writeInt(1);
					return;
				}
			}
		}
		os.writeInt(0);
	}

	// Methode permettant de lancer le menu
	@Override
	public void run() {
		// TODO Auto-generated method st ub
		try {
			this.menu(socket);
		} catch (Exception e) {
			// Deconnexion
			if (e.getClass().equals(SocketException.class))serveur.deconnexion(portServer);
			else this.run();
		}
	}
}