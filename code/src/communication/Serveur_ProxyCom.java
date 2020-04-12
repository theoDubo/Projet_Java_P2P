package communication;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fonctionnement.ServeurProxy;

public class Serveur_ProxyCom{
	// Declaration des variables
	private ServerSocket socket;
	private ArrayList<Integer> portsArray = new ArrayList<Integer>();
	public Map <Integer, Map <String, ArrayList<Integer>>> registry = new HashMap <Integer, Map <String, ArrayList<Integer>>>();
	// Map ayant le port du socket server en cle et un objet en tant que valeur
	public Map<Integer,PortsManagement> tracker = new HashMap<Integer,PortsManagement>();

	// Methode permettant l'ajout du port a la liste des connexions
	public ArrayList<Integer> addPort(int port) {
		this.portsArray.add(port);
		// On ajoute le port et les donnees stockees dans la classe PortsManagement a la map tracker
		this.tracker.put(port, new PortsManagement());
		return this.portsArray;
	}

	// Methode ayant pour but de calculer/mettre a jour le nombre d'upload et de download d'une application
	public void updateTrackerValues(String portEmission, String portDestination) {
		// Conversion des ports au format d'entiers
		int portE = Integer.parseInt(portEmission);
		int portD = Integer.parseInt(portDestination);

		// Parmi toutes les valeurs de la map, on cherche a savoir s'il s'agit d'un upload ou d'un download
		for (Integer key : tracker.keySet() ) {
			if(key==portE) tracker.get(key).setNbEmission();
			if(key==portD) tracker.get(key).setNbDestination();
		}
	}

	// Methode principale de la classe, elle gere le proxy et cree les threads associes aux differentes connexions
	public void ecoute()throws Exception {
		// Declaration et initialisation des variables
		Socket sss;
		// On cree le socket server du manager
		socket=new ServerSocket(12345);
		
		while(true) {
			System.out.println("En attente ...");
			// Acceptation de la connexion
			sss = socket.accept(); 
			System.out.println("connexion acceptee : "+sss);
			// Une fois la connexion acceptee on recupere les moyens de communiquer
			DataInputStream entreeSocket = new DataInputStream(sss.getInputStream());
			DataOutputStream os = new DataOutputStream(sss.getOutputStream());
			int test = entreeSocket.readInt();
			// On ajoute a la liste des serveurs le serveur associe au client qui se connecte
			this.addPort(test);
			// On lui renvoie la liste des servers connectes
			ServerArray(os);
			// On lance le systeme de proxy
			Thread t = new Thread(new ServeurProxy(sss,this, this.portsArray.get(this.portsArray.size()-1)));
			// Demarrage du thread
			t.start();
		}
	}

	// Methode permettant de renvoyer la liste des serveurs connectes au proxy
	public void ServerArray(DataOutputStream s) throws IOException {
		// On envoie au socket les ports auxquels sont connectes les serveurs
		if (portsArray.size()==1) {
			s.writeInt(-1);
		}
		else {
			for (int i : portsArray) {
				s.writeInt(i);
			}
			s.writeInt(-1);
		}
	}

	// Methode permettant de gerer la deconnexion d'un des serveurs
	public void deconnexion(int i) {
		// On supprime le serveur deconnectes des differentes listes
		this.portsArray.remove(this.portsArray.indexOf(i));
		this.registry.remove(i);
		this.tracker.remove(i);
	}

	// Methode d'execution de la classe
	public static void main(String[] arg0) {
		try {
			// Creation d'un serveur proxy de communication et lancement de l'ecoute
			Serveur_ProxyCom serveur = new Serveur_ProxyCom();
			serveur.ecoute();		
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
}