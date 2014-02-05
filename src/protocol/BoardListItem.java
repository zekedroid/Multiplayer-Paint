package protocol;

/**
 * This class is helpful for displaying boards in lists
 * It groups the board id, name, and index in the list
 *
 */
public class BoardListItem {
    
    /**
     * The name of the board
     */
    private final String boardName;
    
    /**
     * The index of the board within the list
     */
    private final int boardIndex;
    
    /**
     * The ID of the board
     */
    private final int boardID;
    
    /**
     * Creates a BoardListItem
     * @param boardName the name of the board
     * @param boardIndex the index of the board
     * @param boardID the id of the board
     */
    public BoardListItem(String boardName, int boardIndex, int boardID){
        this.boardName = boardName;
        this.boardIndex = boardIndex;
        this.boardID = boardID;
    }
    
    /**
     * @return the name of the board
     */
    public String getBoardName(){
        return this.boardName;
    }
    
    /**
     * @return the index of the board
     */
    public int getBoardIndex(){
        return this.boardIndex;
    }
    
    /**
     * @return the id of the board
     */
    public int getBoardID(){
        return this.boardID;
    }
}
