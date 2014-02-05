package protocol;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import adts.Line;
import adts.LobbyModel;
import adts.Whiteboard;
import server.UserThread;

/**
 * Is used by the server to handle messages and update the ADT accordingly.
 */
public class MessageHandler {
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
    public static final String RESP_USERNAME_CHANGED = "changed_username";
    public static final String RESP_USERS_FOR_BOARD = "users_for_board_id";
    public static final String RESP_CURRENT_BOARD_ID = "current_board_id";
    public static final String RESP_FAILED = "failed";
    public static final String RESP_DONE = "done";
    public static final String RESP_LOGGED_OUT = "logged_out";
    public static final String RESP_DRAW = "draw";
    public static final String RESP_BOARD_LINES = "board_lines";
    public static final String RESP_CLEAR = "clear_board";
    public static final String RESP_WELCOME = "welcome";

    /**
     * Is used on the server's side.
     * Handles the input String and updates the input lobbyModel appropriately.
     * Also sends the appropriate response back to the input UserThread.
     * @param input
     * @param userThread
     * @param lobbyModel
     */
    public static void handleMessage(String input, UserThread userThread,
            LobbyModel lobbyModel) {
        String command = input.split(" ")[0];

        if (command.equals(MessageHandler.REQ_GET_BOARD_IDS)) {
            MessageHandler.handleRequestGetBoardIDs(input, userThread,
                    lobbyModel);
        } else if (command.equals(MessageHandler.REQ_SET_USERNAME)) {
            MessageHandler.handleRequestSetUsername(input, userThread,
                    lobbyModel);
        } else if (command.equals(MessageHandler.REQ_CREATE_BOARD)) {
            MessageHandler.handleRequestCreateBoard(input, userThread,
                    lobbyModel);
        } else if (command.equals(MessageHandler.REQ_GET_CURRENT_BOARD_ID)) {
            MessageHandler.handleRequestGetCurrentBoard(input, userThread,
                    lobbyModel);
        } else if (command.equals(MessageHandler.REQ_GET_USERS_FOR_BOARD_ID)) {
            MessageHandler.handleRequestGetUsersForBoardID(input, userThread,
                    lobbyModel);
        } else if (command.equals(MessageHandler.REQ_JOIN_BOARD_ID)) {
            MessageHandler.handleRequestJoinBoardID(input, userThread,
                    lobbyModel);
        } else if (command.equals(MessageHandler.REQ_LOGOUT)) {
            MessageHandler.handleRequestLogout(input, userThread, lobbyModel);
        } else if (command.equals(MessageHandler.REQ_GET_USERS_IN_MY_BOARD)) {
            MessageHandler.handleRequestGetUsersInMyBoard(input, userThread,
                    lobbyModel);
        } else if (command.equals(MessageHandler.REQ_LEAVE_BOARD)) {
            MessageHandler.handleRequestLeaveBoard(input, userThread,
                    lobbyModel);
        } else if (command.equals(MessageHandler.REQ_DRAW)) {
            MessageHandler.handleRequestDraw(input, userThread, lobbyModel);
        } else if (command.equals(MessageHandler.REQ_CLEAR)) {
            MessageHandler.handleRequestClear(input, userThread, lobbyModel);
        }
    }

    /**
     * Req: get_board_ids 
     * Resp: board_ids [id1] [userName1] [id2] [userName2] [id3] [userName3]
     */
    private static void handleRequestGetBoardIDs(String input,
            UserThread userThread, LobbyModel lobbyModel) {
            userThread.output(MessageHandler.makeResponseBoardIDs(lobbyModel.getWhiteboards()));
    }

    /**
     * Req: set_username [newUserName]
     * Resp (to all users in board): users_for_board_id [boardID] [userName1] [userName2]...
     * Resp (to user who made request): changed_username [newUserName]
     * Resp (to all the users in the lobby, if the user who changed his name is in the lobby): users_for_board -1 [userName1] [userName2]
     */
    private static void handleRequestSetUsername(String input,
            UserThread userThread, LobbyModel lobbyModel) {
        Set<String> userNames = new HashSet<String>();
        Set<Integer> userIDsOfUsersInSameBoard = new HashSet<Integer>();

        String[] splitString = input.split(" ");
        int userID = userThread.getUserID();
        String newName = splitString[1];
        newName = lobbyModel.changeUserName(newName, userID);
        int boardID = lobbyModel.getBoardIDThatUserIDIsIn(userID);
        if (boardID != LobbyModel.LOBBY_ID) {
            userIDsOfUsersInSameBoard = lobbyModel
                    .getUserIDsOfUsersInSameBoardAsGivenUserID(userID);
            for (int uID : userIDsOfUsersInSameBoard) {
                userNames.add(lobbyModel.getUserNameForUserID(uID));
            }
        }

        String response = MessageHandler.makeResponseUsersForBoardID(boardID,
                userNames);
        userThread.broadcast(response);
        userThread.output(MessageHandler.makeResponseUsernameChanged(newName));
        if(boardID == LobbyModel.LOBBY_ID){
            notifyLobbyUsers(userThread, lobbyModel, true, boardID);
        }
    }

    /**
     * Req: create_board [boardName]
     * Resp (to all other users): board_ids [id1] [userName1] [id2] [userName2] [id3] [userName3]
     * Resp (to user who made request): board_ids [id1] [userName1] [id2] [userName2] [id3] [userName3]
     * AND
     * current_board_id [currentBoardID]
     * Resp (to all the users in the lobby, if the user who changed his name is in the lobby): users_for_board -1 [userName1] [userName2]
     */
    private static void handleRequestCreateBoard(String input,
            UserThread userThread, LobbyModel lobbyModel) {
        String[] splitString = input.split(" ");
        int userID = userThread.getUserID();
        String boardName = splitString[1];
        int boardID = lobbyModel.addBoard(boardName);
        lobbyModel.userJoinBoard(userID, boardID);
        userThread.broadcast(MessageHandler.makeResponseBoardIDs(lobbyModel
                .getWhiteboards()));
        userThread.output(MessageHandler.makeResponseBoardIDs(lobbyModel
                .getWhiteboards()));
        userThread.output(MessageHandler.makeResponseCurrentBoardID(boardID));
        notifyLobbyUsers(userThread, lobbyModel, true, boardID);
    }

    /**
     * Req: get_current_board_id
     * Resp: current_board_id [boardID]
     */
    private static void handleRequestGetCurrentBoard(String input,
            UserThread userThread, LobbyModel lobbyModel) {
        int boardID = lobbyModel.getBoardIDThatUserIDIsIn(userThread
                .getUserID());
        userThread.output(MessageHandler.makeResponseCurrentBoardID(boardID));
    }

    /**
     * Req: get_users_for_board_id [boardID]
     * Resp: users_for_board [boardID] [userName1] [userName2]...
     */
    private static void handleRequestGetUsersForBoardID(String input,
            UserThread userThread, LobbyModel lobbyModel) {
        int boardID = Integer.parseInt(input.split(" ")[1]);
        Set<String> userNames = lobbyModel.getUserNamesForBoardID(boardID);
        userThread.output(MessageHandler.makeResponseUsersForBoardID(boardID,
                userNames));
    }

    /**
     * Req: join_board_id [boardID]
     * Resp (to all users in board): users_for_board_id [boardID] [userName1] [userName2]...
     * Resp (to user who made request): board_lines [numberOfUserNames] [numberOfLines] [userName1] [userName2] ... [userName_N] [x1] [y1] [x2] [y2] [strokeThickness] [r] [g] [b] [a] [x1] [y1] [x2] [y2] [strokeThickness] [r] [g] [b] [a] [x1] [y1] [x2] [y2] [strokeThickness] [r] [g] [b] [a] [x1] [y1] [x2] [y2] [strokeThickness] [r] [g] [b] [a]...
     * Resp (to all the users in the lobby, if the user who changed his name is in the lobby): users_for_board -1 [userName1] [userName2]
     */
    private static void handleRequestJoinBoardID(String input,
            UserThread userThread, LobbyModel lobbyModel) {
        int boardID = Integer.parseInt(input.split(" ")[1]);
        try {
            lobbyModel.userJoinBoard(userThread.getUserID(), boardID);
            Set<String> userNames = lobbyModel.getUserNamesForBoardID(boardID);
            String response = MessageHandler.makeResponseUsersForBoardID(
                    boardID, userNames);
            List<Line> lines = lobbyModel.getLinesForBoardID(boardID);
            userThread.broadcast(response);
            userThread.output(MessageHandler.makeResponseBoardLines(lines,userNames));
            notifyLobbyUsers(userThread, lobbyModel, true, boardID);
        } catch (Exception ex) {
            userThread.output(MessageHandler.makeResponseFailed());
        }
    }

    /**
     * Req: logout
     * Resp (to all users in board): users_for_board [boardID] [userName1] [userName2]...
     * Resp (to user who made request): logged_out
     * Resp (to all the users in the lobby, if the user who changed his name is in the lobby): users_for_board -1 [userName1] [userName2]
     */
    private static void handleRequestLogout(String input,
            UserThread userThread, LobbyModel lobbyModel) {
        int boardID = lobbyModel.getBoardIDThatUserIDIsIn(userThread
                .getUserID());
        lobbyModel.deleteUser(userThread.getUserID());
        if (boardID != -1) {
            Set<String> userNames = lobbyModel.getUserNamesForBoardID(boardID);
            String response = MessageHandler.makeResponseUsersForBoardID(
                    boardID, userNames);
            userThread.broadcast(response);
        }
        notifyLobbyUsers(userThread, lobbyModel, true, boardID);
        userThread.output(MessageHandler.makeResponseLoggedOut());
        userThread.closeSocket();
    }

    /**
     * Req: get_users_in_my_board
     * Resp: users_for_board_id [boardID] [userName1] [userName2]...
     * (if not in a board): failed
     */
    private static void handleRequestGetUsersInMyBoard(String input,
            UserThread userThread, LobbyModel lobbyModel) {
        int boardID = lobbyModel.getBoardIDThatUserIDIsIn(userThread.getUserID());
        if (boardID != -1) {
            Set<String> userNames = lobbyModel.getUserNamesForBoardID(boardID);
            userThread.output(MessageHandler.makeResponseUsersForBoardID(
                    boardID, userNames));
        } else {
            userThread.output(MessageHandler.makeResponseFailed());
        }
    }

    /**
     * Req: leave_board
     * Resp (to all users in board): users_for_board [boardID] [userName1] [userName2]...
     * Resp (to user who made request): done
     * Resp (to all the users in the lobby, if the user who changed his name is in the lobby): users_for_board -1 [userName1] [userName2]
     */
    private static void handleRequestLeaveBoard(String input,
            UserThread userThread, LobbyModel lobbyModel) {
        int boardID = lobbyModel.getBoardIDThatUserIDIsIn(userThread
                .getUserID());
        if (boardID != -1) {
            lobbyModel.userLeaveBoard(userThread.getUserID(), boardID);
            Set<String> userNames = lobbyModel.getUserNamesForBoardID(boardID);
            String response = MessageHandler.makeResponseUsersForBoardID(
                    boardID, userNames);
            userThread.broadcast(response);
        }
        notifyLobbyUsers(userThread, lobbyModel, true, boardID);
        userThread.output(MessageHandler.makeResponseDone());
    }

    /**
     * Req: req_draw [x1] [y1] [x2] [y2] [strokeThickness] [r] [g] [b] [a]
     * Resp (to all users in board including user who made request): draw [x1] [y1] [x2] [y2] [strokeThickness] [r] [g] [b] [a]
     * (if not in a board): failed
     */
    private static void handleRequestDraw(String input, UserThread userThread,
            LobbyModel lobbyModel) {
        int boardID = lobbyModel.getBoardIDThatUserIDIsIn(userThread
                .getUserID());
        if (boardID != -1) {
            String[] splitInput = input.split(" ");
            int x1 = Integer.parseInt(splitInput[1]);
            int y1 = Integer.parseInt(splitInput[2]);

            int x2 = Integer.parseInt(splitInput[3]);
            int y2 = Integer.parseInt(splitInput[4]);

            float strokeThickness = Float.parseFloat(splitInput[5]);

            int r = Integer.parseInt(splitInput[6]);
            int g = Integer.parseInt(splitInput[7]);
            int b = Integer.parseInt(splitInput[8]);
            int a = Integer.parseInt(splitInput[9]);

            Line line = new Line(x1, y1, x2, y2, strokeThickness, r, g, b, a);
            lobbyModel.addLineToBoardID(line, boardID);

            Set<Integer> userIDsOfUsersInSameBoard = lobbyModel
                    .getUserIDsForBoardID(boardID);
            String response = MessageHandler.makeResponseDraw(line);

            userThread.broadcast(response, userIDsOfUsersInSameBoard);
            userThread.output(response);
        } else {
            userThread.output(MessageHandler.makeResponseFailed());
        }
    }

    /**
     * Req: req_clear_board
     * Resp (to all users in board including user who made request): clear_board
     */
    private static void handleRequestClear(String input, UserThread userThread,
            LobbyModel lobbyModel) {
        int boardID = lobbyModel.getBoardIDThatUserIDIsIn(userThread
                .getUserID());
        if (boardID != -1) {
            lobbyModel.clearBoard(boardID);
            String response = MessageHandler.makeResponseClearBoard();
            Set<Integer> userIDsInSameBoard = lobbyModel.getUserIDsForBoardID(boardID);
            userThread.broadcast(response, userIDsInSameBoard);
            userThread.output(response);
        } else {
            userThread.output(MessageHandler.makeResponseFailed());
        }
    }

    /**
     * Sends a users_for_board_id response to all users in the lobby
     * @param userThread a thread to use for broadcasting
     * @param lobbyModel the lobby model
     */
    public static void notifyLobbyUsers(UserThread userThread, LobbyModel lobbyModel,boolean includingSelf, int userThreadsBoardID){
        Set<Integer> userIDs = lobbyModel.getUserIDsForBoardID(LobbyModel.LOBBY_ID);
        Set<String> userNames = lobbyModel.getUserNamesForBoardID(LobbyModel.LOBBY_ID);
        String response = MessageHandler.makeResponseUsersForBoardID(LobbyModel.LOBBY_ID, userNames);
        userThread.broadcast(response, userIDs);
        if(includingSelf && userThreadsBoardID==LobbyModel.LOBBY_ID){
            userThread.output(response);
        }
    }
    /*************************************************************/

    /**
     * @return 'done'
     */
    private static String makeResponseDone() {
        return MessageHandler.RESP_DONE;
    }

    /**
     * @return 'failed'
     */
    private static String makeResponseFailed() {
        return MessageHandler.RESP_FAILED;
    }

    /**
     * @return 'logged_out'
     */
    private static String makeResponseLoggedOut() {
        return MessageHandler.RESP_LOGGED_OUT;
    }
    
    private static String makeResponseUsernameChanged(String newName){
        return String.format("%s %s", RESP_USERNAME_CHANGED, newName);
    }

    /**
     * @param boardID
     *            the board id
     * @return 'current_board_id [boardID]'
     */
    private static String makeResponseCurrentBoardID(int boardID) {
        return String.format("%s %d", MessageHandler.RESP_CURRENT_BOARD_ID,
                boardID);
    }

    /**
     * @param boardIDs
     *            the set of IDs
     * @return 'board_ids [id1] [username1] [id2] [username2] [id3] [username3]...'
     */
    private static String makeResponseBoardIDs(Collection<Whiteboard> boards) {
        StringBuilder response = new StringBuilder();
        response.append(MessageHandler.RESP_BOARD_IDS);
        for(Whiteboard board : boards){
            response.append(" " + board.getBoardID() + " " + board.getBoardName());
        }
        return response.toString();
    }

    /**
     * @param boardID
     *            the id of the board
     * @param userNames
     *            the names of the users in the board
     * @return 'users_for_board [boardID] [userName1] [userName2]...'
     */
    private static String makeResponseUsersForBoardID(int boardID,
            Set<String> userNames) {
        StringBuilder response = new StringBuilder();
        response.append(MessageHandler.RESP_USERS_FOR_BOARD);
        response.append(" " + boardID);
        for (String userName : userNames) {
            response.append(" " + userName);
        }
        return response.toString();
    }

    /**
     * @param boardID
     *            the id of the board
     * @param line
     *            the line to draw
     * @return 'draw [x1] [y1] [x2] [y2] [strokeThickness] [r] [g] [b] [a]'
     */
    private static String makeResponseDraw(Line line) {
        return String
                .format("%s %s", MessageHandler.RESP_DRAW, line.toString());
    }

    /**
     * @param lines
     *            the list of lines
     * @return board_lines [x1] [y1] [x2] [y2] [strokeThickness] [r] [g] [b] [a]
     *         [x1] [y1] [x2] [y2] [strokeThickness] [r] [g] [b] [a] [x1] [y1]
     *         [x2] [y2] [strokeThickness] [r] [g] [b] [a] [x1] [y1] [x2] [y2]
     *         [strokeThickness] [r] [g] [b] [a]...
     */
    private static String makeResponseBoardLines(List<Line> lines, Set<String> userNames) {
        StringBuilder response = new StringBuilder();
        response.append(MessageHandler.RESP_BOARD_LINES);
        response.append(" " + userNames.size());
        response.append(" " + lines.size());
        for (String userName : userNames){
            response.append(" " + userName);
        }
        for (Line line : lines) {
            response.append(" " + line.toString());
        }
        return response.toString();
    }

    private static String makeResponseClearBoard() {
        return MessageHandler.RESP_CLEAR;
    }
    
    /*************************************************************/

    public static String makeRequestStringGetBoardIDs() {
        return MessageHandler.REQ_GET_BOARD_IDS;
    }

    public static String makeRequestStringSetUsername(String newName) {
        return String.format("%s %s", MessageHandler.REQ_SET_USERNAME,
                newName.replace(" ", "_"));
    }

    public static String makeRequestStringCreateBoard(String boardName) {
        return String.format("%s %s", MessageHandler.REQ_CREATE_BOARD,
                boardName.replace(" ", "_"));
    }

    public static String makeRequestStringGetCurrentBoardID() {
        return MessageHandler.REQ_GET_CURRENT_BOARD_ID;
    }

    public static String makeRequestStringGetUsersForBoardID(int boardID) {
        return String.format("%s %d",
                MessageHandler.REQ_GET_USERS_FOR_BOARD_ID, boardID);
    }

    public static String makeRequestStringJoinBoardID(int boardID) {
        return String
                .format("%s %d", MessageHandler.REQ_JOIN_BOARD_ID, boardID);
    }

    public static String makeRequestStringLogout() {
        return MessageHandler.REQ_LOGOUT;
    }

    public static String makeRequestStringGetUsersInMyBoard() {
        return MessageHandler.REQ_GET_USERS_IN_MY_BOARD;
    }

    public static String makeRequestStringLeaveBoard() {
        return MessageHandler.REQ_LEAVE_BOARD;
    }

    public static String makeRequestStringDraw(Line line) {
        return String.format("%s %s", MessageHandler.REQ_DRAW, line.toString());
    }

    public static String makeClearRequest() {
        return MessageHandler.REQ_CLEAR;
    }
}