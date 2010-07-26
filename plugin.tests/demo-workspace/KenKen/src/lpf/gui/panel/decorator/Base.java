package lpf.gui.panel.decorator;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;


/**
 * This is the base class within the decorator chain. 
 * 
 * @author heineman
 * @author Nam Do
 */
public class Base extends Decorator {
	
	private static final long serialVersionUID = -1545846195722322814L;
	
	/**
	 *	GridLayer 
	 */

	public static final int CELLSIZE = 100;
	
	public static final int CAGESIZE = 3;

	public static final int GRIDSIZE = 1;
	
	/**
	 *	CageLayer
	 */
	public static final int CAGE_POSITION = 0;
	
	public static final int FONT_HEIGHT = CELLSIZE / 5;
	
	public static final int MARGIN = CELLSIZE / 10;
	
	/**
	 *	CellLayer
	 */	
	public static final int LEFTOFFSETDIGIT = CELLSIZE * 3 / 8;
	
	public static final int BOTTOMOFFSETDIGIT = CELLSIZE / 4;
	
	public static final int LEFTOFFSETMARKS = 5;
	
	public static final int BOTTOMOFFSETMARKS = 2;
	
	public static final Font fontDigit = new Font("Arial", Font.BOLD, CELLSIZE / 2);
	
	public static final Font fontMarks = new Font("Arial", Font.ITALIC, CELLSIZE / 4);


	/**
	 * The base drawing Concrete drawer will put everything into a screen image so that
	 * it can, ultimately, be copied into the given drawing field.
	 * 
	 * @param size
	 */
	public Base (Dimension size) {		
		super(null);
		this.setSize(size);
	}

	/**
	 * Request game state to be redrawn.
	 */
	@Override
	public void paintComponent (Graphics g) {}
}
