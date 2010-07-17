package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import lpf.gui.KenKenGUI;

/**
 * Listener to handle when the user clicks Remove Incorrect menu item
 * @author Nam Do
 *
 */
public class RemoveIncorrectListener implements ActionListener {

	/** KenKen Gui */
	private KenKenGUI gui;

	/**
	 * Constructor for RemoveIncorrectListener
	 * @param gui
	 */
	public RemoveIncorrectListener(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Remove Incorrect Values and repaint the panel 
	 */
	public void actionPerformed(ActionEvent e) {
		if (gui.isPlaying())
		{
			this.gui.getPuzzle().removeIncorrect();
			this.gui.getMainPanel().getGamePanel().paintGamePanel(gui.getPuzzle());
			
			// Clear the command history
			this.gui.initiateCommandHistory();
		}
	}

}
