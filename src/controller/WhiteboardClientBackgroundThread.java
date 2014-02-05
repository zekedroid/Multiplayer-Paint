package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

import protocol.ClientSideResponseHandler;

/**
 * Use this class to send tasks to Swing. It must be used whenever mutating the
 * given JSwing object. It creates a new Thread object. Also uses the global
 * LOGGER to track serverResponses when needed.
 * 
 */
public class WhiteboardClientBackgroundThread extends Thread {

	private final static Logger LOGGER = Logger
			.getLogger(WhiteboardClientBackgroundThread.class.getName());

	private final WhiteboardClient gui;
	private final BufferedReader in;

	/**
	 * Set the parameters using this Constructor.
	 * 
	 * @param gui
	 *            LobbyGUI instance to be modified
	 * @param in
	 *            server response
	 */
	public WhiteboardClientBackgroundThread(WhiteboardClient gui, BufferedReader in) {
		this.gui = gui;
		this.in = in;
	}

	@Override
	/**
	 * This client background thread listens for responses from the server and handles them.
	 */
	public void run() {
		String serverResponse;
		try {
			while ((serverResponse = in.readLine()) != null) {
				LOGGER.config(serverResponse);
				ClientSideResponseHandler.handleResponse(serverResponse,
						this.gui);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
