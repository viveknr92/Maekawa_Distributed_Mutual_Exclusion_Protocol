import java.io.IOException;

/**
	 * Run server and then run client thread after some delay.
	 * 
	 */
public class MutualExclusion {
	
	public MutualExclusion(Maekawa object){
		
		Thread incomingMessageThread = new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				object.runServer();
			}
		});
		incomingMessageThread.start();
		try {
			Thread.currentThread();
			Thread.sleep(30);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		Thread outgoingMessageThread = new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					object.runClient();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		outgoingMessageThread.start();
	}
}
