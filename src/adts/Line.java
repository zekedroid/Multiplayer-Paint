package adts;

/**
 * Used to create Line objects which represent Canvas line segments. These
 * describe the locational properties, width properties, and color properties.
 * 
 * Specifically, it captures the following information:
 *      The start point (x1,y1)
 *      The end point (x2,y2)
 *      The color (r,g,b,a)
 *      The stroke thickness (strokeThickness)
 * 
 * Concurrency argument:
 *      This class is completely immutable. Every field private, final, and an integer.
 *      Since a line cannot be changed at all after it's created, there's no fear of race
 *      conditions. Thus, it is threadsafe.
 * 
 */
public class Line {

	/**
	 * The first x coordinate
	 */
	private final int x1;

	/**
	 * The first y coordinate
	 */
	private final int y1;

	/**
	 * The second x coordinate
	 */
	private final int x2;

	/**
	 * The second y coordinate
	 */
	private final int y2;

	/**
	 * The stroke thickness
	 */
	private final float strokeThickness;

	/**
	 * The alpha value
	 */
	private final int a;

	/**
	 * The red value
	 */
	private final int r;

	/**
	 * The green value
	 */
	private final int g;

	/**
	 * The blue value
	 */
	private final int b;

	/**
	 * @param x1
	 *            The first x coordinate
	 * @param y1
	 *            The first y coordinate
	 * @param x2
	 *            The second x coordinate
	 * @param y2
	 *            The second y coordinate
	 * @param strokeThickness
	 *            The stroke thickness
	 * 
	 * @param r
	 *            The red value
	 * @param g
	 *            The green value
	 * @param b
	 *            The blue value
	 * @param a
	 *            The alpha value
	 */
	public Line(int x1, int y1, int x2, int y2, float strokeThickness, int r,
			int g, int b, int a) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.strokeThickness = strokeThickness;
		this.a = a;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	/**
	 * @return The first x coordinate
	 */
	public int getX1() {
		return x1;
	}

	/**
	 * @return The first y coordinate
	 */
	public int getY1() {
		return y1;
	}

	/**
	 * @return The second x coordinate
	 */
	public int getX2() {
		return x2;
	}

	/**
	 * @return The second y coordinate
	 */
	public int getY2() {
		return y2;
	}

	/**
	 * @return The stroke thickness
	 */
	public float getStrokeThickness() {
		return strokeThickness;
	}

	/**
	 * @return The alpha value
	 */
	public int getA() {
		return a;
	}

	/**
	 * @return The red value
	 */
	public int getR() {
		return r;
	}

	/**
	 * @return The green value
	 */
	public int getG() {
		return g;
	}

	/**
	 * @return The blue value
	 */
	public int getB() {
		return b;
	}

	@Override
	/**
	 * @returns a String representation of this line
	 */
	public String toString() {
		return String.format("%d %d %d %d %f %d %d %d %d", this.getX1(),
				this.getY1(), this.getX2(), this.getY2(),
				this.getStrokeThickness(), this.getR(), this.getG(),
				this.getB(), this.getA());
	}

}
