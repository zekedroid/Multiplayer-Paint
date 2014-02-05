package adts;

import java.util.ArrayList;
import java.util.List;

/**
 * ADT that represents an instance of a Whiteboard.
 * 
 * Concurrency argument:
 *      The id is a final private integer and the name is a string (immutable). 
 *      The name and lines are the only field that can be changed, so we synchronize all
 *      the methods that manipulate them. Thus the class is threadsafe.
 */
public class Whiteboard {
    
    /**
     * The ID of this board, does not change!
     */
    private final int boardID;

    /**
     * The name of this board
     */
    private String boardName;

    /**
     * The list of lines that have been drawn The last line is the latest one
     * that has been drawn
     */
    private final List<Line> drawnLines;

    /**
     * Creates a board with the given boardID and boardName. The
     * board is cleared such that all pixels are white.
     * 
     * @param boardID
     *            the ID of the board
     * @param boardName
     *            the name of the board
     */
    public Whiteboard(int boardID, String boardName) {
        this.boardID = boardID;
        this.boardName = boardName;
        this.drawnLines = new ArrayList<Line>();
    }

    /**
     * Creates a board with the given boardID. The board is
     * cleared such that all pixels are white. The boardName is "Board"+boardID
     * (ex. if boardID = 2, the boardName is "Board2")
     * 
     * @param boardID
     *            the ID of the board
     */
    public Whiteboard(int boardID) {
        this(boardID, "Board" + boardID);
    }

    /**
     * @param l the line to add to the list of drawn lines
     */
    public synchronized void addLine(Line l) {
        this.drawnLines.add(l);
    }

    /**
     * @return all the drawn lines
     */
    public synchronized List<Line> getLines() {
        return this.drawnLines;
    }

    /**
     * @return the ID of the board
     */
    public int getBoardID() {
        return this.boardID;
    }

    /**
     * @return the name of the board
     */
    public synchronized String getBoardName() {
        return this.boardName;
    }

    /**
     * sets the name of the board
     * 
     * @param boardName the new name of the board
     */
    public synchronized void setBoardName(String boardName) {
        this.boardName = boardName;
    }
    
    
    /**
     * Deletes all the lines in the board
     */
    public synchronized void clearBoard(){
        this.drawnLines.clear();
    }
}
