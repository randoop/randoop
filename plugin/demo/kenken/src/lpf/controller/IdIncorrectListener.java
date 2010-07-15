package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import lpf.gui.KenKenGUI;

/**
 * Listener to handle when the user clicks Identify Incorrect menu item
 * @author Wang Han
 * @author Nam Do
 * 
 */
public class IdIncorrectListener implements ActionListener {
	
	/** KenKen Gui */
	private KenKenGUI gui;	

	/**
	 * Constructor for IdIncorrectListener
	 * @param gui
	 */
	public IdIncorrectListener(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Identifies Incorrect and displays on the board game
	 */
	public void actionPerformed(ActionEvent e) {
		if (this.gui.isPlaying())
		{
			this.gui.getPuzzle().identifyIncorrect();
			this.gui.getMainPanel().getGamePanel().paintGamePanel(gui.getPuzzle());
		}
		
	}

}
