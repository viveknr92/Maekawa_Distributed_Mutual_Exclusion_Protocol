import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
	 * This class is used to create persistent client connections through
	 * multiple server threads. Each Thread is sharing one ProcessMessage
	 * object to synchronize all server threads.
	 * 
	 */
public class clientConnections extends Thread{
	Socket clientConnected;
	MaekawaMsgHandler working;
	
	public clientConnections(Socket clientConnected, Maekawa object){
		this.clientConnected=clientConnected;
		this.working = new MaekawaMsgHandler(object);
	}
	
	public void run(){
		InputStream inputStream = null;
		ObjectInputStream reader = null;
		Message message = null;
		try{
			inputStream = clientConnected.getInputStream();
			reader = new ObjectInputStream(inputStream);
			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		while(true){
			try{
				try {
					message = (Message) reader.readObject();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				synchronized (working) {
					working.MsgHandling(message);
			}
		}
			catch(EOFException e){
				e.printStackTrace();
				try {
					reader.reset();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}
