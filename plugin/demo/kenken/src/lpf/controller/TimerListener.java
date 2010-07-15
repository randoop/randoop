package lpf.controller;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import lpf.gui.KenKenGUI;

/**
 * Listener to handle when the user clicks Timer menu item
 * @author Nam Do
 * @author Wang Han
 *
 */
public class TimerListener implements ItemListener {
	
	/** KenKen Gui */
	KenKenGUI gui;
	
	/**
	 * Constructor for TimerListener
	 * @param gui
	 */
	public TimerListener(KenKenGUI gui) {
		this.gui = gui;
	}	

	/**
	 * Hide or unhide the Timer
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			gui.getMainPanel().getTimerPanel().setVisible(true);
		}
		else
		{
			gui.getMainPanel().getTimerPanel().setVisible(false);
		}
	}
}
