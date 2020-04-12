package communication;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public interface Client {
	// Declaration de trois fonctions dans cette interface donc le but est de connecter, recuperer et deconnecte un socket
	public int connect(int i) throws UnknownHostException,IOException;
	public Socket getSocket(int nb);
	public void deconnect(int nb)throws IOException;	
} 
