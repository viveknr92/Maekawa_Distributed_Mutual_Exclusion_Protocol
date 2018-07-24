
public class Mutex_Protocol_MsgHandler{
	
		String message = null;
		Mutex_Protocol object;
		
		public Mutex_Protocol_MsgHandler(String message,Mutex_Protocol object) {
			this.message=message;
		}
		
		public Mutex_Protocol_MsgHandler(Mutex_Protocol object) {
			this.object = object;
		}

		public void MsgHandling(String message){
		
		String[] msgParameters,tempRequest;
		String requestQueue;
		
		synchronized(object.sentMsgQueue){
			if(message!=null){
				msgParameters = message.split("#");
				int sending_id = Integer.parseInt(msgParameters[1].trim());
				int scalar = Integer.parseInt(msgParameters[2].trim());
				int[] newVector= object.fromString(msgParameters[3].trim());
				for (int i=0; i < newVector.length; i++){ 
					if (newVector[i] >= object.csEnterVector[i])
						object.csEnterVector[i] = newVector[i];
					
				}
				if(scalar > object.timeStamp)
					object.timeStamp = scalar;
				object.timeStamp++;
				/**
				 * received message: 'Request' to access CS
				 */
				if(msgParameters[0].equals("request")){	
					if(!object.isLocked){
						object.lockedProcess[0]=sending_id;
						object.lockedProcess[1]=scalar;
						object.isLocked = true;
						object.timeStamp++;
						object.sendMessage("lock", sending_id, object.timeStamp);
					}else{
						//push into pending priority queue based on time stamp.
						object.msgSent=object.requestQueue.offer(sending_id+" "+scalar);
						String s = object.requestQueue.peek();
						String[] parts = s.split(" ");
						int topWaitingID = Integer.parseInt(parts[0].trim());
						int topWaitingSeq = Integer.parseInt(parts[1].trim());
						if(scalar < object.lockedProcess[1] || (scalar == object.lockedProcess[1] && sending_id < object.lockedProcess[0])){
							if(!object.isInqSent){
								object.timeStamp++;
								object.sendMessage("inquire", object.lockedProcess[0],object.timeStamp);//sendInq
								object.isInqSent = true;
							}
							else if(topWaitingSeq < scalar || topWaitingSeq == scalar && topWaitingID < sending_id ){
								object.timeStamp++;
								object.sendMessage("fail", sending_id,object.timeStamp);//sendFail
							}
						}
						else{
							object.timeStamp++;
							object.sendMessage("fail", sending_id, object.timeStamp);//send fail
						}
						
					}
				}
					/**
					 * received message: 'Grant' to access CS
					 */
				else if(msgParameters[0].equals("lock")){
					boolean done=object.grantLock(sending_id);
					
				}
					/**
					 * received message: 'Inquire' to release access to CS
					 */
				else if(msgParameters[0].equals("inquire")){
					if(object.hasReceivedFailed.containsValue(false)){
						object.timeStamp++;
						object.sendMessage("yield", sending_id, object.timeStamp);//send yield
						object.hasReceivedFailed.remove(sending_id);
						object.NumLocks--;
					}else if((object.NumLocks < object.quorums.length )){
						
						object.inqMsgs.add(sending_id+" "+scalar);
					}
					
				}
					/**
					 * received message: 'Fail' to access CS
					 */
				else if(msgParameters[0].equals("fail")){						
					//monitor the processes from which fail has been received
					object.hasReceivedFailed.put(sending_id, false);
					//Check if there is any in inq queue, if yes send yield, decrement NumLocks.
					while(!object.inqMsgs.isEmpty()){
						String m = object.inqMsgs.poll();
						String[] p = m.split(" ");
						object.timeStamp++;
						object.sendMessage("yield", Integer.parseInt(p[0]), object.timeStamp);
						object.hasReceivedFailed.remove(Integer.parseInt(p[0]));
						object.NumLocks--;
					}
				
				}
					/**
					 * received message: 'Yield' to give back access of CS
					 */
				else if(msgParameters[0].equals("yield")){
					object.isLocked = false;
					object.isInqSent = false;
					
					object.msgSent=object.requestQueue.offer(object.lockedProcess[0]+" "+object.lockedProcess[1]);
					requestQueue = object.requestQueue.peek();
					if(requestQueue==null){
						object.lockedProcess[0]=null;
						object.lockedProcess[1]=null;
					}else if(requestQueue!=null){
						requestQueue = object.requestQueue.poll();
						tempRequest = requestQueue.split(" ");
						object.lockedProcess[0]=Integer.parseInt(tempRequest[0]);
						object.lockedProcess[1]=Integer.parseInt(tempRequest[1]);
						object.isLocked = true;
						object.timeStamp++;
						object.sendMessage("lock", Integer.parseInt(tempRequest[0]), object.timeStamp);
					}
				}
					/**
					 * received message: 'Release' after CS access
					 */
				else if(msgParameters[0].equals("release")){
					object.isLocked = false;
					object.isInqSent = false;
					requestQueue = object.requestQueue.peek();
					if(requestQueue==null){
						object.lockedProcess[0]=null;
						object.lockedProcess[1]=null;
					}else if(requestQueue!=null){
						requestQueue = object.requestQueue.poll();
						tempRequest = requestQueue.split(" ");
						object.lockedProcess[0]=Integer.parseInt(tempRequest[0]);
						object.lockedProcess[1]=Integer.parseInt(tempRequest[1]);
						object.isLocked = true;
						object.timeStamp++;
						object.sendMessage("lock", Integer.parseInt(tempRequest[0]), object.timeStamp);
					}
				}	
			}
		 }
	  }
	
	}
