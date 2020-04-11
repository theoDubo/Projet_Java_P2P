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
	private ServerSocket socket ;
	private ArrayList<Integer> portsArray = new ArrayList<Integer>();
	public Map <Integer, Map <String, ArrayList<Integer>>> registry = new HashMap <Integer, Map <String, ArrayList<Integer>>>();
	
	// Map ayant le port du socket server en cle et un objet en tant que valeur
	public Map<Integer,PortsManagement> tracker = new HashMap<Integer,PortsManagement>();
	
	//------------------------------------------------------------------------------------------------addPort
	//*** Methode permettant l'ajout du port a la liste des connexions
	public ArrayList<Integer> addPort(int port) {
		this.portsArray.add(port);
		this.tracker.put(port, new PortsManagement());
		return this.portsArray;
	}

	//-------------------------------------------------------------------------------------------------updateTrackerValues
	//*** Methode ayant pour but de calculer/mettre a jour le nombre d'upload
	//***et de download d'une application

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
	
	
	//-------------------------------------------------------------------------------------------------Ecoute
	//*** Methode principal de la classe, elle gere le proxy et cree les threads
	//*** associes au differentes connexions
	public void ecoute()throws Exception {
		// on cree le socket server du manager
		socket=new ServerSocket(12345);
		
		Socket sss;
		while(true) {
			System.out.println("En attente ...");
			sss = socket.accept(); 
			System.out.println("connexion acceptee : "+sss);
		
			//une fois la connexion acceptee on recupere les moyens de communiquer
			DataInputStream entreeSocket = new DataInputStream(sss.getInputStream());
			DataOutputStream os = new DataOutputStream(sss.getOutputStream());
			int test = entreeSocket.readInt();
			//on ajoute a la liste des serveurs le serveurs associes au client qui se connecte
			this.addPort(test);
			
			//on lui renvoi la liste des servers connectes
			ServerArray(os);
			
			//on lance le system de proxy
			Thread t = new Thread(new ServeurProxy(sss,this, this.portsArray.get(this.portsArray.size()-1)));
			t.start();
		}
	}
	
//-------------------------------------------------------------------------------------------------------------------ServerArray
//*** Methode permettant de renvoyer la liste des serveurs connectes au proxy
	public void ServerArray(DataOutputStream s) throws IOException {
		// TODO Auto-generated method stub
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

//--------------------------------------------------------------------------------------------------------------------Deconnexion
//*** Methode permettant de gerer la deconnexion d'un des serveurs
	public void deconnexion(int i) {
		this.portsArray.remove(this.portsArray.indexOf(i));
		this.registry.remove(i);
		this.tracker.remove(i);
	}

//----------------------------------------------------------------------------------------------------------------------MAIN
//*** Methode d'execution de la classe
	public static void main(String[] arg0) {
		try {
			Serveur_ProxyCom serveur = new Serveur_ProxyCom();
			serveur.ecoute();		
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
}