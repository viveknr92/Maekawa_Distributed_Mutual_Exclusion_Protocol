/**
 * @author 
 * @since 
 * @version 1.0
 * This is tester code to check if mutual execution persists in given
 * output file set.
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
/**
 * Test class which accepts two command line arguments: number of nodes and number of requests
 * and checks whether mutual exclusion is achieved. 
 *
 */
public class MutexTester {
	int noOfNodes, noOfRequests;
	BufferedReader[] files;
	int[][] vectors;
	/**
	 * Method to convert string to integer array
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
	 * Method to check if the two arrays (vectors) are comparable or not using vector clock.
	 * This helps to conclude whether the processes executed critical section in concurrence or not.
	 * @param v First vector to compare
	 * @param w Second vector to compare
	 * @return
	 */
	static boolean	isconcurrent(int v[], int w[])
	{
		boolean greater=false, less=false;

		for (int i=0; i < v.length; i++) 
			if (v[i] > w[i])
				greater = true;
			else if (v[i] < w[i])
				less = true;
		if (greater && less)
			return true;	/* the vectors are concurrent */
		else
			return false;	/* the vectors are not concurrent */
	}
	public static void main(String args[]) throws IOException {
		
		MutexTester object = new MutexTester();
		if (args.length != 2) {
            System.exit(0);
            }
		object.noOfNodes = Integer.parseInt(args[0]);
		object.noOfRequests = Integer.parseInt(args[1]);
		object.files = new BufferedReader[object.noOfNodes];
		object.vectors = new int[object.noOfNodes][];
		for(int i =0;i<object.noOfNodes;++i){
			try {
				object.files[i] = new BufferedReader(new InputStreamReader(new FileInputStream("logs-"+i+".out")));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		boolean concurrent=false;
		for(int j = 0;j<object.noOfRequests&!concurrent;++j){
			for(int i =0;i<object.noOfNodes;++i){
				object.vectors[i] = fromString(object.files[i].readLine());
			}
			for(int i =0;i<object.noOfNodes&!concurrent;++i){
				for(int k =i+1;k<object.noOfNodes&!concurrent;++k){
					concurrent=isconcurrent(object.vectors[i], object.vectors[k]);
				}				
			}			
		}
		if(concurrent)
			System.out.println("DME not satisfied");
		else
			System.out.println("DME satisfied");
		
	}
}
