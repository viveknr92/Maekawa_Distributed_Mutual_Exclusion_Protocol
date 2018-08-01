/**
 * 
 * @author Bharat M Bhavsar, Sri Vidya Varanasi, Arvind Pandiyan
 * @since 04/01/2016
 * @version 1.0
 * @description
 * 	This program uses Maekawa Distributed Mutual Exclusion (DME) Protocol to ensure
 *  that each Critical Execution request is satisfied and mutual
 *  exclusion exists while a process is in critical section. 
 */

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
 * class to generate random number for exponential distribution
 * of csExecution time & interRequest time.
 *
 */
class ExponentialRV{
	protected double mu = 1.0;
	public ExponentialRV(double mu){
		this.mu = mu;
	}
	
	public int nextInt(){
		return (int)(-mu * Math.log(Math.random()));
	}
	
}

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
	
	static MessageProcessor working;
	String[] quorumMembers;
	static int noOfNodes;
	Socket[] clientSocketForNode;
	DataOutputStream[] clientOutputStream;
	DataInputStream[] serverInputStream;
	int nodeId;
	static String[] nodeNames;
	static int[] nodePorts;
	static Queue<String> outgoingMessages;
	Queue<String> inqMessages;
	String configFile;
	int interReqDelay,csExecTime,noOfReq;
	FileOutputStream out = null;
	static Boolean isLocked = false;
	static Integer grantCounter = 0;
	Comparator<String> sortQueue = new PQsort();
	PriorityQueue<String> pendingRequests = new PriorityQueue<String>(10,sortQueue);
	Integer[] lockedProcess = new Integer[2];
	boolean InqSent = false;
	HashMap<Integer,Boolean> quorumResponse;
	public static Integer seqNumber = 0;
	boolean csRequestGranted = false;
	boolean messageOffered = false;
	int[] csEnterVector;
	int[] csTestVector;
	boolean res=true;
	
	/**
	 * Comparator to manage PriorityQueue.add() so that each request
	 * is prioritized based on both logical clock and process ID (Lamport Total-Ordering)
	 * 
	 */
	public static class PQsort implements Comparator<String> {
		 
		@Override
		public int compare(String arg0, String arg1) {
			String[] p,q;
			p = arg0.split(" ");
			q = arg1.split(" ");
			
			if (Integer.parseInt(p[1]) > Integer.parseInt(q[1])) return 1;
			if (Integer.parseInt(p[1]) < Integer.parseInt(q[1]) ) return -1;
			if ((Integer.parseInt(p[1])==Integer.parseInt(q[1])) && (Integer.parseInt(p[0]) > Integer.parseInt(q[0]))) return 1;
			if ((Integer.parseInt(p[1])==Integer.parseInt(q[1])) && (Integer.parseInt(p[0]) < Integer.parseInt(q[0]))) return -1;
			if(arg0.equalsIgnoreCase(arg1)) return 0;
			return -1;
		}
	}
	
	/**
	 * server function which will be called to make server of node
	 * up and running before it starts sending CS request messages
	 * to quorum members. This server function creates new Threads
	 * on each socket.accept() to maintain persistent client connections.
	 */
	public void runServer() {
			
			ServerSocket serverSocketForNode;
			Socket serverSocket=null;
			working = new MessageProcessor();
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
				new clientPersistent(serverSocket).start();
			}
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * This class is used to create persistent client connections through
	 * multiple server threads. Each Thread is sharing one ProcessMessage
	 * object to synchronize all server threads.
	 * 
	 */
	class clientPersistent extends Thread{
		Socket clientConnected;
		
		public clientPersistent(Socket clientConnected){
			this.clientConnected=clientConnected;
			
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
							working.messageProcessing(message);
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
	
	/**
	 * This is actual Maekawa DME protocol algorithm implementation.
	 * The object of this class is shared by each server thread to
	 * synchronize DME for each server threads.
	 * 
	 */
	class MessageProcessor{
	
		String message = null;
		
		public MessageProcessor(String message) {
			this.message=message;
		}
		
		public MessageProcessor() {
			// TODO Auto-generated constructor stub
		}

		public void messageProcessing(String message){
		
		String[] messageParts,tempPendingQueueHead;
		String pendingQueueHead;
		
		synchronized(outgoingMessages){
			if(message!=null){
				messageParts = message.split("#");
				int senderID = Integer.parseInt(messageParts[1].trim());
				int seqNum = Integer.parseInt(messageParts[2].trim());
				int[] newVector= fromString(messageParts[3].trim());
				for (int i=0; i < newVector.length; i++){ 
					if (newVector[i] >= csEnterVector[i])
						csEnterVector[i] = newVector[i];
					
				}
				if(seqNum > seqNumber)
					seqNumber = seqNum;
				seqNumber++;
				switch(messageParts[0]){
				/**
				 * received message: 'Request' to access CS
				 */
				case "REQ":		
					if(!isLocked){
						lockedProcess[0]=senderID;
						lockedProcess[1]=seqNum;
						isLocked = true;
						seqNumber++;
						sendMessage("GRANT", senderID, seqNumber);
					}else{
						//push into pending priority queue based on time stamp.
						messageOffered=pendingRequests.offer(senderID+" "+seqNum);
						String s = pendingRequests.peek();
						String[] parts = s.split(" ");
						int topWaitingID = Integer.parseInt(parts[0].trim());
						int topWaitingSeq = Integer.parseInt(parts[1].trim());
						if(seqNum < lockedProcess[1] || (seqNum == lockedProcess[1] && senderID < lockedProcess[0])){
							if(!InqSent){
								seqNumber++;
								sendMessage("INQ", lockedProcess[0],seqNumber);//sendInq
								InqSent = true;
							}
							else if(topWaitingSeq < seqNum || topWaitingSeq == seqNum && topWaitingID < senderID ){
								seqNumber++;
								sendMessage("FAIL", senderID,seqNumber);//sendFail
							}
						}
						else{
							seqNumber++;
							sendMessage("FAIL", senderID, seqNumber);//send fail
						}
						
					}
					break;
					/**
					 * received message: 'Grant' to access CS
					 */
				case "GRANT":
					boolean done=grantResponse(senderID);
					break;
					/**
					 * received message: 'Inquire' to release access to CS
					 */
				case "INQ":
					if(quorumResponse.containsValue(false)){
						seqNumber++;
						sendMessage("YIELD", senderID, seqNumber);//send yield
						quorumResponse.remove(senderID);
						grantCounter--;
					}else if((grantCounter < quorumMembers.length )){
						
						inqMessages.add(senderID+" "+seqNum);
					}
					break;
					/**
					 * received message: 'Fail' to access CS
					 */
				case "FAIL":						
					//monitor the processes from which fail has been received
					quorumResponse.put(senderID, false);
					//Check if there is any in inq queue, if yes send yield, decrement grantCounter.
					while(!inqMessages.isEmpty()){
						String m = inqMessages.poll();
						String[] p = m.split(" ");
						seqNumber++;
						sendMessage("YIELD", Integer.parseInt(p[0]), seqNumber);
						quorumResponse.remove(Integer.parseInt(p[0]));
						grantCounter--;
					}
					break;
					/**
					 * received message: 'Yield' to give back access of CS
					 */
				case "YIELD":
					isLocked = false;
					InqSent = false;
					
					messageOffered=pendingRequests.offer(lockedProcess[0]+" "+lockedProcess[1]);
					pendingQueueHead = pendingRequests.peek();
					if(pendingQueueHead==null){
						lockedProcess[0]=null;
						lockedProcess[1]=null;
					}else if(pendingQueueHead!=null){
						pendingQueueHead = pendingRequests.poll();
						tempPendingQueueHead = pendingQueueHead.split(" ");
						lockedProcess[0]=Integer.parseInt(tempPendingQueueHead[0]);
						lockedProcess[1]=Integer.parseInt(tempPendingQueueHead[1]);
						isLocked = true;
						seqNumber++;
						sendMessage("GRANT", Integer.parseInt(tempPendingQueueHead[0]), seqNumber);
					}
					break;
					/**
					 * received message: 'Release' after CS access
					 */
				case "REL":
					isLocked = false;
					InqSent = false;
					pendingQueueHead = pendingRequests.peek();
					if(pendingQueueHead==null){
						lockedProcess[0]=null;
						lockedProcess[1]=null;
					}else if(pendingQueueHead!=null){
						pendingQueueHead = pendingRequests.poll();
						tempPendingQueueHead = pendingQueueHead.split(" ");
						lockedProcess[0]=Integer.parseInt(tempPendingQueueHead[0]);
						lockedProcess[1]=Integer.parseInt(tempPendingQueueHead[1]);
						isLocked = true;
						seqNumber++;
						sendMessage("GRANT", Integer.parseInt(tempPendingQueueHead[0]), seqNumber);
					}
					break;
				default:
					break;						
				}
			}
		}
		}
	
	}
	
	/**
	 * This client function is used to create single client thread.
	 * This client thread will first create socket connection and
	 * outputStream for each socket and connects to each possible server
	 * and saves these connections to use same outputStream each time for
	 * same server connection.
	 * @throws IOException
	 */
	public void runClient() throws IOException{
		clientSocketForNode = new Socket[noOfNodes];
		clientOutputStream = new DataOutputStream[noOfNodes];
		String currentOutgoingMessege = null;
		String[] messageParts;
		/**
		 * create and save all client outgoingStreams for all possible
		 * servers that node can connect to
		 */
		for(int neighbor=0; neighbor<noOfNodes;){
			try{
				clientSocketForNode[neighbor] = new Socket(nodeNames[neighbor], nodePorts[neighbor]);
				clientOutputStream[neighbor] = new DataOutputStream(clientSocketForNode[neighbor].getOutputStream());
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
			synchronized (outgoingMessages) {
				while(!outgoingMessages.isEmpty()){
					currentOutgoingMessege = outgoingMessages.poll();
					messageParts = currentOutgoingMessege.split("#");
					int receiverID = Integer.parseInt(messageParts[4].trim());
					String messageToBeSent = messageParts[0]+ "#" + messageParts[1] + "#" + messageParts[2] + "#" + messageParts[3];
					clientOutputStream[receiverID].writeUTF(messageToBeSent);
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
		quorumResponse.put(senderID,true);
		grantCounter++;		
		if(grantCounter == quorumMembers.length){
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
	private static int[] fromString(String string) {
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
		synchronized(outgoingMessages){
			seqNumber++;
			
			boolean done=broadcastToQuorum("REQ", seqNumber);
		}
			
		try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        };
        
        synchronized(outgoingMessages){
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
		int[] tempArray=null;
		synchronized(outgoingMessages){
			//broadcast release
			seqNumber++;
			csEnterVector[nodeId]++;
			boolean done=broadcastToQuorum("REL", seqNumber);
			grantCounter = 0;
			csRequestGranted = false;
			quorumResponse = new HashMap<Integer,Boolean>();
			inqMessages = new LinkedList<String>();
			tempArray = csEnterVector;
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
		System.out.println("Exiting CS "+nodeId + " "+ Arrays.toString(tempArray)+" "+" DME::"+res);
	}
	
	/**
	 * function to generate all broadcast messages
	 * @param message
	 * @param currentSeqNumber
	 * @return
	 */
	public boolean broadcastToQuorum(String message, int currentSeqNumber){
			for(String s : quorumMembers){
				sendMessage(message, Integer.parseInt(s), currentSeqNumber);
				
			}
		return true;
	}
	
	/**
	 * function to generate message to be sent and put it into outgoingMessages queue
	 * @param message
	 * @param neighbor
	 * @param currentSeqNumber
	 */
	public void sendMessage(String message, int neighbor, int currentSeqNumber){
			String messageToQueue = message+"#"+nodeId+"#"+currentSeqNumber+"#"+Arrays.toString(csEnterVector)+"#"+neighbor;
			outgoingMessages.add(messageToQueue);
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
				Thread.sleep(new ExponentialRV(csExecTime).nextInt());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			csExit();
			
			try {
				//if(difference>0)
					Thread.sleep(new ExponentialRV(interReqDelay).nextInt());
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
	
	
	/**
	 * Main of the class to parse all input arguments received on command line
	 * and call main application once all parsed arguments are saved.
	 * @param args
	 */
	public static void main(String args[]) {
		
		Maekawa object = new Maekawa();
		if (args.length != 6) {
            System.exit(0);
            }
		object.nodeId = Integer.parseInt(args[0]);
		noOfNodes = args[1].split("#").length;
		nodeNames = new String[noOfNodes];
		nodePorts = new int[noOfNodes];
		for (String s : args[1].split("#") ) {
			String[] parts = s.split("\\s+");
			int nodeIndex = Integer.parseInt(parts[0]);
			nodeNames[nodeIndex] = parts[1];
			nodePorts[nodeIndex] = Integer.parseInt(parts[2]);
		}		
		object.quorumMembers = args[2].split("\\s+");
		object.csExecTime = Integer.parseInt(args[3]);
		object.interReqDelay = Integer.parseInt(args[4]);
		object.noOfReq = Integer.parseInt(args[5].trim());
		object.configFile = "output";
		try {
			object.out = new FileOutputStream(object.configFile+".out");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			object.out = new FileOutputStream("logs-"+object.nodeId+".out");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Maekawa.outgoingMessages = new LinkedList<String>();
		object.inqMessages = new LinkedList<String>();
		object.quorumResponse = new HashMap<Integer,Boolean>();
		object.csEnterVector = new int[noOfNodes];
		object.csTestVector = new int[noOfNodes];
		object.Application();
	}   
}
