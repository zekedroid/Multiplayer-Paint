package logger;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a Logger used for systematically enabling/disabling debug lines.
 */
public class BoardLogger {

	/**
	 * Attaches handlers to the LOGGER instance. Will also create the fileTxt
	 * file and sets the level to ALL by default.
	 * 
	 * @throws IOException
	 */
	static public void setup() throws IOException {

		// Get the global logger to configure it
		Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

		// create the conosole handler
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		LOGGER.addHandler(handler);

	}
}
