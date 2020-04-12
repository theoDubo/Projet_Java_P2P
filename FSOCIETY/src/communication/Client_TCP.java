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
	// Declaration des variables
	private List<Socket> socket=new ArrayList<Socket>();
	private static int nb_conn=0;
	private int sockServer;
	private Socket proxy;
	private String root=null;
	Timer timer;
	private Scanner scanner;

	// Isolation d'un bloc de code pouvant etre repete via un timer
	TimerTask timTas=new TimerTask() {
		@Override
		public void run() {
			// Declaration de variables locales
			int i;
			boolean isContained=false;
			try {
				// On recupere le flux de donnees sortant du socket proxy
				PrintStream os=new PrintStream(proxy.getOutputStream());
				// Creation d'un flux de donnees entrant avec les donnees entrantes du socket proxy
				DataInputStream is=new DataInputStream(proxy.getInputStream());
				// Tant que l'on ne receptionne pas d'entier egal à -1
				while((i=is.readInt())!=-1) {
					// On recherche le port recu parmi la liste des sockets
					for(Socket s : socket) if (s.getPort()==i)isContained=true;
					// Si il n'existe aucune correspondance alors se connecte sur le port recu
					if (!isContained)connect(i);
				}
			}catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	};

	// Constructeur prenant en parametre une chaine de caracteres, un entier et un scanner
	public Client_TCP(String r,int s, Scanner sc) {
		this.root=r;
		this.sockServer=s;
		timer = new Timer();
		this.scanner = sc;

	}

	// Constructeur prenant en parametre un entier
	public Client_TCP(int s) {
		this.sockServer=s;
	}

	// Methode de verifier l'existence du fichier passe en parametre du get
	public boolean isFichier(String fichier,DataInputStream es,PrintStream os)throws Exception { 	
		// Decalration des variables
		int value;
		os.println("isFichier "+fichier);
		// Si on recoit un entier negatif alors il s'agit d'une erreur de fichier
		if ((value=es.readInt())<0)throw new Exception("Reception file exist fail");
		// Sinon tout est correct
		if (value==0)return false;
		return true;
	}

	// Methode retournant un entier et permettant de se connecter ou plutot de creer un socket sur le port precise
	public int connect(int i) throws UnknownHostException, IOException {
		socket.add(new Socket("localhost",i));
		System.out.println("connexion validÃ© "+(nb_conn+1));
		return nb_conn++;
	}

	// Methode permettant de retourner le socket suivant l'index precise de la liste de sockets
	public Socket getSocket(int nb) {
		return socket.get(nb);
	}


	// Getter permettant de retourner une map deja classee qui contient les paquet allant de packetSelected1 à packetSelected2 et leurs tableaux de bytes associes
	public SortedMap<Integer, byte[]> get(String fileName,Socket sock,int packetSelected1, int packetSelected2) throws FileNotFoundException, IOException, Exception { 
		// Declaration des streams de lecture et d'ecriture de donnees
		DataInputStream	dis = new DataInputStream(sock.getInputStream());
		PrintStream sortieSocket= new PrintStream(sock.getOutputStream());
		PrintStream proxySocketOut=new PrintStream(proxy.getOutputStream());

		// Creation de la variable du suivi de lecture
		int compte; 

		// Envoi des requetes au serveur
		sortieSocket.println("get "+fileName+" "+packetSelected1+" "+packetSelected2);
		proxySocketOut.println("get "+fileName+" "+packetSelected1+" "+packetSelected2);

		// Creation d'une map classee
		SortedMap<Integer, byte[]> filePackets = new TreeMap<Integer, byte[]>();	

		// Parcours de la plage de paquets
		for(int i=packetSelected1;i<=packetSelected2;i++) {
			// Tableau de bytes permettant de recuperer des blocs de donnees de 4ko
			byte [] buffer = new byte[4000];

			// Un retour negatif est significatif d'une erreur
			if((compte=dis.read(buffer,0,4000))<0)throw new Exception("bytes not read");

			// Sinon lecture reussie
			// Creation d'un autre tableau de byte a partir de celui obtenu plus haut
			byte [] result = new byte[compte];

			// Si le compte n'y est pas on compare les deux tableaux

			// Sinon on ajoute le resultat a la map classee correspond a la file des paquets
			if (compte != 4000)for(int j=0;j<compte;j++)result[j]=buffer[j];
			else result=buffer;
			filePackets.put(i,result);
		}
		return filePackets;
	}

	// Methode permettant de lancer la fonction get
	public void get(String[]var,ClientManager clientm) throws Exception{
		// Declaration des variables dont certaines d'entre elles permettent de recuperer les flux entrants et sortants de donnees du proxy
		PrintStream sortieSocket= new PrintStream(this.proxy.getOutputStream());
		DataInputStream	entreeSocket = new DataInputStream(this.proxy.getInputStream());
		SortedMap<Integer,byte[]> mapping = new TreeMap<Integer,byte[]>();
		byte[] tampon;
		int taille,val;

		// On verifie que le fichier existe dans la memoire du proxy
		if (!isFichier(var[1],entreeSocket,sortieSocket))throw new Exception("file does not exist");

		// On recupere des donnees de stockage concernant le fichier depuis les differents serveurs
		sortieSocket.println("search "+var[1]);
		Map<Integer,ArrayList<Integer>> which= new HashMap<Integer,ArrayList<Integer>>();
		
		// Tant qu'on ne detecte pas d'erreur
		while((taille=entreeSocket.readInt())!= -1){
			if (taille==-2) {
				sortieSocket.close();
				throw new Exception("Upload ratio unreached");
			}
			// Ajout dans la map du contenu du socket d'entree
			which.put(taille, readArrayList(entreeSocket));
		}
		
		// Creation et recuperation du lien vers le premier serveur possedant le fichier
		// Recuperation des cles de la map 
		Set<Integer> keys=which.keySet();
		ArrayList<Integer> keysA=new ArrayList<Integer>(keys);

		// On verifie la correspondance du socket
		Socket i = resolveSocket(keysA.get(0));

		// Creation des liens avec le premier serveur
		PrintStream sortieSock= new PrintStream(i.getOutputStream());
		DataInputStream	entreeSock = new DataInputStream(i.getInputStream());

		// Recuperation de la taille du fichier
		sortieSock.println("size "+var[1]);
		taille=entreeSock.readInt();
		System.out.println("taille : "+taille);
		
		// On divise sa taille pour voir combien de blocs de 4ko on peut faire
		val=taille/4000; 

		// On cree le tampon de la taille du fichier
		tampon = new byte[taille];

		// S'il n'y a qu'un seu1 bloc de 4ko de donnees
		if (val<=1) { 
			sortieSock.println("get "+var[1]);
			sortieSocket.println("get "+var[1]+" 1");
			// Lecture des bytes
			entreeSock.read(tampon);
			
			// On les place dans la map
			mapping.put(0, tampon);

			// S'il y a plus de donnees on peut separer l'information sur les differents serveurs
			// getFichier separe les donnnees a recuperer sur les differents sockets relies au client 
		}	else mapping = getFichier(var[1],taille,which); 


		// Ecriture des donnees du fichier
		if( clientm.ecrireFichier(var[1],mapping)<-1) throw new Exception("Ecrire Fichier failed");		
	}


	// Methode permettant de lire une arraylist<Integer> sur le socket
	private ArrayList<Integer> readArrayList(DataInputStream entreeSocket) throws IOException {
		// Declaration et initialisation des donnees
		String check=entreeSocket.readUTF();
		String[] val=check.split(" ");
		ArrayList<Integer> arraylist= new ArrayList<Integer>();
		// Pour chaque valeur de l'arraylist
		for (String str : val) {
			// On verifie que la chaine soit vide - isBlank() necessite java 11
			if (str.isBlank())continue;
			// On la converti au format Integer puis on l'ajoute
			arraylist.add(Integer.parseInt(str));
		}
		return arraylist;
	}

	// Methode permettant de recuperer le paquet souhaite
	private SortedMap<Integer,byte[]> get(String fileName,Socket sock,int packetSelected) throws Exception{
		// Declaration des streams de lecture et d'ecriture de donnees
		DataInputStream	dis = new DataInputStream(sock.getInputStream());
		PrintStream sortieSocket= new PrintStream(sock.getOutputStream());
		PrintStream prox = new PrintStream(proxy.getOutputStream());

		// Creation de la variable du suivi de lecture
		int compte;
		
		// Envoi des requetes au serveur
		sortieSocket.println("get "+fileName+" "+packetSelected);
		prox.println("get "+fileName+" "+packetSelected);
		
		// On cree une autre map que l'on va retourner et elle contient le numero de paquet voulu et son tableau de bytes
		SortedMap<Integer, byte[]> filePacket = new TreeMap<Integer, byte[]>();	
		byte [] buffer = new byte[4000];
		if((compte=dis.read(buffer,0,4000))<0) throw new Exception("bytes not read");

		// On recopie le buffer afin de stocker que ce qui est effectivement lu puis on compare
		byte [] result= new byte[compte];
		if (compte!= 4000) for (int i = 0; i < compte;i++) result[i]=buffer[i];
		else result=buffer;

		// Ajout dans la map classee
		filePacket.put(packetSelected,result);
		return filePacket;
	}

	// Methode qui permet de remplir la map mapping avec les blocs issus de plusieurs serveurs
	private SortedMap<Integer,byte[]> getFichier(String string, int taille,Map<Integer,ArrayList<Integer>>wich) throws FileNotFoundException, IOException, Exception {
		// Declaration d'une SortedMap de resultat
		SortedMap<Integer,byte[]> result = new TreeMap<Integer,byte[]>();

		// Declaration et initialisation du nombre de paquets pour avoir le fichier entier
		int nbPack = taille/4000;
		int r = taille%4000;

		// Differenciation entre le cas exact et le cas approximatif
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

	// Methode permettant de demander un paquet de facon aleatoire
	private SortedMap<Integer, byte[]> askPacket(Map<Integer,ArrayList<Integer>>wich,String string, int k) throws Exception {
		// Creation de la liste des cles
		Set<Integer> keyreference = wich.keySet();
		ArrayList<Integer> keys = new ArrayList<Integer>(keyreference);

		// Declaration des variables de traitement
		int i,compteur=0;
		Random rand = new Random();

		// Boucle aleatoire de selection de serveur en fonction du paquet souhaite
		while(true) {
			// Recuperation d'un port aleatoire
			i=keys.get(rand.nextInt(keys.size()));
			// Si le serveur associe detient le paquet souhaitee
			if (wich.get(i).contains(k))return get(string,resolveSocket(i),k);
			if(++compteur > (keys.size()+2)*100) {
				System.out.println("retour par non selection de bon serveur");
				return null;
			}
		}
	}

	// Methode qui permet de recuperer la liste des serveurs connectes au proxy - identique a celle du timer Task
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

	// Methode permettant de determiner le socket associe a un port et de savoir si celui-ci est present dans la liste des connexions
	private Socket resolveSocket(int i) {
		// Declaration et initialisation d'une variable de traitement
		int k =0;
		// Si on a pas depasse la fin de la liste de connexion et que le port ne correspond pas au socket alors on continue
		while((socket.get(k++).getPort()!=i)&& (k+1)!=socket.size());
		if (k>socket.size()-1) {// si on a atteint la fin
			System.out.println("not found");
			return null;
		}
		// Sinon on retourne le socket trouve
		return socket.get(k-1); 
	}

	// Methode principale de la classe, elle permet d'executer les demandes du client
	public void menu(ClientManager client) throws Exception {
		// Declaration et initialisation des variables 
		String chaine = "";
		String[] var;

		System.out.println("Tapez vos commandes ou FIN pour arreter : (get, racine)");
		
		// Recuperation de la commande entree
		chaine = scanner.nextLine();

		// Si la fin du dialogue n'est pas demandee
		while (!chaine.equals("FIN")) {
			// On verifie que la chaine possede au moins deux parametres
			var=chaine.split(" ");
			switch(var[0]) {
			// Si la commande entree commence par get
			case "get":
				if (var.length!=2)throw new Exception("invalid parameter number");
				else {
					System.out.println("En attente ...");
					// On fait appel a la methode vue precedemment
					get(var,client);
				}
				break;
			// Si la commande entree commence par racine
			case "racine" :
				if (var.length!=2)throw new Exception("invalid Parameter number");
				try {
					// On change la racine par celle precisee
					client.setRacine(var[1]);
					changeRacine();
					isAllFiles(client,proxy);
				} catch(Exception e) {
					scanner.close();
					e.printStackTrace();
				}
				break;
			// Si la commande entree commence par ls
			case "ls":
				try {
					// Appel a la methode ls
					ls();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("Tapez vos commandes ou FIN pour arreter :");
			
			// Recuperation de la commande entree
			chaine = scanner.nextLine();
		}
		System.out.println("deconnexion.");
		scanner.close();
	}

	// Methode permettant de changer la racine
	private void changeRacine() throws IOException {
		// On recupere la flux de sortie du proxy
		PrintStream ps = new PrintStream(proxy.getOutputStream());
		// On envoi au serveur les donnees
		ps.println("ChangeRoot "+sockServer);
	}
	

	// Methode qui permet d'afficher la liste des fichiers disponible sur le reseau
	private void ls() throws IOException {
		// Declaration et initialisation des flux de donnees recuperes
		DataInputStream is = new DataInputStream(proxy.getInputStream());
		PrintStream ps = new PrintStream(proxy.getOutputStream());
		// On envoi la commande ls au serveur
		ps.println("ls");
		// On lit les donnes que l'on separe ensuite
		String check=is.readUTF();
		String[] val=check.split(" ");
		// On affiche les fichiers disponibles
		System.out.println("Liste des fichiers disponibles sur le reseau :");
		for(String str : val)System.out.println(str);

	}

	// Methode qui renvoie tous les fichiers et leur nombre de blocs
	public void isAllFiles(ClientManager client,Socket sss) throws IOException{
		// Declaration et initialisation des flux de donnees recuperes
		PrintStream os = new PrintStream(sss.getOutputStream());
		String nomBlock;
		// On recupere la racine du client 
		final File folder = new File(client.getracine());
		
		// Pour chaque fichier de la liste des fichiers
		for(final File file : folder.listFiles()){
			// On nomme le bloc de la maniere suivante
			nomBlock = "fichierDispo "+ file.getName() + " " + blockFile(file);
			// Envoi au socket et ecriture des bytes dans l'outputstream
			os.println(nomBlock);
			System.out.println("Envoi...");
		}
	}

	// Methode qui retourne le nombre de blocs du fichier
	public int blockFile(File cheminFichier)throws IOException {
		// Declaration et initialisation des variables
		long taille = cheminFichier.length();
		int nbBlock;
		
		// Si le bloc est inferieur a 4ko alors cela signifie que le fichier est plus petit que 4ko, il y a donc un seul bloc
		if (taille < 4000) {
			nbBlock = 1;
		} else {
			// Sinon on calcule le nombre de blocs necessaires
			if (taille%4000 > 0) {
				nbBlock = ((int) (taille/4000)+1);
			} else {
				nbBlock = (int) (taille/4000);
			}
		}
		return nbBlock;
	}

	// Methode d'execution de la classe
	public void run() {
		// Declaration des variables
		ClientManager clientm;
		Scanner sc=new Scanner(System.in);
		
		// Si l'utilisateur n a pas demande a utiliser un dossier alors on garde le chemin par defaut
		if (this.root==null)clientm=new ClientManager(this);

		// Sinon on modifie le chemin dans le manager
		else clientm=new ClientManager(this,root);

		// On stocke les numeros des sockets dans la liste
		try {
			// Premierement on se connecte au proxy
			if((proxy=new Socket("localhost",12345))==null)throw new Exception("Proxy unreachable");

			// Ensuite on envoie au proxy le port du serveur associe a cet utilisateur
			new DataOutputStream(proxy.getOutputStream()).writeInt(sockServer);
			isAllFiles(clientm,proxy);

			// On met la liste a jour
			miseAJour();
			
			// On ouvre le menu 
			timer.schedule(timTas, 60000,60000);
			this.menu(clientm);

			// On ferme le scanner
			sc.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Methode qui deconnecte le client
	public void deconnect(int nb) throws IOException {
		socket.get(nb).close();
	}
}
