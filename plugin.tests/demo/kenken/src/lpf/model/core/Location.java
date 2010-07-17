package lpf.model.core;

import java.io.Serializable;

/**
 * Set of operation in Location
 * 
 * @author wanghan
 */
public class Location implements Serializable {
	/** SerialUID **/
	private static final long serialVersionUID = 4435197065773084642L;

	/** Row Coordinate **/
	public final int row;
	
	/** Column Coordinate **/
	public final char column;
	
	/**
	 * Initiate Location with row and column
	 * @param row
	 * @param column
	 */
	public Location(int row, char column) {
		this.row = row;
		this.column = column;
	}

	/**
	 * Check two Locations whether they are equal
	 * @param obj	the reference object with which to compare.
	 * @return		true if two Locations have the same row coordinate and column coordinate; 
	 * 				false if otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof Location) {
			Location otherLoc = (Location) o;
			return this.row == otherLoc.row && this.column == otherLoc.column;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return row * 100 + column;
	}
	
	@Override
	public String toString() {
		return "("+this.column+","+this.row+")";
	}
}
