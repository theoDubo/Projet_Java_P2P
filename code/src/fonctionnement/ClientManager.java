package fonctionnement;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
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
	private Client_TCP client;
	private String racine="./dossierClient/";
	
	public ClientManager(Client_TCP client) {
		super();
		this.client = client;
	}

	public String getracine() {
		return racine;
	}
	
	public void setRacine(String r) {
		try {
			if (r.charAt(0)!='.' && r.charAt(r.length())!='/') throw new Exception("invalid path definition");
			else racine =r;
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("new way :");
			Scanner sc = new Scanner(System.in);
			setRacine(sc.nextLine());

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




