package controller;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import logger.BoardLogger;
import protocol.BoardListItem;
import protocol.Client;
import protocol.ClientSideMessageMaker;
import protocol.MessageHandler;
import view.Canvas;
import adts.Line;
import adts.LobbyModel;
import adts.User;

/**
 * 
 * This is the Controller and GUI for the Lobby. It serves as the connection
 * between the user View of the Canvas and the Server and Model. These
 * Controllers are independent instances given unique IDs by the Model.
 * 
 * IMPORTANT LOGGER INFO: The logger is initialized at the beginning of the file
 * to start collecting logs. Logs are put into levels based on their importance.
 * All exceptions are SEVERE while many of the other smaller logs like server
 * responses are INFO.
 * 
 * Thread-safety:
 * 
 * We have one dedicated thread for receiving messages and updating the UI
 * other threads cannot do that. Each client is able to send Line drawing
 * requests from their Canvas to the server and, given the Model is thread-safe,
 * it will broadcast the action to every client in the same Whiteboard. Only now
 * does anything get drawn. This way we eliminate concurrency bugs; ie. local
 * Canvases are showing real-time images of the master Canvas in the
 * thread-safe Model.
 * 
 * The real measure of thread-safety are the rep-invariants. These include: no
 * local drawing allowed, meaning that if there is no server connection, nothing
 * should be getting drawn. Two, one Canvas per LobbyGUI.
 * 
 * Testing strategy:
 * 
 * Because of its hard-to-test nature, all testing must be done by manually
 * using the GUI itself. The way this was done is as follows and in this order:
 * 
 * @category the first GUI is the JOptionPane used to connect to a given server
 *           IP. Test that it correctly connects (when there is a running server
 *           at the given IP) and that incorrect IPs return a JOptionPane
 *           showing the failure to connect. Also check that the Cancel button
 *           and red X will cause the entire program to exit. (verify with the
 *           Logger's WARNING: Exiting Lobby)
 * @category the second GUI is the main LobbyGUI. Start by testing the general
 *           aesthetics: make sure the labels, lists, boxes, and buttons are in
 *           the correct order and that no resizing of the window is allowed.
 *           Also make sure the tables auto-scroll by adding enough boards or
 *           users until there is an overflow.
 * @category test the username button by entering a string and watching it
 *           change the username label.
 * @category test the create whiteboard button and expect to be automatically
 *           taken to a Canvas. Then leave the board and test that a new board
 *           is now listed in the table.
 * @category Finish testing by adding multiple users to the same LobbyModel and
 *           assuring their actions of creating boards/changing their usernames,
 *           are reflected here.
 * 
 */
public class WhiteboardClient extends JFrame implements Client {

	/**
	 * Use the classname for the logger, this way you can refactor
	 */
	private final static Logger LOGGER = Logger.getLogger(WhiteboardClient.class.getName());

	/**
	 * Needed for didit
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The port that the server runs on
	 */
	private final int port;
	
	/**
	 * The socket that the users connect to
	 */
	private Socket socket;
	
	/**
	 * The output stream
	 */
	private PrintWriter out;
	
	/**
	 * The input stream
	 */
	private BufferedReader in;

	/**
	 * Background thread to handle incoming messages
	 */
	private final WhiteboardClientBackgroundThread serverMessagesThread;

	/**
	 * canvas which allows drawing on whiteboard
	 */
	private Canvas canvas;

	/**
	 * Label displaying current username
	 */
	private final JLabel labelUserName;
	
	/**
	 * Button which allows setting of a new username
	 */
	private final JButton btnSetUserName;
	
	/**
	 * Button which allows creating a new board
	 */
	private final JButton btnCreateBoard;

	/**
	 * List of boards
	 */
	private final JList<String> lstBoards;
	
	/**
	 * Allows scrolling list of boards
	 */
	private final JScrollPane scrollLstBoards;
	
	/**
	 * List model for list of boards
	 */
	private final DefaultListModel<String> lstMdlBoards;

	/**
	 * List of user
	 */
	private final JList<String> lstUsers;
	
	/**
	 * Allows scrolling list of users
	 */
	private final JScrollPane scrollLstUsers;
	
	/**
	 * List model for list of users
	 */
	private final DefaultListModel<String> lstMdlUsers;

	/**
	 * Layout manager
	 */
	private final GroupLayout layout;

	/**
	 * We store this object as self, because it's needed for the action listeners
	 */
	private final WhiteboardClient self;

	/**
	 * The user who is currently using the application
	 */
	private User user;

	/**
	 * The list of items in the boards list 
	 * (a BoardListItem consists of a name, id, and index in the list)
	 */
	private List<BoardListItem> boardListItems;
	
	/**
	 * Construct LobbyGUI with the given port and hostName
	 * @param hostName the hostname
	 * @param port the port number
	 */
	public WhiteboardClient(String hostName, int port) {
		setupLogger(Level.OFF);
		this.port = port;
		// get the hostname and create the socket
		int attemptedConnections = 0;
		int MAX_ALLOWED_CONNECTIONS = 10;
		while (this.in == null && attemptedConnections < MAX_ALLOWED_CONNECTIONS) {
			try {
			    attemptedConnections++;
				hostName = JOptionPane.showInputDialog(
						"Enter the hostname of the whiteboard server:",
						"localhost");
				if (hostName == null ) {
					LOGGER.warning("Exiting Lobby");
					System.exit(0);
				}
				LOGGER.info("Hostname (IP) inputted: " + hostName);
				this.socket = new Socket(hostName, this.port);
				this.out = new PrintWriter(socket.getOutputStream(), true);
				this.in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
			} catch (Exception ex) {

				LOGGER.severe("Failed to connect to server ");
				JOptionPane.showMessageDialog(this,
						"Could not connect to given hostname. Try again.");
			}
		} 
		
		if(attemptedConnections >= MAX_ALLOWED_CONNECTIONS){
		    JOptionPane.showMessageDialog(null, "You've tried connecting too many times!");
		    System.exit(0);
		}

		// sets this current object
		this.self = this;

		// launch a thread to listen for messages
		this.serverMessagesThread = new WhiteboardClientBackgroundThread(this, this.in);
		this.serverMessagesThread.start();

		// create the UI to view and change the username and new whiteboards
		this.labelUserName = new JLabel("User: placeholder");
		this.btnSetUserName = new JButton("Change Username");
		this.btnSetUserName.addActionListener(new SetUserNameListener());
		this.btnCreateBoard = new JButton("Create Whiteboard");
		this.btnCreateBoard.addActionListener(new CreateWhiteboardListener());

		// create the list of boards
		this.lstMdlBoards = new DefaultListModel<String>();
		this.lstBoards = new JList<String>(this.lstMdlBoards);
		this.lstBoards.setSelectedIndex(0);
		this.lstBoards.addMouseListener(new JoinBoardListener());
		this.scrollLstBoards = new JScrollPane(this.lstBoards);

		// create the list of users
		this.lstMdlUsers = new DefaultListModel<String>();
		this.lstUsers = new JList<String>(this.lstMdlUsers);
		this.lstUsers.setSelectedIndex(0);
		this.scrollLstUsers = new JScrollPane(this.lstUsers);

		// Create the content pane
		Container contentPane = this.getContentPane();
		this.layout = new GroupLayout(contentPane);
		contentPane.setLayout(this.layout);
		this.layout.setAutoCreateGaps(true);
		this.layout.setAutoCreateContainerGaps(true);

		this.layout
				.setHorizontalGroup(this.layout
						.createParallelGroup()
						.addGroup(
								this.layout
										.createSequentialGroup()
										.addComponent(this.labelUserName, 100,
												100, 100)
										.addComponent(this.btnCreateBoard)
										.addComponent(this.btnSetUserName))
						.addGroup(
								this.layout
										.createSequentialGroup()
										.addComponent(this.scrollLstBoards,
												250, 250, 250)
										.addComponent(this.scrollLstUsers))

				);
		this.layout.setVerticalGroup(this.layout
				.createSequentialGroup()
				.addGroup(
						this.layout.createParallelGroup()
								.addComponent(this.labelUserName)
								.addComponent(this.btnCreateBoard)
								.addComponent(this.btnSetUserName))
				.addGroup(
						this.layout.createParallelGroup()
								.addComponent(this.scrollLstBoards)
								.addComponent(this.scrollLstUsers)));

		// Set properties of the frame
		this.setTitle("Whiteboard Lobby");
		this.setSize(500, 300);
		this.setResizable(false);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.addWindowListener(new WindowListen());
		
        // Make the necessary requests
		this.makeRequest(ClientSideMessageMaker.makeRequestStringGetBoardIDs());
		this.makeRequest(ClientSideMessageMaker.makeRequestStringGetUsersForBoardID(LobbyModel.LOBBY_ID));
	}

	/**
	 * Set up the logger
	 * @param level the level of the logger
	 */
	private void setupLogger(Level level) {
		try {
			BoardLogger.setup();
			LOGGER.setLevel(level);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Problems with creating the log files");
		}
	}

	/**
	 * Makes a request to the server
	 * @param req the request to make
	 */
	public void makeRequest(String req) {
		out.println(req);
		LOGGER.fine("REQ: " + req);
	}

	/**
	 * When we receive a list of names for the lobby, 
	 * populate the users list
	 * @param rcvdNames the names of users currently in the lobby
	 */
	public void onReceiveUserNames(List<String> rcvdNames) {
		final List<String> userNames = rcvdNames;
		SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {
				lstMdlUsers.clear();
				for (String userName : userNames) {
					lstMdlUsers.addElement(userName);
				}
				lstUsers.setSelectedIndex(lstMdlUsers.size() - 1);
			}
		});
	}

	/**
	 * When a user's name has been changed,
	 * update the username to reflect this
	 * @param rcvdName the new username
	 */
	public void onReceiveUsernameChanged(String rcvdName) {
		final String newName = rcvdName;
		SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {
				user.setName(newName);
				labelUserName.setText("User: " + newName);
				JOptionPane.showMessageDialog(null, "Changed username to "
						+ newName);
				if (canvas != null) {
					canvas.onReceiveUsernameChanged(newName);
				}
			}
		});
	}

	/**
	 * When we are welcomed to the server,
	 * keep track of our id and default username
	 */
	public void onReceiveWelcome(int id) {
		LOGGER.info("Successful connection to server");
		this.user = new User(id);
		labelUserName.setText("User: User" + String.valueOf(id));
	}

	private class SetUserNameListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String newUser = JOptionPane.showInputDialog("Enter new user name");
			user.setName(newUser);
			if (newUser == null) {
				LOGGER.warning("No username set on exit of JOptionPane");
				return;
			}
			makeRequest(ClientSideMessageMaker
					.makeRequestStringSetUsername(newUser));
		}
	}

	private class CreateWhiteboardListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String newBoard = JOptionPane
					.showInputDialog("Enter new Whiteboard name");
			if (newBoard == null) {
				LOGGER.warning("No canvas created on exit of JOptionPane");
				return;
			}
			newBoard = newBoard.replace(" ", "_");
			if(lstMdlBoards != null){
			    for(int i = 0; i < lstMdlBoards.size(); i++){
			        if(lstMdlBoards.get(i).equals(newBoard)){
			            JOptionPane.showMessageDialog(null, "That board already exists!");
			            return;
			        }
			    }
			}

			if (newBoard.equals("")) {
				newBoard = "Board" + (new Random()).nextInt(100000);
			}
			canvas = new Canvas(self, user.getName(), -1, newBoard);
			canvas.setVisible(true);
			setVisible(false);
			out.println(ClientSideMessageMaker
					.makeRequestStringCreateBoard(newBoard));
		}
	}

	private class JoinBoardListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				int selectedIndex = lstBoards.getSelectedIndex();
				for (BoardListItem boardListItem : boardListItems) {
					if (boardListItem.getBoardIndex() == selectedIndex) {
						canvas = new Canvas(self, user.getName(),
								boardListItem.getBoardID(),
								boardListItem.getBoardName());
						canvas.setVisible(true);
						setVisible(false);
						out.println(MessageHandler
								.makeRequestStringJoinBoardID(boardListItem
										.getBoardID()));
					}
				}
			}
		}
	}

	@Override
	public void onReceiveDraw(Line l) {
		if (canvas != null)
			canvas.onReceiveDraw(l);
	}

	@Override
	public void onReceiveBoardLines(List<Line> ls, Set<String> userNames) {
		if (canvas != null) {
			canvas.onReceiveBoardLines(ls, userNames);
		}
	}

	@Override
	public void onReceiveClear() {
		if (canvas != null)
			canvas.onReceiveClear();
	}

	@Override
	public void onReceiveUsers(int boardID, List<String> users) {
		if (canvas != null)
			canvas.onReceiveUsers(boardID, users);
		final int finalBoardID = boardID;
		final List<String> finalUsers = users;
		SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {
				if (finalBoardID == LobbyModel.LOBBY_ID) {
					lstMdlUsers.clear();
					for (String user : finalUsers) {
						lstMdlUsers.addElement(user);
					}
				}
			};
		});

	}

	@Override
	public void onReceiveCurrentBoardID(int boardID) {
		if (canvas != null)
			canvas.onReceiveCurrentBoardID(boardID);
	}

	@Override
	public void onReceiveBoardIDs(Map<Integer, String> rcvdBoardNameForID) {
		final Map<Integer, String> boardNameForID = rcvdBoardNameForID;
		SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {
				boardListItems = new ArrayList<BoardListItem>();
				int i = 0;
				for (int boardID : boardNameForID.keySet()) {
					boardListItems.add(new BoardListItem(boardNameForID
							.get(boardID), i, boardID));
					i++;
				}
				lstMdlBoards.clear();
				for (BoardListItem boardListItem : boardListItems) {
					lstMdlBoards.addElement(boardListItem.getBoardName());
					;
				}
			}
		});
	}

	private class WindowListen implements WindowListener {

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
			LOGGER.warning("Active LobbyGUI closed");
		}

		@Override
		public void windowClosed(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowOpened(WindowEvent e) {
		}
	}

	/**
	 * This is the main function.
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
	    int port = 4444;
        String hostName = "localhost";
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
                    } else if (flag.equals("--ip")) {
                        hostName = arguments.remove();
                    } else {
                        throw new IllegalArgumentException("unknown option: \"" + flag + "\"");
                    }
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("unable to parse number for " + flag);
                }
            }
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err.println("usage: WhiteboardClient [--port PORT] [--ip IP]");
            return;
        }
        final String finalHostName = hostName;
        final int finalPort = port;
        SwingUtilities.invokeLater(new Thread(){
            @Override
            public void run() {
                new WhiteboardClient(finalHostName,finalPort);
            }
        });

	}
}
