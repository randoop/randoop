package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import lpf.gui.KenKenGUI;
import lpf.gui.MessageDialog;
import lpf.model.kenken.KenKenPuzzle;
import lpf.model.kenken.fileManagement.FileLoader;

/**
 * Listener to handle when the user clicks New Game menu item
 * @author Nam Do
 *
 */
public class NewGameListener implements ActionListener {
	
	/** KenKen Gui */
	private KenKenGUI gui;	

	/**
	 * Constructor for NewGameLIstener
	 * @param gui
	 */
	public NewGameListener(KenKenGUI gui) {
		this.gui = gui;
	}

	public void actionPerformed(ActionEvent e) {
		
		if (gui.isPlaying())
		{			
			if (!MessageDialog.showAlertDlg("Do you want to play a new game?"))
			{
				return;
			}
		}
		
		/** Invalid Preference */
		if (gui.getPreference() == null)
		{			
			JOptionPane.showMessageDialog(gui, "No Library loaded.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		/** Create a new game*/
		FileLoader fl = gui.getPuzzleLibrary().randomPuzzleLoader(gui.getPreference());
		KenKenPuzzle puzzle = fl.getKenKenPuzzle();
				
		if (puzzle != null)
		{
			this.gui.buildNewGame(puzzle);			
		}		
	}
}
