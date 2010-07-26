package lpf.model.kenken.fileManagement;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import lpf.model.core.Cell;
import lpf.model.core.Grid;
import lpf.model.core.Location;
import lpf.model.core.Value;
import lpf.model.kenken.Cage;
import lpf.model.kenken.InvalidKenKenPuzzleException;
import lpf.model.kenken.KenKenPuzzle;
import lpf.model.kenken.preferences.Difficulty;

/**
 * Used to parse a KenKenPuzzle from an xml file or archive. The puzzle is not
 * processed until getKenKenPuzzle() is called. If getKenKenPuzzle() returns
 * null then the specified File or Entry of the ZipFile could not be parsed as a
 * KenKenXML file. Either the puzzle did not match the xml schema or it did not
 * represent a valid KenKenPuzzle. Causes of failure include overlapping cages,
 * invalid cages, and too many solution boards provided.
 * 
 * @author Peter Kalauskas, Neeraj Bajaj
 */
public class FileLoader {
	/**
	 * The size of the loaded puzzle or 0 if the puzzle is invalid
	 */
	int puzzleSize;

	/**
	 * The difficulty of the loaded puzzle or null if the puzzle is invalid
	 */
	Difficulty puzzleDifficulty;

	/**
	 * True if this FileLoader can load a valid puzzle.
	 */
	boolean isValid;

	/**
	 * The input method used by this FileLoader
	 */
	InputStreamGetter input;

	public FileLoader(ZipFile library, ZipEntry entry) {
		input = new InputStreamGetter(library, entry);
		setLoaderInfo();
	}

	public FileLoader(File puzzleLocation) {
		input = new InputStreamGetter(puzzleLocation);
		setLoaderInfo();
	}

	/**
	 * Sets information about this FileLoader. This will determine if this
	 * FileLoader contains a valid puzzle. If it does, size and difficulty will
	 * be set.
	 */
	void setLoaderInfo() {
		KenKenPuzzle puzzle = getKenKenPuzzle();
		if (puzzle != null) {
			isValid = true;
			puzzleSize = puzzle.getSize();
			puzzleDifficulty = puzzle.getDifficultyLevel();
		} else {
			isValid = false;
			puzzleSize = 0;
			puzzleDifficulty = null;
		}
	}

	/**
	 * @return the size of the puzzle or 0 if the puzzle is not valid
	 */
	public int getPuzzleSize() {
		return puzzleSize;
	}

	/**
	 * @return the difficulty of the puzzle of null if the puzzle is not valid
	 */
	public Difficulty getPuzzleDifficulty() {
		return puzzleDifficulty;
	}

	/**
	 * Returns true if the puzzle is valid. If the puzzle is invalid,
	 * getKenKenPuzzle will return null.
	 * 
	 * @return true if the puzzle is valid.
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * Constructs a new KenKenPuzzle from the source given in the FileLoader
	 * constructor.
	 * 
	 * @return a KenKenPuzzle if the source contained a valid KenKenPuzzle; null
	 *         otherwise
	 */
	public KenKenPuzzle getKenKenPuzzle() {
		KenKenPuzzle kenKenPuzzle = null;
		try {
			if (matchesSchema(input.getInputStream())) {
				kenKenPuzzle = buildPuzzle(input.getInputStream());
			}
		} catch (Exception e) {
			// do nothing, getKenKenPuzzle() will just return null
		}
		return kenKenPuzzle;
	}

	/**
	 * Attempts to build a puzzle from the given InputStream. This assumes that
	 * the schema has already been validated. If the source does not represent a
	 * valid KenKenPuzzle then an exception will be thrown.
	 * 
	 * @param puzzleStream
	 * @return
	 */
	static KenKenPuzzle buildPuzzle(InputStream puzzleStream) throws Exception {
		// now show how to access its information
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(puzzleStream);

		// Start parsing with the puzzle element. There is only one puzzle
		// attribute (as verified by the schema). We'll start parsing here.
		Element puzzle = (Element) doc.getElementsByTagName("puzzle").item(0);

		if (!puzzle.getAttribute("type").equals("kenken(tm)")) {
			throw new InvalidKenKenPuzzleFileException(
					"Puzzle is not of type \"kenken(tm)\"");
		}

		NodeList boardsList = puzzle.getElementsByTagName("board");
		Element solutionBoard = getSolutionBoardElement(boardsList);

		Grid solutionGrid = parseGrid(solutionBoard, getSize(puzzle));

		// We know there is only one cages element since this is validated
		// by the schema. If no cages are given cagesElement will be null.
		Element cagesElement = (Element) puzzle.getElementsByTagName("cages").item(0);
		if (cagesElement == null) {
			throw new InvalidKenKenPuzzleFileException("No cages given");
		}

		// Add all the cages to a list
		ArrayList<Cage> cages = new ArrayList<Cage>();
		NodeList cageList = cagesElement.getElementsByTagName("cage");
		for (int s = 0; s < cageList.getLength(); s++) {
			cages.add(parseCage((Element) cageList.item(s)));
		}

		// Put everything together, if the KenKenPuzzle constructor throws
		// an exception we know the puzzle is invalid.
		KenKenPuzzle kkp = new KenKenPuzzle(cages, solutionGrid);
		kkp.setDifficulty(getDifficulty(puzzle));
		kkp.setInitialGrid(new Grid(kkp.getSolutionGrid().width,
				kkp.getSolutionGrid().height));
		return kkp;
	}

	// Difficulty will be between 1-10 as specified by the schema, so we
	// don't need to check it
	static int getDifficulty(Element puzzle) {
		return Integer.parseInt(puzzle.getAttribute("difficulty"));
	}

	/**
	 * Returns the size of the puzzle given by the attribute.
	 * 
	 * @param puzzle
	 * @return
	 * @throws InvalidKenKenPuzzleException
	 *             if the size is <1 or >9
	 */
	static int getSize(Element puzzle) throws InvalidKenKenPuzzleException {
		int width = Integer.parseInt(puzzle.getAttribute("width"));
		int height = Integer.parseInt(puzzle.getAttribute("height"));

		if (0 < width && width <= 9 && width == height) {
			return width;
		} else {
			throw new InvalidKenKenPuzzleException(
					"Bad puzzle size specified, width: " + width + ", height: "
							+ height);
		}
	}

	/**
	 * Gets the solution board element from the list of boards.
	 * 
	 * @param boardsList
	 *            the NodeList of all boards
	 * @return the element representing the solution board
	 * @throws InvalidKenKenPuzzleFileException
	 *             if more than one solution board exists
	 */
	static Element getSolutionBoardElement(NodeList boardsList)
			throws InvalidKenKenPuzzleFileException {
		Element solutionBoard = null;
		for (int s = 0; s < boardsList.getLength(); s++) {
			Node firstBoardNode = boardsList.item(s);
			if (firstBoardNode.getNodeType() == Node.ELEMENT_NODE) {
				Element firstBoardElement = (Element) firstBoardNode;
				if (firstBoardElement.getAttribute("name").equals("solution")) {
					if (solutionBoard == null) {
						solutionBoard = firstBoardElement;
					} else {
						throw new InvalidKenKenPuzzleFileException(
								"Too many solution boards given.");
					}
				}
			}
		}
		if (solutionBoard == null) {
			throw new InvalidKenKenPuzzleFileException("No solution board.");
		}

		return solutionBoard;
	}

	/**
	 * Converts the Element representing a grid into a Grid.
	 * 
	 * @param solution
	 * @return A Grid object
	 * @throws InvalidKenKenPuzzleFileException
	 *             if not all rows are the same length or the grid is not
	 *             square; if
	 */
	static Grid parseGrid(Element solutionBoard, int size)
			throws InvalidKenKenPuzzleFileException {
		NodeList rowsList = solutionBoard.getElementsByTagName("row");

		// check that the number of rows equals size
		if (rowsList.getLength() != size) {
			throw new InvalidKenKenPuzzleFileException(
					"Specified number of rows do not agree with rows given.");
		}

		// check that all rows are of length size, and convert these rows to a
		// string
		String solutionString = "";
		for (int i = 0; i < rowsList.getLength(); i++) {
			String newRow = ((Element) rowsList.item(i)).getAttribute("contents");
			if (newRow.length() != size) {
				throw new InvalidKenKenPuzzleFileException("Invalid solution format.");
			}
			solutionString += newRow + "\n";
		}

		// convert the string representation of the grid into a Grid object.
		String[] rows = solutionString.split("\n");

		int i = 0;
		char grid[][] = new char[size][size];
		for (String s : rows) {
			grid[i++] = s.toCharArray();
		}

		Grid solutionGrid = new Grid(size, size);
		Iterator<Cell> it = solutionGrid.iterator();
		Cell cell;
		while (it.hasNext()) {
			cell = it.next();
			cell.setDigit(new Value(grid[cell.loc.row - 1][cell.loc.column - 'A']));
		}

		return solutionGrid;
	}

	/**
	 * Parses a cage from it's element in the xml source.
	 * 
	 * @param cage
	 *            an Element representing a Cage.
	 * @return a Cage object.
	 */
	static Cage parseCage(Element cage) {
		int finalValue = Integer.parseInt(cage.getAttribute("value"));
		char operation = cage.getAttribute("operation").charAt(0);

		ArrayList<Location> locations = new ArrayList<Location>();
		NodeList locationList = cage.getElementsByTagName("cell");
		for (int j = 0; j < locationList.getLength(); j++) {
			locations.add(parseLocation((Element) locationList.item(j)));
		}

		return new Cage(operation, finalValue, locations);
	}

	/**
	 * Converts an Element representing a location into a Location object.
	 * 
	 * @param location
	 *            an Element representing a location
	 * @return a Location object
	 */
	static Location parseLocation(Element location) {
		int row = Integer.parseInt(location.getAttribute("row"));
		char column = location.getAttribute("column").charAt(0);

		return new Location(row, column);
	}

	/**
	 * Checks that the puzzle at the specified location is valid against the
	 * provided (http://users.wpi.edu/~heineman/lpf/lpf.xsd) schema.
	 * 
	 * @return true if the source matches the schema
	 * 
	 * @author Neeraj Bajaj, Sun Microsystems.
	 */
	static boolean matchesSchema(InputStream puzzleStream) {
		try {
			// parse schema first, see compileSchema function to see how
			// Schema object is obtained.
			Schema schema = compileSchema(new URL(
					"http://users.wpi.edu/~heineman/lpf/lpf.xsd"));

			// this "Schema" object is used to create "Validator" which
			// can be used to validate instance document against the schema
			// or set of schemas "Schema" object represents.
			Validator validator = schema.newValidator();

			// set ErrorHandle on this validator
			PuzzleLoaderErrorHandler errorHandler = new PuzzleLoaderErrorHandler();
			validator.setErrorHandler(errorHandler);

			// Validate this instance document against the instance document
			// supplied
			validator.validate(new StreamSource(puzzleStream));

			// Return true if no errors or warnings occurred.
			return !errorHandler.hasErrors();
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Parse the given schema and return in-memory representation of that
	 * schema. Compiling the schema is very simple, just pass the path of schema
	 * to <code>newSchema()</code> function and it will parse schema, check the
	 * validity of schema document as per the schema language, compute in-memory
	 * representation and return it as <code>Schema</code> object. Note that If
	 * schema imports/includes other schemas, those schemas will be parsed too.
	 * 
	 * @param String
	 *            path to schema file
	 * @return Schema in-memory representation of schema.
	 * 
	 * @author Neeraj Bajaj, Sun Microsystems.
	 */
	static Schema compileSchema(URL schema) throws SAXException {
		// Get the SchemaFactory instance which understands W3C XML Schema
		// language
		SchemaFactory sf = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		return sf.newSchema(schema);
	}
}
