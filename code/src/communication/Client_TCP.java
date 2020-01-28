package communication;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client_TCP {
	
	public static void main(String[] args) throws Exception {
		byte[] tampon = new byte[1024];
		// Création d'un socket UDP sur un port choisi par le système
		Socket socket = new Socket("localhost",40000);

		BufferedReader entreeSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintStream sortieSocket= new PrintStream(socket.getOutputStream());
		
		// Scanner sur System.in
		Scanner scanner = new Scanner(System.in);
		
		String chaine = "",capture;
		
		while(!chaine.equalsIgnoreCase("FIN")) {
			System.out.println("Tapez vos commandes ou FIN pour arrêter :");
			// lecture clavier
			chaine = scanner.nextLine();
			sortieSocket.println(chaine);//envoi d'une chaine de caractère
			capture=entreeSocket.readLine();
			System.out.println(capture);
			
			
		}
		socket.close();
		scanner.close();
		entreeSocket.close();
	}
}
