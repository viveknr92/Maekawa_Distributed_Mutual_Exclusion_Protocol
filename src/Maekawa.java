import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;


/**
 * Main class which handles input arguments at command line
 * and executes DME program with help of persistent client server connection
 * where server processes all incoming messages and adds response to message
 * queue which is accessed by client thread to send message of respective
 * outgoingStream. Initial request is created through broadcast using client thread
 * and server waits for accept() and create new server thread on each socket.accept().
 * This is to maintain persistent connection for every client request.  
 *
 */
public class Maekawa {
	
	String[] quorums;
	int noOfNodes;
	Socket[] clientSocket;
	DataOutputStream[] clientOStream;
	DataInputStream[] serverInStream;
	int nodeId;
	String[] nodeNames;
	int[] nodePorts;
	Queue<String> OutMsgs;
	Queue<String> inqMsgs;
	String configFile;
	int interReqDelay,csExecTime,noOfReq;
	FileOutputStream out = null;
	Boolean isLocked = false;
	Integer NoOfGrants = 0;
	Comparator<String> OurQueue = new PriorityQ();
	PriorityQueue<String> pendingRequests = new PriorityQueue<String>(10,OurQueue);
	Integer[] lockedProcess = new Integer[2];
	boolean InqSent = false;
	HashMap<Integer,Boolean> QuorumReply;
	public Integer seqNumber = 0;
	boolean csRequestGranted = false;
	boolean messageOffered = false;
	int[] csEnterVector;
	int[] csTestVector;
	boolean res=true;
	
	/**
	 * server function which will be called to make server of node
	 * up and running before it starts sending CS request messages
	 * to quorum members. This server function creates new Threads
	 * on each socket.accept() to maintain persistent client connections.
	 */
	public void runServer() {
			
			ServerSocket serverSocketForNode;
			Socket serverSocket=null;
			try
			{
			serverSocketForNode = new ServerSocket(nodePorts[nodeId]);
			
			while(true)
			{
				try
				{
				serverSocket = serverSocketForNode.accept();
				serverSocket.setTcpNoDelay(true);
				}
				catch(IOException e){
					
				}
				new clientConnections(serverSocket, this).start();
			}
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * This is actual Maekawa DME protocol algorithm implementation.
	 * The object of this class is shared by each server thread to
	 * synchronize DME for each server threads.
	 * 
	 */
	
	
	/**
	 * This client function is used to create single client thread.
	 * This client thread will first create socket connection and
	 * outputStream for each socket and connects to each possible server
	 * and saves these connections to use same outputStream each time for
	 * same server connection.
	 * @throws IOException
	 */
	public void runClient() throws IOException{
		clientSocket = new Socket[noOfNodes];
		clientOStream = new DataOutputStream[noOfNodes];
		String currentOutgoingMessege = null;
		String[] messageParts;
		/**
		 * create and save all client outgoingStreams for all possible
		 * servers that node can connect to
		 */
		for(int neighbor=0; neighbor<noOfNodes;){
			try{
				clientSocket[neighbor] = new Socket(nodeNames[neighbor], nodePorts[neighbor]);
				clientOStream[neighbor] = new DataOutputStream(clientSocket[neighbor].getOutputStream());
				neighbor++;
			}
			catch (ConnectException e){
				try {
					Thread.currentThread();
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		
		/**
		 * pick each message popped from outgoingMessage queue
		 * and put into respective outgoingStream until queue
		 * is empty.
		 */
		while(true){
			synchronized (OutMsgs) {
				while(!OutMsgs.isEmpty()){
					currentOutgoingMessege = OutMsgs.poll();
					messageParts = currentOutgoingMessege.split("#");
					int receiverID = Integer.parseInt(messageParts[4].trim());
					String messageToBeSent = messageParts[0]+ "#" + messageParts[1] + "#" + messageParts[2] + "#" + messageParts[3];
					clientOStream[receiverID].writeUTF(messageToBeSent);
				}
			}
		
		}
	}
	
	/**
	 * to check if node has received all grants to enter CS
	 * @param senderID
	 * @return
	 */
	public synchronized boolean grantResponse(int senderID){
		QuorumReply.put(senderID,true);
		NoOfGrants++;		
		if(NoOfGrants == quorums.length){
			csRequestGranted=true;
			notifyAll();
		}
		return true;
	}
	
	/**
	 * convert String array to integer array
	 * @param string
	 * @return
	 */
	public static int[] fromString(String string) {
	    String[] strings = string.replace("[", "").replace("]", "").split(", ");
	    int result[] = new int[strings.length];
	    for (int i = 0; i < result.length; i++) {
	      result[i] = Integer.parseInt(strings[i]);
	    }
	    return result;
	  }
	
	/**
	 * Run server and then run client thread after some delay.
	 * 
	 */
	class MutualExclusion {
		
		public MutualExclusion(){
			
			Thread incomingMessageThread = new Thread(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					runServer();
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
						runClient();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			outgoingMessageThread.start();
		}
	}
	
	/**
	 * function to generate all Critical Section (CS) enter
	 * requests through broadcast to quorum members
	 * and wait until all requests are satisfied.
	 * This is blocking call at node end waiting notifyAll() from grantResponse().
	 */
	synchronized void csEnter(){
		synchronized(OutMsgs){
			seqNumber++;
			
			boolean done=broadcastToQuorum("REQ", seqNumber);
		}
			
		try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        };
        
        synchronized(OutMsgs){
        	csEnterVector[nodeId]++;
        	for(int i = 0;i< csEnterVector.length ; ++i)
        		csTestVector[i] = csEnterVector[i];
        }
	}
	
	/**
	 * function to release CS by sending broadcast message to
	 * all quorum members and initializing all related variables/ datastructures
	 */
	void csExit(){
		int[] MyArray=null;
		synchronized(OutMsgs){
			//broadcast release
			seqNumber++;
			csEnterVector[nodeId]++;
			boolean done=broadcastToQuorum("REL", seqNumber);
			NoOfGrants = 0;
			csRequestGranted = false;
			QuorumReply = new HashMap<Integer,Boolean>();
			inqMsgs = new LinkedList<String>();
			MyArray = csEnterVector;
			for(int i = 0;i< csEnterVector.length ; ++i)
				if(i!=nodeId&& csTestVector[i]!=csEnterVector[i])
					res =false;
			try {
				out.write((Arrays.toString(csEnterVector)+"\n").getBytes());
				//out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		System.out.println("Exiting CS "+nodeId + " "+ Arrays.toString(MyArray)+" "+" DME::"+res);
	}
	
	/**
	 * function to generate all broadcast messages
	 * @param message
	 * @param currentSeqNumber
	 * @return
	 */
	public boolean broadcastToQuorum(String message, int currentSeqNumber){
			for(String s : quorums){
				sendMessage(message, Integer.parseInt(s), currentSeqNumber);
				
			}
		return true;
	}
	
	/**
	 * function to generate message to be sent and put it into OutMsgs queue
	 * @param message
	 * @param neighbor
	 * @param currentSeqNumber
	 */
	public void sendMessage(String message, int neighbor, int currentSeqNumber){
			String messageToQueue = message+"#"+nodeId+"#"+currentSeqNumber+"#"+Arrays.toString(csEnterVector)+"#"+neighbor;
			OutMsgs.add(messageToQueue);
	}
	
	/**
	 * Actual application module to call server to make it up and running and
	 * then create CS enter broadcast requests through csEnter() and
	 * release requests through csExit() calls
	 * Note that csEnter() is blocking call.
	 */
	public void Application (){
		MutualExclusion me = new MutualExclusion();
		
		try {
			Thread.currentThread();
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		for(int i =0 ; i < noOfReq; ++i ){
			csEnter();
			try {
				Thread.sleep(new ExpProbTime(csExecTime).NextNum());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			csExit();
			
			try {
				//if(difference>0)
					Thread.sleep(new ExpProbTime(interReqDelay).NextNum());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		try {
			//out.write(logs.getBytes());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(nodeId + " is finished with all requests");
	}
}
