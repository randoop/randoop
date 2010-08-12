package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import lpf.gui.About;
import lpf.gui.KenKenGUI;

/**
 * Listener to handle when the user clicks About menu item
 * @author Nam Do
 * @author Wang Han
 * 
 */
public class AboutListener implements ActionListener {
	
	/** KenKen Gui */
	KenKenGUI gui;

	/**
	 * Constructor for AboutListener
	 * @param gui
	 */
	public AboutListener(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Create a new About panel;
	 */
	public void actionPerformed(ActionEvent e) {
		new About(gui);
	}

}
