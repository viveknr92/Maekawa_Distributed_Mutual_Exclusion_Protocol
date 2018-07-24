import java.util.Comparator;

/**
	 * Comparator to manage PriorityQueue.add() so that each request
	 * is prioritized based on both logical clock and process ID (Lamport Total-Ordering)
	 * 
 */


public class PriorityQ implements Comparator<Message> {
	 
	@Override
	public int compare(Message arg0, Message arg1) {
		String[] p,q;
		p = arg0.msgString.split(" ");
		q = arg1.msgString.split(" ");
		
		if (Integer.parseInt(p[1]) > Integer.parseInt(q[1])) return 1;
		if (Integer.parseInt(p[1]) < Integer.parseInt(q[1]) ) return -1;
		if ((Integer.parseInt(p[1])==Integer.parseInt(q[1])) && (Integer.parseInt(p[0]) > Integer.parseInt(q[0]))) return 1;
		if ((Integer.parseInt(p[1])==Integer.parseInt(q[1])) && (Integer.parseInt(p[0]) < Integer.parseInt(q[0]))) return -1;
		if(arg0.msgString.equalsIgnoreCase(arg1.msgString)) return 0;
		return -1;
	}
}