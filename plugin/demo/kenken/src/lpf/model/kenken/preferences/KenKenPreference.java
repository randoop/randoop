package lpf.model.kenken.preferences;

import java.util.prefs.Preferences;

/**
 * Class for storing the user's preferences for ken-ken games. This uses java's
 * preference API to store the preferred size, difficulty, and library location.
 * 
 * All KenKenPreference objects are equal, so if one KenKenPreference object
 * modifies a preference, this new preference change will appear for all other
 * KenKenPreference objects.
 * 
 * @author Peter Kalauskas
 */
public class KenKenPreference {
	private static final String LIBRARY_LOCATION = "lib_loc";
	private static final String DIFFICULTY = "diff";
	private static final String SIZE = "size";

	/**
	 * Returns this user's stored difficulty preference.
	 * 
	 * @return the preferred difficulty, or null if none is set
	 */
	public Difficulty getPreferredDifficulty() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());

		try {
			return Difficulty.getDifficulty(prefs.getInt(DIFFICULTY, 0));
		} catch (InvalidDifficultyException e) {
			return null;
		}
	}

	/**
	 * Sets the preferred difficulty for this user and overwrites any previous
	 * preference.
	 * 
	 * @param preferredDifficulty
	 *            the new preferred difficulty
	 */
	public void setPreferredDifficulty(Difficulty preferredDifficulty) {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());

		prefs.putInt(DIFFICULTY, preferredDifficulty.toInt());
	}

	/**
	 * Returns this user's stored size preference.
	 * 
	 * @return the preferred size, or null if none is set
	 */
	public Integer getPreferredSize() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());

		int size = prefs.getInt(SIZE, Integer.MIN_VALUE);
		if (size != Integer.MIN_VALUE) {
			return size;
		} else {
			return null;
		}
	}

	/**
	 * Sets the preferred size for this user and overwrites any previous
	 * preference.
	 * 
	 * @param preferredSize
	 *            the new preferred size
	 */
	public void setPreferredSize(Integer preferredSize) {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());

		if (preferredSize == null) {
			prefs.put(SIZE, "");
		} else {
			prefs.putInt(SIZE, preferredSize);
		}
	}

	/**
	 * Gets the puzzle library location from the user's preferences.
	 * 
	 * @return the file path of the library location, or null if none is set -
	 *         NOTE: This is not guaranteed to be a valid library location, or
	 *         even a valid file path!!
	 */
	public String getPuzzleLibraryLocation() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());

		return prefs.get(LIBRARY_LOCATION, null);
	}

	/**
	 * This will set the preferred library location. When doing this, both
	 * difficulty and size will be erased.
	 * 
	 * @param puzzleLibraryLocation
	 *            the new file path of the puzzle library - NOTE: this does not
	 *            need to be valid and will not be validated!!
	 */
	public void setPuzzleLibraryLocation(String puzzleLibraryLocation) {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());

		prefs.put(LIBRARY_LOCATION, puzzleLibraryLocation);
		prefs.remove(DIFFICULTY);
		prefs.remove(SIZE);
	}

	/**
	 * Clear's all of the user's preferences. All getters will return null.
	 */
	public void clearPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());

		prefs.remove(DIFFICULTY);
		prefs.remove(SIZE);
		prefs.remove(LIBRARY_LOCATION);
	}
}
