package protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import controller.WhiteboardClient;
import adts.Line;
import adts.LobbyModel;

/**
 * Is used by LobbyGUI to process responses from the server and update the GUI
 * accordingly.
 */
public class ClientSideResponseHandler {

	private final static Logger LOGGER = Logger.getLogger(ClientSideResponseHandler.class.getName());

	public static void handleResponse(String input, WhiteboardClient userGUI) {
		LOGGER.finest("RESP: " + input);

		String command = input.split(" ")[0];
		String[] tokens = input.replace(command, "").trim().split(" ");
		if (command.equals(MessageHandler.RESP_BOARD_IDS)) {
			handleBoardIDs(tokens, userGUI);
		} else if (command.equals(MessageHandler.RESP_USERNAME_CHANGED)) {
			handleUsernameChanged(tokens, userGUI);
		} else if (command.equals(MessageHandler.RESP_WELCOME)) {
			handleWelcome(tokens, userGUI);
		} else if (command.equals(MessageHandler.RESP_DRAW)) {
			handleDraw(tokens, userGUI);
		} else if (command.equals(MessageHandler.RESP_BOARD_LINES)) {
			handleBoardLines(tokens, userGUI);
		} else if (command.equals(MessageHandler.RESP_CLEAR)) {
			handleClear(tokens, userGUI);
		} else if (command.equals(MessageHandler.RESP_USERS_FOR_BOARD)) {
			handleUsersForBoard(tokens, userGUI);
		} else if (command.equals(MessageHandler.RESP_CURRENT_BOARD_ID)) {
			handleCurrentBoardID(tokens, userGUI);
		}
	}

	private static void handleCurrentBoardID(String[] tokens, WhiteboardClient userGUI) {
		int boardID = Integer.parseInt(tokens[0]);
		userGUI.onReceiveCurrentBoardID(boardID);
	}

	private static void handleUsersForBoard(String[] tokens, WhiteboardClient userGUI) {
		List<String> users = new ArrayList<String>();
		int boardID = Integer.parseInt(tokens[0]);
		for (int i = 1; i < tokens.length; i++)
			users.add(tokens[i]);
		userGUI.onReceiveUsers(boardID, users);
	}

	private static void handleClear(String[] tokens, WhiteboardClient userGUI) {
		userGUI.onReceiveClear();
	}

	private static void handleBoardIDs(String[] tokens, WhiteboardClient userGUI) {
		if (tokens.length <= 1)
			return;

		Map<Integer, String> boardNameForID = new HashMap<Integer, String>();

		int i = 0;
		while (i < tokens.length) {
			if (Integer.parseInt(tokens[i]) != LobbyModel.LOBBY_ID)
				boardNameForID.put(Integer.parseInt(tokens[i]), tokens[i + 1]);
			i = i + 2;
		}
		userGUI.onReceiveBoardIDs(boardNameForID);
	}

	private static void handleUsernameChanged(String[] tokens, WhiteboardClient userGUI) {
		userGUI.onReceiveUsernameChanged(tokens[0]);
	}

	private static void handleWelcome(String[] tokens, WhiteboardClient userGUI) {
		userGUI.onReceiveWelcome(Integer.parseInt(tokens[0]));
	}

	private static void handleDraw(String[] tokens, WhiteboardClient userGUI) {
		int x1 = Integer.parseInt(tokens[0]);
		int y1 = Integer.parseInt(tokens[1]);
		int x2 = Integer.parseInt(tokens[2]);
		int y2 = Integer.parseInt(tokens[3]);
		float strokeThickness = Float.parseFloat(tokens[4]);
		int r = Integer.parseInt(tokens[5]);
		int g = Integer.parseInt(tokens[6]);
		int b = Integer.parseInt(tokens[7]);
		int a = Integer.parseInt(tokens[8]);
		Line l = new Line(x1, y1, x2, y2, strokeThickness, r, g, b, a);
		userGUI.onReceiveDraw(l);
	}

	public static void handleBoardLines(String[] tokens, WhiteboardClient userGUI) {
		List<Line> lines = new ArrayList<Line>();
		Set<String> userNames = new HashSet<String>();
		int numUsers = Integer.parseInt(tokens[0]);
		int i = 0;
		for (i = 2; i < numUsers + 2; i++) {
			userNames.add(tokens[i]);
		}
		int x1, y1, x2, y2, r, g, b, a;
		float strokeThickness;
		while (i < tokens.length) {
			x1 = Integer.parseInt(tokens[i]);
			y1 = Integer.parseInt(tokens[i + 1]);
			x2 = Integer.parseInt(tokens[i + 2]);
			y2 = Integer.parseInt(tokens[i + 3]);
			strokeThickness = Float.parseFloat(tokens[i + 4]);
			r = Integer.parseInt(tokens[i + 5]);
			g = Integer.parseInt(tokens[i + 6]);
			b = Integer.parseInt(tokens[i + 7]);
			a = Integer.parseInt(tokens[i + 8]);
			i = i + 9;
			lines.add(new Line(x1, y1, x2, y2, strokeThickness, r, g, b, a));
		}
		userGUI.onReceiveBoardLines(lines, userNames);
	}
}
