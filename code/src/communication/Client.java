package communication;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public interface Client {
	public int connect(int i) throws UnknownHostException,IOException;
	public Socket getSocket(int nb);
	public void deconnect(int nb)throws IOException;	
} 
