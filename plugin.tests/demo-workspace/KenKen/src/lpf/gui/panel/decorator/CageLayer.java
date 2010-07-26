package lpf.gui.panel.decorator;

import java.awt.Graphics;
import java.util.Collection;
import java.util.Iterator;

import lpf.model.core.Location;
import lpf.model.kenken.Cage;
import lpf.model.kenken.KenKenPuzzle;

/**
 * CageLayer is responsible to draw the Cages
 * @author Nam Do
 * 
 */
public class CageLayer extends Decorator {
	private static final long serialVersionUID = 6885663237415054084L;
	
	private KenKenPuzzle puzzle;

	/**
	 * Constructor for CageLayer
	 * @param inner
	 * @param puzzle
	 */
	public CageLayer(Decorator inner, KenKenPuzzle puzzle) {
		super(inner);
		this.setSize(inner.getSize());
		this.puzzle = puzzle;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Collection<Cage> cages = this.puzzle.getCages();
		
		for (Cage cage : cages) {
			Iterator<Location> locs = cage.iterator();

			Location currentUpperLeft = null;

			while (locs.hasNext()) {
				Location loc = locs.next();

				// Check that if this location may be the most upper left
				// location
				if (isMoreUpperLeft(loc, currentUpperLeft)) {
					currentUpperLeft = loc;
				}

				// Check if a cage line should be drawn above.
				if (loc.row - 1 < 1
						|| !cage
								.contains(new Location(loc.row - 1, loc.column))) {
					// The neighboring location is not part of this cage.
					// Draw a bold line between loc and otherLoc.
					g.fillRect(Base.CAGE_POSITION + (loc.column - 'A')
							* (Base.CELLSIZE + 1), Base.CAGE_POSITION
							+ (loc.row - 1) * (Base.CELLSIZE + 1),
							Base.CELLSIZE + Base.CAGESIZE,
							Base.CAGESIZE);
				}

				// Check if a cage line should be drawn below.
				if (loc.row + 1 > 26
						|| !cage
								.contains(new Location(loc.row + 1, loc.column))) {
					g.fillRect(Base.CAGE_POSITION + (loc.column - 'A')
							* (Base.CELLSIZE + 1), Base.CAGE_POSITION + loc.row
							* (Base.CELLSIZE + 1), Base.CELLSIZE
							+ Base.CAGESIZE, Base.CAGESIZE);
				}

				// Check if a cage line should be drawn left.
				if (loc.column - 1 < 'A'
						|| !cage.contains(new Location(loc.row,
								(char) (loc.column - 1)))) {
					g.fillRect(Base.CAGE_POSITION + (loc.column - 'A')
							* (Base.CELLSIZE + 1), Base.CAGE_POSITION
							+ (loc.row - 1) * (Base.CELLSIZE + 1),
							Base.CAGESIZE, Base.CAGESIZE
									+ Base.CELLSIZE);
				}

				// Check if a cage line should be drawn right.
				// if () {
				if (loc.column + 1 > 'Z'
						|| !cage.contains(new Location(loc.row,
								(char) (loc.column + 1)))) {
					g.fillRect(Base.CAGE_POSITION + (loc.column - 'A' + 1)
							* (Base.CELLSIZE + 1), Base.CAGE_POSITION
							+ (loc.row - 1) * (Base.CELLSIZE + 1),
							Base.CAGESIZE, Base.CAGESIZE
									+ Base.CELLSIZE);
				}
			}

			// Draw the constraint in the highest, farthest left cell
			String operation;
			if (cage.operation == '/') {
				operation = "÷";
			} else if (cage.operation == '*') {
				operation = "×";
			} else {
				operation = "" + cage.operation;
			}
			g.drawString("" + cage.finalValue + operation, Base.CAGE_POSITION
					+ (currentUpperLeft.column - 'A')
					* (Base.CELLSIZE + 1) + Base.MARGIN, Base.CAGE_POSITION
					+ (currentUpperLeft.row - 1) * (Base.CELLSIZE + 1)
					+ Base.FONT_HEIGHT + Base.MARGIN);
		}
	}

	/**
	 * Returns true if loc is higher and more upper left than currentUpperLeft.
	 * 
	 * Height takes priority over leftness.
	 */
	boolean isMoreUpperLeft(Location loc, Location currentUpperLeft) {
		return currentUpperLeft == null
				|| (loc.row < currentUpperLeft.row && loc.column < currentUpperLeft.column);
	}
}
