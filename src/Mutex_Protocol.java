import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;


public class Mutex_Protocol {
	
	String[] quorums;
	int noOfNodes;
	Socket[] clientSocket;
	DataOutputStream[] clientOStream;
	DataInputStream[] serverInStream;
	int nodeId;
	String[] nodeNames;
	int[] nodePorts;
	Queue<String> sentMsgQueue;
	Queue<String> inqMsgs;
	String configFile;
	int interReqDelay,csExecTime,noOfReq;
	FileOutputStream out = null;
	Boolean isLocked = false;
	Integer NumLocks = 0;
	Comparator<String> OurQueue = new PriorityQ();
	PriorityQueue<String> requestQueue = new PriorityQueue<String>(10,OurQueue);
	Integer[] lockedProcess = new Integer[2];
	boolean isInqSent = false;
	HashMap<Integer,Boolean> hasReceivedFailed;
	public Integer timeStamp = 0;
	boolean csRequestGranted = false;
	boolean msgSent = false;
	int[] csEnterVector;
	int[] csTestVector;
	boolean res=true;
	int totalMsgsCount;
	long startTime;
	long endTime;
	long startTime_throughput;
	long endTime_throughput;
	//PrintWriter writer;
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
		

		while(true){
			synchronized (sentMsgQueue) {
				while(!sentMsgQueue.isEmpty()){
					currentOutgoingMessege = sentMsgQueue.poll();
					messageParts = currentOutgoingMessege.split("#");
					int receiverID = Integer.parseInt(messageParts[4].trim());
					String messageToBeSent = messageParts[0]+ "#" + messageParts[1] + "#" + messageParts[2] + "#" + messageParts[3];
					clientOStream[receiverID].writeUTF(messageToBeSent);
				}
			}
		
		}
	}
	
	public synchronized boolean grantLock(int senderID){
		hasReceivedFailed.put(senderID,true);
		NumLocks++;		
		if(NumLocks == quorums.length){
			csRequestGranted=true;
			notifyAll();
		}
		return true;
	}

	public static int[] fromString(String string) {
	    String[] strings = string.replace("[", "").replace("]", "").split(", ");
	    int result[] = new int[strings.length];
	    for (int i = 0; i < result.length; i++) {
	      result[i] = Integer.parseInt(strings[i]);
	    }
	    return result;
	  }
	

	class MutualExclusion {
		
		public MutualExclusion(){
			
			Thread incomingMessageThread = new Thread(new Runnable(){
				@Override
				public void run() {
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
	

	synchronized void csEnter(){
		synchronized(sentMsgQueue){
			timeStamp++;
			startTime = System.currentTimeMillis();			
			boolean done=broadcastToQuorum("request", timeStamp);
		}
			
		try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        };
        System.out.println("Node: " + nodeId + " Enter CS ");
        synchronized(sentMsgQueue){
        	csEnterVector[nodeId]++;
        	for(int i = 0;i< csEnterVector.length ; ++i)
        		csTestVector[i] = csEnterVector[i];
        }
	}

	void csExit(){
		int[] MyArray=null;
		synchronized(sentMsgQueue){
			timeStamp++;
			csEnterVector[nodeId]++;
			endTime   = System.currentTimeMillis();
			boolean done=broadcastToQuorum("release", timeStamp);
			NumLocks = 0;
			csRequestGranted = false;
			hasReceivedFailed = new HashMap<Integer,Boolean>();
			inqMsgs = new LinkedList<String>();
			MyArray = csEnterVector;
			for(int i = 0;i< csEnterVector.length ; ++i)
				if(i!=nodeId&& csTestVector[i]!=csEnterVector[i])
					res =false;
			try {
				out.write((Arrays.toString(csEnterVector)+"\n").getBytes());
			
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Node: " + nodeId + " Leave CS "+ " "+ Arrays.toString(MyArray));
			System.out.println("Node: " + nodeId + " Response time : " + (endTime - startTime));
			//writer.println("Response time: " + (endTime - startTime));
		}
		
	}

	public boolean broadcastToQuorum(String message, int currentSeqNumber){
			for(String s : quorums){
				sendMessage(message, Integer.parseInt(s), currentSeqNumber);
				
			}
		return true;
	}

	public void sendMessage(String message, int neighbor, int currentSeqNumber){
			totalMsgsCount++;
			String messageToQueue = message+"#"+nodeId+"#"+currentSeqNumber+"#"+Arrays.toString(csEnterVector)+"#"+neighbor;
			sentMsgQueue.add(messageToQueue);
	}
	
	public void Application (){
		MutualExclusion me = new MutualExclusion();
//		File file = new File("node_" + noOfNodes +
//					"/" + "test_data_" + nodeId + "_" + interReqDelay + "_" + csExecTime + ".csv");
//
//		try {
//			file.createNewFile();
//		} catch (IOException e3) {
//			// TODO Auto-generated catch block
//			e3.printStackTrace();
//		}
//		try {
//			writer = new PrintWriter(file);
//		} catch (FileNotFoundException e2) {
//			e2.printStackTrace();
//		}
		try {
			Thread.currentThread();
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		startTime_throughput = startTime = System.currentTimeMillis();	
		for(int i =0 ; i < noOfReq; ++i ){
			csEnter();
			try {
				Thread.sleep(new ExpProbTime(csExecTime).RandomNum());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			csExit();
			
			try {
		
					Thread.sleep(new ExpProbTime(interReqDelay).RandomNum());
			} catch (InterruptedException e) {
			
				e.printStackTrace();
			}
		}
		
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		endTime_throughput = System.currentTimeMillis();
		System.out.println("Node: " + nodeId + " Total Msg Complexity: " + totalMsgsCount);
		System.out.println("Node: " + nodeId + " is finished with all requests");
		System.out.println("Node: " + nodeId + " Total Time taken : " + (endTime_throughput - startTime_throughput));
//		writer.println("Total Msg Complexity: " + totalMsgsCount);
//		writer.println("Total Response Time: " + (endTime_throughput - startTime_throughput));
//		writer.close();
	}
}
