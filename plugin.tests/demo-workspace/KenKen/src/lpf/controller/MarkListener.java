package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import lpf.commands.AddMarkCommand;
import lpf.commands.RemoveMarkCommand;
import lpf.gui.KenKenGUI;
import lpf.model.core.Cell;
import lpf.model.core.Value;

/**
 * Listener to handle the Mark
 * @author Nam Do
 *
 */
public class MarkListener implements ActionListener {

	/** KenKen Gui */
	private KenKenGUI gui;
	
	/** Array of mark buttons */
	private JCheckBox[] markButtons;

	/**
	 * Constructor for MarkListener
	 * @param gui
	 */
	public MarkListener(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Perform an action when the user changes a Mark
	 */
	public void actionPerformed(ActionEvent e) {
		JCheckBox mark = (JCheckBox) e.getSource();
		
		Value value = new Value(mark.getText().charAt(0));
		Cell cell = this.gui.getCurrentCell();
		
		if (mark.isSelected())
		{
			this.gui.getHistory().perform(new AddMarkCommand(cell, value));
			cell.clearDigit();
		}
		else
		{
			this.gui.getHistory().perform(new RemoveMarkCommand(cell, value));
		}
		
		this.markButtons = this.gui.getMainPanel().getMarkPanel().getMarkButtons();
		int size = markButtons.length;
		boolean isEmpty = true;
		for (int i = 0; i < size - 1; i++)
		{
			if (markButtons[i].isSelected())
			{
				isEmpty = false;
			}
		}
		
		/** Displays No option when no marks have been placed */
		if (isEmpty)
		{
			markButtons[size - 1].setSelected(true);
		}
		else
		{
			markButtons[size - 1].setSelected(false);
		}	
		
		this.gui.getMainPanel().getGamePanel().paintGamePanel(gui.getPuzzle());
		
	}

}
