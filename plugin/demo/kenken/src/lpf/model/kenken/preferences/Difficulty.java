package lpf.model.kenken.preferences;

/**
 * Enumerated type for storing the difficulty. Difficulty falls into three
 * categories: EASY, MEDIUM, and HARD. The numbers 1 through 10 are mapped to
 * these difficulties by the getDifficulty(int) function.
 * 
 * @author Peter Kalauskas
 */
public enum Difficulty {
	EASY, MEDIUM, HARD;

	/**
	 * Returns the difficulty of an integer as an enumerated type.
	 * 
	 * @param d
	 *            a difficulty between 1 and 10
	 * @return the enumerated type for this integer
	 * @throws InvalidDifficultyException
	 *             if d < 1 or 10 < d
	 */
	public static Difficulty getDifficulty(int d) throws InvalidDifficultyException {
		if (d >= 1 && d < 4) {
			return Difficulty.EASY;
		} else if (d >= 4 && d < 7) {
			return Difficulty.MEDIUM;
		} else if (d >= 7 && d <= 10) {
			return Difficulty.HARD;
		} else {
			throw new InvalidDifficultyException(d);
		}
	}

	/**
	 * Converts the difficulty to an integer. Note that for most cases where d
	 * is an integer: d != getDifficulty(d).toInt().
	 * 
	 * @return an integer equal to this difficulty
	 */
	public int toInt() {
		switch (this) {
		case EASY:
			return 1;
		case MEDIUM:
			return 4;
		case HARD:
			return 7;
		default:
			return 0;
		}
	}
}
