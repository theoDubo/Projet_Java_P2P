package fonctionnement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import communication.Serveur_ProxyCom;

public class ServeurProxy implements Runnable {
	
	Socket socket;
	Serveur_ProxyCom serveur;

	public ServeurProxy(Socket socket, Serveur_ProxyCom serveur) {
		super();
		this.socket = socket;
		this.serveur = serveur;
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
			//verification que la chaine ai au moins 2 parametres
			if (!chaine.equals("FIN")) {
				if (chaine.indexOf(" ")<0)throw new Exception("invalid parameter number");
				var=chaine.split(" ");
				switch(var[0]) {
				case "get":
					// ATTENDRE LA QUESTION 3
					if (var.length==2) {
						tst = get(var[1],sss);
						if (tst<0)throw new Exception("get Exception, failed");
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