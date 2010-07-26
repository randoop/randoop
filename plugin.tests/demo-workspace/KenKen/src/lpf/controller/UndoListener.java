package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import lpf.gui.KenKenGUI;
import lpf.model.core.Cell;

/**
 * Listener to handle when the user clicks Undo menu item 
 * @author Nam Do
 *
 */
public class UndoListener implements ActionListener {

	/** KenKen Gui */
	private KenKenGUI gui;

	/**
	 * Constructor for UndoListener
	 * @param gui
	 */
	public UndoListener(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Undo and repaint the panels
	 */
	public void actionPerformed(ActionEvent e) {
		if (this.gui.getHistory().canUndo())
		{			
			this.gui.getHistory().undo();
			Cell cell = this.gui.getCurrentCell();
			this.gui.getMainPanel().getGamePanel().paintGamePanel(gui.getPuzzle());
			this.gui.getMainPanel().getValuePanel().updateCell(cell);
			this.gui.getMainPanel().getMarkPanel().updateCell(cell);
		}
	}
}
