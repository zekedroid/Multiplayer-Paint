package view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import logger.BoardLogger;
import protocol.Client;
import protocol.ClientSideMessageMaker;
import turtle.DrawableTurtle;
import turtle.LineSegment;
import adts.Line;
import controller.WhiteboardClient;

/**
 * Canvas represents a drawing surface that allows the user to draw on it
 * freehand, with the mouse.
 * 
 * Functionality: draw with three sizes of stroke, 12 different colors, erase,
 * draw a random spiral, clear the board, leave the board and go back to lobby,
 * see who's currently collaborating on the board.
 * 
 * Thread-safety:
 * 
 * All changes to the board are performed by repainting the entire Canvas and
 * since this is the only operation, and since it is thread-safe itself, the
 * pane is free of these types of concurrency bugs. The second argument is that
 * the user never draws locally. This means that any action is first sent to the
 * server which is then broadcasted to all members of the board. Only then do
 * any changes appear. This blackboxes the server/client interaction and allows
 * for each Canvas connected to the same Lobby Model to have the lastest,
 * "master" copy of the board on the server.
 * 
 * Testing:
 * 
 * @category general aesthetics are the first thing to notice. Do the buttons
 *           appear where they're supposed to and if they do, are they firing
 *           the right listeners:
 * 
 *           1. The eraser must be setting the pen to white color and large
 *           stroke width.
 * 
 *           2. The pencil should change the stroke width to 1 and the color
 *           back to black if previously white. Test by pressing multiple colors
 *           and always getting white for eraser and anything else for pencil.
 * 
 *           3. The three stroke sizes are tested by looking at the size of a
 *           freehand stroke in any color. Even with eraser mode on, the stroke
 *           can be modified for finer erasing resolution
 * 
 *           4. Draw turtle draws a random spiral with the given color and
 *           stroke witdh 1 somewhere inside the white space. Test it by
 *           clicking it several times and trying different colors to make sure
 *           they all work and that the window layout is not drawn over.
 * 
 *           5. Clear board will clear anything on the board. Draw something and
 *           watch it dissapear once we press the button.
 * 
 *           6. LEAVE BOARD should close the Canvas and reopen the lobby.
 * 
 *           7. The color palate can be tested by clicking on each color and
 *           making sure the boundaries are set correctly (the correct color
 *           should be selected at each boundary of each square). It must also
 *           be tested with combination of all other buttons. The only one that
 *           should change the color is eraser and pencil.
 * 
 *           8. The current color label should always change the square color to
 *           the current color therefore try clicking every button previously
 *           tested, changing colors and making sure what is drawn at each step
 *           is the color displayed in this square.
 * 
 *           9. The Active Users table has no functionality other than
 *           displaying all other users collaborating in a white font color
 *           while the current user is in large font and yello.
 * 
 * @category The next part is testing that a Canvas is initialized correctly if
 *           given a set of lines which is done by creating a table by another
 *           user, having them draw for a while, then connecting this user to
 *           the same board and expecting all lines to be pre-loaded.
 * 
 * @category Testing the user list, it's crucial to see that no name can extend
 *           beyond the layout window.
 */
public class Canvas extends JPanel implements Client {
	/**
	 * This is the GUI acting as the View for a Whiteboard. It is drawn in such
	 * a way that almost every component is dependent on the Constructor
	 * parameters.
	 */
	private static final long serialVersionUID = 1L;

	// image where the user's drawing is stored
	private Image drawingBuffer;

	/**
	 * Logger for Canvas. Level 0.
	 */
	// assumes the current class is called logger
	private final static Logger LOGGER = Logger.getLogger(Canvas.class
			.getName());

	/*
	 * Dimensions defined by their component name followed by X, x-coordinate,
	 * Y, y-coordinate, W, width, and H, height
	 */

	/**
	 * The dimension for margins applied where needed. Defaults to 3.
	 */
	private final int margins;

	/**
	 * Width of entire Canvas.
	 */
	private int canvasW;
	/**
	 * Height of entire Canvas.
	 */
	private int canvasH;

	/**
	 * Width of drawable part of the Canvas.
	 */
	private int drawableCanvasW;
	/**
	 * Height of drawable part of the Canvas.
	 */
	private int drawableCanvasH;

	/**
	 * Height of the button window layout
	 */
	private int windowW;
	/**
	 * Width of the button window layout
	 */
	private int windowH;

	/**
	 * Width of each individual button. Set to width of button window layout
	 * minus 2 margins.
	 */
	private int buttonW;
	/**
	 * Height of each individual button. Set to the height of the Canvas.
	 */
	private int buttonH;
	/**
	 * In degrees, the radius of the square corners
	 */
	private final float buttonArc;
	/**
	 * Y position of the current color button square
	 */
	private int currentColorSquareY;
	/**
	 * Size of the squares in the color palate
	 */
	private int sizeColorSquare;
	/**
	 * List with String representation of the text to display for each button.
	 */
	final List<String> buttonText;
	/**
	 * A Map from button text (used as the identifier) to the x,y coordinates of
	 * the button.
	 */
	HashMap<String, List<Integer>> buttonBoundaries;
	/**
	 * A Map from Color button (used as the identifier) to the x,y coordinates
	 * of the button.
	 */
	final HashMap<Color, List<Integer>> colorButtonBoundaries;
	/**
	 * Total number of buttons. Used to determine the relative height of each
	 * button.
	 */
	private final int numOfButtons;

	/**
	 * The instance of the JFram we are constructing
	 */
	private final JFrame window;

	/**
	 * Width of shape drawing. It is the number of pixels any given line will
	 * draw above/below. It is always odd so as to allow for equal number of
	 * pixels above and below.
	 */
	private float lineStroke;
	private final float windowStroke;

	/**
	 * The active list of users connected to the board. It gets wiped and
	 * redrawn every time the controller provides new inputs
	 */
	private List<String> userNames;

	// Color properties of different components in the board
	private final Color buttonColor;
	private Color textColor;
	private Color lineColor;
	private final Color windowBackground;
	private Color boardColor;
	private final List<Color> basicColors;

	private final WhiteboardClient lobby;

	private String user;
	private int boardID;

	/**
	 * Make a canvas.
	 * 
	 * @param lobby
	 *            the instance of the LobbyGUI Controller which handles the data
	 *            traffic
	 * @param user
	 *            every Canvas must be instantiated with a user String which is
	 *            the userName of the user who started it
	 * @param boardID
	 *            the ID of the current board
	 * @param boardName
	 *            String name given to the Canvas
	 */
	public Canvas(WhiteboardClient lobby, String user, int boardID,
			String boardName) {

		setupLogger(Level.ALL);

		this.userNames = new ArrayList<String>();
		this.lobby = lobby;
		this.user = user;
		this.boardID = boardID;

		window = new JFrame("Collaborative Whiteboard: " + boardName);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(new BorderLayout());

		window.setExtendedState(window.getExtendedState()
				| JFrame.MAXIMIZED_BOTH);
		window.setMinimumSize(new Dimension(800, 800));
		this.lineStroke = 1; // default to 1 pixel

		addDrawingController();
		// note: we can't call makeDrawingBuffer here, because it only
		// works *after* this canvas has been added to a window. Have to
		// wait until paintComponent() is first called.

		// set the size of the canvas
		this.canvasW = 800;
		this.canvasH = 800;

		// set default values of components in the canvas
		this.windowStroke = 0; // no border on button window
		this.margins = 3; // a margin size of 3 applied evenly throughout
		this.windowW = this.canvasW > 500 ? 200 : this.canvasW / 5;
		this.windowH = this.canvasH;

		// set the size of the canvas
		this.drawableCanvasW = this.canvasW - 2 * margins - windowW;
		this.drawableCanvasH = this.canvasH - 2 * margins;

		/*
		 * Button properties: text, boundaries, color, margins
		 */

		/*
		 * IMPORTANT: This array has a lot of power. Simply add a new entry and
		 * not only will the button be properly inserted in its right place, but
		 * its boundaries will also be set so go down to the click listener and
		 * add a conditional to match this string and you're done.
		 */
		this.buttonText = Arrays.asList("Eraser", "Pencil", "Stroke Small",
				"Stroke Medium", "Stroke Large", "Draw turtle", "Clear board",
				"LEAVE BOARD");

		this.numOfButtons = buttonText.size();
		// leave 1 margin on either side
		this.buttonW = windowW - 2 * margins;
		// we use only a third the height to leave space for colors
		this.buttonH = (int) ((windowH / 3.0) / numOfButtons);
		this.buttonArc = 30;
		// for the color palate
		this.sizeColorSquare = (int) ((windowW - 2 * margins) / 4f);

		// define boundaries of buttons
		this.buttonBoundaries = new HashMap<String, List<Integer>>();
		for (int i = 0; i < numOfButtons; ++i) {
			int xPos1 = margins;
			int yPos1 = margins + i * buttonH;
			int xPos2 = buttonW - margins;
			int yPos2 = (i + 1) * buttonH - margins;

			buttonBoundaries.put(buttonText.get(i),
					Arrays.asList(xPos1, yPos1, xPos2, yPos2));
		}

		this.colorButtonBoundaries = new HashMap<Color, List<Integer>>();

		// initialize colors
		this.buttonColor = new Color(100, 100, 100, 100); // light black
		this.windowBackground = new Color(141, 233, 181, 255); // light green
		this.textColor = new Color(0); // black
		this.lineColor = Color.BLACK; // default to black
		this.boardColor = Color.WHITE; // default to white
		this.basicColors = Arrays.asList(Color.BLACK, Color.BLUE, Color.CYAN,
				Color.DARK_GRAY, Color.GRAY, Color.GREEN, Color.MAGENTA,
				Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW);

		window.add(this, BorderLayout.CENTER);
		window.pack();
		// Initialize the user list
		window.setVisible(true);
		// Add windowListener
		window.addWindowListener(new WindowListen());
		// Add a listener for resizing events
		window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// resetSizes();
				// should implement this later. It works but I forgot to also
				// add lines that were already there.
			}
		});

	}

	/**
	 * When the user resizes the window, all the drawable components get resized
	 * and redrawn.
	 */
	private void resetSizes() {

		// set the size of the canvas
		this.canvasW = 800;
		this.canvasH = 820;

		// set default values of components in the canvas
		this.windowW = this.canvasW > 500 ? 200 : this.canvasW / 5;
		this.windowH = this.canvasH;

		// set the size of the canvas
		this.drawableCanvasW = this.canvasW - 2 * margins - windowW;
		this.drawableCanvasH = this.canvasH - 2 * margins;

		// leave 1 margin on either side
		this.buttonW = windowW - 2 * margins;
		// we use only a third the height to leave space for colors
		this.buttonH = (int) ((windowH / 3.0) / numOfButtons);
		// for the color palate
		this.sizeColorSquare = (int) ((windowW - 2 * margins) / 4f);

		// define boundaries of buttons
		this.buttonBoundaries = new HashMap<String, List<Integer>>();
		for (int i = 0; i < numOfButtons; ++i) {
			int xPos1 = margins;
			int yPos1 = margins + i * buttonH;
			int xPos2 = buttonW - margins;
			int yPos2 = (i + 1) * buttonH - margins;

			buttonBoundaries.put(buttonText.get(i),
					Arrays.asList(xPos1, yPos1, xPos2, yPos2));
		}

		makeDrawingBuffer();
	}

	/**
	 * Creates the logger and sets the requested level of priority.
	 * 
	 * @param level
	 *            default to ALL to see all messages. Set to INFO or above to
	 *            see most relevant messages.
	 */
	private void setupLogger(Level level) {
		try {
			BoardLogger.setup();
			LOGGER.setLevel(level);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Problems with creating the log files");
		}
	}

	/**
	 * Controller can use this function to add/remove users from the board.
	 * 
	 * @param users
	 *            a String array composed of every username
	 */
	public void createUserList(Collection<String> users) {
		userNames = new ArrayList<String>();
		for (String user : users) {
			userNames.add(user);
		}
		createUserTable();
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		// If this is the first time paintComponent() is being called,
		// make our drawing buffer.
		if (drawingBuffer == null) {
			makeDrawingBuffer();
		}

		// Copy the drawing buffer to the screen.
		g.drawImage(drawingBuffer, 0, 0, null);
	}

	/**
	 * Make the drawing buffer and draw some starting content for it. It draws
	 * all necessary starting components such as backgrounds and buttons
	 */
	private void makeDrawingBuffer() {
		drawingBuffer = createImage(getWidth(), getHeight());
		fillWithWhite();
		createButtonLayout();
		Set<String> oneUser = new HashSet<String>();
		oneUser.add(this.user);
		createUserList(oneUser);
	}

	/**
	 * Make the drawing buffer's background. This includes a GRAY back rectangle
	 * and the white "drawable canvas" on top.
	 */
	public void fillWithWhite() {
		final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

		/*
		 * Create a gray layer to separate the button window, in green, from the
		 * drawable area, in white.
		 */
		Color grayBackground = Color.GRAY;
		g.setColor(grayBackground);
		g.fillRect(0, 0, canvasW, canvasH);

		/*
		 * Create the white drawable area and the button window
		 */
		g.setColor(boardColor);
		g.fillRect(margins + windowW, margins, drawableCanvasW-margins*5, drawableCanvasH-margins*6);
		createButtonLayout();

		// IMPORTANT! every time we draw on the internal drawing buffer, we
		// have to notify Swing to repaint this component on the screen.
		this.repaint();
	}

	/**
	 * Creates a rectangle with a given stroke width, color, x and y positions
	 * as referenced from the top left corner, and width and height
	 * 
	 * @param g
	 *            Graphics2D object to modify
	 * @param stroke
	 *            a float representing the number of pixels to give the edge of
	 *            the rectangle. Must be odd and greater than 0.
	 * @param color
	 *            a Color object to match the fill and stroke color
	 * @param x
	 *            x-position with increasing coordinate left to right, starting
	 *            at left wall
	 * @param y
	 *            y-position with increasing coordinate top to bottom, starting
	 *            at top wall
	 * @param width
	 *            width of rectangle
	 * @param height
	 *            height of rectangle
	 */
	private void createFilledRectangle(Graphics2D g, float stroke, Color color,
			int x, int y, int width, int height) {
		g.setStroke(setStrokeWidth(stroke));
		g.setColor(color);
		g.fillRect(x, y, width, height);
	}

	/**
	 * Prints text to the graphics provided
	 * 
	 * @param g
	 *            Graphics2D object to modify
	 * @param text
	 *            String to print
	 * @param x
	 *            x-position of the left-aligned text
	 * @param y
	 *            y-position of the top-aligned text
	 * @param textColor
	 *            Color given to the text
	 * @param option
	 *            int that specifies "normal", 0, "bold", 1, "italic", 2
	 * @param size
	 *            text size
	 */
	private void createText(Graphics2D g, String text, int x, int y,
			Color textColor, int option, int size) {
		g.setColor(textColor);
		Font font = new Font("Verdana", option, size);

		g.setFont(font);
		g.drawString(text, x, y);
	}

	/**
	 * Used to draw a rounded rectangle to a provided Graphics2D object. Can be
	 * filled or not.
	 * 
	 * @param g
	 *            Graphics2D object to modify
	 * 
	 * @param color
	 *            a Color object to match the fill and stroke color
	 * @param x
	 *            x-position with increasing coordinate left to right, starting
	 *            at left wall
	 * @param y
	 *            y-position with increasing coordinate top to bottom, starting
	 *            at top wall
	 * @param width
	 *            width of rectangle
	 * @param height
	 *            height of rectangle
	 * @param xArc
	 *            value in degrees of the radius of the corners
	 * @param yArc
	 *            value in degrees of the radius of the corners
	 * @param fill
	 *            boolean which is true if the rectangle is to be filled. False
	 *            otherwise.
	 */
	private void createRoundedFilledRectangle(Graphics2D g, Color color, int x,
			int y, int width, int height, float xArc, float yArc, boolean fill) {
		g.setColor(color);
		Shape button = new RoundRectangle2D.Float(x, y, width, height, xArc,
				yArc);
		if (fill) {
			g.fill(button);
		}
		g.draw(button);
	}

	/**
	 * Draws the button layout window as well as the buttons and their text.
	 * 
	 * @param g
	 *            the Graphics2D object to work with
	 */
	private void createButtonsAndText(Graphics2D g) {
		for (int i = 0; i < numOfButtons; ++i) {

			String textToDisplay = buttonText.get(i);
			int yPos1 = buttonBoundaries.get(textToDisplay).get(1);
			int adjustedButtonH = buttonH - 2 * margins;

			createRoundedFilledRectangle(g, buttonColor, margins, yPos1,
					buttonW - margins, adjustedButtonH, buttonArc, buttonArc,
					true);

			// the x-position of the text's left starting position
			int xStringPos = 5 * margins;
			int yStringPos = yPos1 + buttonH / 2 + margins;

			int textSize = 15;

			FontMetrics metric = g.getFontMetrics();
			int textHeight = metric.getHeight();
			int textWidth = textSize * buttonText.get(i).length();

			int safetyBreak = 0;
			while (textHeight > 1
					&& safetyBreak < 100
					&& (textWidth > this.windowW - 2 * margins || textHeight > this.buttonH)) {
				textSize--;
				safetyBreak++;
				textHeight--;
				textWidth = textSize * buttonText.get(i).length();
			}

			createText(g, buttonText.get(i), xStringPos, yStringPos, textColor,
					1, textSize);

		}
	}

	/**
	 * Draws the color palate and the "current color" square.
	 * 
	 * @param g
	 *            the Graphics2D object to work with
	 */
	private void createColorPalate(Graphics2D g) {
		int sizeColorSquare = (int) ((windowW - 2 * margins) / 4f);
		int beginPosY = windowH / 3 + margins;

		/*
		 * After drawing them, append their boundaries to link an action to them
		 */
		Iterator<Color> useColor = basicColors.iterator();
		for (int i = 0; i < 4; ++i) {
			int xPos = margins + i * sizeColorSquare;
			for (int j = 0; j < 3; ++j) {
				Color colorSquare = useColor.next();
				int yPos = beginPosY + j * sizeColorSquare;
				createFilledRectangle(g, 1, colorSquare, xPos, yPos,
						sizeColorSquare, sizeColorSquare);
				this.colorButtonBoundaries.put(
						colorSquare,
						Arrays.asList(xPos, yPos, xPos + sizeColorSquare, yPos
								+ sizeColorSquare));
			}
		}
	}

	/**
	 * Create a small square representing the current color selected. The
	 * y-position is the beginning position of the color squares plus the number
	 * of squares high, in this case 3, plus a margins.
	 * 
	 * @param g
	 *            the Graphics2D object to work with
	 */
	private void createCurrentColorSquare(Graphics2D g) {
		int yStringPos = windowH / 3 + margins + 3 * this.sizeColorSquare
				+ this.sizeColorSquare / 2;
		int xStringPos = 3 * margins;

		String currentColorString = "Current color:";
		int textSize = 25;

		int textWidth = textSize * currentColorString.length();

		int safetyBreak = 0;
		while (safetyBreak < 20 && (textWidth > this.windowW)) {
			textSize--;
			safetyBreak++;
			textWidth = textSize * currentColorString.length();
		}

		createText(g, currentColorString, xStringPos, yStringPos, textColor, 1,
				textSize);

		this.currentColorSquareY = yStringPos - this.sizeColorSquare * 2 / 3;
		int ySquarePos = this.currentColorSquareY + this.sizeColorSquare / 3;
		int xSquarePos = windowW - margins - sizeColorSquare / 4 * 3;

		createFilledRectangle(g, 1, lineColor, xSquarePos, ySquarePos,
				sizeColorSquare / 2, sizeColorSquare / 2);
	}

	/**
	 * The Canvas has a table with all the users connected to it. This
	 * information is handled by the function which the controller calls on to
	 * add users.
	 * 
	 */
	private void createUserTable() {
		final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

		int yPos = this.currentColorSquareY + windowW / 4;
		int xPos = margins;
		yPos = yPos + margins;
		int tableWidth = windowW - 2 * margins;
		int tableHeight = windowH - yPos - margins;

		g.setColor(new Color(0, 102, 204));
		// the background of the table
		g.fillRect(xPos, yPos, tableWidth, tableHeight);

		g.setColor(Color.BLACK);
		g.setStroke(setStrokeWidth(2));
		g.drawRect(xPos, yPos, tableWidth, tableHeight);

		// the user header
		int MAX_CHARS_FOR_FIRST_USER = 9;
		int MAX_CHARS_FOR_OTHER_USERS = 15;
		g.drawRect(xPos, yPos, tableWidth, tableHeight / 10);
		int xStringPos = 3 * margins;
		int yStringPos = yPos + tableHeight / 15;
		createText(g, "Active Users", xStringPos, yStringPos, Color.WHITE, 1,
				13);

		// insert the active users supplied by the controller
		int startingY = yPos + tableHeight / 15 + 3 * margins;
		int heightOfString = tableHeight / 15;
		Collections.sort(userNames);
		userNames.remove(this.user);
		String tableEntry = "1. "
				+ (this.user.length() < MAX_CHARS_FOR_FIRST_USER ? this.user
						: this.user.substring(0, MAX_CHARS_FOR_FIRST_USER)
								+ "...");
		createText(g, tableEntry, xStringPos, startingY + heightOfString * (1),
				Color.YELLOW, 1, 18);
		for (int i = 0; i < userNames.size(); i++) {
			tableEntry = String.valueOf(i + 2)
					+ ". "
					+ (userNames.get(i).length() < MAX_CHARS_FOR_OTHER_USERS ? userNames
							.get(i) : userNames.get(i).substring(0,
							MAX_CHARS_FOR_OTHER_USERS)
							+ "...");
			createText(g, tableEntry, xStringPos, startingY + heightOfString
					* (i + 2), Color.WHITE, 1, 13);
		}
		this.repaint();
	}

	/**
	 * Creates the button window. This window contains all functional buttons as
	 * well as the color charts
	 */
	private void createButtonLayout() {
		final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

		// window layout background
		createFilledRectangle(g, windowStroke, windowBackground, 0, 0, windowW,
				windowH);

		createButtonsAndText(g);
		createColorPalate(g);
		createCurrentColorSquare(g);
		createUserTable();
	}

	/**
	 * Draw a line between two points (x1, y1) and (x2, y2), specified in pixels
	 * relative to the upper-left corner of the drawing buffer.
	 * 
	 * @param l
	 *            Line to draw
	 * @param withRepaint
	 *            will repaint if true, wont otherwise
	 * 
	 */
	public synchronized void drawLineSegment(Line l, boolean withRepaint) {
		Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

		g.setStroke(new BasicStroke(l.getStrokeThickness(), 1, 1));
		g.setColor(new Color(l.getR(), l.getG(), l.getB(), l.getA()));

		g.drawLine(l.getX1(), l.getY1(), l.getX2(), l.getY2());
		if (withRepaint) {
			this.repaint();
		}
	}

	/**
	 * Set strokeWidth
	 * 
	 * @param s
	 *            number of pixels used when drawing
	 */
	private Stroke setStrokeWidth(float s) {
		return new BasicStroke(s);
	}

	/**
	 * Creates a way to get correct coordinates for drawing since our button
	 * layout is part of the canvas, we have to make sure we don't draw over it.
	 * 
	 * @param x
	 *            an int representing the potential x position to draw on
	 * @return a new, adjusted value of x
	 */
	private int[] adjustedPos(int x, int y) {
		int newX = x;
		int newY = y;
		if (x < windowW + lineStroke / 2.0 + margins) {
			newX = (int) (windowW + (lineStroke / 2.0) + margins);
		} else if (x > canvasW - lineStroke / 2.0 - margins) {
			newX = (int) (canvasW - margins - lineStroke / 2.0);
		}

		if (y < lineStroke / 2.0 + margins) {
			newY = (int) (lineStroke / 2.0 + margins);
		} else if (y > canvasH - lineStroke / 2.0 - margins) {
			newY = (int) (canvasH - margins - lineStroke / 2.0);
		}
		int[] result = new int[2];
		result[0] = newX;
		result[1] = newY;
		return result;

	}

	/**
	 * Add the mouse listener that supports the user's freehand drawing.
	 */
	private void addDrawingController() {
		DrawingController controller = new DrawingController();
		addMouseListener(controller);
		addMouseMotionListener(controller);
	}

	/**
	 * DrawingController handles the user's freehand drawing. It includes all
	 * the relevant listeners needed to press buttons and draw on the Canvas.
	 */
	private class DrawingController implements MouseListener,
			MouseMotionListener {
		/*
		 * store the coordinates of the last mouse event, so we candraw a line
		 * segment from that last point to the point of the nextmouse event.
		 */
		private int[] lastPos = new int[2];
		private List<LineSegment> turtleLines;

		/*
		 * When mouse button is pressed down, start drawing.
		 */
		public void mousePressed(MouseEvent e) {

			lastPos = adjustedPos(e.getX(), e.getY());

		}

		/*
		 * When mouse moves while a button is pressed down, send the server a
		 * line segment.
		 */
		public void mouseDragged(MouseEvent e) {

			int[] pos = adjustedPos(e.getX(), e.getY());
			int x = pos[0];
			int y = pos[1];

			Line l = new Line(lastPos[0], lastPos[1], x, y, lineStroke,
					lineColor.getRed(), lineColor.getGreen(),
					lineColor.getBlue(), lineColor.getAlpha());
			lobby.makeRequest(ClientSideMessageMaker.makeRequestStringDraw(l));
			lastPos = adjustedPos(x, y);
		}

		public void mouseMoved(MouseEvent e) {
		}

		/*
		 * This is used for button selection. It gives buttons their actions.
		 */
		public void mouseClicked(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();

			// check where the click happened and if within a button's
			// boundaries, act accordingly
			String action = "";
			for (String button : buttonBoundaries.keySet()) {
				List<Integer> boundaries = buttonBoundaries.get(button);
				if (x >= boundaries.get(0) && x <= boundaries.get(2)
						&& y >= boundaries.get(1) && y <= boundaries.get(3)) {
					action = button;
				}
			}

			Color colorAction = null;
			for (Color color : colorButtonBoundaries.keySet()) {
				List<Integer> boundaries = colorButtonBoundaries.get(color);
				if (x >= boundaries.get(0) && x <= boundaries.get(2)
						&& y >= boundaries.get(1) && y <= boundaries.get(3)) {
					colorAction = color;
				}
			}

			final Graphics2D g = (Graphics2D) drawingBuffer.getGraphics();

			if (action.equals("Eraser")) {
				lineStroke = 25;
				lineColor = Color.WHITE;
				createCurrentColorSquare(g);
				repaint();
			}

			if (action.equals("Pencil")) {
				lineStroke = 1;
				if (lineColor.equals(Color.WHITE)) {
					lineColor = Color.BLACK;
				}
				createCurrentColorSquare(g);
				repaint();

			}

			if (action.equals("Stroke Small")) {
				lineStroke = 1;
			}

			if (action.equals("Stroke Medium")) {
				lineStroke = 5;
			}

			if (action.equals("Stroke Large")) {
				lineStroke = 11;
			}

			if (action.equals("Draw turtle")) {
				DrawableTurtle turtle = new DrawableTurtle();
				drawTurtle(turtle);
				turtleLines = turtle.draw();
				for (int i = 0; i < turtleLines.size(); i++) {
					Line l = new Line((int) turtleLines.get(i).start.x,
							(int) turtleLines.get(i).start.y,
							(int) turtleLines.get(i).end.x,
							(int) turtleLines.get(i).end.y, 1,
							lineColor.getRed(), lineColor.getGreen(),
							lineColor.getBlue(), lineColor.getAlpha());
					lobby.makeRequest(ClientSideMessageMaker
							.makeRequestStringDraw(l));
				}

			}

			if (action.equals("Clear board")) {
				lobby.makeRequest(ClientSideMessageMaker
						.makeRequestStringClear());
			}

			if (action.equals("LEAVE BOARD")) {
				window.dispose();
				lobby.setVisible(true);
				lobby.makeRequest(ClientSideMessageMaker
						.makeRequestStringLeaveBoard());
			}

			if (colorAction != null) {
				lineColor = colorAction;
				createCurrentColorSquare(g);
				repaint();

			}

		}

		/**
		 * Creates random spirals on the screen given a turtle object to work
		 * with by moving forward and turning at each step
		 * 
		 * @param turtle
		 *            the drawableTurtle object
		 */
		private void drawTurtle(DrawableTurtle turtle) {

			int resolution = 200; // number of turns to make

			// these are angles which create spiral drawings
			// change bounds to get even more random pictures. avoid angles
			// smaller
			// than 30 for maximal awesomeness
			int lowerBound = 105;
			int upperBound = 115;

			// create three random angles between your lowerBound and upperBound
			double rand1 = lowerBound + Math.random()
					* (upperBound - lowerBound);
			double rand2 = lowerBound + Math.random()
					* (upperBound - lowerBound);
			double rand3 = lowerBound + Math.random()
					* (upperBound - lowerBound);

			for (int i = 0; i < resolution; i++) { // now run through your
													// resolution

				// simply move forward with incremental length sides
				turtle.forward(i);
				// this will create three, randomly selected, drawings
				// superimposed.
				if (i < resolution / 3)
					turtle.turn(rand1);
				else if (i < 2 * resolution / 3)
					turtle.turn(rand2);
				else
					turtle.turn(rand3);
			}
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}
	}

	private class WindowListen implements WindowListener {

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
			LOGGER.warning("Active Canvas closed");
		}

		@Override
		public void windowClosed(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowOpened(WindowEvent e) {
		}
	}

	@Override
	public void onReceiveUsernameChanged(String rcvdName) {
		this.user = rcvdName;
	}

	@Override
	public void onReceiveBoardIDs(Map<Integer, String> boardNameForID) {
		return;
	}

	@Override
	public void onReceiveWelcome(int id) {
		return;
	}

	@Override
	public void onReceiveDraw(Line l) {
		final Line line = l;
		SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {
				drawLineSegment(line, true);
			}
		});

	}

	@Override
	public void onReceiveBoardLines(List<Line> ls, Set<String> uNames) {
		final List<Line> lines = ls;
		final Set<String> uN = uNames;
		SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {
				for (Line line : lines) {
					drawLineSegment(line, false);
				}
				createUserList(uN);
			}
		});
	}

	@Override
	public void onReceiveClear() {
		this.fillWithWhite();
	}

	@Override
	public void onReceiveUsers(int boardID, List<String> users) {
		if (boardID != this.boardID)
			return;
		this.createUserList(users);
	}

	@Override
	public void onReceiveCurrentBoardID(int boardID) {
		this.boardID = boardID;
	}

}