package communication;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import fonctionnement.ClientManager;

public class Client_TCP implements Client,Runnable {
	private List<Socket> socket=new ArrayList<Socket>();
	private static int nb_conn=0;
	private int sockServer;
	private Socket proxy;
	private String root=null;
	Timer timer;
	private Scanner scanner;

	TimerTask timTas=new TimerTask() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
			PrintStream os=new PrintStream(proxy.getOutputStream());
			DataInputStream is=new DataInputStream(proxy.getInputStream());
			
			os.println("maj");
			int i;
			boolean isContained=false;
			while((i=is.readInt())!=-1) {
				for(Socket s : socket) if (s.getPort()==i)isContained=true;
				if (!isContained)connect(i);
			}
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	};
	public Client_TCP(String r,int s, Scanner sc) {
		this.root=r;
		this.sockServer=s;
		timer = new Timer();
		this.scanner = sc;

	}
	public Client_TCP(int s) {
		this.sockServer=s;
	}
	
	
	public boolean isFichier(String fichier,DataInputStream es,PrintStream os)throws Exception { 	//méthode qui reçoit le retour pour l'existence du fichier passer en paramètre du get
		int value;
		os.println("isFichier "+fichier);
		if ((value=es.readInt())<0)throw new Exception("Reception file exist fail");
		if (value==0)return false;
		return true;
	}

	public int connect(int i) throws UnknownHostException, IOException {
		socket.add(new Socket("localhost",i));
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
		PrintStream proxySocketOut=new PrintStream(proxy.getOutputStream());

		//envoi de la requete au serveur
		sortieSocket.println("get "+fileName+" "+packetSelected1+" "+packetSelected2);
		proxySocketOut.println("get "+fileName+" "+packetSelected1+" "+packetSelected2);
		
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

//------------------------------------------------------------------------------------------------------------------------------------Main GET
//*** Methode de demarrage de la fonction get
	
	public void get(String[]var,ClientManager clientm) throws Exception{
		// Creation d'un socket sur un port choisi par le systeme
		PrintStream sortieSocket= new PrintStream(this.proxy.getOutputStream());
		DataInputStream	entreeSocket = new DataInputStream(this.proxy.getInputStream());
		SortedMap<Integer,byte[]> mapping = new TreeMap<Integer,byte[]>();
		byte[] tampon;
		int taille,val;
		
		//verification le fichier existe en memoire du proxy
		if (!isFichier(var[1],entreeSocket,sortieSocket))throw new Exception("file does not exist");

		//recuperation des donnees de stockage concernant le fichier depuis les differents serveurs
		sortieSocket.println("search "+var[1]);
		Map<Integer,ArrayList<Integer>> which= new HashMap<Integer,ArrayList<Integer>>();
		
		while((taille=entreeSocket.readInt())!= -1){
				if (taille==-2) {
					sortieSocket.close();
					throw new Exception("Upload ratio unreached");
				}
				which.put(taille, readArrayList(entreeSocket));
		}
		//Creation recuperation du lien vers le premier serveur possedant le fichier
		//recuperation des clees de la map 
		
		Set<Integer> keys=which.keySet();
		ArrayList<Integer> keysA=new ArrayList<Integer>(keys);
		
		Socket i = resolveSocket(keysA.get(0));
		
		//creation des liens avec le premier serveur
		PrintStream sortieSock= new PrintStream(i.getOutputStream());
		DataInputStream	entreeSock = new DataInputStream(i.getInputStream());
		
		//on recupere la taille du fichier
		sortieSock.println("size "+var[1]);
		taille=entreeSock.readInt();
		System.out.println("taille : "+taille);
		//on divise sa taille pour voir combien de block de 4ko on peut faire
		val=taille/4000; 

		//on cree le tampon de la taille du fichier
		tampon = new byte[taille];

		if (val<=1) { // si il n'y a que 1 block max de 4ko de donnees
			sortieSock.println("get "+var[1]);
			sortieSocket.println("get "+var[1]+" 1");
			entreeSock.read(tampon);
			mapping.put(0, tampon);

			//Si il y 'a plus de donnees on peut separer l'information sur les differents serveurs
		}	else mapping = getFichier(var[1],taille,which); //getFichier sépare la data à récupéré sur les différents sockets reliés au client 


		//on écrit les données récoltés dans un fichier
		if( clientm.ecrireFichier(var[1],mapping)<-1) throw new Exception("Ecrire Fichier failed");		
	}

	
//--------------------------------------------------------------------------------------------------------------------------read ArrayList
//*** Methode permettant de lire un arraylist<Integer> sur le socket
	
	private ArrayList<Integer> readArrayList(DataInputStream entreeSocket) throws IOException {
		String check=entreeSocket.readUTF();
		String[] val=check.split(" ");
		ArrayList<Integer> var= new ArrayList<Integer>();
		for (String bob : val) {
			if (bob.isBlank())continue;
			var.add(Integer.parseInt(bob));
		}
		return var;
	}
	
	
//--------------------------------------------------------------------------------------------------------------------------------Get Packet
//*** Methode permettant de recuperer le paquet souhaite
	private SortedMap<Integer,byte[]> get(String fileName,Socket sock,int packetSelected) throws Exception{
		// Stream de lecture de donnees
		DataInputStream	dis = new DataInputStream(sock.getInputStream());
		PrintStream sortieSocket= new PrintStream(sock.getOutputStream());
		PrintStream prox = new PrintStream(proxy.getOutputStream());
		
		int compte;
		//envoi de la requête au serveur
		sortieSocket.println("get "+fileName+" "+packetSelected);
		prox.println("get "+fileName+" "+packetSelected);
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

//-------------------------------------------------------------------------------------------------------------------------------GetFichier	
//*** permet de remplir la map mapping avec les blocs issus de plusieurs serveurs
	private SortedMap<Integer,byte[]> getFichier(String string, int taille,Map<Integer,ArrayList<Integer>>wich) throws FileNotFoundException, IOException, Exception {


		//definition d'une SortedMap de résultat et d'une de remplissage
		SortedMap<Integer,byte[]> result = new TreeMap<Integer,byte[]>();

		//definition du nombre de paquet pour avoir le fichier entier
		int nbPack= taille/4000;
		int r= taille%4000;
		
		//differenciation entre le cas exact et le cas approximatif
		if (r>0) {
			for (int i = 1; i < nbPack+2; i++) {
				result.putAll(askPacket(wich,string,i));
			}
		} else {
			for (int i = 1; i < nbPack+1;i++) {
				result.putAll(askPacket(wich,string,i));
			}
		}
		return result;

	}
	
//------------------------------------------------------------------------------------------------------------------------askPacket
//*** Methode permettant de demander un paquet de façon aleatoire
	private SortedMap<Integer, byte[]> askPacket(Map<Integer,ArrayList<Integer>>wich,String string, int k) throws Exception {
		//creation de la liste des cles
		Set<Integer> keyreference = wich.keySet();
		ArrayList<Integer> keys = new ArrayList<Integer>(keyreference);
		
		//declaration des variables de traitement
		int i,compteur=0;
		Random rand = new Random();
		
		//boucle random de selection de serveur en fonction du paquet souhaite
		while(true) {
			//recuperation d'un port aleatoire (ça ne marche pas correctement 
			i=keys.get(rand.nextInt(keys.size()));
			//si le serveur associe detient le paquet souhaitee
			if (wich.get(i).contains(k))return get(string,resolveSocket(i),k);
			if(++compteur > (keys.size()+2)*100) {
				System.out.println("retour par non selection de bon serveur");
				return null;
			}
		}
	}
	
	
	
//----------------------------------------------------------------------------------------------------------------------------miseAJour
//*** Methode qui permet de recuperer la liste des serveurs connectes au proxy
//*** C est la meme methode que dans le timer Task

	public void miseAJour() throws IOException {
		DataInputStream is=new DataInputStream(proxy.getInputStream());
		int i;
		//boolean permettant de verifier si le port est deja dans l application
		boolean isContained;
		while((i=is.readInt())!=-1) {
			isContained=false;
			//si i est a l'interieur de la liste on change le boolean
			for(Socket s : socket) if (s.getPort()==i)isContained=true;
			if (!isContained)connect(i);
		}
	}
	
	
//------------------------------------------------------------------------------------------------------------------------resolveSocket
//*** Methode permettant de determiner le socket associe a un port si celui-ci est
//*** present dans la liste des connexions
	
	private Socket resolveSocket(int i) {
		int k =0;
		// Si on a pas depasser la fin de la liste de connexion et
		//  que le port ne correspond pas au socket on continue
		while((socket.get(k++).getPort()!=i)&& (k+1)!=socket.size());
		if (k>socket.size()-1) {// si on a atteint la fin
			System.out.println("not found");
			return null;
		}
		return socket.get(k-1); // sinon on retourne le socket trouve
	}

	
//-----------------------------------------------------------------------------------------------------------------------------MENU
//*** Methode principal de la classe, elle permet d'executer les demandes du client
	public void menu(ClientManager client) throws Exception {
		String chaine = "";
		String[] var;

		System.out.println("Tapez vos commandes ou FIN pour arreter : (get, racine)");
		chaine = scanner.nextLine();

		while (!chaine.equals("FIN")) {
			//verification que la chaine ai au moins 2 paramÃ¨tres
			var=chaine.split(" ");

			switch(var[0]) {
			case "get":
				if (var.length!=2)throw new Exception("invalid parameter number");
				else {
					System.out.println("En attente ...");
					get(var,client);
				}
				
				break;
			case "racine" :
				if (var.length!=2)throw new Exception("invalid Parameter number");
				try {
					client.setRacine(var[1]);
					changeRacine();
					isAllFiles(client,proxy);
				} catch(Exception e) {
					scanner.close();
					e.printStackTrace();
				}
				
				break;
			case "ls":
				try {
					ls();
					
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("Tapez vos commandes ou FIN pour arreter :");
			chaine = scanner.nextLine();
		}
		System.out.println("deconnexion.");
		//à réfléchir
		scanner.close();
	}


private void changeRacine() throws IOException {
	PrintStream ps = new PrintStream(proxy.getOutputStream());
	System.out.println("je suis ici");
	ps.println("ChangeRoot "+sockServer);
}
//---------------------------------------------------------------------------------------------------------------------------------------LS
//*** permet d'afficher la liste des fichiers disponible sur le reseau
		
private void ls() throws IOException {
		DataInputStream is = new DataInputStream(proxy.getInputStream());
		PrintStream ps = new PrintStream(proxy.getOutputStream());
		ps.println("ls");
		String check=is.readUTF();
		String[] val=check.split(" ");
		System.out.println("Liste des fichiers disponibles sur le reseau :");
		for(String s : val)System.out.println(s);
		
}

//--------------------------------------------------------------------------------------------------------------------------------IsAllFiles
//***Renvoie tous les fichier et son nombre de block
	public void isAllFiles(ClientManager client,Socket sss) throws IOException{
		PrintStream os = new PrintStream(sss.getOutputStream());
		final File folder = new File(client.getracine());
		//définition d'une SortedMap de résultat et d'une de remplissage
		String nomBlock;
		for(final File file : folder.listFiles()){
			nomBlock = "fichierDispo "+ file.getName() + " " + blockFile(file);
			// On envoi ça au socket
			// Ecriture des bytes dans l'outputstream
			os.println(nomBlock);
			System.out.println("Envoi...");
		}
	}
	
//--------------------------------------------------------------------------------------------------------------------------------BlockFiles
//***Return le nombre de block du fichier
	public int blockFile(File cheminFichier)throws IOException {
		long taille = cheminFichier.length();
		int nbBlock;
		if ( taille < 4000) {
			nbBlock = 1;
		} else {
			if (taille%4000 > 0) {
				nbBlock = ((int) (taille/4000)+1);
			} else {
				nbBlock = (int) (taille/4000);
			}
		}
		return nbBlock;
	}


//---------------------------------------------------------------------------------------------------------------------------------RUN
//***methode d execution de la classe
	public void run() {
		ClientManager clientm;
		Scanner sc=new Scanner(System.in);
		// si l'utilisateur n a pas demande a utiliser un dossier on garde le chemin par defaut
		if (this.root==null)clientm=new ClientManager(this);
		
		// si non on modifie le chemin dans le manager
		else clientm=new ClientManager(this,root);
		
		// pour stocker les numéros des sockets dans la liste
		try {
			//premiere etape se connecter au proxy
			if((proxy=new Socket("localhost",12345))==null)throw new Exception("Proxy unreachable");
		
			//deuxieme etape envoyer au proxy le port du serveur associe a cet user
			new DataOutputStream(proxy.getOutputStream()).writeInt(sockServer);
			isAllFiles(clientm,proxy);
			
			miseAJour();
			// On ouvre le menu 
			timer.schedule(timTas, 60000,60000);
			this.menu(clientm);
			
			sc.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

//---------------------------------------------------------------------------------------------------------------------------------deconnect
//***Deconnecte le client
	public void deconnect(int nb) throws IOException {
		socket.get(nb).close();
	}
}
