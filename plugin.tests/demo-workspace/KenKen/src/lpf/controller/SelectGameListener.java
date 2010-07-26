package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import lpf.gui.KenKenGUI;
import lpf.gui.MessageDialog;
import lpf.model.kenken.KenKenPuzzle;
import lpf.model.kenken.fileManagement.FileLoader;

/**
 * Listener to handle when the user clicks Select Game menu item
 * @author Nam Do
 *
 */
public class SelectGameListener implements ActionListener {
	
	/** KenKen Gui */
	KenKenGUI gui;

	/**
	 * Constructor for SelectGameListener
	 * @param gui
	 */
	public SelectGameListener(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Create a new game from an XML file
	 */
	public void actionPerformed(ActionEvent e) {
		if (gui.isPlaying())
		{			
			if (!MessageDialog.showAlertDlg("Do you want to select a new game?"))
			{
				return;
			}
		}
		
		FileLoader fl;
		int returnVal = gui.getMenu().getFcFile().showOpenDialog(gui);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	fl = new FileLoader(gui.getMenu().getFcFile().getSelectedFile().getAbsoluteFile());
        	KenKenPuzzle puzzle = fl.getKenKenPuzzle();
        	
        	if (puzzle != null){        		
        		this.gui.setFileLoader(fl);
        		this.gui.buildNewGame(puzzle);
			}else{
				JOptionPane.showMessageDialog(null, "Invalid KenKen Game.", "Error", JOptionPane.ERROR_MESSAGE);
        	}       
        } 
	}

}
