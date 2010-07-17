package lpf.model.kenken.fileManagement;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * ErrorHandler for parsing KenKenPuzzle files.
 * 
 * An error or warning will cause hasErrors() to return true.
 * 
 * @author Peter Kalauskas, Neeraj Bajaj
 */
public class PuzzleLoaderErrorHandler implements ErrorHandler {
	boolean hasErrors;

	public PuzzleLoaderErrorHandler() {
		hasErrors = false;
	}

	public void error(SAXParseException sAXParseException) throws SAXException {
		// System.err.println("ERROR: " + sAXParseException.toString());
		hasErrors = true;
	}

	public void fatalError(SAXParseException sAXParseException) throws SAXException {
		// System.err.println("FATAL ERROR: " + sAXParseException.toString());
		hasErrors = true;
	}

	public void warning(SAXParseException sAXParseException) throws SAXException {
		// System.err.println("WARNING: " + sAXParseException.toString());
		hasErrors = true;
	}

	/**
	 * @return true if an error or warning occurred
	 */
	public boolean hasErrors() {
		return hasErrors;
	}
}
