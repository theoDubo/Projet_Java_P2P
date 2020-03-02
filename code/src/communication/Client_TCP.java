package communication;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

import fonctionnement.ClientManager;

public class Client_TCP implements Client {
	private List<Socket> socket=new ArrayList<Socket>();
	private static int nb_conn=0;


	public boolean isFichier(String fichier,DataInputStream es,PrintStream os)throws Exception { 	//méthode qui reçoit le retour pour l'existence du fichier passer en paramètre du get
		int value;
		os.println("isFichier "+fichier);
		if ((value=es.readInt())<0)throw new Exception("Reception file exist fail");
		if (value==0)return false;
		return true;
	}

	public int connect(int i) throws UnknownHostException, IOException {
		socket.add(new Socket("localhost",i));
		System.out.println("Connexion...");
		System.out.println("connexion validé "+(nb_conn+1));
		return nb_conn++;
	}

	public Socket getSocket(int nb) {
		return socket.get(nb);
	}


	// Getter permettant de retourner une map deja classee qui contient les paquet allant de 
	// packetSelected1 � packetSelected2 et leurs tableaux de bytes associes
	public SortedMap<Integer, byte[]> get(String fileName,Socket sock,int packetSelected1, int packetSelected2) throws FileNotFoundException, IOException, Exception { 
		// Stream de lecture et d'écriture de donnees
		DataInputStream	dis = new DataInputStream(sock.getInputStream());
		PrintStream sortieSocket= new PrintStream(sock.getOutputStream());

		//envoi de la requête au serveur
		sortieSocket.println("get "+fileName+" "+packetSelected1+" "+packetSelected2);

		// Creation d'une map classee
		SortedMap<Integer, byte[]> filePackets = new TreeMap<Integer, byte[]>();	
		
		//creation de la variable du suivi de lecture
		int compte; 
		for(int i=packetSelected1;i<=packetSelected2;i++) {
			// On parcours la plage de paquets
			byte [] buffer = new byte[4000];
			if((compte=dis.read(buffer,0,4000))<0)throw new Exception("bytes not read");
			
			//byte effectivement lues
			byte [] result = new byte[compte];
			if (compte != 4000)for(int j=0;j<compte;j++)result[j]=buffer[j];
			else result=buffer;
			filePackets.put(i,result);

		}
		return filePackets;
	}

	//méthode principal de récupération du fichier, il appelle les autres méthodes get par l'intermédiaire de getFichier
	public void get(String[]var,ClientManager clientm) throws Exception{
		// Creation d'un socket sur un port choisi par le systeme
		PrintStream sortieSocket= new PrintStream(this.getSocket(0).getOutputStream());
		DataInputStream	entreeSocket = new DataInputStream(this.getSocket(0).getInputStream());
		SortedMap<Integer,byte[]> mapping = new TreeMap<Integer,byte[]>();
		byte[] tampon;
		int taille,val;

		if (!isFichier(var[1],entreeSocket,sortieSocket))throw new Exception("file does not exist");

		//on recupere la taille du fichier
		sortieSocket.println("size "+var[1]);
		taille=entreeSocket.readInt();

		//on divise sa taille pour voir combien de block de 4ko on peut faire
		val=taille/4000; 

		//on cree le tampon de la taille du fichier
		tampon = new byte[taille];

		if (val<1) { // si il n'y a que 1 block max de 4ko de donnees
			sortieSocket.println("get "+var[1]);
			entreeSocket.read(tampon);
			mapping.put(0, tampon);

			//Si il y 'a plus de donnees on peut separer l'information sur les differents serveurs
		}	else mapping = getFichier(var[1],taille); //getFichier sépare la data à récupéré sur les différents sockets reliés au client 

		
		//on écrit les données récoltés dans un fichier
		if( clientm.ecrireFichier(var[1],mapping)<-1) throw new Exception("Ecrire Fichier failed");		
	}

	// Getter permettant de retourner une map deja classee qui contient le paquet et son tableau de byte asso	public SortedMap<Integer, byte[]> get(String fileName,Socket sock,int packetSelected) throws FileNotFoundException, IOException, Exception { 
	private SortedMap<Integer,byte[]> get(String fileName,Socket sock,int packetSelected) throws Exception{
		// Stream de lecture de donnees
		DataInputStream	dis = new DataInputStream(sock.getInputStream());
		PrintStream sortieSocket= new PrintStream(sock.getOutputStream());
		int compte;
		//envoi de la requête au serveur
		sortieSocket.println("get "+fileName+" "+packetSelected);

		// On cree une autre map que l'on va retourner et elle contient le numero de paquet voulu et son tableau de byte
		SortedMap<Integer, byte[]> filePacket = new TreeMap<Integer, byte[]>();	
		byte [] buffer = new byte[4000];
		if((compte=dis.read(buffer,0,4000))<0) throw new Exception("bytes not read");
		
		//recopie de la valeur de buffer afin de stocker que ce qui est effectivement lu
		byte [] result= new byte[compte];
		if (compte!= 4000) for (int i = 0; i < compte;i++) result[i]=buffer[i];
		else result=buffer;
		
		//ajout dans la sortedmap
		filePacket.put(packetSelected,result);
		return filePacket;
	}

	private SortedMap<Integer,byte[]> getFichier(String string, int taille) throws FileNotFoundException, IOException, Exception {
		// TODO Auto-generated method stub
		// permet de remplir la map mapping avec les blocs issus de plusieurs serveurs -> gestion des arrêts de serveur avec un try catch

		//définition d'une SortedMap de résultat et d'une de remplissage
		SortedMap<Integer,byte[]> result = new TreeMap<Integer,byte[]>();

		// définition du nombre de paquet que doit recevoir chaque serveur
		int packServer = taille / (socket.size()*4000);

		//définition du reste traité par le dernier serveur
		int reste = taille - socket.size()*4000 * packServer;

		//boucle gérant la répartition entre les serveur
		for (int i=0;i<socket.size();i++) {
			//récupération des paquets réglementaire
			if (packServer==1) {
				result.putAll(get(string,socket.get(i),i*packServer+1));
			} else {
				System.out.println("Demande des paquets de "+i*packServer+" à "+(i+1)*packServer);
				result.putAll(get(string,socket.get(i),i*packServer,(i+1)*packServer));
			}
			//récupération des paquets restants
			if (reste !=0 && i==socket.size()-1) result.putAll(get(string,socket.get(i),(i+1)*packServer+1));
		}
		return result;

	}

	public void menu(ClientManager client) throws Exception {
		// Scanner sur System.in
		Scanner scanner = new Scanner(System.in);
		String chaine = "";
		String[] var;

		System.out.println("Tapez vos commandes ou FIN pour arreter : (get, racine)");
		chaine = scanner.nextLine();

		while (!chaine.equals("FIN")) {
			//vÃ©rification que la chaine ai au moins 2 paramÃ¨tres
			if (chaine.indexOf(" ")<0)throw new Exception("invalid parameter number");
			//			sortieSocket.println(chaine);//envoi d'une chaine de caractÃ¨re
			var=chaine.split(" ");

			switch(var[0]) {
			case "get":
				System.out.println("En attente ...");
				get(var,client);
				break;
			case "Racine" : 
				try {
					client.setRacine(var[1]);
				} catch(Exception e) {
					e.printStackTrace();

				}
			}
			System.out.println("Tapez vos commandes ou FIN pour arrÃªter :");
			chaine = scanner.nextLine();
		}
		System.out.println("deconnexion.");
		//à réfléchir
		scanner.close();

	}

	public static void main(String[] args) {
		Client_TCP client = new Client_TCP();
		ClientManager clientm = new ClientManager(client);
		// pour stocker les numéros des sockets dans la liste
		try {
			for(int i = 40000; i <40003 ; i ++) {
				client.connect(i);
			}
			client.menu(clientm);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void deconnect(int nb) throws IOException {
		socket.get(nb).close();
	}
}
