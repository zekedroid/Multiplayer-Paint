package protocol;

import adts.Line;

/**
 * 
 * This class is responsible for creating properly formatted request strings.
 *
 */
public class ClientSideMessageMaker {
    public static final String REQ_GET_BOARD_IDS = "get_board_ids";
    public static final String REQ_SET_USERNAME = "set_username";
    public static final String REQ_CREATE_BOARD = "create_board";
    public static final String REQ_GET_CURRENT_BOARD_ID = "get_current_board_id";
    public static final String REQ_GET_USERS_FOR_BOARD_ID = "get_users_for_board_id";
    public static final String REQ_JOIN_BOARD_ID = "join_board_id";
    public static final String REQ_LOGOUT = "logout";
    public static final String REQ_GET_USERS_IN_MY_BOARD = "get_users_in_my_board";
    public static final String REQ_LEAVE_BOARD = "leave_board";
    public static final String REQ_DRAW = "req_draw";
    public static final String REQ_CLEAR = "req_clear";

    public static final String RESP_BOARD_IDS = "board_ids";
    public static final String RESP_USERS_FOR_BOARD = "users_for_board_id";
    public static final String RESP_CURRENT_BOARD_ID = "current_board_id";
    public static final String RESP_FAILED = "failed";
    public static final String RESP_DONE = "done";
    public static final String RESP_LOGGED_OUT = "logged_out";
    public static final String RESP_DRAW = "draw";
    public static final String RESP_BOARD_LINES = "board_lines";

    /**
     * Returns the String corresponding to a request to get all board IDs.
     */
    public static String makeRequestStringGetBoardIDs() {
    	return ClientSideMessageMaker.REQ_GET_BOARD_IDS;
    }

    /** 
     * Returns the String corresponding to a request to change username to the input String.
     * @param newName the new username
     */
    public static String makeRequestStringSetUsername(String newName) {
        return String.format("%s %s", ClientSideMessageMaker.REQ_SET_USERNAME,
                newName.replace(" ", "_"));
    }

    /**
     * Returns the String corresponding to a request to create a board with
     * name specified by the input String.
     * @param boardName the name of the board to be created
     */
    public static String makeRequestStringCreateBoard(String boardName) {
        return String.format("%s %s", ClientSideMessageMaker.REQ_CREATE_BOARD,
                boardName.replace(" ", "_"));
    }

    /**
     * Returns the String corresponding to a request to get the current board ID.
     */
    public static String makeRequestStringGetCurrentBoardID() {
        return ClientSideMessageMaker.REQ_GET_CURRENT_BOARD_ID;
    }

    /**
     * Returns the String corresponding to a request to get the users who are 
     * in the board with ID specified by the argument.
     * @param the ID of the board
     */
    public static String makeRequestStringGetUsersForBoardID(int boardID) {
        return String.format("%s %d",
                ClientSideMessageMaker.REQ_GET_USERS_FOR_BOARD_ID, boardID);
    }

    /**
     * Returns the String corresponding to a request to join the board 
     * with ID specified by the argument.
     * @param the ID of the board to join
     */
    public static String makeRequestStringJoinBoardID(int boardID) {
        return String.format("%s %d", ClientSideMessageMaker.REQ_JOIN_BOARD_ID,
                boardID);
    }

    /**
     * Returns the String corresponding to a request to log out.
     */
    public static String makeRequestStringLogout() {
        return ClientSideMessageMaker.REQ_LOGOUT;
    }

    /**
     * Returns the String corresponding to a request to get the users in the current board.
     */
    public static String makeRequestStringGetUsersInMyBoard() {
        return ClientSideMessageMaker.REQ_GET_USERS_IN_MY_BOARD;
    }

    /**
     * Returns the String corresponding to a request to leave the current board.
     */
    public static String makeRequestStringLeaveBoard() {
        return ClientSideMessageMaker.REQ_LEAVE_BOARD;
    }

    /**
     * Returns the String corresponding to a request to draw the input Line on the current board.
     * @param line: The Line to draw.
     */
    public static String makeRequestStringDraw(Line line) {
        return String.format("%s %s", ClientSideMessageMaker.REQ_DRAW,
                line.toString());
    }
    
    /**
     * Returns the String corresponding to a request to clear the current board.
     */
    public static String makeRequestStringClear() {
        return String.format("%s", ClientSideMessageMaker.REQ_CLEAR);
    }

}
