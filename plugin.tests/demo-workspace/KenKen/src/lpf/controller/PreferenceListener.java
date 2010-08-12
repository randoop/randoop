package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import lpf.gui.KenKenGUI;
import lpf.gui.MessageDialog;
import lpf.gui.PreferenceGUI;
import lpf.model.kenken.KenKenPuzzle;
import lpf.model.kenken.fileManagement.FileLoader;

/**
 * Listener to handle when the user clicks Preference menu item
 * @author Wang Han
 * @author Nam Do
 * 
 */
public class PreferenceListener implements ActionListener {
	
	/** KenKen Gui */
	private KenKenGUI gui;

	/**
	 * Constructor for PreferenceListener
	 * @param gui
	 */
	public PreferenceListener(KenKenGUI gui) {
		this.gui = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (gui.isPlaying())
		{			
			if (!MessageDialog.showAlertDlg("Set preference will stop the current game, do you want to continue?"))
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
		
		/** Create a new game based on the saved preference */
		this.gui.getMainPanel().getTimerPanel().stopTimer();
		new PreferenceGUI(gui, false);
		
		FileLoader fl = gui.getPuzzleLibrary().randomPuzzleLoader(gui.getPreference());
		KenKenPuzzle puzzle = fl.getKenKenPuzzle();
		
		if (puzzle != null)
		{
			this.gui.buildNewGame(puzzle);
		}
	}
}
