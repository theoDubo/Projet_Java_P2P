package communication;

import java.net.Socket;

public interface Serveur {
	public void ecoute()throws Exception;
	public int get(String nomFichier, Socket sss);
	/*public boolean isFichier(String nomFichier);*/
	public void deconnexion(Socket sss);

}
