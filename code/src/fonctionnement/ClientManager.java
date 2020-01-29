package fonctionnement;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

import communication.Client_TCP;

public class ClientManager implements Runnable {
	private Client_TCP client;
	
	public ClientManager(Client_TCP client) {
		super();
		this.client = client;
	}

	public void get(String[]var) throws Exception {

		int bytesRead,current=0;
		InputStream entreeSocket = client.getSocket().getInputStream();
		if (var.length!=2 ) throw new Exception("invalid parameter number");
/*		pour recevoir la taille du fichier pour créer  le fichier ------------- à voir avec le professeur tout à l'heure*/
		byte[] tampon =new byte[Integer.SIZE];
		if (entreeSocket.read(tampon, 0, 1)<0)throw new Exception("Reception length fail");
		if (Integer.valueOf(tampon.toString()) <0) throw new Exception("Not a number exception");
		tampon = new byte[Integer.valueOf(tampon.toString())];

		//ouverture du stream permettant d'ecrire le fichier
		FileOutputStream fos = new FileOutputStream(client.getracine()+var[1]);
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		// Retourne le nombre de bytes lues
		bytesRead = entreeSocket.read(tampon,current,tampon.length);

		// Tant que le nombre de bytes lues est positif ou nul
		do {
			bytesRead =	entreeSocket.read(tampon, current, (tampon.length-current));
			if(bytesRead >= 0) current += bytesRead;
		} while(bytesRead > -1);

		// Lecture des bytes de ce flux de sortie de bytes dans le tableau tableaudebyte � 0
		bos.write(tampon, 0 , current);
		bos.flush();
		bos.close();
		entreeSocket.close();

	}

	public void menu(Socket socket) throws Exception {
		// Création d'un socket sur un port choisi par le système
		client.connect();
		PrintStream sortieSocket= new PrintStream(socket.getOutputStream());

		// Scanner sur System.in
		Scanner scanner = new Scanner(System.in);
		String chaine = "";
		String[] var;

		System.out.println("Tapez vos commandes ou FIN pour arrêter :");
		chaine = scanner.nextLine();
		while (!chaine.equals("FIN")) {
			//vérification que la chaine ai au moins 2 paramètres
			if (chaine.indexOf(" ")<0)throw new Exception("invalid parameter number");
			sortieSocket.println(chaine);//envoi d'une chaine de caractère
			var=chaine.split(" ");

			switch(var[0]) {
			case "get":
				System.out.println("En attente ...");
				get(var);
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
		}
	}
}


