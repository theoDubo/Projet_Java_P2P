package fonctionnement;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import communication.Serveur_FTP;

public class ServerManager implements Runnable {
	Socket socket;
	Serveur_FTP serveur;
	String root="./dossierServeur/";

	public ServerManager(Socket socket, Serveur_FTP serveur) {
		super();
		this.socket = socket;
		this.serveur = serveur;
	}


	private void menu(Socket sss)throws Exception { //manque l'ajout de fonction mais sinon Ã§a marche
		// TODO Auto-generated method stub		
		BufferedReader entreeSocket = new BufferedReader(new InputStreamReader(sss.getInputStream()));

		//lecture d'une chaine envoyee par le client
		String chaine="";
		int tst;
		String[] var;
		do {
			while ((chaine = entreeSocket.readLine())==null);
			//verification que la chaine ai au moins 2 parametres
			if (!chaine.equals("FIN")) {
				if (chaine.indexOf(" ")<0)throw new Exception("invalid parameter number");
				var=chaine.split(" ");
				switch(var[0]) {
				case "get":
	/* à modifier*/	if (var.length>2) throw new Exception("invalid parameter number");
					tst = get(var[1],sss);
					if (tst<0)throw new Exception("get Exception, failed");
					break;
					// les autres fonctions d'utilisations Ã©ventuels
				}
			}
		}while (!chaine.equals("FIN"));
		serveur.deconnexion(sss);
	}


	public int get(String nomFichier,Socket sss) throws IOException { // fonction de rÃ©cupÃ©ration d'un fichier
		String nomF =root+nomFichier;
		//ouverture du fichier
		File fich = new File(nomF);
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		
		//test d'existence du fichier
		if (!fich.exists())	os.writeInt(0);
		else os.writeInt(1);
		
		// declaration du chemin du fichier et du tableau de données du fichier
		Path fileLocation = Paths.get(nomF);
		byte[] data = Files.readAllBytes(fileLocation);
		
		os.writeInt(data.length);
		// On envoi ça au socket

		System.out.println("Envoi...");

		// Ecriture des bytes dans l'outputstream
		os.write(data,0,data.length);
		os.flush();

		return 0;
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
