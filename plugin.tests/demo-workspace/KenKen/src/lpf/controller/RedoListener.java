package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import lpf.gui.KenKenGUI;
import lpf.model.core.Cell;

/**
 * 
 * Listener to handle when the user clicks Redo menu item
 * @author Nam Do
 *
 */
public class RedoListener implements ActionListener {

	/** KenKen Gui */
	private KenKenGUI gui;

	/**
	 * Constructor for RedoListener
	 * @param gui
	 */
	public RedoListener(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Redo and repaint the panels
	 */
	public void actionPerformed(ActionEvent e) {
		
		
		if (this.gui.getHistory().canRedo())
		{
			this.gui.getHistory().redo();
			Cell cell = this.gui.getCurrentCell();
			this.gui.getMainPanel().getGamePanel().paintGamePanel(gui.getPuzzle());
			this.gui.getMainPanel().getValuePanel().updateCell(cell);
			this.gui.getMainPanel().getMarkPanel().updateCell(cell);
		}
	}
}
