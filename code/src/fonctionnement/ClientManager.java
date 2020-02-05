package fonctionnement;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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

	public String ByteToSt(byte[] tampon){
		String chaine="";
		int fraise=0xff;
		for (int i =0; i<tampon.length;i++) {
			chaine=chaine + ((int)tampon[i]&fraise);
		}
		return chaine;
	}
	
	public void get(String var) throws Exception {
		int bytesRead,current=0;
		InputStream entreeSocket =client.getSocket().getInputStream();
		if (var==null ) throw new Exception("invalid parameter number");
		byte[] tampon = new byte[550000];



		if (entreeSocket.read(tampon,0,tampon.length)<0)throw new Exception("Reception file exist fail");

		if (Integer.valueOf(ByteToSt(tampon))!=1)throw new Exception("file does not exist");
		//		pour recevoir la taille du fichier pour crÃ©er  le fichier ------------- Ã  voir avec le professeur tout Ã  l'heure
		if (entreeSocket.read(tampon,0,tampon.length)<0)throw new Exception("Reception length fail");

		int value; 
		if ((value =Integer.valueOf(ByteToSt(tampon))) <0) throw new Exception("Not a number exception");
		tampon = new byte[Integer.valueOf(value)];
		 
		//ouverture du stream permettant d'ecrire le fichier - ok 
		FileOutputStream fos = new FileOutputStream(client.getracine()+var);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		System.out.println("file created");

		// Tant que le nombre de bytes lues est positif ou nul
		bytesRead = entreeSocket.read(tampon,0,tampon.length);
		System.out.println("bit lues "+bytesRead);
		current = bytesRead;
		/*		// Tant que le nombre de bytes lues est positif ou nul
		do {
			bytesRead = entreeSocket.read(tampon, current, (tampon.length-current));
			if(bytesRead >= 0) current += bytesRead;
		} while(bytesRead > -1);
		 */
		// Lecture des bytes de ce flux de sortie de bytes dans le tableau tableaudebyte ï¿½ 0
		bos.write(tampon,0,tampon.length);
		bos.flush();
		bos.close();
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


