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

	Socket socket;
	Serveur_ProxyCom serveur;
	private int portServer;

	//------------------------------------------------------------------------------------------------Constructeur
	//*** permer la creation de l'objet

	public ServeurProxy(Socket socket, Serveur_ProxyCom serveur,int s) {
		super();
		this.socket = socket;
		this.serveur = serveur;
		this.portServer=s;
	}



	//-----------------------------------------------------------------------------------------------RatioComputation
	//*** check si le serveur telecharge plus qu'il n'envoi, en dessous d'un tiers
	//*** blockage du telechargement

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

	//---------------------------------------------------------------------------------------------Methode HandleTheGet packet	
	//*** permet de gerer les demandes get au niveau du tracker pour les demandes de paquets

	public Map <Integer, Map <String,  ArrayList<Integer>>> handleTheGet(int socketPort, String fileName, String packetNumber,String portDestination) {
		synchronized(serveur.registry) { //une ecriture a la fois
			if(serveur.registry.get(socketPort).containsKey(fileName)) {
				serveur.registry.get(socketPort).get(fileName).add(Integer.parseInt(packetNumber));
			}else {
				Map <String, ArrayList<Integer>> serverregistry = new HashMap<String, ArrayList<Integer>>();
				serverregistry.put(fileName, new ArrayList<Integer>());
				serveur.registry.get(socketPort).putAll(serverregistry);
			}
			serveur.updateTrackerValues(String.valueOf(socketPort),portDestination);
			return serveur.registry;
		}
	}


	//-----------------------------------------------------------------------------------------------Methode listeFichier
	//*** Permet de creer la liste des serveurs avec les fichiers et leur blocs

	public void creationListeFichier(int port,String nom,String bloc) throws IOException {

		ArrayList<Integer> block=creationChaine(bloc);
		if (serveur.registry.containsKey(port)) {
			if (serveur.registry.get(port).containsKey(nom)) { 

				//si le registre connait deja le serveur et le fichier on ajoute les numeros de blocs
				serveur.registry.get(port).get(nom).addAll(block);

				//on enleve les potentiels doublons
				Set<Integer> set = new HashSet<Integer>() ;
				set.addAll(serveur.registry.get(port).get(nom)) ;
				serveur.registry.get(port).get(nom).clear();
				serveur.registry.get(port).get(nom).addAll(set);
			}
			else {
				// si le registre connait deja le serveur mais pas le fichier on ajoute une nouvelle entree de map
				serveur.registry.get(port).put(nom,block);
			}
		} else { // si le registre ne connait ni le serveur ni le fichier donc
			//on cree une entree de map pour le serveur
			serveur.registry.put(port, new HashMap<String,ArrayList<Integer>>());

			//on cree une entree de map pour le fichier avec les bon numeros de blocs
			serveur.registry.get(port).put(nom,block);
		}
	}


	//-----------------------------------------------------------------------------------------------Creation de chaine
	//Retourne le nombre de block sous format [1,2,x,x]
	private ArrayList<Integer> creationChaine(String Bloc) {
		ArrayList<Integer> nbBlockBonFormat=new ArrayList<Integer>();
		for(int i =1; i<Integer.parseInt(Bloc)+1;i++) {
			nbBlockBonFormat.add(i);
		}
		return nbBlockBonFormat;
	}


	//-------------------------------------------------------------------------------------------------Methode HandleTheGet plage	
	public Map <Integer, Map <String,  ArrayList<Integer>>> handleTheGet(int socketPort, String fileName, String packetNumber1, String packetNumber2,String portdestination) {
		synchronized(serveur.registry) { //une ecriture a la fois
			int packet1 = Integer.parseInt(packetNumber1);
			int packet2 = Integer.parseInt(packetNumber2);

			if(serveur.registry.get(socketPort).containsKey(fileName)) {
				for(int i = packet1; i<= packet2;i++) {
					serveur.registry.get(socketPort).get(fileName).add(i);
				}		
			}else {
				Map <String, ArrayList<Integer>> serverregistry = new HashMap<String, ArrayList<Integer>>();
				serverregistry.put(fileName, new ArrayList<Integer>());
				serveur.registry.get(socketPort).putAll(serverregistry);		
			}
			// Appel a la methode de mise a jour des informations
			serveur.updateTrackerValues(String.valueOf(socketPort),portdestination);
			return serveur.registry;
		}
	}


	// ---------------------------------------------------------------------------------------------------MENU
	private void menu(Socket sss) throws Exception { 
		// TODO Auto-generated method stub		
		BufferedReader entreeSocket;
		entreeSocket = new BufferedReader(new InputStreamReader(sss.getInputStream()));

		//lecture d'une chaine envoyee par le client
		String chaine="";
		String[] var;
		do {
			while((chaine=entreeSocket.readLine())==null);
			System.out.println("message recu : "+chaine);
			// Le format du get attendu est le suivant : "get nomFichier 1 "
			// ou "get nomFichier 1 4"
			if (!chaine.equals("FIN")) {
				if (chaine.indexOf(" ")<0)throw new Exception("invalid parameter number");
				var=chaine.split(" ");
				switch(var[0]) {
				case "get":
					if (var.length==3) {
						handleTheGet(portServer,var[1],var[2],var[3]);
					}
					if (var.length==4) {
						handleTheGet(portServer,var[1],var[2],var[3],var[4]);
					}
					break;
				case "search":
					if(var.length>=3)throw new Exception("invalid parameter number");
					search(var[1]);
					break;
				case "isFichier" :
					if (var.length>2) throw new Exception("invalid parameter number");
					isFichier(var[1],sss);
					break;
				case "fichierDispo":
					if (var.length>3)throw new Exception("invalid parameter number");
					creationListeFichier(sss.getPort(),var[1],var[2]);
					break;
				case "maj":
					serveur.ServerArray(new DataOutputStream(sss.getOutputStream()));
					break;
				case "ls":
					ls();
					break;

				}
			}
		}while (!chaine.equals("FIN"));
		serveur.deconnexion(portServer);
	}


	private void ls() throws IOException {
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		ArrayList<String> check = new ArrayList<String>();
		for (Map.Entry<Integer, Map<String,ArrayList<Integer>>> entry : serveur.registry.entrySet()) {
			//pour tous les serveurs

			for (Map.Entry<String,ArrayList<Integer>> entry2 : entry.getValue().entrySet()) {
				//pour tous les fichiers references dans le serveur
				if (!check.contains(entry2.getKey()))check.add(entry2.getKey());
			}
		}
		writeArrayList(os,check);

	}


	public void writeArrayList(DataOutputStream os,ArrayList ar) throws IOException {
		String chaine="";
		String pass = ar.toString();
		String[] pass2=pass.substring(1, pass.length()-1).split(",");
		for (String s : pass2)chaine+=s+" ";
		os.writeUTF(chaine);
	}


	//---------------------------------------------------------------------------------------------Methode search	
	//*** recherche dans le registre quel serveur detient le fichier demande et renvoi la liste
	//*** avec les paquets que chacun des serveurs detient

	private void search(String string) throws IOException {// vérifie qu'on a une entrée pour le fichier sinon renvoi -1
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		if(!ratiosComputation(portServer)){
			os.writeInt(-2);
		}else {
			for (Map.Entry<Integer, Map<String,ArrayList<Integer>>> entry : serveur.registry.entrySet()) {
				//pour tous les serveurs

				for (Map.Entry<String,ArrayList<Integer>> entry2 : entry.getValue().entrySet()) {
					//pour tous les fichiers references dans le serveur

					if (entry2.getKey().matches(string)) {
						//si le nom du fichier correspond alors on envoi le numéro associé au serveur et les blocs contenus
						os.writeInt(entry.getKey());
						writeArrayList(os,entry2.getValue());
						System.out.println("j'ai envoye "+entry.getKey()+" "+entry2.getValue());
					}
				}

			}
		}
		//si aucun serveur n'a le fichier on le signal en envoyant -1 au serveur qui demande
		os.writeInt(-1);
	}

	//------------------------------------------------------------------------------------------------------Methode isFichier
	public void isFichier(String nomF, Socket sss) throws IOException{
		DataOutputStream os = new DataOutputStream(sss.getOutputStream());
		for (Map.Entry<Integer, Map<String,ArrayList<Integer>>> entry : serveur.registry.entrySet()) {
			//pour tous les serveurs

			for (Map.Entry<String,ArrayList<Integer>> entry2 : entry.getValue().entrySet()) {
				//pour tous les fichiers references dans le serveur

				if (entry2.getKey().matches(nomF)) {
					os.writeInt(1);
					return;
				}
			}
		}
		os.writeInt(0);
	}

	//--------------------------------------------------------------------------------------------------------Methode RUN
	@Override
	public void run() {
		// TODO Auto-generated method st ub
		try {
			this.menu(socket);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if (e.getClass().equals(SocketException.class))serveur.deconnexion(portServer);
			else this.run();
		}
	}
}