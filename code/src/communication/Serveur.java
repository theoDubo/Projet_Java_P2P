package communication;

import java.net.Socket;

public interface Serveur {
	public void ecoute(int val)throws Exception;
	public void deconnexion(Socket sss);

}
