package lpf.model.kenken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import lpf.model.core.Cell;
import lpf.model.core.Grid;
import lpf.model.core.Location;

public class Cage {
	public final char operation;
	public final int finalValue;
	final Collection<Location> locations;

	public Cage(char operation, int finalValue, Collection<Location> locations) {
		this.operation = operation;
		this.finalValue = finalValue;
		this.locations = locations;
	}
	
	public boolean contains(Location loc) {
		return locations.contains(loc);
	}

	public boolean isValid(Grid solution) {
		Iterator<Location> it = locations.iterator();
		Collection<Cell> constrainedCells = new ArrayList<Cell>();
		while (it.hasNext()) {
			Location loc = it.next();
			constrainedCells.add(solution.getCellAtLocation(loc));
		}

		switch (this.operation) {
		case '+':
			return calculateAddition(constrainedCells);
		case '-':
			return calculateSubtraction(constrainedCells);
		case '/':
			return calculateDivision(constrainedCells);
		case '*':
			return calculateMultiplication(constrainedCells);
		default:
			// If the operation is anything else, the cage is invalid
			return false;
		}
	}

	/**
	 * Check whether locations are adjacent
	 * 
	 * @return true if all locations are adjacent; False otherwise.
	 */
	public boolean isAdjacent() {
		boolean adjCells[] = new boolean[locations.size()];

		adjCells[0] = true;

		findAdjCells(0, adjCells);

		for (int i = 0; i < locations.size(); i++) {
			if (adjCells[i] == false) {
				return false;
			}
		}

		return true;
	}

	private void findAdjCells(int i, boolean[] adjCells) {
		for (int j = 0; j < locations.size(); j++) {
			if (adjCells[j] == false && isAdjacentOfTwo(i, j)) {
				adjCells[j] = true;
				findAdjCells(j, adjCells);
			}
		}
	}

	private boolean isAdjacentOfTwo(int i, int j) {
		int delta_x, delta_y;
		int i_row, j_row;
		char i_column, j_column;

		i_row = ((ArrayList<Location>) locations).get(i).row;
		i_column = ((ArrayList<Location>) locations).get(i).column;
		j_row = ((ArrayList<Location>) locations).get(j).row;
		j_column = ((ArrayList<Location>) locations).get(j).column;

		delta_x = i_column - j_column;
		delta_y = i_row - j_row;

		if (Math.abs(delta_x) + Math.abs(delta_y) == 1) {
			return true;
		} else {
			return false;
		}
	}

	private boolean calculateAddition(Collection<Cell> cells) {
		int finalvalue = 0;

		for (Cell cell : cells) {
			finalvalue = finalvalue + ctoi(cell.getDigit().value);
		}

		return this.finalValue == finalvalue;
	}

	private boolean calculateMultiplication(Collection<Cell> cells) {
		int finalvalue = 1;

		for (Cell cell : cells) {
			finalvalue = finalvalue * ctoi(cell.getDigit().value);
		}

		return this.finalValue == finalvalue;
	}

	private boolean calculateDivision(Collection<Cell> cells) {
		int finalvalue = maxval(cells) / minval(cells);
		return this.finalValue == finalvalue;
	}

	private boolean calculateSubtraction(Collection<Cell> cells) {
		int finalvalue = maxval(cells) - minval(cells);
		return this.finalValue == finalvalue;
	}

	public Iterator<Location> iterator() {
		return locations.iterator();
	}

	private static int maxval(Collection<Cell> cells) {
		int max = 0;

		for (Cell cell : cells) {
			if (ctoi(cell.getDigit().value) > max) {
				max = ctoi(cell.getDigit().value);
			}
		}

		return max;
	}

	private static int minval(Collection<Cell> cells) {
		int min = Integer.MAX_VALUE;

		for (Cell cell : cells) {
			if (ctoi(cell.getDigit().value) < min) {
				min = ctoi(cell.getDigit().value);
			}
		}

		return min;
	}

	private static int ctoi(char c) {
		return c - '0';
	}
}
