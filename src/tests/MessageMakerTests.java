package tests;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import adts.Line;
import protocol.ClientSideMessageMaker;

/**
 * Tests that the client-side messages being sent to the server are properly
 * parsed and compiled.
 */
public class MessageMakerTests {

	/*
	 * Testing strategy
	 * 
	 * Goal: Check that ClientSideMessageMaker makes the right messages.
	 * 
	 * Strategy: Test all message formats that it's expected to create.
	 * Important for drawing lines is the order of the parameters. Even though
	 * these tests are simple, they are crucial for the major server/client
	 * testing suite to work.
	 */

	@Test
	public void get_board_IDs_test() {
		assertEquals("get_board_ids",
				ClientSideMessageMaker.makeRequestStringGetBoardIDs());
	}

	@Test
	public void set_username_test() {
		assertEquals("set_username newUsername123",
				ClientSideMessageMaker
						.makeRequestStringSetUsername("newUsername123"));
	}

	@Test
	public void create_board_test() {
		assertEquals("create_board newBoardName987",
				ClientSideMessageMaker
						.makeRequestStringCreateBoard("newBoardName987"));
	}

	@Test
	public void get_users_for_board_ID_test() {
		assertEquals("get_users_for_board_id 234",
				ClientSideMessageMaker.makeRequestStringGetUsersForBoardID(234));
	}

	@Test
	public void logout_test() {
		assertEquals("logout", ClientSideMessageMaker.makeRequestStringLogout());
	}

	@Test
	public void get_users_in_my_board_test() {
		assertEquals("get_users_in_my_board",
				ClientSideMessageMaker.makeRequestStringGetUsersInMyBoard());
	}

	@Test
	public void leave_board_test() {
		assertEquals("leave_board",
				ClientSideMessageMaker.makeRequestStringLeaveBoard());
	}

	@Test
	public void req_draw_test() {
		int x1 = 30;
		int y1 = 60;
		int x2 = 90;
		int y2 = 210;
		float strokeThickness = 15;
		int r = 125;
		int g = 255;
		int b = 0;
		int a = 10;
		Line line = new Line(x1, y1, x2, y2, strokeThickness, r, g, b, a);

		assertEquals(
				"req_draw 30 60 90 210 " + String.format("%f", strokeThickness)
						+ " 125 255 0 10",
				ClientSideMessageMaker.makeRequestStringDraw(line));
	}

}