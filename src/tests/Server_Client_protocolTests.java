package tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import adts.Line;
import protocol.ClientSideMessageMaker;
import server.WhiteboardServer;

/**
 * Testing suite used to ensure server/client side messaging works correctly.
 * The full protocol messaging supported can be found in the Requests PDF.
 * Server/client interactions are the heart of how this collaborative whiteboard
 * system works so it is key that it is thoroughly tested.
 * 
 * It looks like Didit gets mad when we create servers
 * and access ports and create clients. So, we don't run the tests
 * on Didit.
 * 
 * 
 * WHERE ARE THE ASSERT STATEMENTS?
 * 
 *
 * We're creating servers and clients, and since messages arrive out of order, 
 * we implement the following technique for tests.
 * 
 * Every test has a two second timeout
 * 
 * A test fails when it calls pollQueueForMessage and gets caught in an infinite loop
 * 
 * A test passes when it finishes all the lines of codes in its body within 2 seconds
 * @category no_didit
 */
public class Server_Client_protocolTests {

	/*
	 * Testing strategy
	 * 
	 * Goal: Make sure the server responds correctly to various requests given
	 * by the clients. Must test every message supported in the protocol. Of
	 * utmost priority is to check the different behaviors of clients in the
	 * lobby, in a canvas, or in different canvases. Refer to the Requests PDF
	 * for more info.
	 * 
	 * Strategy: because only one server can be occupying a socket at any given
	 * time, this test suite exploits the many other open sockets which are open
	 * for use. This way, every test can initialize an instance of a server and
	 * do work on a fresh copy. Note how each test will still connect to
	 * "localhost" but the port is always different.
	 * 
	 * IMPORTANT: in the offchance that a server is initialized on a socket
	 * which is occupied, the test will catch this exception, report to the
	 * console, and continue looking for other ports.
	 * 
	 * For most of the request/response types, we will create four clients. Two
	 * of them will join a board, one of them will join another board, and one
	 * of them will not be in any boards. This way, when we do an action, we can
	 * see it from all relevant perspectives: -Self -Other users in my board
	 * -Other users in other boards -Other users not in any boards.
	 * 
	 * Start by testing that an instance of the server compiles (test the
	 * constructur as well). Continue on to test client connections and initial
	 * welcoming messages. Advance into the protocol messages and test every
	 * possible combination. This include things like joining a board that
	 * doesn't exist, joining one where users exist and have lines drawn/no
	 * lines drawn, changing of usernames, creating boards and seeing them on
	 * the WhiteboardClient table, and more which are given in detail below.
	 */

	/*
	 * For the purpose of testing, one server and four clinets are used per
	 * test. Each request string is evaluated beforehand.
	 */
	int port;
	String testHost = "127.0.0.1"; // localhost, can be replaced to test remote
									// servers
	WhiteboardServer server;
	SimpleClient client1;
	SimpleClient client2;
	SimpleClient client3;
	
	/**
     * Client 1 and Client 3 make two boards, and Client 2 ensures that 
     * it receives a message indicating that the boards have been made
     * @throws IOException
     */
    @Test(timeout = 2000)
    public void create_board_test() throws IOException{
        this.initialize();
        // Client 1 creates a board
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringCreateBoard("BoardName1"));
        
        // Client 1 ensures that the board has been created
        pollQueueForMessage(client1.getQueue(), "board_ids -1 Lobby 0 BoardName1", false);
        
        // Client 3 creates a board
        client3.makeRequest(ClientSideMessageMaker.makeRequestStringCreateBoard("BoardName2"));
        
        // Client 2 ensures that the board has been created
        pollQueueForMessage(client2.getQueue(), "board_ids -1 Lobby 0 BoardName1 1 BoardName2", false);
    }
    
    /**
     * Client 1 changes his username, and Client 2 will observe that
     * @throws IOException
     */
    @Test(timeout = 2000)
    public void set_username_test() throws IOException{
        this.initialize();
        // Client 1 changes his username
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringSetUsername("SomeUserName"));
        
        // Client 2 recieves a message indicating that the username has been changed
        pollQueueForMessage(client2.getQueue(), "users_for_board_id -1 User2 User1 SomeUserName", false);
    }
    
    /**
     * Client 1 and Client 3 make two boards, and Client 2 makes a get_board_ids
     * request and ensures that it gets the right response
     * @throws IOException
     */
    @Test(timeout = 2000)
    public void get_boards_request() throws IOException{
        this.initialize();
        
        // Client 1 creates a board
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringCreateBoard("BoardName1"));
        
        // Client 2 checks if the board has been created
        pollQueueForMessage(client2.getQueue(), "board_ids -1 Lobby 0 BoardName1", false);
        
        // Client 3 creates a board
        client3.makeRequest(ClientSideMessageMaker.makeRequestStringCreateBoard("BoardName2"));
        
        // Client 2 checks if the board is created
        pollQueueForMessage(client2.getQueue(), "board_ids -1 Lobby 0 BoardName1 1 BoardName2", false);
        
        // Client 2 asks for the current board ids
        client2.makeRequest(ClientSideMessageMaker.makeRequestStringGetBoardIDs());
        
        // Client 2 checks that the current board ids are right
        pollQueueForMessage(client2.getQueue(), "board_ids -1 Lobby 0 BoardName1 1 BoardName2", false);
    }
    
    /**
     * Client 1 creates a board, Client 2 joins the board,
     * Client 2 gets the current board id
     * @throws IOException
     */
    @Test(timeout = 2000)
    public void get_current_board_ids() throws IOException{
        this.initialize();
        
        // Client 1 creates a board
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringCreateBoard("BoardName1"));
        
        // Client 1 checks that the board has been created
        pollQueueForMessage(client1.getQueue(), "board_ids -1 Lobby 0 BoardName1", false);
        
        // Client 2 joins the board
        client2.makeRequest(ClientSideMessageMaker.makeRequestStringJoinBoardID(0));
        
        // Client 1 checks that Client 2 has joined
        pollQueueForMessage(client1.getQueue(), "users_for_board_id 0 User0 User1", false);
        
        // Client 2 asks for the current board id
        client2.makeRequest(ClientSideMessageMaker.makeRequestStringGetCurrentBoardID());
        
        // Client 2 checks that the current board id is correct
        pollQueueForMessage(client2.getQueue(), "current_board_id 0", false);
    }
    
    /**
     * Client 1 creates a board, Client 2 joins the board, Client 1 checks if
     * it receives a message which tells it that Client 2 has joined
     * @throws IOException 
     */
    @Test(timeout = 2000)
    public void join_board_test() throws IOException{
        this.initialize();
        
        // Client 1 creates a board
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringCreateBoard("BoardName1"));
        
        // Client 1 checks that the board has been created
        pollQueueForMessage(client1.getQueue(), "board_ids -1 Lobby 0 BoardName1", false);
        
        // Client 2 asks to join the board
        client2.makeRequest(ClientSideMessageMaker.makeRequestStringJoinBoardID(0));
        
        // Client 1 checks that Client 2 has joined
        pollQueueForMessage(client1.getQueue(), "users_for_board_id 0 User0 User1", false);
    }
    
    /**
     * Client 1 creates a board, Client 2 joins the board, 
     * Client 3 joins the board, Client 2 gets the
     * users for the current board id and ensures
     * that all the users are there
     * @throws IOException 
     */
    @Test(timeout = 2000)
    public void get_users_for_board_id_test() throws IOException{
        this.initialize();
        
        // Client 1 creates a board
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringCreateBoard("BoardName1"));
        
        // Client 1 checks that the board has been created
        pollQueueForMessage(client1.getQueue(), "board_ids -1 Lobby 0 BoardName1", false);
        
        // Client 3 asks to create a board
        client3.makeRequest(ClientSideMessageMaker.makeRequestStringCreateBoard("BoardName2"));
        
        // Client 3 checks that the board has been created
        pollQueueForMessage(client3.getQueue(), "board_ids -1 Lobby 0 BoardName1 1 BoardName2", false);
        
        // Client 2 joins the board that Client 3 made
        client2.makeRequest(ClientSideMessageMaker.makeRequestStringJoinBoardID(1));
        
        // Client 3 checks that Client 2 has joined the board 
        pollQueueForMessage(client3.getQueue(), "users_for_board_id 1 User2 User1", false);
        
        // Client 2 asks for the current board id
        client2.makeRequest(ClientSideMessageMaker.makeRequestStringGetCurrentBoardID());
        
        // Client 2 checks that the board id is correct
        pollQueueForMessage(client2.getQueue(), "current_board_id 1", false);
    }
    
    /**
     * Client 1 creates a board, Client 1 draws a line
     * @throws IOException
     */
    @Test(timeout = 2000)
    public void req_draw_test() throws IOException{
        this.initialize();
        // Client 1 creates a board
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringCreateBoard("BoardName1"));
        
        // Client 1 checks that the board has been created
        pollQueueForMessage(client1.getQueue(), "board_ids -1 Lobby 0 BoardName1", false);
        
        // Client 1 draws a line
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringDraw(new Line(0, 1, 2, 3, 4, 5, 6, 7, 8)));
        
        // Client 1 checks that the line has been drawn
        pollQueueForMessage(client1.getQueue(), "draw 0 1 2 3 4.000000 5 6 7 8", false);
        
    }
    
    @Test(timeout = 2000)
    public void req_clear_board_test() throws IOException{
        this.initialize();
        // Client 1 creates a board
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringCreateBoard("BoardName1"));
        
        // Client 1 checks that the board has been created
        pollQueueForMessage(client1.getQueue(), "board_ids -1 Lobby 0 BoardName1", false);
        
        // Client 1 draws a line
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringDraw(new Line(0, 1, 2, 3, 4, 5, 6, 7, 8)));
        
        // Client 1 checks that the line has been drawn
        pollQueueForMessage(client1.getQueue(), "draw 0 1 2 3 4.000000 5 6 7 8", false);
        
        // Client 2 joins the board that Client 3 made
        client2.makeRequest(ClientSideMessageMaker.makeRequestStringJoinBoardID(0));
        
        // Client 2 checks that it has joined and that the board lines are correct 
        pollQueueForMessage(client2.getQueue(), "board_lines 2 1 User0 User1 0 1 2 3 4.000000 5 6 7 8", false);
        
        // Client 2 tries to clear the board
        client2.makeRequest(ClientSideMessageMaker.makeRequestStringClear());
        
        
        // Client 3 checks if the clear message is there
        pollQueueForMessage(client1.getQueue(), "clear_board", false);
        
    }
    
    /**
     * Client 2 logs out
     * @throws IOException
     */
    @Test(timeout = 2000)
    public void logout_test() throws IOException{
        this.initialize();
        
        // Client 2 logs out
        client2.makeRequest(ClientSideMessageMaker.makeRequestStringLogout());
        
        // Client 1 observes that Client 2 has logged out
        pollQueueForMessage(client1.getQueue(), "users_for_board_id -1 User0 User2", false);
        
    }
    
    
    /**
     * Client 1 creates a board,
     * Client 3 joins the board,
     * Client 2 joins the board,
     * Client 2 asks for the users in the current board
     * @throws IOException
     */
    @Test(timeout = 2000)
    public void get_users_in_my_board_test() throws IOException{
        this.initialize();
        
        // Client 1 creates a board
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringCreateBoard("BoardName1"));
        
        // Client 1 checks that the board has been created
        pollQueueForMessage(client1.getQueue(), "board_ids -1 Lobby 0 BoardName1", false);
        
        // Client 3 joins the board
        client3.makeRequest(ClientSideMessageMaker.makeRequestStringJoinBoardID(0));
        
        // Client 1 checks that Client 3 has joined the board 
        pollQueueForMessage(client1.getQueue(), "users_for_board_id 0 User0 User2", false);

        // Client 2 joins the board
        client2.makeRequest(ClientSideMessageMaker.makeRequestStringJoinBoardID(0));
        
        // Client 3 checks that Client 2 has joined the board 
        pollQueueForMessage(client3.getQueue(), "users_for_board_id 0 User2 User1 User0", false);
        
        // Client 2 asks for the users in the current board
        client2.makeRequest(ClientSideMessageMaker.makeRequestStringGetUsersInMyBoard());
        
        // Client 2 checks that the users in the board are correct
        pollQueueForMessage(client2.getQueue(), "users_for_board_id 0 User0 User1 User2", false);
        
    }
    
    /**
     * Client 1 creates a board,
     * Client 3 joins the board,
     * Client 1 leaves the board
     * @throws IOException
     */
    @Test(timeout = 2000)
    public void leave_board_test() throws IOException{
        this.initialize();
        
        // Client 1 creates a board
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringCreateBoard("BoardName1"));
        
        // Client 1 checks that the board has been created
        pollQueueForMessage(client1.getQueue(), "board_ids -1 Lobby 0 BoardName1", false);
        
        // Client 3 joins the board
        client3.makeRequest(ClientSideMessageMaker.makeRequestStringJoinBoardID(0));
        
        // Client 1 checks that Client 3 has joined the board 
        pollQueueForMessage(client1.getQueue(), "users_for_board_id 0 User0 User2", false);

        // Client 1 leaves the board
        client1.makeRequest(ClientSideMessageMaker.makeRequestStringLeaveBoard());
        
        // Client 3 checks that Client 1 is now back in the lobby
        pollQueueForMessage(client2.getQueue(), "users_for_board_id -1 User1 User0", false);
    }
    
	/**
	 * Randomly finds an open port and returns it if it is available.
	 */
	private int getAvailablePort() throws IOException {
		int port = 0;
		Random RANDOM = new Random();
		do {
			port = RANDOM.nextInt(20000) + 1000;
		} while (!isPortAvailable(port));

		return port;
	}

	/**
	 * Given a port number, it checks to see if it is in use. It returns true if
	 * it is not in use.
	 * 
	 * @param port
	 *            integer port number to check
	 */
	private boolean isPortAvailable(final int port) throws IOException {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			return true;
		} catch (final IOException e) {
		} finally {
			if (ss != null) {
				ss.close();
			}
		}

		return false;
	}

	/**
	 * Helper function to start up a server on a given port, and four clients,
	 * all on localhost. Uses the checkResponse() helper function to assert the
	 * clients connected to the server.
	 * 
	 * Thread-safety:
	 * 
	 * Since we are first checking for an open port and then creating it based
	 * off of that integer value, there is a possibility of this socket getting
	 * used before actualling connecting to it. However, this goes beyond the
	 * scope of this project and if in the future it is a nessesary feature,
	 * simply return an available socket and connect to it directly.
	 * 
	 * @param port
	 *            given socket number on which to open the server/connect the
	 *            clinets to.
	 * @throws IOException
	 *             if there is a connection timeout, an IOException is thrown.
	 */
	public void initialize() throws IOException {
		port = getAvailablePort();

		this.server = new WhiteboardServer(port);
		this.server.serve();
		
		this.client1 = new SimpleClient(testHost, port);
		pollQueueForMessage(client1.getQueue(), "welcome 0", false);
		this.client2 = new SimpleClient(testHost, port);
		pollQueueForMessage(client2.getQueue(), "welcome 1", false);
		this.client3 = new SimpleClient(testHost, port);
		pollQueueForMessage(client3.getQueue(), "welcome 2", false);
	}
	
	/**
	 * Takes a expected message and actual message,
	 * splits them,
	 * sorts them,
	 * and checks that corresponding elements are equal
	 * @param expected the expected message
	 * @param actual the actual message
	 * @return
	 */
	private boolean correctMessage(String expected, String actual){
	    if(actual == null){
	        return false;
	    }
	    String[] splitExpected = expected.split(" ");
	    String[] splitActual = actual.split(" ");
	    if(splitActual.length != splitExpected.length){
            return false;
        }
	    Arrays.sort(splitActual);
	    Arrays.sort(splitExpected);
	    for(int i = 0; i < splitActual.length; i++){
	        if(!splitActual[i].equals(splitExpected[i])){
	            return false;
	        }
	    }
	    return true;
	    
	}

	/**
	 * Keep popping elements off the message queue until we find an input such that
	 * correctMessage(input, expectedMessage) is true
	 * 
	 * Since we have a while(true) loop here, the function will never terminate 
	 * if the queue does not have the desired message.
	 * 
	 * Why do we use an infinite loop? Because all our tests have a 2 second timeout.
	 * 
	 * A test fails when it calls pollQueueForMessage and gets caught in a loop
	 * 
	 * A test passes when it finishes all the lines of codes in its body
	 * @param queue the queue of a client (contains all the messages that the client has received)
	 * @param expectedMessage the expected message
	 * @param verbose if true, we print out all the messages we pop off the queue
	 */
	private void pollQueueForMessage(ConcurrentLinkedQueue<String> queue, String expectedMessage, boolean verbose){
	    String input;
	    while(true){
            if(!queue.isEmpty()){
                input = queue.remove();
                if(verbose)
                    System.out.println(input);
                if(correctMessage(input, expectedMessage)){
                    queue.clear();
                    return;
                }
            }
        }
	}
	
}


/**
 * Class that reads from an input stream and puts the messages on a queue
 */
class SimpleClientIncomingMessageThread extends Thread{
    
    /**
     * The queue that contains all the messages that have been received so far
     */
    private final ConcurrentLinkedQueue<String> queue;
    
    /**
     * The input stream
     */
    private final BufferedReader in;
    
    /**
     * Construct SimpleClientIncomingMessageThread
     * @param queue the queue which will contain all the messages that have been received so far
     * @param in the input stream
     */
    public SimpleClientIncomingMessageThread(ConcurrentLinkedQueue<String> queue, BufferedReader in) {
        this.queue = queue;
        this.in = in;
    }
    
    /**
     * Runs the thread - simply reads messages and pushes them onto the queue
     */
    @Override
    public void run() {
        String input;
        while(true){
            try {
                input = in.readLine();
                if(input != null){
                    queue.add(input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * A client that handles a connection to a server
 */
class SimpleClient {
    
    /**
     * The hostname to connect to
     */
	String host;
	
	/**
	 * The socket that we use for connection
	 */
	Socket socket;
	
	/**
	 * The output stream
	 */
	PrintWriter out;
	
	/**
	 * The input stream
	 */
	BufferedReader in;
	
	/**
	 * The string we receive from the server
	 */
	String serverResponse;
	
	/**
	 * A queue that contains the messages we've received from the server
	 */
	ConcurrentLinkedQueue<String> queue;
	
	/**
	 * Constructs a simple client
	 * @param host the hostname
	 * @param port the port number
	 */
	public SimpleClient(String host, int port) {
		try {
			this.host = host;
			this.socket = new Socket(host, port);
			this.out = new PrintWriter(socket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.queue = new ConcurrentLinkedQueue<String>();
			new SimpleClientIncomingMessageThread(queue, in).start();
		} catch (Exception ex) {}
	}
	
	/**
	 * Closes the streams and the socket
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		this.out.close();
		this.in.close();
		this.socket.close();
	}

	/**
	 * Writes a message to the output stream
	 * @param req the message to put on the output stream
	 */
	public void makeRequest(String req) {	
		out.println(req);
		try {
			Thread.sleep(1); // Wait for 1 ms to give server time to respond.
		} catch (InterruptedException e) {
			System.out.println("Error waiting in makeRequest.");
		}
	}
	
	/**
	 * @return A queue that contains the messages we've received from the server
	 */
	public ConcurrentLinkedQueue<String> getQueue(){
	    return queue;
	}
	
}
