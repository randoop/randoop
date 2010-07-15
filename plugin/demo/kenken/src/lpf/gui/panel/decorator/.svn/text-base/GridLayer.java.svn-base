package lpf.gui.panel.decorator;

import java.awt.Graphics;

import lpf.model.core.Grid;
import lpf.model.kenken.KenKenPuzzle;

/**
 * GridLayer is responsible to draw the Grid
 * @author Nam Do
 * 
 */
public class GridLayer extends Decorator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 243814589285096899L;

	private KenKenPuzzle puzzle;

	/**
	 * Constructor for GridLayer
	 * @param inner
	 * @param puzzle
	 */
	public GridLayer(Decorator inner, KenKenPuzzle puzzle) {
		super(inner);
		this.setSize(inner.getSize());
		this.puzzle = puzzle;
	}

	public void paintComponent(Graphics g) {		
		super.paintComponent(g);
		
		Grid grid = puzzle.getPlayerGrid(); 
		

		/**
		 * Cage is painted by 3 strokes (3 pixels) Grid is painted by 1 stroke
		 * (1 pixel) Size of a cell is 40 pixels
		 */

		// Draw the horizontal lines
		for (int i = 1; i < grid.height; i++) {
			g.drawLine(1, 1 + i * Base.CELLSIZE + (i - 1) * Base.GRIDSIZE, 1
					+ (grid.height) * Base.CELLSIZE + (grid.height - 1) * Base.GRIDSIZE,
					1 + i * Base.CELLSIZE + (i - 1) * Base.GRIDSIZE);

		}

		// Draw the vertical lines
		for (int i = 1; i < grid.height; i++) {
			g.drawLine(1 + i * Base.CELLSIZE + (i - 1) * Base.GRIDSIZE, 1, 1 + i
					* Base.CELLSIZE + (i - 1) * Base.GRIDSIZE, 1 + (grid.height)
					* Base.CELLSIZE + (grid.height - 1) * Base.GRIDSIZE);

		}
	}
}
