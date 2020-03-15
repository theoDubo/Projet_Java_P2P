import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TEST {

	public static void main(String[] args) {
		String root="./dossierServeur/";
		String nomFichier="fichier";
		String nomF =root+nomFichier;
		//ouverture du fichier
		File fich = new File(nomF);

		//crÃ©ation du tampon de la taille du fichier (Ã  Ã©ventuellement remplacÃ© par datagramme par la suite)
		//byte [] tableaudebytes  = new byte [(int)fich.length()];

		Path fileLocation = Paths.get(nomF);
		try {
			byte[] data = Files.readAllBytes(fileLocation);
			System.out.println(data.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}



}

