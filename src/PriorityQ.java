import java.util.Comparator;


public class PriorityQ implements Comparator<String> {
	 
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