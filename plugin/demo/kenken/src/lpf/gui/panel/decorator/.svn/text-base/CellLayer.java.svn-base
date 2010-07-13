package lpf.gui.panel.decorator;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.Set;

import lpf.model.core.Cell;
import lpf.model.core.Grid;
import lpf.model.core.Value;
import lpf.model.kenken.KenKenPuzzle;

/**
 * CellLayer is responsible to draw Digits, Marks
 * @author Wang Han
 * @author Nam Do
 * 
 */
public class CellLayer extends Decorator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4852204420533979623L;

	private KenKenPuzzle puzzle;
	
	private Grid grid;

	public CellLayer(Decorator inner, KenKenPuzzle puzzle) {
		super(inner);
		this.setSize(inner.getSize());
		this.puzzle = puzzle;
		this.grid = puzzle.getPlayerGrid();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		int size = puzzle.getPlayerGrid().height;
		String strMark = "";
		

		// Draw digits
		g.setFont(Base.fontDigit);
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Value value = grid.cells[i][j].getDigit();
				if (value != null) {
					g.drawString(value.value + "",
							(Base.CELLSIZE + Base.GRIDSIZE) * j
									+ Base.LEFTOFFSETDIGIT,
							(Base.CELLSIZE + Base.GRIDSIZE) * (i + 1)
									- Base.BOTTOMOFFSETDIGIT);
				}
			}
		}

		
		Collection<Cell> incorrectCollection = puzzle.getIdentifiedIncorrectCells();

		if (incorrectCollection != null && incorrectCollection.size() != 0) {
			Cell[] array = new Cell[incorrectCollection.size()];
			incorrectCollection.toArray(array);
			Cell cell = null;
			int i,j;
			Value value;
			g.setColor(Color.RED);
			for (int index = 0; index < array.length; index++) {
				cell = array[index];

				i = cell.loc.row - 1;
				j = cell.loc.column - 'A';
				value = grid.cells[i][j].getDigit();

				// If the cell has digit and the digit value has not been
				// changed compared to
				// the value when identify incorrect was clicked down,
				// still highlight the cell.
				// Else, delete the cell from the incorrect cell collection.
				if (value != null && value.equals(cell.getDigit())) {
					g.drawString(value.value + "",
							(Base.CELLSIZE + Base.GRIDSIZE) * j
									+ Base.LEFTOFFSETDIGIT,
							(Base.CELLSIZE + Base.GRIDSIZE) * (i + 1)
									- Base.BOTTOMOFFSETDIGIT);
				} else {
					cell.clearDigit();
				}
			}
		}

		// Draw marks
		g.setFont(Base.fontMarks);
		g.setColor(Color.RED);

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				strMark = "";
				for (int m = 0; m < size; m++) {

					Set<Value> marks = grid.cells[i][j].getMarks();
					if (marks.size() > 0 && grid.cells[i][j].getDigit() == null)
						if (marks.contains((new Value((char) (m + '1'))))) {
							strMark += "" + (m + 1);
						}

				}

				g.drawString(strMark, (Base.CELLSIZE + Base.GRIDSIZE) * j
						+ Base.LEFTOFFSETMARKS, (Base.CELLSIZE + Base.GRIDSIZE)
						* (i + 1) - Base.BOTTOMOFFSETMARKS);

			}
		}

	}

	public void setGrid(Grid grid) {
		this.grid = grid;
	}
}
