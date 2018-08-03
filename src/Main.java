import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedList;

public class Main {
	/**
	 * Main of the class to parse all input arguments received on command line
	 * and call main application once all parsed arguments are saved.
	 * @param args
	 */
	public static void main(String args[]) {
		
		Mutex_Protocol mutex = new Mutex_Protocol();
		if (args.length != 6) {
            System.exit(0);
            }
		mutex.nodeId = Integer.parseInt(args[0]);
		mutex.noOfNodes = args[1].split("#").length;
		mutex.nodeNames = new String[mutex.noOfNodes];
		mutex.nodePorts = new int[mutex.noOfNodes];
		for (String s : args[1].split("#") ) {
			String[] parts = s.split("\\s+");
			int nodeIndex = Integer.parseInt(parts[0]);
			mutex.nodeNames[nodeIndex] = parts[1];
			mutex.nodePorts[nodeIndex] = Integer.parseInt(parts[2]);
		}		
		mutex.quorums = args[2].split("\\s+");
		mutex.csExecTime = Integer.parseInt(args[3]);
		mutex.interReqDelay = Integer.parseInt(args[4]);
		mutex.noOfReq = Integer.parseInt(args[5].trim());
		mutex.configFile = "output";
		try {
			mutex.out = new FileOutputStream(mutex.configFile+".out");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			mutex.out = new FileOutputStream("logs-"+mutex.nodeId+".out");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mutex.sentMsgQueue = new LinkedList<String>();
		mutex.inqMsgs = new LinkedList<String>();
		mutex.hasReceivedFailed = new HashMap<Integer,Boolean>();
		mutex.csEnterVector = new int[mutex.noOfNodes];
		mutex.csTestVector = new int[mutex.noOfNodes];
		mutex.Application();
	}   
}
