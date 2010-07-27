package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import lpf.gui.KenKenGUI;
import lpf.gui.MessageDialog;
import lpf.gui.PreferenceGUI;
import lpf.model.kenken.fileManagement.PuzzleLibrary;

/**
 * Listener to handle when the user clicks Select Library menu item
 * @author Nam Do
 * 
 */
public class SelectLibraryListener implements ActionListener {

	/** KenKen Gui */
	private KenKenGUI gui;

	/**
	 * Constructor for SelectLibraryListener
	 * @param gui
	 */
	public SelectLibraryListener(KenKenGUI gui) {
		super();
		this.gui = gui;
	}

	/**
	 * Select a new Library and ask for the Preference
	 */
	public void actionPerformed(ActionEvent e) {
		if (gui.isPlaying()) {
			if (!MessageDialog.showAlertDlg("Do you want to select a new library?")) {
				return;
			}
		}

		PuzzleLibrary lib;
		int returnVal = gui.getMenu().getFcLib().showOpenDialog(gui);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			lib = new PuzzleLibrary(gui.getMenu().getFcLib().getSelectedFile()
					.getAbsoluteFile());
			if (lib.isValid()) {
				this.gui.setPuzzleLibrary(lib);
				this.gui.getMenu().getMniPref().setEnabled(true);
				this.gui.getMenu().getMniNew().setEnabled(true);				
				new PreferenceGUI(gui, true);
			} else {
				JOptionPane.showMessageDialog(gui, "Invalid Library.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
