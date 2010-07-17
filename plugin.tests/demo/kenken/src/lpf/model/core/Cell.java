package lpf.model.core;

import java.io.Serializable;
import java.util.Set;

/**
 * Set of operation in Cell
 * 
 * @author wanghan
 */
public class Cell implements Serializable {
	/** SerialUID **/
	private static final long serialVersionUID = 1040192047807644013L;

	public final Location loc;

	/** Marks **/
	private Set<Value> marks;

	/** Digits **/
	private Value digit;

	/**
	 * Initiate Cell with location
	 * 
	 * @param loc
	 *            location of Cell
	 */
	public Cell(Location loc) {
		this.loc = loc;
		this.marks = new ArraySet<Value>();
		this.digit = null;
	}

	/**
	 * Add Mark into Cell
	 * 
	 * @param v
	 *            mark to be added
	 */
	public void addMark(Value v) {
		marks.add(v);
	}

	/**
	 * Remove a Mark from Marks
	 * 
	 * @param v
	 *            mark to be removed
	 */
	public void removeMark(Value v) {
		marks.remove(v);
	}

	/**
	 * Clear all Marks in Cell
	 */
	public void clearMarks() {
		marks.clear();
	}

	/**
	 * Get Marks in Cell
	 * 
	 * @return set of Marks
	 */
	public Set<Value> getMarks() {
		return marks;
	}

	/**
	 * Get Digit in Cell
	 * 
	 * @return Digit of Cell
	 */
	public Value getDigit() {
		return digit;
	}

	/**
	 * Set Digit in Cell
	 * 
	 * @param v
	 *            Digit to be added
	 */
	public void setDigit(Value v) {
		this.digit = v;
	}

	/**
	 * Check two Grids whether they are equal
	 * 
	 * @param o
	 *            reference object with which to compare
	 * @return true if both Cells have the same Digits at the same Location;
	 *         false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Cell) {
			Cell other = (Cell) o;
			if (loc.equals(other.loc)) {
				if (digit == null && other.digit == null) {
					return true;
				} else if (digit != null) {
					return digit.equals(other.digit);
				} else if (other.digit != null) {
					return other.digit.equals(digit);
				}
			}
		}
		return false;
	}

	/**
	 * Check whether Cell is occupied
	 * 
	 * @return true if Digit is in Cell; false if no Digit in Cell
	 */
	public boolean isOccupied() {
		if (digit != null) {
			return true;
		}

		return false;
	}

	/**
	 * Clear Digit in Cell
	 */
	public void clearDigit() {
		digit = null;
	}
}
