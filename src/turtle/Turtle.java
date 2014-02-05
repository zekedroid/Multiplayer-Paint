package turtle;
import java.util.List;

/**
 * Turtle interface
 *
 * Defines the interface shared for FakeTurtle (for testing) and the
 * real turtle (for displaying things on screen).  Note that the
 * standard directions/rotations use 'logo' semantics: initial heading
 * of zero is 'up', and positive angles rotate the turtle clockwise.
 *
 * We implement:  forward, turn right, and draw
 */
public interface Turtle {

    public void forward(int units);

    public void turn(double angle);

    public List<LineSegment> draw();

}
