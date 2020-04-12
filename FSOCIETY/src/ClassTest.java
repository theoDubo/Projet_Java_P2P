import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClassTest {

	public static void main(String[] args) {

		// Declaration des variables relatives au fichier
		String root="./dossierServeur/";
		String nomFichier="fichier";
		String nomF =root+nomFichier;

		// Ouverture du fichier
		File fich = new File(nomF);
		
		// Recuperation de l'emplacement du fichier
		Path fileLocation = Paths.get(nomF);
		try {
			// Tableau de byte stockant les bytes du fichier que l'on affiche par la suite
			byte[] data = Files.readAllBytes(fileLocation);
			System.out.println(data.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

