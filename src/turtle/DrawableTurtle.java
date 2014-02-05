package turtle;

import java.util.ArrayList;
import java.util.List;

/**
 * Turtle for drawing.
 */
public class DrawableTurtle implements Turtle {

	List<Action> actionList;
	List<LineSegment> lines;

	Point currentPosition;
	double currentHeading;

	public DrawableTurtle() {
		int randomX = 350 + (int) (Math.random() * (500));
		int randomY = 100 + (int) (Math.random() * (600));
		
		this.currentPosition = new Point(randomX, randomY);
		this.currentHeading = 0.0;
		this.lines = new ArrayList<LineSegment>();
		this.actionList = new ArrayList<Action>();
	}

	/**
	 * Command to send the turtle forward a number of units.
	 * 
	 * @param units
	 *            number of pixels to go in currentHeading's direction; must be
	 *            positive.
	 */
	public void forward(int units) {
		double newX = this.currentPosition.x
				+ Math.cos(Math.toRadians(90.0 - currentHeading))
				* (double) units;
		double newY = this.currentPosition.y
				+ Math.sin(Math.toRadians(90.0 - currentHeading))
				* (double) units;

		LineSegment lineSeg = new LineSegment(this.currentPosition.x,
				this.currentPosition.y, newX, newY);
		this.lines.add(lineSeg);
		this.currentPosition = new Point(newX, newY);

		this.actionList.add(new Action(ActionType.FORWARD, units, 0.0,
				"forward " + units + " units", lineSeg));
	}

	/**
	 * Change the heading by some degrees clockwise.
	 * 
	 * @param degrees
	 *            amount of change in angle, in degrees, with positive being
	 *            clockwise.
	 */
	public void turn(double degrees) {
		degrees = (degrees % 360 + 360) % 360;
		this.currentHeading += degrees;
		if (this.currentHeading >= 360.0)
			this.currentHeading -= 360.0;
		this.actionList.add(new Action(ActionType.TURN, 0, degrees, "turn "
				+ degrees + " degrees", null));
	}

	@Override
	public List<LineSegment> draw() {
		return this.lines;

	}

}
