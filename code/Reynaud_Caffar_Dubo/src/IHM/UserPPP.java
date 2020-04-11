package IHM;

import java.util.Scanner;
import java.util.Timer;

import communication.Client_TCP;
import communication.Serveur_FTP;

public class UserPPP {
	
	
	public static void main(String[] args) throws Exception{
		Scanner sc = new Scanner(System.in);
		System.out.println("Quel port pour le serveur de cette machine ?");
		Client_TCP client = new Client_TCP();
		Serveur_FTP serveur= new Serveur_FTP(Integer.parseInt(sc.nextLine()));
		Thread tServeur = new Thread(serveur);
		Thread tClient = new Thread(client);
		tServeur.start();
		tClient.start();	
	}


}
