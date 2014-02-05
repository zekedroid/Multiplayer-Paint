package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import adts.Line;
import adts.User;
import adts.Whiteboard;

/**
 * Tests that the adts have the proper behavior
 */
public class ADTTests {
    /*
     * Testing strategy
     * 
     * Goal: Check that adts can manipulate their data correctly
     * 
     * Strategy: Test all the methods that are used in this program
     * and ensure that the states change accordingly
     */
    
    /**
     * Create a line and ensure that all the getters work
     */
    @Test
    public void test_line_getters() {
        Line line = new Line(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals(line.getX1(), 1);
        assertEquals(line.getY1(), 2);
        assertEquals(line.getX2(), 3);
        assertEquals(line.getY2(), 4);
        assertEquals(line.getStrokeThickness(), 5, 0.01);
        assertEquals(line.getR(), 6);
        assertEquals(line.getG(), 7);
        assertEquals(line.getB(), 8);
        assertEquals(line.getA(), 9);
    }
    
    /**
     * Create a line and ensure that it's turned into 
     * a string correctly
     */
    @Test
    public void test_line_toString(){
        Line line = new Line(1,2,3,4,5,6,7,8,9);
        assertEquals("1 2 3 4 5.000000 6 7 8 9", line.toString());
    }
    
    /**
     * User getters
     */
    @Test
    public void test_user_getters(){
        User user = new User(1, "name");
        assertEquals(user.getID(), 1);
        assertEquals(user.getName(), "name");
        
        User anotherUser = new User(2);
        assertEquals(anotherUser.getID(), 2);
        assertEquals(anotherUser.getName(), "User2");
        
    }
    
    /**
     * Change a username
     */
    @Test
    public void test_username_change(){
        User user = new User(1, "initial name");
        assertEquals("initial name", user.getName());
        
        user.setName("final name");
        
        assertEquals("final name", user.getName());
    }
    
    /**
     * Test Whiteboard getters
     */
    @Test
    public void test_whiteboard_getters(){
        Whiteboard board = new Whiteboard(1);
        assertEquals(board.getBoardID(), 1);
        assertEquals(board.getBoardName(), "Board1");
        
        Whiteboard anotherBoard = new Whiteboard(3, "Other board");
        assertEquals(anotherBoard.getBoardID(), 3);
        assertEquals("Other board", anotherBoard.getBoardName());
    }
    
    /**
     * Test whiteboard name changing
     */
    @Test
    public void test_whiteboard_name_change(){
        Whiteboard board = new Whiteboard(1, "Some name");
        assertEquals("Some name", board.getBoardName());
        board.setBoardName("other name");
        assertEquals(board.getBoardName(), "other name");
    }    
    

}
