package lpf.model.core;

import java.io.Serializable;
import java.util.Iterator;


/**
 * Set of operation in Grid
 * 
 * @author wanghan
 */
public class Grid implements Serializable {

	/** SerialUID **/
	private static final long serialVersionUID = -2264170181548104309L;

	/** Grid width **/
	public final int width;
	
	/** Grid Height **/
	public final int height;
	
	/** Cells **/
	public Cell[][] cells;

	/**
	 * Set Grid's width and height, initiate cells in Grid
	 * @param w width of Grid
	 * @param h height of Grid
	 */
	public Grid(int w, int h) {
		this.width = w;
		this.height = h;
		this.cells = new Cell[h][w];
		
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				this.cells[i][j] = new Cell( new Location( i + 1, itoc(j)) );
			}
		}
	}

	/**
	 * Get Cell at specified location 
	 * @param loc 	target location
	 * @return 		cell at specified location 
	 */
	public Cell getCellAtLocation(Location loc) {		
		return (Cell)cells[loc.row - 1][ctoi(loc.column)];
	}
	
	/**
	 * Get cells iterators
	 * @return cells iterator
	 */
	public Iterator<Cell> iterator() {
		return new CellsIterator(cells, width, height);
	}
	
	/**
	 * Convert character to integer 
	 * @param c	target character
	 * @return	converted integer
	 */
	private int ctoi(char c)  {
		return c - 'A';
	}
	
	/**
	 * Convert integer to character
	 * @param i	target integer
	 * @return	converted character
	 */
	private char itoc(int i) {
		return (char)(i + 'A');
	}
	
	/**
	 * Check two Grids whether they are equal
	 * @param o	reference object with which to compare 
	 * @return	true if both Grids have the same cells at the same location; 
	 * 			false if any cell in one Grid is not the same with
	 * 			the corresponding cell in the other Grid.
	 */
	public boolean equals(Object o) {
		if(o instanceof Grid) {
			// Check two Grids have the same height and width
			if (height != ((Grid)o).height || width != ((Grid)o).width) {
				return false;
			}

			// Check two Grids have the same cells at same location
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					if (!cells[i][j].equals( ((Grid)o).cells[i][j]) )
						return false;
				}
			}
			return true;
		}
		return false;
	}
}
