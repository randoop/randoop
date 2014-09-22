package lpf.main;

import java.awt.event.WindowListener;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import lpf.controller.ExitListener;
import lpf.gui.KenKenGUI;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException e) {
		}
		catch (ClassNotFoundException e) {
		} 
		catch (InstantiationException e) {
		}
		catch (IllegalAccessException e) {
		}

		KenKenGUI gui = new KenKenGUI();
		gui.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				
		gui.addWindowListener (new ExitListener(gui));


		// launch everything and go!
		gui.setVisible (true);
	}
}
