import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class clientConnections extends Thread{
	Socket clientConnected;
	Mutex_Protocol_MsgHandler working;
	
	public clientConnections(Socket clientConnected, Mutex_Protocol object){
		this.clientConnected=clientConnected;
		this.working = new Mutex_Protocol_MsgHandler(object);
	}
	
	public void run(){
		InputStream inputStream = null;
		DataInputStream reader = null;
		String message = null;
		try{
			inputStream = clientConnected.getInputStream();
			reader = new DataInputStream(new BufferedInputStream(inputStream));
			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		while(true){
			try{
				while(reader.available()>0){
					message = reader.readUTF();
					synchronized (working) {
						working.MsgHandling(message);
					}
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
