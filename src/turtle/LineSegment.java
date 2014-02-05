package turtle;

/**
 * Class for a line segment in pixel space.
 */
public class LineSegment {
	public final Point start;
	public final Point end;

	/**
	 * Constructor that takes in 4 integers for the coordinates.
	 * 
	 * @param startx x-coordinate of start point
	 * 
	 * @param starty y-coordinate of start point
	 * 
	 * @param endx x-coordinate of end point
	 * 
	 * @param endy y-coordinate of end point
	 */
	public LineSegment(double startx, double starty, double endx, double endy) {
		this.start = new Point(startx, starty);
		this.end = new Point(endx, endy);
	}

	/**
	 * Constructor that takes in the start point and end point.
	 * 
	 * @param start one end of the line segment
	 * 
	 * @param end the other end of the line segment
	 */
	public LineSegment(Point start, Point end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Calculates the length of this segment.
	 * 
	 * @return the length of the line segment
	 */
	public double length() {
		return Math.sqrt(Math.pow(this.start.x - this.end.x, 2.0)
				+ Math.pow(this.start.y - this.end.y, 2.0));
	}

	/**
	 * Returns a String representation of this LineSegment.
	 */
	public String toString() {
		return String.format("%d %d %d %d", (int) this.start.x, (int) this.start.y,
				(int) this.end.x, (int) this.end.y);
	}

}
