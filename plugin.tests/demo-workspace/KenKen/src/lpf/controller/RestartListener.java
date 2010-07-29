package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import lpf.gui.KenKenGUI;
import lpf.gui.MessageDialog;
import lpf.model.kenken.KenKenPuzzle;

/**
 * Listener to handle when the user clicks Restart menu item
 * @author Nam Do
 *
 */
public class RestartListener implements ActionListener {

	/** KenKen Gui */
	KenKenGUI gui;
	
	/**
	 * Constructor for RestartListener
	 * @param gui
	 */
	public RestartListener(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Restart the game and repaint the panel
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (gui.isPlaying())
		{			
			if (!MessageDialog.showAlertDlg("Do you want to restart this game?"))
			{
				return;
			}
		}
		
		KenKenPuzzle puzzle = this.gui.getPuzzle();
		puzzle.setInitialGrid(puzzle.getInitialGrid());
		this.gui.buildNewGame(puzzle);
	}
}
