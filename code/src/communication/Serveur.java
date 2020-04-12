package communication;

import java.net.Socket;

public interface Serveur {
	// Declaration d'une unique fonction permettant la deconnexion d'un socket precise
	public void deconnexion(Socket sss);

}
