package lpf.model.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Set;

public class Puzzle {
	/** InitialGrid **/
	Grid initialGrid;

	/** PlayerGrid **/
	Grid playerGrid;

	/** SolutionGrid **/
	Grid solutionGrid;

	/** Difficulty **/
	private int difficulty;

	/**
	 * Construct Puzzle with SolutionGrid
	 * 
	 * @param solution
	 *            initiate puzzle with the solution grid
	 */
	public Puzzle(Grid solution) {
		this.solutionGrid = (Grid) deepClone(solution);
	}

	/**
	 * Check playerGrid with SolutionGrid to see whether he wins or not
	 * 
	 * @return true if all the cells are right; false otherwise
	 */
	public boolean hasWon() {
		if (solutionGrid != null && playerGrid != null) {
			return solutionGrid.equals(playerGrid);
		} else {
			return false;
		}
	}

	/**
	 * Returns the size of the puzzle. KenKen puzzles are square so this gives
	 * us the width and height of the game.
	 * 
	 * @return the size of the KenKenPuzzle
	 */
	public int getSize() {
		// height = width, so it doesn't matter which we use to return the size.
		return this.solutionGrid.height;
	}

	/**
	 * Return the set of incorrect cells
	 * 
	 * @return set of incorrect cells
	 */
	public Set<Cell> getIncorrect() {
		ArraySet<Cell> identifiedIncorrectCells = new ArraySet<Cell>();

		Iterator<Cell> it = getPlayerGrid().iterator();
		while (it.hasNext()) {
			Cell cell = it.next();

			// only check values that have been placed
			if (cell.getDigit() != null) {
				// If this cell does not equals the cell in the solution grid at
				// the
				// same location it is incorrect.
				if (!cell.equals(getSolutionGrid().getCellAtLocation(cell.loc))) {
					// Copy the cell content into a new cell object,
					// then add it into incorrect collection
					Location loc = new Location(cell.loc.row, cell.loc.column);
					Cell incorrectCell = new Cell(loc);
					incorrectCell.setDigit(new Value(cell.getDigit().value));
					identifiedIncorrectCells.add(incorrectCell);
				}
			}
		}

		return identifiedIncorrectCells;
	}

	/**
	 * Reset the playerGrid by initialGrid
	 */
	public void reset() {
		playerGrid = (Grid) deepClone(initialGrid);
	}

	/**
	 * Return solutionGrid
	 * 
	 * @return solutionGrid
	 */
	public Grid getSolutionGrid() {
		return solutionGrid;
	}

	/**
	 * Return playerGrid
	 * 
	 * @return playerGrid
	 */
	public Grid getPlayerGrid() {
		return playerGrid;
	}

	/**
	 * Set initialGrid
	 * 
	 * @param g
	 *            initialGrid
	 */
	public void setInitialGrid(Grid g) {
		this.initialGrid = (Grid) deepClone(g);
		this.playerGrid = (Grid) deepClone(g);
	}

	/**
	 * Return initialGrid
	 * 
	 * @return initialGrid
	 */
	public Grid getInitialGrid() {
		return initialGrid;
	}

	/**
	 * Return difficulty
	 * 
	 * @return difficulty
	 */
	public int getDifficulty() {
		return difficulty;
	}

	/**
	 * Set difficulty
	 * 
	 * @param d
	 *            difficulty
	 */
	public void setDifficulty(int d) {
		this.difficulty = d;
	}

	/**
	 * Clone object
	 * 
	 * @param src
	 *            reference object to be cloned
	 * @return Generated object
	 */
	protected Object deepClone(Object src) {
		Object o = null;
		try {
			if (src != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(src);
				oos.close();

				ByteArrayInputStream bais = new ByteArrayInputStream(baos
						.toByteArray());
				ObjectInputStream ois = new ObjectInputStream(bais);

				o = ois.readObject();
				ois.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return o;
	}
}
