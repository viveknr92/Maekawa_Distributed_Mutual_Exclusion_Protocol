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
		
		Maekawa object = new Maekawa();
		if (args.length != 6) {
            System.exit(0);
            }
		object.nodeId = Integer.parseInt(args[0]);
		object.noOfNodes = args[1].split("#").length;
		object.nodeNames = new String[object.noOfNodes];
		object.nodePorts = new int[object.noOfNodes];
		for (String s : args[1].split("#") ) {
			String[] parts = s.split("\\s+");
			int nodeIndex = Integer.parseInt(parts[0]);
			object.nodeNames[nodeIndex] = parts[1];
			object.nodePorts[nodeIndex] = Integer.parseInt(parts[2]);
		}		
		object.quorums = args[2].split("\\s+");
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
		object.OutMsgs = new LinkedList<Message>();
		object.inqMsgs = new LinkedList<Message>();
		object.QuorumReply = new HashMap<Integer,Boolean>();
		object.csEnterVector = new int[object.noOfNodes];
		object.csTestVector = new int[object.noOfNodes];
		object.Application();
	}   
}
