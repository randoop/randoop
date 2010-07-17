package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import lpf.gui.KenKenGUI;
import lpf.gui.MessageDialog;

/**
 * Listener to handle when the user clicks Exit menu item or Close button
 * @author Nam Do
 *
 */
public class ExitListener implements ActionListener, WindowListener {
	
	/** KenKen Gui */	
	private KenKenGUI gui;
	
	/**
	 * Constructor for ExitListener
	 * @param gui
	 */
	public ExitListener(KenKenGUI gui)
	{
		this.gui = gui;
	}
	
	/**
	 * Asks the user to exit or not
	 */
	public void close()
	{
		if (gui.isPlaying())
		{			
			if (MessageDialog.showAlertDlg("Do you want to quit this game?"))
			{
				System.exit(0);
			}
			else
			{
				return;
			}
		}
		else
		{
			System.exit(0);
		}
	}

	public void actionPerformed(ActionEvent e) {
		this.close();
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		this.close();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

}
