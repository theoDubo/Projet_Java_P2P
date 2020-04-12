package IHM;
import java.util.Scanner;
import communication.Client_TCP;
import communication.Serveur_FTP;

// Classe de lancement du serveur
public class UserPPP {
	public static void main(String[] args) throws Exception{
		// Declaration et initialisation des variables
		Scanner sc = new Scanner(System.in);
		String path=null;
		int port;
		// Affichage utilisateur et recuperation de la saisie en ligne de commande
		System.out.println("Souhaiter vous choisir un chemin pour le dossier utilisateur ?(O/N)");
		if (sc.nextLine().equalsIgnoreCase("O")) {
			System.out.println("ecrivez le chemin : ");
			path=sc.nextLine();
		}
		System.out.println("Quel port pour le serveur de cette machine ?");
		
		// Creation des differents objets et lancement des threads associes
		Serveur_FTP serveur= new Serveur_FTP((port=Integer.parseInt(sc.nextLine())),path);
		Client_TCP client=new Client_TCP(path,port,sc);
		Thread tServeur = new Thread(serveur);
		Thread tClient = new Thread(client);
		tServeur.start();
		tClient.start();		
	}


}
