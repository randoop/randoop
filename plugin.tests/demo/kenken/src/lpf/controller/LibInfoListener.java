package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import lpf.gui.KenKenGUI;
import lpf.gui.LibInfo;

/**
 * Listener to handle when the user clicks Library Info menu item
 * @author Wang Han
 * @author Nam Do
 * 
 */
public class LibInfoListener implements ActionListener {
	
	/** KenKen Gui */
	KenKenGUI gui;

	/**
	 * 
	 * @param gui
	 */
	public LibInfoListener(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Displays Library info
	 */
	public void actionPerformed(ActionEvent e) {
		new LibInfo(gui);
	}

}
