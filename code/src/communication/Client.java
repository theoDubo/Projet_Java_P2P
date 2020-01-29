package communication;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public interface Client {
	public void connect() throws UnknownHostException,IOException;
	public String getracine();
	public Socket getSocket();
	public void setRacine(String r)throws Exception;
	public void deconnect()throws IOException;	
} 
