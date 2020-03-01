package fonctionnement;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

import communication.Client_TCP;

public class ClientManager implements Runnable {
	private Client_TCP client;

	public ClientManager(Client_TCP client) {
		super();
		this.client = client;
	}

	//methode qui recoit le retour pour l'existence du fichier passer en parametre du get
	public boolean isFichier(DataInputStream es)throws Exception { 
		int value;
		if ((value=es.readInt())<0)throw new Exception("Reception file exist fail");
		if (value==0)return false;
		return true;
	}

	// Getter permettant de creer une map ordonnee associant entier et tableau de bytes.
	// Un entier correspond a un numero de paquet
	@SuppressWarnings("resource")
	public SortedMap<Integer, byte[]> getFile(String fileName) throws Exception {
		// Stream de lecture de donnees
		DataInputStream	dis = new DataInputStream(client.getSocket().getInputStream());

		// declaration des variables
		byte[] buffer;
		int packet = 0;
		int fileSize,packetsNumber,bytesRead;
		long bytesSkipped;
		SortedMap<Integer, byte[]> filePackets = new TreeMap<Integer, byte[]>();
		
		// Creation du fichier de sortie
		FileOutputStream fos = new FileOutputStream(client.getracine()+fileName);
		fos.close();

		// recuperation de la taille de fichier distant
		if (!isFichier(dis))throw new Exception("file does not exist");

		//	Reception de la taille du fichier pour creer la copie 
		if ((fileSize=dis.readInt())<0)throw new Exception("Reception length fail"); 
		if (fileSize <0) throw new Exception("Not a number exception");

		// Si la taille du fichier est inferieure a 4Ko alors
		// on ecrit stocke tout dans une Map avec un unique paquet.
		// Ce qui est equivalent au fait de tout ecrire d'un seul coup
		// mais cette methode n'ecrit pas dans le fichier
		if(fileSize<4000) {
			buffer = new byte[fileSize];
			packet=1;
			filePackets.put(packet, buffer);
		} else {
			packetsNumber = fileSize/4000;
			// Ici on lit les blocs de 4000 octets entiers
			for(int i=0;i<packetsNumber;i++) {
				packet=i+1;
				buffer = new byte[4000];
				bytesRead=dis.read(buffer,0,4000);
				filePackets.put(packet, buffer);
			}
			// Ici on lit le bloc restant
			packet+=1;
			buffer = new byte[fileSize%4000];
			bytesRead=dis.read(buffer,0,fileSize%4000);
			filePackets.put(packet, buffer);
		}
		return filePackets;
	}

	// Getter permettant de retourner une map deja classee qui contient le paquet et son tableau de byte associe
	public SortedMap<Integer, byte[]> downloadPacket(String fileName,String packetSelected) throws FileNotFoundException, IOException, Exception { 
		SortedMap<Integer, byte[]> filePacket;	
		int packet = Integer.parseInt(packetSelected);
		// Verification de l'existence du fichier en premier lieu
		File tempFile = new File(client.getracine()+fileName);
		if(tempFile.exists()) {
			// Verification de l'existence du paquet selectionne
			if(getFile(fileName).containsKey(packet)) {
				// On cree une autre map que l'on va retourner et elle contient le numero de paquet voulu et son tableau de byte
				filePacket = new TreeMap<Integer, byte[]>();	
				filePacket.put(packet, getFile(fileName).get(packet));
			}else {
				throw new Exception("packet : "+packetSelected+" does not exist");
			}
		}else {
			throw new Exception("file does not exist");
		}
		return filePacket;
	}
	
	// Getter permettant de retourner une map deja classee qui contient les paquet allant de 
	// packetSelected1 � packetSelected2 et leurs tableaux de bytes associes
	public SortedMap<Integer, byte[]> downloadPackets(String fileName,String packetSelected1, String packetSelected2) throws FileNotFoundException, IOException, Exception { 
		SortedMap<Integer, byte[]> filePackets;	
		int firstPacket = Integer.parseInt(packetSelected1);
		int lastPacket = Integer.parseInt(packetSelected2);
		// Verification de l'existence du fichier en premier lieu
		File tempFile = new File(client.getracine()+fileName);
		if(tempFile.exists()) {
			// Verification de l'existence des paquets selectionnes
			// Si ces deux paquets existent alors il est inutile de verifier
			// que les paquets compris dans la plage d'adresses existent aussi.
			// La methode get ci-dessus les a trie par ordre croissant.
			if(getFile(fileName).containsKey(firstPacket)&&getFile(fileName).containsKey(lastPacket)) {
				// On cree une autre map que l'on va retourner et elle contient la plage de paquets selectionnes et leurs tableaux de bytes
				filePackets = new TreeMap<Integer, byte[]>();	
				for(int i=firstPacket;i<=lastPacket;i++) {
					filePackets.put(i, getFile(fileName).get(i));
				}
			}else {
				throw new Exception("be sure the selected packets exist");
			}
		}else {
			throw new Exception("file does not exist");
		}
		return filePackets;
	}
				
	public void menu(Socket socket) throws Exception {
		// Création d'un socket sur un port choisi par le système
		PrintStream sortieSocket= new PrintStream(socket.getOutputStream());

		// Scanner sur System.in
		Scanner scanner = new Scanner(System.in);
		String chaine = "";
		String[] var;

		System.out.println("Tapez vos commandes ou FIN pour arrêter :");
		chaine = scanner.nextLine();
		while (!chaine.equals("FIN")) {
			//verification que la chaine ai au moins 2 parametres
			if (chaine.indexOf(" ")<0)throw new Exception("invalid parameter number");
			sortieSocket.println(chaine);//envoi d'une chaine de caractère
			var=chaine.split(" ");

			switch(var[0]) {
			case "get":
				System.out.println("En attente ...");
				getFile(var[1]);
				break;
			
			case "downloadPacket":
				System.out.println("En attente ...");
				downloadPacket(var[1],var[2]);
				break;
				
			case "downloadPackets":
				System.out.println("En attente ...");
				downloadPackets(var[1],var[2],var[2]);
				break;
			}
			System.out.println("Tapez vos commandes ou FIN pour arrêter :");
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