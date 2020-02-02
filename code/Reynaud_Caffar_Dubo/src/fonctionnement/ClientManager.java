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
	
	
	
	
	
	

	public void get(String var) throws Exception {
		System.out.println("je suis entré dans la fonction");
		int bytesRead,current=0;
		InputStream entreeSocket =client.getSocket().getInputStream();
		if (var==null ) throw new Exception("invalid parameter number");
		byte[] tampon = new byte[5404444];

/*		pour recevoir la taille du fichier pour créer  le fichier ------------- à voir avec le professeur tout à l'heure
		if (entreeSocket.read(tampon,0,tampon.length)<0)throw new Exception("Reception length fail");

		System.out.println(tampon.toString() + "Jean michel henry ----------------------------");
		if (Integer.valueOf(tampon.toString()) <0) throw new Exception("Not a number exception");
		tampon = new byte[Integer.valueOf(tampon.toString())];
*/
		System.out.println("en fonctionnement");
		
		//ouverture du stream permettant d'ecrire le fichier - ok 
		FileOutputStream fos = new FileOutputStream(client.getracine()+var);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		System.out.println("fichier créé");
				
		// Tant que le nombre de bytes lues est positif ou nul
		bytesRead = entreeSocket.read(tampon,0,tampon.length);
		System.out.println("bit lues "+bytesRead);
		current = bytesRead;
		
		// Tant que le nombre de bytes lues est positif ou nul
		do {
			bytesRead = entreeSocket.read(tampon, current, (tampon.length-current));
			if(bytesRead >= 0) current += bytesRead;
		} while(bytesRead > -1);
		// Lecture des bytes de ce flux de sortie de bytes dans le tableau tableaudebyte � 0
		bos.write(tampon,0,tampon.length);
		bos.flush();
		bos.close();
		entreeSocket.close();

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
			//vérification que la chaine ai au moins 2 paramètres
			if (chaine.indexOf(" ")<0)throw new Exception("invalid parameter number");
			sortieSocket.println(chaine);//envoi d'une chaine de caractère
			var=chaine.split(" ");

			switch(var[0]) {
			case "get":
				System.out.println("En attente ...");
				get(var[1]);
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


