package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import adts.LobbyModel;

/**
 * Creates a new WhiteboardServer instance which is bound to a socket and will
 * multi-thread to handle multiple clients. Its main method allows for a
 * connection to socket number 4444.
 */
public class WhiteboardServer {
	private Socket socket;
	private final ServerSocket serverSocket;
	private final LobbyModel lobbyModel;
	private final List<UserThread> userThreads;
	private final Thread serverThread;
	private final WhiteboardServer thisServer;

	/**
	 * Initializes a server by binding it to its port, creating an array of
	 * incoming userThreads. Will serve with a single thread.
	 * 
	 * @param port
	 *            the socket port to connect to
	 * @throws IOException
	 */
	public WhiteboardServer(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.lobbyModel = new LobbyModel();
		this.userThreads = new ArrayList<UserThread>();
		this.thisServer = this;
		this.serverThread = new Thread(new Runnable() {
			public void run() {
				try {
					thisServer.singleThreadedServe();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Begins a server thread.
	 */
	public void serve() throws IOException {
		this.serverThread.start();
	}

	/**
	 * Begins a server without threading. Once started, it will listen in for
	 * new user connections and create a new thread for each successful one.
	 * 
	 * @throws IOException
	 */
	public void singleThreadedServe() throws IOException {
		while (true) {
			socket = serverSocket.accept();
			int userID = this.lobbyModel.addUser();
			UserThread thread = new UserThread(socket, userID,
					this.userThreads, this.lobbyModel);
			this.userThreads.add(thread);
			thread.start();
		}

	}

	/**
	 * This is the main method.
	 */
	public static void main(String[] args) {
		int port = 4444;
        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
        
        try {
            while ( ! arguments.isEmpty()) {
                String flag = arguments.remove();
                try {
                    if (flag.equals("--port")) {
                        port = Integer.parseInt(arguments.remove());
                        if (port < 0 || port > 65535) {
                            throw new IllegalArgumentException("port " + port + " out of range");
                        }
                    } else {
                        throw new IllegalArgumentException("unknown option: \"" + flag + "\"");
                    }
                    
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("unable to parse number for " + flag);
                } 
            }
            runWhiteboardServer(port);

        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err.println("usage: WhiteboardServer [--port PORT]");
            return;
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            System.err.println("usage: WhiteboardServer [--port PORT]");
            return;
        }
	}

	/**
	 * Method used by the main method to start a server.
	 * 
	 * @param port
	 *            socket integer to connect to.
	 * @throws IOException
	 */
	public static void runWhiteboardServer(int port) throws IOException {
		WhiteboardServer server;
		try {
			server = new WhiteboardServer(port);
			server.serve();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
