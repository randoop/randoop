package lpf.gui.panel;

import java.awt.Dimension;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import lpf.controller.ClearAllMarksListener;
import lpf.controller.MarkListener;
import lpf.gui.KenKenGUI;
import lpf.model.core.Cell;
import lpf.model.core.Value;

/**
 * A panel that displays the Marks
 * @author Nam Do
 * 
 */
public class MarkPanel extends JPanel {

	private static final long serialVersionUID = -3377417788384716874L;

	private KenKenGUI gui;

	private JCheckBox[] markButtons;
	
	private int size;	

	/**
	 * Constructor for MarkPanel
	 * @param gui
	 */
	public MarkPanel(KenKenGUI gui)
	{
		this.gui = gui;
		this.size = gui.getPuzzle().getSize();
		
		this.initialize();
	}

	/**
	 * Initialize the Panel with a list of check boxes
	 */
	private void initialize() {
		this.setBorder(BorderFactory.createTitledBorder("Mark"));
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setVisible(false);
		
		markButtons = new JCheckBox[size + 1];
		
		int i;
		String value;
		
		for (i = 0; i < size; i++) {
			value = "" + (i + 1);
			markButtons[i] = new JCheckBox(value);
			markButtons[i].setActionCommand(value);
			markButtons[i].addActionListener(new MarkListener(gui));

			this.add(markButtons[i]);
		}
		
		value = "Clear";
		markButtons[i] = new JCheckBox(value);
		markButtons[i].setActionCommand(value);
		markButtons[i].addActionListener(new ClearAllMarksListener(gui));

		this.add(markButtons[i]);
	}

	/** 
	 * Update the Panel based on the current select cell
	 * @param cell
	 */
	public void updateCell(Cell cell) {
		if (cell == null)
		{	
			this.setVisible(false);
			return;
		}
		this.setPreferredSize(new Dimension(this.gui.getMainPanel().getGamePanel().getPreferredSize().width, 50));
		
		this.setVisible(true);

		Set<Value> marks = cell.getMarks();

		int i;

		for (i = 0; i < size; i++) {
			if (marks.contains((new Value((char) (i + '1'))))) {
				markButtons[i].setSelected(true);
			}
			else
			{
				markButtons[i].setSelected(false);
			}
		}
		if (marks.isEmpty()) {
			markButtons[i].setSelected(true);
		}
	}

	/**
	 * Disable the panel to block interactions with user
	 */
	public void stopGame() {
		for (int i = 0; i < markButtons.length; i++) {
			this.markButtons[i].setEnabled(false);
		}
	}

	/**
	 * Get mark buttons
	 * @return
	 */
	public JCheckBox[] getMarkButtons() {
		return markButtons;
	}
	
	
}
