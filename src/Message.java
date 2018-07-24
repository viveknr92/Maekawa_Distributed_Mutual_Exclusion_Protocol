import java.io.Serializable;
import java.util.Arrays;

public class Message implements Serializable{
	public Message(String messageType, int nodeId, int neighbor, int timestamp, int[] csEnterVector) {
		super();
		this.messageType = messageType;
		this.nodeId = nodeId;
		this.neighbor = neighbor;
		this.timestamp = timestamp;
		this.csEnterVector = csEnterVector;
		this.msgString = messageType+"#"+nodeId+"#"+timestamp+"#"+Arrays.toString(csEnterVector)+"#"+neighbor;
	}
	String messageType;
	int nodeId;
	int neighbor;
	int timestamp;
	int[] csEnterVector;
	String msgString;
}
