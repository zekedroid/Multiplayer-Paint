package protocol;

import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OutgoingServerMessageQueue extends Thread{
    
    /**
     * The queue of outgoing messages
     */
    private final ConcurrentLinkedQueue<OutgoingServerMessage> outgoingServerMessages;
    
    /**
     * Construct the outgoing messages queue
     */
    public OutgoingServerMessageQueue(){
        this.outgoingServerMessages = new ConcurrentLinkedQueue<OutgoingServerMessage>();
    }
    
    /**
     * Adds a message to the queue
     * @param outgoingMessage the message to add
     */
    public void addMessage(OutgoingServerMessage outgoingMessage){
        this.outgoingServerMessages.add(outgoingMessage);
    }
    
    /**
     * Outputs messages as they come on the queue
     */
    @Override
    public void run() {
        OutgoingServerMessage outgoingServerMessage;
        try {
            while (true) {
                if(!outgoingServerMessages.isEmpty()){
                    outgoingServerMessage = outgoingServerMessages.remove();
                    for(PrintWriter out : outgoingServerMessage.getOutStreams()){
                        out.println(outgoingServerMessage.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
