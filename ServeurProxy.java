package fonctionnement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import communication.Serveur_ProxyCom;

public class ServeurProxy implements Runnable {

	Socket socket;
	Serveur_ProxyCom serveur;
	private Map <Integer, Map <String, ArrayList<Integer>>> registry = new HashMap <Integer, Map <String, ArrayList<Integer>>>();

	public ServeurProxy(Socket socket, Serveur_ProxyCom serveur) {
		super();
		this.socket = socket;
		this.serveur = serveur;
	}

	public Map <Integer, Map <String,  ArrayList<Integer>>> handleTheGet(int socketPort, String fileName, String packetNumber) {
		if(registry.get(socketPort).containsKey(fileName)) {
			registry.get(socketPort).get(fileName).add(Integer.parseInt(packetNumber));
		}else {
			Map <String, ArrayList<Integer>> serverRegistry = new HashMap<String, ArrayList<Integer>>();
			serverRegistry.put(fileName, new ArrayList<Integer>());
			registry.get(socketPort).putAll(serverRegistry);
		}
		return registry;
	}


	public Map <Integer, Map <String,  ArrayList<Integer>>> handleTheGet(int socketPort, String fileName, String packetNumber1, String packetNumber2) {
		int packet1 = Integer.parseInt(packetNumber1);
		int packet2 = Integer.parseInt(packetNumber2);

		if(registry.get(socketPort).containsKey(fileName)) {
			for(int i = packet1; i<= packet2;i++) {
				registry.get(socketPort).get(fileName).add(i);
			}		
		}else {
			Map <String, ArrayList<Integer>> serverRegistry = new HashMap<String, ArrayList<Integer>>();
			serverRegistry.put(fileName, new ArrayList<Integer>());
			registry.get(socketPort).putAll(serverRegistry);		
		}
		return registry;
	}

	private void menu(Socket sss)throws Exception { 
		// TODO Auto-generated method stub		
		BufferedReader entreeSocket = new BufferedReader(new InputStreamReader(sss.getInputStream()));

		//lecture d'une chaine envoyee par le client
		String chaine="";
		int tst = 0;
		String[] var;
		do {
			while ((chaine = entreeSocket.readLine())==null);
			// Le format du get attendu est le suivant : "get nomFichier 1 "
			// ou "get nomFichier 1 4"
			if (!chaine.equals("FIN")) {
				if (chaine.indexOf(" ")<0)throw new Exception("invalid parameter number");
				var=chaine.split(" ");
				switch(var[0]) {
				case "get":
					if (var.length==3) {
						handleTheGet(sss.getPort(),var[1],var[2]);
					}
					if (var.length==4) {
						handleTheGet(sss.getPort(),var[1],var[2],var[3]);
					}
					break;
				}
			}
		}while (!chaine.equals("FIN"));
		serveur.deconnexion(sss);
	}

	@Override
	public void run() {
		// TODO Auto-generated method st ub
		try {
			this.menu(socket);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.run();
		}
	}
}