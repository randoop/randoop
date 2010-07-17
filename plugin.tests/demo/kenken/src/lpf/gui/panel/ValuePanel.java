package lpf.gui.panel;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import lpf.controller.ValueListener;
import lpf.gui.KenKenGUI;
import lpf.model.core.Cell;
import lpf.model.core.Value;

/**
 * A panel to display the Value
 * @author Nam Do
 * @author Wang Han
 * 
 */
public class ValuePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1874217230275024572L;
	
	private KenKenGUI gui;

	private JRadioButton[] valueButtons;
	
	private int size;
	
	/**
	 * Constructor for ValuePanel
	 * @param gui
	 */
	public ValuePanel(KenKenGUI gui)
	{
		this.gui = gui;
		this.size = this.gui.getPuzzle().getSize();		
		this.initialize();
	}
	
	/**
	 * Initialize the Panel with a list of radio buttons
	 */
	private void initialize() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setPreferredSize(new Dimension(100, this.gui.getMainPanel().getGamePanel().getPreferredSize().height));
		this.setVisible(false);
		
		ButtonGroup group = new ButtonGroup();
		
		valueButtons = new JRadioButton[size + 1];
		
		int i;
		String value;
		
		for (i = 0; i < size; i++)
		{
			value = "" + (i + 1);
			valueButtons[i] = new JRadioButton(value);			
			valueButtons[i].setActionCommand(value);
			valueButtons[i].addActionListener(new ValueListener(gui, false));			
			group.add(valueButtons[i]);
			this.add(valueButtons[i]);
		}
		value = "Clear";
		valueButtons[i] = new JRadioButton(value);
		valueButtons[i].setActionCommand(value);
		valueButtons[i].addActionListener(new ValueListener(gui, true));		
		group.add(valueButtons[i]);
		this.add(valueButtons[i]);
	}

	/**
	 * Update the panel based on the current selected cell
	 * @param cell
	 */
	public void updateCell(Cell cell){
		if (cell == null)
		{	
			this.setVisible(false);
			return;
		}
		this.setBorder(BorderFactory.createTitledBorder(cell.loc.toString()));
		this.setVisible(true);
		
		int i;
		Value digit = cell.getDigit();
		
		for (i = 0; i < size; i++)
		{
			if ((digit != null) && (digit.equals(new Value(valueButtons[i].getText().charAt(0)))))			
			{
				valueButtons[i].setSelected(true);
			}
		}
		
		if (digit == null)
		{
			valueButtons[i].setSelected(true);
		}
	}
	
	/**
	 * Disable the panel to block interactions with user
	 */
	public void stopGame()
	{
		for (int i = 0; i < valueButtons.length; i++) {
			this.valueButtons[i].setEnabled(false);
		}
	}
	
}
