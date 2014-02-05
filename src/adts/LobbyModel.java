package adts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is a system for managing users, boards, and the relationship 
 * (i.e. a set of users are working on a board) between them. The "lobby" 
 * itself is a waiting zone where users are put before they enter a board 
 * 
 * Rep Invariant: 
 * 
 * A user can only be in one board!
 *      This is ensured that each time we add a user,
 *      add a board, join a board, and leave a board.
 *      These are the only ways the association between 
 *      users and boards can change, so the rep invariant
 *      is preserved.
 * 
 * No two users have the same ID. No two boards have the same ID
 *      This is ensured by using two atomic integers. When we 
 *      receive a new user, we increment the integer and assign
 *      the value as the user's ID. When we receive a new board,
 *      we increment the integer and assign the value as the board's ID.
 * 
 * No two users have the same userName. No two boards have the same boardName.
 *      A name (String) is a meaningful string representation identifier of 
 *      a user or board (it is NOT the same thing as the ID, which is an integer).
 *      For instance, a boardName might be "6.005 brainstorming", and a userName
 *      might be "Rob_Miller". We ensure that userNames and boardNames are unique
 *      by checking if any other user has the same name. If so, we append an integer
 *      to the name and increment it until the name is unique. So, if three people
 *      tried to be "Rob_Miller", they will be "Rob_Miller", "Rob_Miller(2)", and 
 *      "Rob_Miller(3)".
 *      
 * Concurrency argument:
 *      All fields have been made final and atomic. 
 *      Thread safe classes (ex. synchronizedMap, AtomicInteger) 
 *      have been used everywhere possible. We have
 *      synchronized all methods that access or
 *      modify the state.
 *      
 */
public class LobbyModel {
    /**
     * A counter used to assign unique ids to each user
     */
    private final AtomicInteger uniqueUserID;

    /**
     * A counter used to assign unique ids to each board
     */
    private final AtomicInteger uniqueBoardID;

    /**
     * Key = user ID Value = user with the given ID
     */
    private final Map<Integer, User> userForID;

    /**
     * Key = board ID Value = board with the given ID
     */
    private final Map<Integer, Whiteboard> boardForID;

    /**
     * Key = board ID Value = list of IDs of users who are using the board with
     * the given ID
     */
    private final Map<Integer, Set<Integer>> userIDsForBoardID;

    /**
     * The ID of the lobby, which is the "board" where users are put
     * before entering a regular board
     */
    public static final int LOBBY_ID = -1;
    
    /**
     * Construct the LobbyModel
     */
    public LobbyModel() {
        uniqueUserID = new AtomicInteger(0);
        uniqueBoardID = new AtomicInteger(0);
        userForID = Collections.synchronizedMap(new HashMap<Integer, User>());
        boardForID = Collections
                .synchronizedMap(new HashMap<Integer, Whiteboard>());
        userIDsForBoardID = Collections
                .synchronizedMap(new HashMap<Integer, Set<Integer>>());
        
        this.boardForID.put(LOBBY_ID, new Whiteboard(LOBBY_ID, "Lobby"));
        this.userIDsForBoardID.put(LOBBY_ID, new HashSet<Integer>());
    }

    /**
     * @return the set of all the whiteboard names
     */
    public synchronized Set<String> getWhiteboardNames() {
        Set<String> whiteboardNames = new HashSet<String>();
        for (Whiteboard wb : this.boardForID.values()) {
            whiteboardNames.add(wb.getBoardName());
        }
        return whiteboardNames;
    }

    /**
     * @return the set of all whiteboard IDs
     */
    public synchronized Set<Integer> getWhiteboardIDs() {
        return this.boardForID.keySet();
    }

    /**
     * @param userID
     * @return the user ids of all the users who are in the same board(s) as the
     *         user with the given userID
     */
    public synchronized Set<Integer> getUserIDsOfUsersInSameBoardAsGivenUserID(
            int userID) {
        Set<Integer> userIDs = new HashSet<Integer>();
        // iterate through each boardID
        for (int boardID : this.boardForID.keySet()) {
            // if the set of users ids in that board include the given userID
            if (this.userIDsForBoardID.get(boardID).contains(userID)) {
                userIDs.addAll(this.userIDsForBoardID.get(boardID));
            }
        }
        return userIDs;
    }
    
    /**
     * @param userID
     *            the id of the user
     * @return the board ID of the board that the user with the given userID is
     *         in, or -1 if the user is not in any board
     */
    public synchronized int getBoardIDThatUserIDIsIn(int userID) {
        for (int boardID : this.boardForID.keySet()) {
            if (this.userIDsForBoardID.get(boardID).contains(userID)) {
                return boardID;
            }
        }
        return LOBBY_ID;
    }

    /**
     * @param userID the id of the user
     * @return the username for the user with the given id
     */
    public synchronized String getUserNameForUserID(int userID) {
        return this.userForID.get(userID).getName();
    }

    /**
     * Returns the user names for the given boardID
     * 
     * @param boardID
     *            the id of the board
     * @return the set of users in the given board
     * @throws IllegalArgumentException
     *             if the boardID does not exist
     */
    public synchronized Set<String> getUserNamesForBoardID(int boardID) {
        if (!(this.boardForID.keySet().contains(boardID)))
            throw new IllegalArgumentException(String.format(
                    "boardID=%d does not exist!", boardID));
        Set<String> userNames = new HashSet<String>();
        for (Integer userID : this.userIDsForBoardID.get(boardID)) {
            userNames.add(this.userForID.get(userID).getName());
        }
        return userNames;
    }

    /**
     * Change the username for a user with the given userID
     * 
     * @param newName
     *            the name that we should change to
     * @param userID
     *            the id of the user
     * @throws IllegalArgumentException
     *             if the userID does not exist
     */
    public synchronized String changeUserName(String newName, int userID) {
        if (!(this.userForID.keySet().contains(userID)))
            throw new IllegalArgumentException(String.format(
                    "userID=%d does not exist!", userID));
        Set<String> names = new HashSet<String>();
        for(Integer u : this.userForID.keySet()){
            if(u != userID){
                names.add(this.userForID.get(u).getName());
            }
        }
        if(!names.contains(newName)){
            this.userForID.get(userID).setName(newName);
            return newName;
        }
        int incrementer = 2;
        String formattedName = "%s(%d)";
        while(names.contains(String.format(formattedName, newName,incrementer)))
            incrementer++;
        this.userForID.get(userID).setName(String.format(formattedName, newName,incrementer));
        return String.format(formattedName, newName,incrementer);
    }

    /**
     * Adds a user to the lobby
     * 
     * @param name
     *            the name of the user
     * @return the id of the user who was added
     */
    public synchronized int addUser(String name) {
        int id = this.uniqueUserID.getAndIncrement();
        this.userForID.put(id, new User(id, name));
        this.userJoinBoard(id, LOBBY_ID);
        return id;
    }

    /**
     * Adds a user to the lobby with an automatically assigned name
     * 
     * @return the id of the user who was added
     */
    public synchronized int addUser() {
        int id = this.uniqueUserID.getAndIncrement();
        this.userForID.put(id, new User(id));
        this.changeUserName(this.userForID.get(id).getName(), id);
        this.userJoinBoard(id, LOBBY_ID);
        return id;
    }

    /**
     * Adds a board to the lobby
     * 
     * @param name
     *            the name of the board
     * @param width
     *            the width of the board
     * @param height
     *            the height of the board
     * @return the id of the board that was added
     */
    public synchronized int addBoard(String name) {
        int id = this.uniqueBoardID.getAndIncrement();
        Whiteboard board = new Whiteboard(id, name);
        this.userIDsForBoardID.put(id, new HashSet<Integer>());
        Set<String> userNames = new HashSet<String>();
        for(Whiteboard brd: this.boardForID.values()){
            userNames.add(brd.getBoardName());
        }
        if(!userNames.contains(name)){
            this.boardForID.put(id, board);
            return id;
        }
        int incrementer = 1;
        while(userNames.contains(String.format("%s(%d)",name,incrementer))){
            incrementer++;
        }
        this.boardForID.get(id).setBoardName(String.format("%s(%d)",name,incrementer));
        return id;
    }

    /**
     * Adds a board to the lobby with an automatically generated name and
     * default height and width
     * 
     * @return the id of the board that was added
     */
    public synchronized int addBoard() {
        return this.addBoard("Board");
    }

    /**
     * Adds the user with the given userID to the board with the given boardID.
     * If the user is in a board (different from the one with the given
     * boardID), then the user is removed from that board
     * 
     * @param userID
     *            the id of the user to be added
     * @param boardID
     *            the id of the board that the user should be added to
     * @throws IllegalArgumentException
     *             if the userID or boardID do not exist
     */
    public synchronized void userJoinBoard(int userID, int boardID) {
        if (!(this.boardForID.keySet().contains(boardID)))
            throw new IllegalArgumentException(String.format(
                    "boardID=%d does not exist!", boardID));
        if (!(this.userForID.keySet().contains(userID)))
            throw new IllegalArgumentException(String.format(
                    "userID=%d does not exist!", userID));
        for (int bID : this.boardForID.keySet()) {
            if (this.userIDsForBoardID.get(bID).contains(userID)) {
                this.userIDsForBoardID.get(bID).remove(userID);
            }
        }
        Set<Integer> userIDs = this.userIDsForBoardID.get(boardID);
        userIDs.add(userID);
    }

    /**
     * Removes the user with the given userID from the board with the given
     * boardID
     * 
     * @param userID
     *            the id of the user to be added
     * @param boardID
     *            the id of the board that the user should be removed from
     * @throws IllegalArgumentException
     *             if the userID or boardID do not exist
     */
    public synchronized void userLeaveBoard(int userID, int boardID) {
        if (!(this.boardForID.keySet().contains(boardID)))
            throw new IllegalArgumentException(String.format(
                    "boardID=%d does not exist!", boardID));
        if (!(this.userForID.keySet().contains(userID)))
            throw new IllegalArgumentException(String.format(
                    "userID=%d does not exist!", userID));
        this.userIDsForBoardID.get(boardID).remove(userID);
        this.userJoinBoard(userID, LOBBY_ID);
    }

    /**
     * Delete user from set of users and remove the user from all the boards
     * 
     * @param userID
     *            the id of the user to delete
     */
    public synchronized void deleteUser(int userID) {
        this.userForID.remove(userID);
        for (int boardID : this.userIDsForBoardID.keySet()) {
            this.userIDsForBoardID.get(boardID).remove(userID);
        }
    }

    /**
     * Returns the user ids in the board with the given boardID
     * 
     * @param boardID
     *            the id of the board
     * @return the set of user ids of the users in the board with the given
     *         board id
     */
    public synchronized Set<Integer> getUserIDsForBoardID(int boardID) {
        return this.userIDsForBoardID.get(boardID);
    }

    /**
     * Draws a line on a board
     * 
     * @param l
     *            the line to add
     * @param boardID
     *            the id of the board we should add the line to
     */
    public synchronized void addLineToBoardID(Line l, int boardID) {
        if (!(this.boardForID.keySet().contains(boardID)))
            throw new IllegalArgumentException(String.format(
                    "boardID=%d does not exist!", boardID));
        this.boardForID.get(boardID).addLine(l);
    }

    /**
     * Gets the lines for the board with the given boardID
     * 
     * @param boardID
     *            the id of the board
     * @return the lines on that board
     */
    public synchronized List<Line> getLinesForBoardID(int boardID) {
        if (!(this.boardForID.keySet().contains(boardID)))
            throw new IllegalArgumentException(String.format(
                    "boardID=%d does not exist!", boardID));
        return this.boardForID.get(boardID).getLines();
    }
    
    /**
     * Clears the board with the given ID
     * @param boardID the board to clear
     */
    public synchronized void clearBoard(int boardID){
        this.boardForID.get(boardID).clearBoard();
    }
  
    /**
     * @return the whiteboards
     */
    public synchronized Collection<Whiteboard> getWhiteboards(){
        return this.boardForID.values();
    }

    /**
     * @return true if the rep invariant is satisfied.
     * 
     * The rep invariant:
     * 1) One user per board
     * 2) All users have unique ids (satisfied because we use user IDs as keys)
     * 3) All boards have unique ids (satisfied because we use board IDs as keys)
     * 4) All users have unique names
     * 5) All boards have unique names
     * 
     */
    public boolean checkRep(){
        
        // 1) One user per board
        Set<Integer> userIDsInBoard = new HashSet<Integer>();
        for(int boardID : this.boardForID.keySet()){
            for(int userID : this.userIDsForBoardID.get(boardID)){
                if(userIDsInBoard.contains(userIDsInBoard)){
                    return false;
                }
                userIDsInBoard.add(userID);
            }
        }
        
        // 4) All users have unique names
        Set<String> userNames = new HashSet<String>();
        for(User user : this.userForID.values()){
            if(userNames.contains(user.getName())){
                return false;
            }
            userNames.add(user.getName());
        }
        
        // 5) All boards have unique names
        Set<String> boardNames = new HashSet<String>();
        for(Whiteboard board : this.boardForID.values()){
            if(boardNames.contains(board.getBoardName())){
                return false;
            }
            boardNames.add(board.getBoardName());
        }
        
        return true;
        
    }
}
