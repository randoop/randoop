package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import lpf.gui.KenKenGUI;

/**
 * Listener to handle when the user clicks Give Up menu item
 * @author Nam Do
 *
 */
public class GiveUpListener implements ActionListener {

	/** KenKen Gui */
	private KenKenGUI gui;

	/**
	 * Constructor for GiveUpListener 
	 * @param gui
	 */
	public GiveUpListener(KenKenGUI gui) {
		this.gui = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/** Does not let the user interact with the game anymore */
		this.gui.setPlaying(false);
	}

}
