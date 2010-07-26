package lpf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import lpf.commands.ClearMarksCommand;
import lpf.gui.KenKenGUI;
import lpf.model.core.Cell;

/**
 * Listener to handle the Mark
 * @author Nam Do
 *
 */
public class ClearAllMarksListener implements ActionListener {

	/** KenKen Gui */
	private KenKenGUI gui;
	
	/** List of mark buttons */
	private JCheckBox[] markButtons;

	/**
	 * Constructor for ClearAllMarksListener
	 * @param gui
	 */
	public ClearAllMarksListener(KenKenGUI gui) {
		this.gui = gui;
	}

	/**
	 * Perform an action when the user selects a Mark
	 */
	public void actionPerformed(ActionEvent e) {
		this.markButtons = this.gui.getMainPanel().getMarkPanel().getMarkButtons();
		
		int size = markButtons.length;
		Cell cell = this.gui.getCurrentCell();
		
		if (markButtons[size - 1].isSelected())
		{
			this.gui.getHistory().perform(new ClearMarksCommand(cell));
			for(int i = 0; i < size - 1; i++)
			{
				markButtons[i].setSelected(false);
			}
		}
		this.gui.getMainPanel().getGamePanel().paintGamePanel(gui.getPuzzle());
	}
}
