package lpf.model.kenken.preferences;

/**
 * An InvalidDifficultyException is thrown by Difficulty when an invalid integer
 * is passed to the getDifficulty(int d) function.
 * 
 * @author Peter Kalauskas
 */
public class InvalidDifficultyException extends Exception {
	private static final long serialVersionUID = 3971067430152109412L;

	/**
	 * @param badDifficulty
	 *            the int value of the difficulty that could not be processed
	 */
	public InvalidDifficultyException(int badDifficulty) {
		super("" + badDifficulty);
	}
}
