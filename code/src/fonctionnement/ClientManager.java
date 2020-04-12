package fonctionnement;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;
import communication.Client_TCP;

public class ClientManager{
	// Declaration et initialisation des variables
	private String racine="./dossierServeur/";
	
	// Constructeur prenant en parametre un objet de type Client_TCP
	public ClientManager(Client_TCP client) {
		super();
	}
	
	// Constructeur prenant en parametres un objet de type Client_TCP et une chaine de caractere
	public ClientManager(Client_TCP client,String r) {
		super();
		setRacine(r);
	}

	// Methode retourner la racine
	public String getracine() {
		return racine;
	}

	// Methode permettat de changer la racine
	public void setRacine(String r) {
		try {
			// Creation d'un nouveau fichier dont le nom est precise en parametre
			File file = new File(r);
			if (!file.isDirectory())throw new Exception("Not a path Exception");
			else racine=r;
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("new way :");
			// Recuperation de la nouvelle racine
			Scanner sc = new Scanner(System.in);
			setRacine(sc.nextLine());
			// Fermeture du scanner
			sc.close();
		}
	}

	// Methode qui recoit un retour pour l'existence du fichier passer en parametre du get
	public boolean isFichier(DataInputStream es)throws Exception { 	
		// Declaration des variables
		int value;
		// Si le retour est negatif alors c'est significatif d'une erreur
		if ((value=es.readInt())<0)throw new Exception("Reception file exist fail");
		if (value==0)return false;
		return true;
	}

	// Methode permettant d'ecrire dans un fichier le contenu du fichier
	public int ecrireFichier(String string, SortedMap<Integer, byte[]> mapping) throws IOException {
		// Declaration et initialisation de flux sortants de donnees ainsi que d'autres variables
		FileOutputStream fos = new FileOutputStream(this.getracine()+string);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		Set<Entry<Integer, byte[]>> s = mapping.entrySet();
		Iterator<Entry<Integer, byte[]>> i = s.iterator();
		
		while (i.hasNext()) {
			Map.Entry<Integer,byte[] > m =(Map.Entry<Integer,byte[]>)i.next();
			bos.write(m.getValue());
		}
		// Fermeture du buffer de sortie
		bos.close();
		return 0;
	}
}