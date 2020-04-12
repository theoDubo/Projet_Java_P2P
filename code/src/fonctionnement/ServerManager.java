package fonctionnement;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import communication.Serveur_FTP;

public class ServerManager implements Runnable {
	// Declaration et initialisation des variables
	Socket socket;
	Serveur_FTP serveur;
	String root="./dossierServeur/";

	// Constructeur prenant en parametres un socket et un serveur
	public ServerManager(Socket socket, Serveur_FTP serveur) {
		super();
		this.socket = socket;
		this.serveur = serveur;
	}

	// Constructeur prenant en parametres un socket, une chaine de caracteres et un serveur
	public ServerManager(Socket socket,String chemin, Serveur_FTP serveur) throws Exception {
		super();
		this.socket = socket;
		this.serveur = serveur;
		File file=new File(chemin);
		if(!file.isDirectory())throw new Exception("Not a path Exception");
		else this.root = chemin;
	}

	// Methode correspond au menu utilisateur
	private void menu(Socket sss)throws Exception {
		// Declaration et initialisation des variables
		BufferedReader entreeSocket = new BufferedReader(new InputStreamReader(sss.getInputStream()));
		String chaine="";
		int tst = 0;
		String[] var;
		// Lecture d'une chaine envoyee par le client
		do {
			while ((chaine = entreeSocket.readLine())==null);
			System.out.println("message recu : "+chaine);
			// Verification que la chaine ait au moins deux parametres
			if (!chaine.equals("FIN")) {
				if (chaine.indexOf(" ")<0)throw new Exception("invalid parameter number");
				var=chaine.split(" ");
				switch(var[0]) {
				// Si la commande commence par get
				case "get":
					if (var.length==2) {
						// Appel a la methode get
						tst = get(var[1],sss);
						if (tst<0)throw new Exception("get Exception, failed");
					}
					if (var.length==3) {
						// Appel a la methode get
						tst = get(var[1],sss,var[2]);
						if (tst<0)throw new Exception("get Exception, failed");
					}
					if (var.length==4) {
						// Appel a la methode get
						tst = get(var[1],sss,var[2],var[3]);
						if (tst<0)throw new Exception("get Exception, failed");
					}
					break;
					// Si la commande commence par size
				case "size":
					if (var.length>2) throw new Exception("invalid parameter number");
					// Appel a la methode sizeIs
					sizeIs(var[1],sss);
					break;
				case "ChangeRoot":
					if (var.length>2)throw new Exception("invalid parameter number");
					setRacine(var[1]);

				}
			}

		}while (!chaine.equals("FIN"));

		// On deconnecte le serveur
		serveur.deconnexion(sss);
	}

	//permet de modifier la route du serveur
	public void setRacine(String r) {
		try {
			File file= new File(r);
			if (!file.isDirectory())throw new Exception("Not a path Exception");
			else root=r;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	// Methode permettant de recuperer un fichier
	public int get(String nomFichier,Socket sss) throws IOException {
		// Declaration et initialisation des variables
		String nomF =root+nomFichier;
		// Recuperation du flux sortant de donnees du socket
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		// Declaration du tableau de donnees du fichier
		byte[] data = Files.readAllBytes(Paths.get(nomF));
		// Envoi des donnees au socket
		os.writeInt(data.length);
		System.out.println("Envoi...");
		// Ecriture des bytes dans l'outputstream
		os.write(data,0,data.length);
		os.flush();
		return 0;
	}

	// Methode permettant de recuperer le bloc d'un fichier
	public int get(String nomFichier,Socket sss,String var) throws IOException {
		// Declaration et initialisation des variables
		int nbDuBlock = Integer.parseInt(var);
		String nomF =root+nomFichier;
		File fich = new File(nomF);
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		// Etablit la correspondance en octets (exemple : 3eme bloc correspond au 12000eme octets)
		int valFinBlock = nbDuBlock * 4000;
		// Declaration du chemin du fichier et du tableau de donnees du fichier
		int octets = (int) fich.length();
		byte[] readBytes;
		Path fileLocation = Paths.get(nomF);

		//Si le fichier est inferieur a 4ko
		if (octets < 4000) {
			// On recupere les bytes associes
			readBytes = new byte[octets];
			try (InputStream inputStream = new FileInputStream(fileLocation.toFile()))
			{
				// On ne veut que les bytes nous interessant
				inputStream.skip(0);
				inputStream.read(readBytes, 0,octets);
			}
		} else {
			// Si le fichier est superieur a 4k
			// On recupere les bytes associes
			readBytes = new byte[4000];
			try (InputStream inputStream = new FileInputStream(fileLocation.toFile()))
			{
				// On passe tout les blocs jusqu'au debut du bloc souhaite
				inputStream.skip(valFinBlock-4000);
				int valStandard = 4000;
				// Signifie que le bloc demande est le dernier
				if ((octets > ((nbDuBlock-1) * 4000)) && ((octets/(nbDuBlock) < 4000))) {
					readBytes = new byte[octets%4000];
					valStandard = octets%4000;
				}
				// On va lire jusqu'a la fin du fichier
				inputStream.read(readBytes, 0, valStandard);
			}
		}
		// On envoie au socket
		System.out.println("Envoi...");
		// Ecriture des bytes dans l'outputstream
		os.write(readBytes,0,readBytes.length);
		os.flush();
		return 0;
	}

	// Methode permettant de recuperer plusieurs blocs d'un fichier
	public int get(String nomFichier,Socket sss, String debutPlage, String finPlage) throws IOException { // fonction de rÃ©cupÃ©ration d'un fichier
		// Declaration et initialisation des variables
		int nbDuBlockDebut = Integer.parseInt(debutPlage);
		int nbDuBlockFin = Integer.parseInt(finPlage);
		String nomF =root+nomFichier;
		File fich = new File(nomF);
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		// Declaration du chemin du fichier et du tableau de donnees du fichier
		int octets = (int) fich.length();
		byte[] readBytes;
		Path fileLocation = Paths.get(nomF);
		//Si le fichier est inferieur a 4ko
		if (octets < 4000) {
			readBytes = new byte[octets];
			try (InputStream inputStream = new FileInputStream(fileLocation.toFile()))
			{
				// On passe tout les blocs jusqu'au debut du bloc souhaite
				inputStream.skip(0);
				inputStream.read(readBytes, 0,octets);
			}
			// On envoie au socket
			System.out.println("Envoi...");
			// Ecriture des bytes dans l'outputstream
			os.write(readBytes,0,readBytes.length);
		} else {
			// Si le fichier est superieur a 4k
			// Envoi 4ko par 4ko et creation d'un tableau qui a la taille du dernier segment
			readBytes = new byte[4000];
			try (InputStream inputStream = new FileInputStream(fileLocation.toFile()))
			{
				// On passe tout les blocs jusqu'au debut du bloc souhaite
				if (nbDuBlockDebut == 1) {
					inputStream.skip(0);
				} else {
					inputStream.skip((nbDuBlockDebut-1)*4000);
				}
				for (int i = nbDuBlockDebut ; i <= nbDuBlockFin; i++ ) {
					// On parcourt la plage de blocs
					if (i == nbDuBlockFin && (octets/(nbDuBlockFin) < 4000) ) {
						readBytes = new byte[octets%4000];
						int valStandard = octets%4000;
						// On recupere le nombre de bytes lus
						int bytesReadCount = inputStream.read(readBytes, 0, valStandard);
						System.out.println("J'ai pris"  + bytesReadCount +"  "+ valStandard);
					} else {
						inputStream.read(readBytes, 0, 4000);
					}
				}
				// On envoi au socket
				System.out.println("Envoi...");
				// Ecriture des bytes dans l'outputstream
				os.write(readBytes,0,readBytes.length);
			}
		}
		return 0;
	}

	// Methode permettant de retourner la taille du fichier
	public void sizeIs(String nomFichier, Socket sss)throws IOException {
		// Declaration et initialisation des variables
		String nomF=root+nomFichier;
		File fich =new File(nomF);
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		os.writeInt((int)fich.length());
	}

	@Override
	public void run() {
		// TODO Auto-generated method st ub
		try {
			// On lance le menu
			this.menu(socket);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.run();
		}
	}
}
