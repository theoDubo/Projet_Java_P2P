package fonctionnement;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;

import communication.Client_TCP;

public class ClientManager implements Runnable {
	private Client_TCP client;

	public ClientManager(Client_TCP client) {
		super();
		this.client = client;
	}

	public boolean isFichier(DataInputStream es)throws Exception { 	//méthode qui reçoit le retour pour l'existence du fichier passer en paramètre du get
		int value;
		if ((value=es.readInt())<0)throw new Exception("Reception file exist fail");
		if (value==0)return false;
		return true;
	}

	public void get(String var) throws Exception {
		// declaration des variables
		// Stream de lecture de données
		DataInputStream	entreeSocket = new DataInputStream(client.getSocket().getInputStream());
		if (var==null ) throw new Exception("invalid parameter number");
		
		// declaration du fichier de sortie
		FileOutputStream fos = new FileOutputStream(client.getracine()+var);
		System.out.println("file created");
		
		// déclaration des variables de tests
		byte[] tampon;
		int value,current;

		
		// recuperation de la taille de fichier distant
		if (!isFichier(entreeSocket))throw new Exception("file does not exist");

		//		pour recevoir la taille du fichier pour creer la copie 
		if ((value=entreeSocket.readInt())<0)throw new Exception("Reception length fail"); 
		if (value <0) throw new Exception("Not a number exception");
		System.out.println("longueur du fichier "+value);
		
		
		//création du tampon de lecture
		tampon = new byte[value];
		 
		//ouverture du stream permettant d'ecrire le fichier
		BufferedOutputStream bos = new BufferedOutputStream(fos);


		// Tant que le nombre de bytes lues est positif ou nul
		value = entreeSocket.read(tampon,0,tampon.length);
		//System.out.println(tampon.length);
		System.out.println("bit lues "+value);
		
		/*
		current = 0;
		// Tant que le nombre de bytes lues est positif ou nul
		do {
			bytesRead = entreeSocket.read(tampon, current, (tampon.length-current));
			if(bytesRead >= 0) current += bytesRead;
		} while(bytesRead > -1);
		*/
		
		// Lecture des bytes de ce flux de sortie de bytes dans le tampon
		bos.write(tampon,0,tampon.length);
		bos.flush();
		bos.close();
		fos.close();
	}




	public void menu(Socket socket) throws Exception {
		// CrÃ©ation d'un socket sur un port choisi par le systÃ¨me
		PrintStream sortieSocket= new PrintStream(socket.getOutputStream());

		// Scanner sur System.in
		Scanner scanner = new Scanner(System.in);
		String chaine = "";
		String[] var;

		System.out.println("Tapez vos commandes ou FIN pour arrÃªter :");
		chaine = scanner.nextLine();
		while (!chaine.equals("FIN")) {
			//vÃ©rification que la chaine ai au moins 2 paramÃ¨tres
			if (chaine.indexOf(" ")<0)throw new Exception("invalid parameter number");
			sortieSocket.println(chaine);//envoi d'une chaine de caractÃ¨re
			var=chaine.split(" ");

			switch(var[0]) {
			case "get":
				System.out.println("En attente ...");
				get(var[1]);
				break;
			}
			System.out.println("Tapez vos commandes ou FIN pour arrÃªter :");
			chaine = scanner.nextLine();
		}
		System.out.println("deconnexion.");
		client.deconnect();
		scanner.close();

	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			this.menu(client.getSocket());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.run();
		}
	}

}


