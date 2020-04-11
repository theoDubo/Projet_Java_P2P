package fonctionnement;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;
import communication.Client_TCP;

public class ClientManager{

	private String racine="./dossierServeur/";
	public ClientManager(Client_TCP client) {
		super();
	}
	
	public ClientManager(Client_TCP client,String r) {
		super();
		setRacine(r);
	}


	public String getracine() {
		return racine;
	}
	
	public void setRacine(String r) {
		try {
			File file= new File(r);
			if (!file.isDirectory())throw new Exception("Not a path Exception");
			else racine=r;
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("new way :");
			Scanner sc = new Scanner(System.in);
			setRacine(sc.nextLine());
			sc.close();
		}
	}

	public boolean isFichier(DataInputStream es)throws Exception { 	//méthode qui reçoit le retour pour l'existence du fichier passer en paramètre du get
		int value;
		if ((value=es.readInt())<0)throw new Exception("Reception file exist fail");
		if (value==0)return false;
		return true;
	}

	public int ecrireFichier(String string, SortedMap<Integer, byte[]> mapping) throws IOException {
		// TODO Auto-generated method stub
		FileOutputStream fos = new FileOutputStream(this.getracine()+string);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		Set<Entry<Integer, byte[]>> s = mapping.entrySet();
		Iterator<Entry<Integer, byte[]>> i = s.iterator();
		while (i.hasNext()) {
			Map.Entry<Integer,byte[] > m =(Map.Entry<Integer,byte[]>)i.next();
			bos.write(m.getValue());
		}
		bos.close();
		return 0;
	}


	//Renvoie un fichier et son nombre de block
	public void isAllFiles(Socket sss) throws IOException{
		PrintStream os = new PrintStream(sss.getOutputStream());
		final File folder = new File(racine);
		//définition d'une SortedMap de résultat et d'une de remplissage
		String nomBlock;
		for(final File file : folder.listFiles()){
			nomBlock = "fichierDispo "+ file.getName() + " " + blockFile(file);
			// On envoi ça au socket
			// Ecriture des bytes dans l'outputstream
			os.println(nomBlock);
			System.out.println("Envoi...");
		}
	}

	//Return le nombre de block du fichier
	public int blockFile(File cheminFichier)throws IOException {
		long taille = cheminFichier.length();
		int nbBlock;
		if ( taille < 4000) {
			nbBlock = 1;
		} else {
			if (taille%4000 > 0) {
				nbBlock = ((int) (taille/4000)+1);
			} else {
				nbBlock = (int) (taille/4000);
			}
		}
		return nbBlock;
	}

/*
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			this.menu(client);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.run();
		}
	}
*/
}




