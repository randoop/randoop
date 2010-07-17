package lpf.model.kenken.fileManagement;

/**
 * An exception to be thrown when there is an error parsing a KenKenPuzzle from
 * a File.
 * 
 * @author Peter Kalauskas
 */
public class InvalidKenKenPuzzleFileException extends Exception {
	private static final long serialVersionUID = 6647280496838702318L;
	
	public InvalidKenKenPuzzleFileException(String msg) {
		super(msg);
	}
}
