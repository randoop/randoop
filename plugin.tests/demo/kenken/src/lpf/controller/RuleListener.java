package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import lpf.gui.KenKenGUI;
import lpf.gui.Rule;

/**
 * Listener to handle when the user clicks Rule menu item
 * @author Nam Do
 * @author Wang Han
 * 
 */
public class RuleListener implements ActionListener {
	
	/** KenKen Gui */
	KenKenGUI gui;

	/**
	 * Constructor for RuleListner
	 * @param gui
	 */
	public RuleListener(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Create a Rule panel
	 */
	public void actionPerformed(ActionEvent e) {
		new Rule(gui);
	}

}
