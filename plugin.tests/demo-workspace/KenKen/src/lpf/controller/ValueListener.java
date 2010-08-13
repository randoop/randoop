package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import lpf.commands.SetDigitCommand;
import lpf.gui.KenKenGUI;
import lpf.model.core.Cell;
import lpf.model.core.Value;

/**
 * Listener to handle the Value
 * @author Nam Do
 *
 */
public class ValueListener implements ActionListener {

	/** KenKen Gui */
	private KenKenGUI gui;

	/** Remove or update */
	private boolean remove;

	/**
	 * 
	 * @param gui
	 * @param remove	true if the user wants to remove a Value
	 * 					false if the user wants to update a Value
	 */
	public ValueListener(KenKenGUI gui, boolean remove) {
		this.gui = gui;
		this.remove = remove;
	}

	/**
	 * Perform an update on changing the Value of a cell, and repaint the Panels
	 */
	public void actionPerformed(ActionEvent e) {
		Cell cell = this.gui.getCurrentCell();
		if (this.remove)
		{
			this.gui.getHistory().perform(new SetDigitCommand(cell, null));
			cell.clearDigit();
		}
		else
		{
			char value = e.getActionCommand().charAt(0);
			this.gui.getHistory().perform(new SetDigitCommand(cell, new Value(value)));			
		}		
		
		this.gui.getMainPanel().getGamePanel().paintGamePanel(gui.getPuzzle());
		
		if (this.gui.getPuzzle().hasWon()){
			this.gui.setPlaying(false);
			JOptionPane.showMessageDialog(gui, "Congratulations, you won!");
		}		
	}
}
