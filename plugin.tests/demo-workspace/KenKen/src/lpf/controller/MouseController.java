package lpf.controller;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import lpf.gui.KenKenGUI;
import lpf.gui.panel.decorator.Base;
import lpf.model.core.Cell;
import lpf.model.core.Location;

/**
 * Listener to handle when user clicks on the game area
 * @author Nam Do
 *
 */
public class MouseController implements MouseListener, MouseMotionListener {

	/** KenKen Gui */
	private KenKenGUI gui;

	/**
	 * Constructor for MouseController
	 * @param gui
	 */
	public MouseController(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Locate Cell based on the location of mouse event
	 * @param point
	 * @return	a Cell
	 */
	private Cell locate(Point point) {
		Cell cell = null;
		int column = getLocation(point.x);
		int row = getLocation(point.y);
		if (!(column == 0 || row == 0)) {
			Location loc = new Location(row, (char) (column - 1 + 'A'));
			cell = gui.getPuzzle().getPlayerGrid().getCellAtLocation(loc);
		}
		return cell;
	}

	/**
	 * Get the row or column based on the ordinate 
	 * @param i
	 * @return
	 */
	private int getLocation(int i) {
		int location = 0;
		i = i - Base.CAGESIZE;

		while (i >= 0) {
			i -= Base.CELLSIZE;
			i -= Base.GRIDSIZE;
			location++;
		}
		if (location > gui.getPuzzle().getSize()) {
			return 0;
		}
		return location;
	}
	
	/**
	 * Perform an action for a MouseEvent
	 * @param e
	 */
	private void doMouse(MouseEvent e)
	{		
		if (this.gui.isPlaying())
		{
			Cell cell = locate(e.getPoint());
			this.gui.setCurrentCell(cell);
			
			/** Update panels */
			this.gui.getMainPanel().getValuePanel().updateCell(cell);
			this.gui.getMainPanel().getMarkPanel().updateCell(cell);
		}
	}

	public void mouseClicked(MouseEvent e) {
		doMouse(e);
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		doMouse(e);
	}

	public void mouseReleased(MouseEvent e) {
		doMouse(e);
	}

	public void mouseDragged(MouseEvent e) {
		doMouse(e);
	}

	public void mouseMoved(MouseEvent e) {
		doMouse(e);
	}
}
