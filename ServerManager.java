package fonctionnement;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
					//Fonctionne pour recup le nom du fichier
					if (var.length==2) {
					tst = get(var[1],sss);
					if (tst<0)throw new Exception("get Exception, failed");
					}//Fonctionne pour recup le  du fichier et du tableau de la case
					if (var.length==3) {
					tst = get(var[1],sss,var[2]);
					if (tst<0)throw new Exception("get Exception, failed");
					}
					if (var.length==4) {
						tst = get(var[1],sss,var[2],var[3]);
					//Fonctionne pour recup le  du fichier et le numéro du tableau de la case et le server ou dl le fichier
					if (tst<0)throw new Exception("get Exception, failed");
					}
					break;
					// les autres fonctions d'utilisations Ã©ventuels
				}
			}
		}while (!chaine.equals("FIN"));
		serveur.deconnexion(sss);
	}

	//Methode 1 - Get fichier, server
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
	
	//Methode 2 - Get fichier, server, numéro case
	public int get(String nomFichier,Socket sss,String var) throws IOException { // fonction de rÃ©cupÃ©ration d'un fichier
		//Je dois uniquement envoyer la valeur demander par exemple le contenu de la 5éme case du tableau
		int nbDuBlock = Integer.parseInt(var);
		int valFinBlock = nbDuBlock * 4000;
		String nomF =root+nomFichier;
		//ouverture du fichier
		File fich = new File(nomF);
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		//test d'existence du fichier
		if (!fich.exists())	os.writeInt(0);
		else os.writeInt(1);
		// declaration du chemin du fichier et du tableau de données du fichier
	//	Path fileLocation = Paths.get(nomF);
		int octets = (int) fich.length();
		byte[] readBytes;
		System.out.println(octets);
		Path fileLocation = Paths.get(nomF);
		//Si le fichier est inférieur à 4ko
		if (octets < 4000) {
	         readBytes = new byte[octets];

			 try (InputStream inputStream = new FileInputStream(fileLocation.toFile()))
		        {
				 System.out.println("TEST");
		            long actuallySkipped = inputStream.skip(0);
		            int bytesReadCount = inputStream.read(readBytes, 0,octets);
		     
		        }
		} else {
			// Si le fichier est supérieur a 4k
         readBytes = new byte[4000];
	        try (InputStream inputStream = new FileInputStream(fileLocation.toFile()))
	        {
	        	//On passe tout les blocks jusqu'au debut du block souhaité
	            long actuallySkipped = inputStream.skip(valFinBlock-4000);
	            int valStandard = 4000;
	            //Signifie que le block demandé est le dernier
	            System.out.println("Valeur "+ octets);
	            int val12 = (octets/(nbDuBlock-1));
	            System.out.println("Valeur2 "+ val12);

	            if ((octets > ((nbDuBlock-1) * 4000)) && ((octets/(nbDuBlock) < 4000))) {
	                readBytes = new byte[octets%4000];
	                valStandard = octets%4000;
	            }
	            System.out.println("Valeur "+ valStandard);
	            //On va lire jusqu'a la fin du fichier
	            int bytesReadCount = inputStream.read(readBytes, 0, valStandard);

	        }
		}
        os.writeInt(readBytes.length);
		// On envoi ça au socket
		System.out.println("Envoi...");
		// Ecriture des bytes dans l'outputstream
		os.write(readBytes,0,readBytes.length);
		os.flush();
		return 0;
	}
 
	//Methode 3 - Get fichier, server, plage de case
	public int get(String nomFichier,Socket sss, String debutPlage, String finPlage) throws IOException { // fonction de rÃ©cupÃ©ration d'un fichier
		//Je dois uniquement envoyer la valeur demander par exemple le contenu de la 5éme case du tableau
				int nbDuBlockDebut = Integer.parseInt(debutPlage);
				int nbDuBlockFin = Integer.parseInt(finPlage);
				int valFinBlock = nbDuBlockFin * 4000;
				String nomF =root+nomFichier;
				//ouverture du fichier
				File fich = new File(nomF);
				DataOutputStream os = new DataOutputStream(socket.getOutputStream());
				//test d'existence du fichier
				if (!fich.exists())	os.writeInt(0);
				else os.writeInt(1);
				// declaration du chemin du fichier et du tableau de données du fichier
			//	Path fileLocation = Paths.get(nomF);
				int octets = (int) fich.length();
				byte[] readBytes;
				Path fileLocation = Paths.get(nomF);
				//Si le fichier est inférieur à 4ko
				if (octets < 4000) {
			         readBytes = new byte[octets];

					 try (InputStream inputStream = new FileInputStream(fileLocation.toFile()))
				        {
				            long actuallySkipped = inputStream.skip(0);
				            int bytesReadCount = inputStream.read(readBytes, 0,octets);    
				        }
					 os.writeInt(readBytes.length);
						// On envoi ça au socket
						System.out.println("Envoi...");
						// Ecriture des bytes dans l'outputstream
						os.write(readBytes,0,readBytes.length);
				} else {
					// Si le fichier est supérieur a 4k
					// Envoie 4ko par 4ko et créé un tableau qui a la taille du dernier segment
		         readBytes = new byte[4000];
			        try (InputStream inputStream = new FileInputStream(fileLocation.toFile()))
			        {
			        	  long actuallySkipped;

			        	if ( nbDuBlockDebut == 1) {
				             actuallySkipped = inputStream.skip(0);

			        	} else {
				             actuallySkipped = inputStream.skip((nbDuBlockDebut-1)*4000);
			        	}
			        for (int i = nbDuBlockDebut ; i <= nbDuBlockFin; i++ ) {
						System.out.println("val skip " + actuallySkipped);
			        	//Gère le dernier block
			            if (i == nbDuBlockFin && (octets/(nbDuBlockFin) < 4000) ) {
			            	System.out.println("JSuis là ");
			                readBytes = new byte[octets%4000];
			                int valStandard = octets%4000;
				            int bytesReadCount = inputStream.read(readBytes, 0, 26);
				        	System.out.println("J'ai pris"  + bytesReadCount +"  "+ valStandard);
			            } else {
			            	int bytesReadCount = inputStream.read(readBytes, 0, 4000);
			        	}
				        os.writeInt(readBytes.length);
			    		// On envoi ça au socket
			    		System.out.println("Envoi...");
			    		// Ecriture des bytes dans l'outputstream
			    		os.write(readBytes,0,readBytes.length);

						}
			        }
				}	
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
