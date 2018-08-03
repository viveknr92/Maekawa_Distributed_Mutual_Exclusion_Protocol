/**
 * @author 
 * @since 
 * @version 1.0

 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class DME_Test {
	int noOfNodes, noOfRequests;
	BufferedReader[] files;
	int[][] vectors;

	private static int[] fromString(String string) {
	    String[] strings = string.replace("[", "").replace("]", "").split(", ");
	    int result[] = new int[strings.length];
	    for (int i = 0; i < result.length; i++) {
	      result[i] = Integer.parseInt(strings[i]);
	    }
	    return result;
	  }
	

	static boolean	isDME_Overlap(int vector1[], int vector2[])
	{
		boolean greater=false, lesser=false;

		for (int i=0; i < vector1.length; i++) 
			if (vector1[i] > vector2[i])
				greater = true;
			else if (vector1[i] < vector2[i])
				lesser = true;
		if (greater && lesser) {
			System.out.println(Arrays.toString(vector1));
			System.out.println(Arrays.toString(vector2));
			return true;	/* the vectors are concurrent */
		}
		else
			return false;	/* the vectors are not concurrent */
	}
	public static void main(String args[]) throws IOException {
		
		DME_Test mutex = new DME_Test();
		if (args.length != 2) {
            System.exit(0);
            }
		mutex.noOfNodes = Integer.parseInt(args[0]);
		mutex.noOfRequests = Integer.parseInt(args[1]);
		mutex.files = new BufferedReader[mutex.noOfNodes];
		mutex.vectors = new int[mutex.noOfNodes][];
		for(int i =0;i<mutex.noOfNodes;++i){
			try {
				mutex.files[i] = new BufferedReader(new InputStreamReader(new FileInputStream("logs-"+i+".out")));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		boolean concurrent=false;
		for(int j = 0;j<mutex.noOfRequests&!concurrent;++j){
			for(int i =0;i<mutex.noOfNodes;++i){
				mutex.vectors[i] = fromString(mutex.files[i].readLine());
			}
			for(int i =0;i<mutex.noOfNodes&!concurrent;++i){
				for(int k =i+1;k<mutex.noOfNodes&!concurrent;++k){
					concurrent=isDME_Overlap(mutex.vectors[i], mutex.vectors[k]);
				}				
			}			
		}
		if(concurrent)
			System.out.println("DME not satisfied");
		else
			System.out.println("DME satisfied");
		
	}
}
