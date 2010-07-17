package lpf.model.kenken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import lpf.model.core.Cell;
import lpf.model.core.Grid;
import lpf.model.core.Location;
import lpf.model.core.Puzzle;
import lpf.model.core.Value;
import lpf.model.kenken.preferences.Difficulty;
import lpf.model.kenken.preferences.InvalidDifficultyException;

/**
 * The KenKenPuzzle is an extension of Puzzle that adds functionality for
 * playing ken-ken games. The most noticeable modification is the addition of
 * cages. This allows the KenKenPuzzle to check if it is valid when it is
 * loaded.
 * 
 * @author Peter Kalauskas, Han Wang
 */
public class KenKenPuzzle extends Puzzle {
	private static Random rand = new Random();

	private Collection<Cage> cages;

	private Collection<Cell> identifiedIncorrectCells;

	/**
	 * Constructs a new KenKen puzzle from the cages and solution grid.
	 * 
	 * @param cages
	 *            A collection of Cages that will be validated here.
	 * @param solution
	 * @throws InvalidKenKenPuzzleException
	 */
	public KenKenPuzzle(Collection<Cage> cages, Grid solution)
			throws InvalidKenKenPuzzleException {
		super(solution);
		this.cages = cages;

		// don't bother parsing a non-square grid
		if (solution.width != solution.height) {
			throw new InvalidKenKenPuzzleException("Solution is not square");
		}
		int size = solution.width;

		// Check that every rows and column has n unique values between
		// 1 and n inclusive, where n is the size of the grid
		for (int row = 1; row <= size; row++) {
			Set<Character> uniqueValues = new HashSet<Character>();
			for (char column = 'A'; column < (char) ('A' + size); column++) {
				Location loc = new Location(row, column);
				Value digit = solution.getCellAtLocation(loc).getDigit();

				if (digit == null) {
					throw new InvalidKenKenPuzzleException("Solution is incomplete.");
				} else if (digit.value < '0' || digit.value > ('0' + size)) {
					throw new InvalidKenKenPuzzleException(
							"Value in solution grid is out of range.");
				} else if (!uniqueValues.add(digit.value)) {
					throw new InvalidKenKenPuzzleException("Row is not unique.");
				}
			}
		}

		// checks that every cage is valid
		ArrayList<Location> cageLocations = new ArrayList<Location>();
		for (Cage cage : this.cages) {
			if (!cage.isValid(solution)) {
				throw new InvalidKenKenPuzzleException("Cage " + cage.operation + " "
						+ cage.finalValue + " invalid.");
			}

			// check that every cell in the cage is adjacent
			if (!cage.isAdjacent()) {
				throw new InvalidKenKenPuzzleException(
						"Cells in Cage are not adjacent");
			}

			// add the locations in this cell to a list of locations
			Iterator<Location> it = cage.iterator();
			while (it.hasNext()) {
				Location loc = it.next();
				cageLocations.add(loc);
			}
		}

		// check that no two cages overlap
		ArrayList<Location> duplicateChecker = new ArrayList<Location>();
		for (Location loc : cageLocations) {
			if (duplicateChecker.contains(loc)) {
				throw new InvalidKenKenPuzzleException("Cages are overlapping");
			} else {
				duplicateChecker.add(loc);
			}
		}

		// Check that every location is covered by a cell
		Iterator<Cell> it = solution.iterator();
		while (it.hasNext()) {
			Cell cell = it.next();
			if (!cageLocations.contains(cell.loc)) {
				throw new InvalidKenKenPuzzleException(
						"Not every cell location is covered by a cage.");
			}
		}
	}

	/**
	 * @return The difficulty of this puzzle as an enumerated type
	 */
	public Difficulty getDifficultyLevel() {
		try {
			return Difficulty.getDifficulty(getDifficulty());
		} catch (InvalidDifficultyException e) {
			// If this error occurs the puzzle should not exist
			return null;
		}
	}

	/**
	 * Puts one unsolved cell from the solution grid into the player grid.
	 */
	public void hint() {
		// create a list of unsolved cells by
		// first - add all solutionGrid cells to the list
		// second - remove all playerGrid cells that are occupied from the list
		ArrayList<Location> unsolved = new ArrayList<Location>();
		Iterator<Cell> it = getSolutionGrid().iterator();
		while (it.hasNext()) {
			unsolved.add(it.next().loc);
		}

		it = getPlayerGrid().iterator();
		while (it.hasNext()) {
			Cell cell = it.next();
			if (cell.isOccupied()) {
				unsolved.remove(cell.loc);
			}
		}

		// if there are cells left to solve
		if (unsolved.size() > 0) {
			// choose a cell at random and add its value in the right cell in
			// the player grid
			Location randLoc = unsolved.get(rand.nextInt(unsolved.size()));
			Value solutionValue = getSolutionGrid().getCellAtLocation(randLoc)
					.getDigit();
			getPlayerGrid().getCellAtLocation(randLoc).setDigit(solutionValue);
		}
	}

	/**
	 * 
	 * @return a Collection of the cages for this puzzle
	 */
	public Collection<Cage> getCages() {
		return cages;
	}

	/**
	 * Identifies the incorrect cells and stored them in a field.
	 */
	public void identifyIncorrect() {
		this.identifiedIncorrectCells = getIncorrect();
	}

	/**
	 * Removes all currently incorrect values from the player grid.
	 */
	public void removeIncorrect() {
		identifyIncorrect();

		for (Cell cell : this.identifiedIncorrectCells) {
			this.getPlayerGrid().getCellAtLocation(cell.loc).setDigit(null);
			cell.setDigit(null);
		}

		this.identifiedIncorrectCells = null;
	}

	/**
	 * @return a Collection of cells that were identified as incorrect the last
	 *         time identifyIncorrect() was called.
	 */
	public Collection<Cell> getIdentifiedIncorrectCells() {
		return identifiedIncorrectCells;
	}

	/**
	 * Clears the identifiedIncorrectCells field.
	 */
	public void clearIdentifiedIncorrectCells() {
		identifiedIncorrectCells = null;
	}
}
