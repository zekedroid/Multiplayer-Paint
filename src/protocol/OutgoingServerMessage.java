package protocol;

import java.io.PrintWriter;
import java.util.Collection;

public class OutgoingServerMessage {
    private final Collection<PrintWriter> out;
    private final String message;
    public OutgoingServerMessage(Collection<PrintWriter> out, String message){
        this.out = out;
        this.message = message;
    }
    public Collection<PrintWriter> getOutStreams(){
        return this.out;
    }
    public String getMessage(){
        return this.message;
    }
}
