package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import lpf.gui.KenKenGUI;

/**
 * Listener to handle when the user clicks Hint item
 * @author Wang Han
 * 
 */
public class HintListener implements ActionListener {

	/** KenKen Gui */
	KenKenGUI gui;

	/**
	 * Constructor for HintListener
	 * @param gui
	 */
	public HintListener(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Display a hint
	 */
	public void actionPerformed(ActionEvent e) {
		if (gui.isPlaying())
		{			
			this.gui.getPuzzle().hint();
			this.gui.getMainPanel().getGamePanel().paintGamePanel(gui.getPuzzle());
		}
	}

}
