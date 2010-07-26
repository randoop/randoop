package lpf.model.kenken.fileManagement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import lpf.model.kenken.preferences.Difficulty;
import lpf.model.kenken.preferences.KenKenPreference;

/**
 * The PuzzleLibrary is constructed from a zipped file containing KenKen
 * puzzles. The puzzles will be parsed an indexed by the PuzzleLibrary into maps
 * so that random puzzles can be retrieved from. Puzzles do not need to be
 * ordered in any fashion inside the archive, the only convention is that puzzle
 * files must end with .xml
 * 
 * @author Peter Kalauskas
 */
public class PuzzleLibrary {
	private static Random rand = new Random();
	ZipFile puzzleArchive;
	Collection<FileLoader> puzzles;
	Map<Integer, Collection<FileLoader>> puzzlesOfSize;
	Map<Difficulty, Collection<FileLoader>> puzzlesOfDifficulty;
	String path;

	/**
	 * Constructs a new puzzle library from the given archive file.
	 * 
	 * @param puzzleArchiveLocation
	 */
	public PuzzleLibrary(File puzzleArchiveLocation) {
		this.puzzles = new ArrayList<FileLoader>();

		this.puzzlesOfSize = new HashMap<Integer, Collection<FileLoader>>();
		this.puzzlesOfDifficulty = new HashMap<Difficulty, Collection<FileLoader>>();

		try {
			this.puzzleArchive = new ZipFile(puzzleArchiveLocation);
			path = puzzleArchiveLocation.getAbsolutePath();
		} catch (IOException e) {
			// isValid() will return false
			return;
		}

		Enumeration<? extends ZipEntry> elements = this.puzzleArchive.entries();
		while (elements.hasMoreElements()) {
			ZipEntry entry = elements.nextElement();

			// ignore directories and files that don't end with .xml
			if (!entry.isDirectory() && entry.getName().endsWith(".xml")) {
				FileLoader loader = new FileLoader(puzzleArchive, entry);

				if (loader.isValid()) {
					Difficulty difficulty = loader.getPuzzleDifficulty();
					if (!this.puzzlesOfDifficulty.containsKey(difficulty)) {
						// if the key isn't there, create a new Set for
						// entries of this difficulty.
						this.puzzlesOfDifficulty.put(difficulty,
								new ArrayList<FileLoader>());
					}
					this.puzzlesOfDifficulty.get(difficulty).add(loader);

					int size = loader.getPuzzleSize();
					if (!this.puzzlesOfSize.containsKey(size)) {
						// create a new Set for entries of this size
						this.puzzlesOfSize.put(size, new ArrayList<FileLoader>());
					}
					this.puzzlesOfSize.get(size).add(loader);

					puzzles.add(loader);
				}
			}
		}
	}

	/**
	 * Checks the validity of this PuzzleLibrary. A puzzle library is valid if
	 * it contains at least one xml puzzle file of type "kenken(tm)" that is
	 * valid.
	 * 
	 * @return true if this PuzzleLibrary object is valid.
	 */
	public boolean isValid() {
		return puzzles.size() != 0;
	}

	/**
	 * Returns a random FileLoader of a puzzle inside this this library that
	 * suites the given preferences.
	 * 
	 * @param preference
	 * @return
	 */
	public FileLoader randomPuzzleLoader(KenKenPreference preference) {
		if (preference.getPreferredDifficulty() == null) {
			return null;
		} else if (preference.getPreferredSize() == null) {
			return randomPuzzleLoader(preference.getPreferredDifficulty());
		} else {
			return randomPuzzleLoader(preference.getPreferredDifficulty(), preference
					.getPreferredSize());
		}
	}

	/**
	 * Returns a random puzzle of the given difficulty (size is disregarded).
	 * 
	 * @param difficulty
	 * @return
	 */
	FileLoader randomPuzzleLoader(Difficulty difficulty) {
		ArrayList<FileLoader> suitablePuzzles = new ArrayList<FileLoader>();
		Iterator<FileLoader> it = this.puzzlesOfDifficulty.get(difficulty).iterator();
		while (it.hasNext()) {
			suitablePuzzles.add(it.next());
		}

		// Get and return a random element from the list of suitable puzzles
		int randIndex = rand.nextInt(suitablePuzzles.size());
		return suitablePuzzles.get(randIndex);
	}

	/**
	 * Returns a random FileLoader with a puzzle of the given difficulty and
	 * size.
	 * 
	 * @param difficulty
	 * @param size
	 * @return
	 */
	FileLoader randomPuzzleLoader(Difficulty difficulty, int size) {
		if (this.puzzlesOfSize.containsKey(size)) {
			ArrayList<FileLoader> suitablePuzzles = new ArrayList<FileLoader>();
			Iterator<FileLoader> it = this.puzzlesOfSize.get(size).iterator();
			while (it.hasNext()) {
				FileLoader fl = it.next();
				if (fl.getPuzzleDifficulty() == difficulty) {
					suitablePuzzles.add(fl);
				}
			}

			// Get and return a random element from the list of suitable puzzles
			int randIndex = rand.nextInt(suitablePuzzles.size());
			return suitablePuzzles.get(randIndex);
		} else {
			return null;
		}
	}

	/**
	 * @return a set of the available difficulties in this library.
	 */
	public Set<Difficulty> availableDifficulties() {
		return this.puzzlesOfDifficulty.keySet();
	}

	/**
	 * @param difficulty
	 *            the difficulty that all puzzles in the returned set should be
	 * @return a set of the available sizes of the given difficulty in the
	 *         library
	 */
	public Set<Integer> availableSizesOfDifficulty(Difficulty difficulty) {
		Set<Integer> availableSizes = new HashSet<Integer>();
		Collection<FileLoader> possibleLoaders = this.puzzlesOfDifficulty
				.get(difficulty);
		if (possibleLoaders == null) {
			return availableSizes;
		}
		for (FileLoader loader : possibleLoaders) {
			availableSizes.add(loader.getPuzzleSize());
		}
		return availableSizes;
	}

	/**
	 * @return the absolute path of the puzzle library
	 */
	public String getPath() {
		return path;
	}

	public int numPuzzlesOfDifficulty(Difficulty difficulty) {
		if (this.puzzlesOfDifficulty.containsKey(difficulty)) {
			return this.puzzlesOfDifficulty.get(difficulty).size();
		} else {
			return 0;
		}
	}

	/**
	 * 
	 * @param difficulty
	 *            difficulty constraint
	 * @param size
	 *            size constraint
	 * @return the number of puzzles of the given difficulty and size
	 */
	public int numPuzzlesOfAttributes(Difficulty difficulty, int size) {
		if (this.puzzlesOfSize.containsKey(size)) {
			int suitablePuzzles = 0;
			Iterator<FileLoader> it = this.puzzlesOfSize.get(size).iterator();
			while (it.hasNext()) {
				if (it.next().getPuzzleDifficulty() == difficulty) {
					suitablePuzzles++;
				}
			}
			return suitablePuzzles;
		} else {
			return 0;
		}
	}

	/**
	 * 
	 * @param size
	 *            size constraint
	 * @return the number of puzzles with the given size
	 */
	public int numPuzzlesOfSize(int size) {
		if (this.puzzlesOfSize.containsKey(size)) {
			return this.puzzlesOfSize.get(size).size();
		} else {
			return 0;
		}
	}

	/**
	 * @return the total number of puzzles in the library
	 */
	public int totalPuzzles() {
		return this.puzzles.size();
	}
}
