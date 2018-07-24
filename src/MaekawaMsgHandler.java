
public class MaekawaMsgHandler{
	
		String message = null;
		Maekawa object;
		
		public MaekawaMsgHandler(String message,Maekawa object) {
			this.message=message;
		}
		
		public MaekawaMsgHandler(Maekawa object) {
			this.object = object;
		}

		public void MsgHandling(String message){
		
		String[] messageParts,tempPendingQueueHead;
		String pendingQueueHead;
		
		synchronized(object.OutMsgs){
			if(message!=null){
				messageParts = message.split("#");
				int senderID = Integer.parseInt(messageParts[1].trim());
				int seqNum = Integer.parseInt(messageParts[2].trim());
				int[] newVector= object.fromString(messageParts[3].trim());
				for (int i=0; i < newVector.length; i++){ 
					if (newVector[i] >= object.csEnterVector[i])
						object.csEnterVector[i] = newVector[i];
					
				}
				if(seqNum > object.seqNumber)
					object.seqNumber = seqNum;
				object.seqNumber++;
				/**
				 * received message: 'Request' to access CS
				 */
				if(messageParts[0].equals("REQ")){	
					if(!object.isLocked){
						object.lockedProcess[0]=senderID;
						object.lockedProcess[1]=seqNum;
						object.isLocked = true;
						object.seqNumber++;
						object.sendMessage("GRANT", senderID, object.seqNumber);
					}else{
						//push into pending priority queue based on time stamp.
						object.messageOffered=object.pendingRequests.offer(senderID+" "+seqNum);
						String s = object.pendingRequests.peek();
						String[] parts = s.split(" ");
						int topWaitingID = Integer.parseInt(parts[0].trim());
						int topWaitingSeq = Integer.parseInt(parts[1].trim());
						if(seqNum < object.lockedProcess[1] || (seqNum == object.lockedProcess[1] && senderID < object.lockedProcess[0])){
							if(!object.InqSent){
								object.seqNumber++;
								object.sendMessage("INQ", object.lockedProcess[0],object.seqNumber);//sendInq
								object.InqSent = true;
							}
							else if(topWaitingSeq < seqNum || topWaitingSeq == seqNum && topWaitingID < senderID ){
								object.seqNumber++;
								object.sendMessage("FAIL", senderID,object.seqNumber);//sendFail
							}
						}
						else{
							object.seqNumber++;
							object.sendMessage("FAIL", senderID, object.seqNumber);//send fail
						}
						
					}
				}
					/**
					 * received message: 'Grant' to access CS
					 */
				else if(messageParts[0].equals("GRANT")){
					boolean done=object.grantResponse(senderID);
					
				}
					/**
					 * received message: 'Inquire' to release access to CS
					 */
				else if(messageParts[0].equals("INQ")){
					if(object.QuorumReply.containsValue(false)){
						object.seqNumber++;
						object.sendMessage("YIELD", senderID, object.seqNumber);//send yield
						object.QuorumReply.remove(senderID);
						object.NoOfGrants--;
					}else if((object.NoOfGrants < object.quorums.length )){
						
						object.inqMsgs.add(senderID+" "+seqNum);
					}
					
				}
					/**
					 * received message: 'Fail' to access CS
					 */
				else if(messageParts[0].equals("FAIL")){						
					//monitor the processes from which fail has been received
					object.QuorumReply.put(senderID, false);
					//Check if there is any in inq queue, if yes send yield, decrement NoOfGrants.
					while(!object.inqMsgs.isEmpty()){
						String m = object.inqMsgs.poll();
						String[] p = m.split(" ");
						object.seqNumber++;
						object.sendMessage("YIELD", Integer.parseInt(p[0]), object.seqNumber);
						object.QuorumReply.remove(Integer.parseInt(p[0]));
						object.NoOfGrants--;
					}
				
				}
					/**
					 * received message: 'Yield' to give back access of CS
					 */
				else if(messageParts[0].equals("YIELD")){
					object.isLocked = false;
					object.InqSent = false;
					
					object.messageOffered=object.pendingRequests.offer(object.lockedProcess[0]+" "+object.lockedProcess[1]);
					pendingQueueHead = object.pendingRequests.peek();
					if(pendingQueueHead==null){
						object.lockedProcess[0]=null;
						object.lockedProcess[1]=null;
					}else if(pendingQueueHead!=null){
						pendingQueueHead = object.pendingRequests.poll();
						tempPendingQueueHead = pendingQueueHead.split(" ");
						object.lockedProcess[0]=Integer.parseInt(tempPendingQueueHead[0]);
						object.lockedProcess[1]=Integer.parseInt(tempPendingQueueHead[1]);
						object.isLocked = true;
						object.seqNumber++;
						object.sendMessage("GRANT", Integer.parseInt(tempPendingQueueHead[0]), object.seqNumber);
					}
				}
					/**
					 * received message: 'Release' after CS access
					 */
				else if(messageParts[0].equals("REL")){
					object.isLocked = false;
					object.InqSent = false;
					pendingQueueHead = object.pendingRequests.peek();
					if(pendingQueueHead==null){
						object.lockedProcess[0]=null;
						object.lockedProcess[1]=null;
					}else if(pendingQueueHead!=null){
						pendingQueueHead = object.pendingRequests.poll();
						tempPendingQueueHead = pendingQueueHead.split(" ");
						object.lockedProcess[0]=Integer.parseInt(tempPendingQueueHead[0]);
						object.lockedProcess[1]=Integer.parseInt(tempPendingQueueHead[1]);
						object.isLocked = true;
						object.seqNumber++;
						object.sendMessage("GRANT", Integer.parseInt(tempPendingQueueHead[0]), object.seqNumber);
					}
				}	
			}
		 }
	  }
	
	}
