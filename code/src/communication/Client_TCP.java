package communication;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client_TCP {
	public static void main(String[] args) throws Exception {
		// Création d'un socket UDP sur un port choisi par le système
		Socket socket = new Socket("localhost",40000);
		
		BufferedReader entreeSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintStream sortieSocket= new PrintStream(socket.getOutputStream());
		
		// Scanner sur System.in
		Scanner scanner = new Scanner(System.in);
		
		String chaine = "";
		
		
		while(!chaine.equalsIgnoreCase("FIN")) {
			System.out.println("Tapez vos phrases ou FIN pour arrêter :");
			// lecture clavier
			chaine = scanner.nextLine();
			sortieSocket.println(chaine);//envoi d'une chaine de caractère
			
			
		}
		socket.close();
	}
}
