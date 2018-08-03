
public class Mutex_Protocol_MsgHandler{
	
		String message = null;
		Mutex_Protocol mutex;
		
		public Mutex_Protocol_MsgHandler(String message,Mutex_Protocol mutex) {
			this.message=message;
		}
		
		public Mutex_Protocol_MsgHandler(Mutex_Protocol mutex) {
			this.mutex = mutex;
		}

		public void MsgHandling(String message){
		
		String[] msgParameters,tempRequest;
		String requestQueue;
		
		synchronized(mutex.sentMsgQueue){
			if(message!=null){
				msgParameters = message.split("#");
				int sending_id = Integer.parseInt(msgParameters[1].trim());
				int scalar = Integer.parseInt(msgParameters[2].trim());
				int[] newVector= mutex.fromString(msgParameters[3].trim());
				for (int i=0; i < newVector.length; i++){ 
					if (newVector[i] >= mutex.csEnterVector[i])
						mutex.csEnterVector[i] = newVector[i];
					
				}
				if(scalar > mutex.timeStamp)
					mutex.timeStamp = scalar;
				mutex.timeStamp++;
				/**
				 * received message: 'Request' to access CS
				 */
				if(msgParameters[0].equals("request")){	
					if(!mutex.isLocked){
						mutex.lockedProcess[0]=sending_id;
						mutex.lockedProcess[1]=scalar;
						mutex.isLocked = true;
						mutex.timeStamp++;
						mutex.sendMessage("lock", sending_id, mutex.timeStamp);
					}else{
						//push into pending priority queue based on time stamp.
						mutex.msgSent=mutex.requestQueue.offer(sending_id+" "+scalar);
						String s = mutex.requestQueue.peek();
						String[] parts = s.split(" ");
						int topWaitingID = Integer.parseInt(parts[0].trim());
						int topWaitingSeq = Integer.parseInt(parts[1].trim());
						if(scalar < mutex.lockedProcess[1] || (scalar == mutex.lockedProcess[1] && sending_id < mutex.lockedProcess[0])){
							if(!mutex.isInqSent){
								mutex.timeStamp++;
								mutex.sendMessage("inquire", mutex.lockedProcess[0],mutex.timeStamp);//sendInq
								mutex.isInqSent = true;
							}
							else if(topWaitingSeq < scalar || topWaitingSeq == scalar && topWaitingID < sending_id ){
								mutex.timeStamp++;
								mutex.sendMessage("fail", sending_id,mutex.timeStamp);//sendFail
							}
						}
						else{
							mutex.timeStamp++;
							mutex.sendMessage("fail", sending_id, mutex.timeStamp);//send fail
						}
						
					}
				}
					/**
					 * received message: 'Grant' to access CS
					 */
				else if(msgParameters[0].equals("lock")){
					boolean done=mutex.grantLock(sending_id);
					
				}
					/**
					 * received message: 'Inquire' to release access to CS
					 */
				else if(msgParameters[0].equals("inquire")){
					if(mutex.hasReceivedFailed.containsValue(false)){
						mutex.timeStamp++;
						mutex.sendMessage("yield", sending_id, mutex.timeStamp);//send yield
						mutex.hasReceivedFailed.remove(sending_id);
						mutex.NumLocks--;
					}else if((mutex.NumLocks < mutex.quorums.length )){
						
						mutex.inqMsgs.add(sending_id+" "+scalar);
					}
					
				}
					/**
					 * received message: 'Fail' to access CS
					 */
				else if(msgParameters[0].equals("fail")){						
					//monitor the processes from which fail has been received
					mutex.hasReceivedFailed.put(sending_id, false);
					//Check if there is any in inq queue, if yes send yield, decrement NumLocks.
					while(!mutex.inqMsgs.isEmpty()){
						String m = mutex.inqMsgs.poll();
						String[] p = m.split(" ");
						mutex.timeStamp++;
						mutex.sendMessage("yield", Integer.parseInt(p[0]), mutex.timeStamp);
						mutex.hasReceivedFailed.remove(Integer.parseInt(p[0]));
						mutex.NumLocks--;
					}
				
				}
					/**
					 * received message: 'Yield' to give back access of CS
					 */
				else if(msgParameters[0].equals("yield")){
					mutex.isLocked = false;
					mutex.isInqSent = false;
					
					mutex.msgSent=mutex.requestQueue.offer(mutex.lockedProcess[0]+" "+mutex.lockedProcess[1]);
					requestQueue = mutex.requestQueue.peek();
					if(requestQueue==null){
						mutex.lockedProcess[0]=null;
						mutex.lockedProcess[1]=null;
					}else if(requestQueue!=null){
						requestQueue = mutex.requestQueue.poll();
						tempRequest = requestQueue.split(" ");
						mutex.lockedProcess[0]=Integer.parseInt(tempRequest[0]);
						mutex.lockedProcess[1]=Integer.parseInt(tempRequest[1]);
						mutex.isLocked = true;
						mutex.timeStamp++;
						mutex.sendMessage("lock", Integer.parseInt(tempRequest[0]), mutex.timeStamp);
					}
				}
					/**
					 * received message: 'Release' after CS access
					 */
				else if(msgParameters[0].equals("release")){
					mutex.isLocked = false;
					mutex.isInqSent = false;
					requestQueue = mutex.requestQueue.peek();
					if(requestQueue==null){
						mutex.lockedProcess[0]=null;
						mutex.lockedProcess[1]=null;
					}else if(requestQueue!=null){
						requestQueue = mutex.requestQueue.poll();
						tempRequest = requestQueue.split(" ");
						mutex.lockedProcess[0]=Integer.parseInt(tempRequest[0]);
						mutex.lockedProcess[1]=Integer.parseInt(tempRequest[1]);
						mutex.isLocked = true;
						mutex.timeStamp++;
						mutex.sendMessage("lock", Integer.parseInt(tempRequest[0]), mutex.timeStamp);
					}
				}	
			}
		 }
	  }
	
	}
