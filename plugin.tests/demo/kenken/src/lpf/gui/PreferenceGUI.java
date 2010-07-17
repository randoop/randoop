package lpf.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;

import lpf.model.kenken.fileManagement.PuzzleLibrary;
import lpf.model.kenken.preferences.Difficulty;
import lpf.model.kenken.preferences.KenKenPreference;

/**
 * @author Wang Han
 * @author Nam Do
 *
 */
public class PreferenceGUI extends JDialog implements ActionListener, ItemListener   {

	private static final long serialVersionUID = 82963468692757940L;
	
	private KenKenGUI gui = null;
	
	private PuzzleLibrary lib = null;
	
	private String strDiffi[] = { Difficulty.EASY.name(), 
							Difficulty.MEDIUM.name(),
							Difficulty.HARD.name()};
	private JComboBox cbDifficulty;
	private JComboBox cbSize;
	
	private Difficulty selectedDiff;
	private Difficulty storedDiff;
	private Integer storedSize;
	boolean afterSelectLib;

	/**
	 * Constructor for Preference
	 * @param gui
	 * @param afterSelectLib
	 */
	public PreferenceGUI(KenKenGUI gui, boolean afterSelectLib) {
		super(gui, true);
		this.gui = gui;
		this.lib = gui.getPuzzleLibrary();
		this.afterSelectLib = afterSelectLib;
		
		// Load the stored preference
		if (gui.getPreference() != null && afterSelectLib == false) {
			storedDiff = gui.getPreference().getPreferredDifficulty();
			storedSize = gui.getPreference().getPreferredSize();
		}
		initialize();
	}

	/**
	 * Draw the Preference
	 */
	private void initialize() {
		Color color = new java.awt.Color(240, 240, 240);
		
		this.setLocation(gui.getLocation().x + 100, gui.getLocation().y + 100);
		this.setSize(400, 200);
		this.setResizable(false);
		this.setForeground(java.awt.Color.lightGray);
		
		// Panel1
		JPanel panel1 = new JPanel();
		panel1.setBackground(color);
		
		// Labels
		JLabel label1 = new JLabel("Difficulty");
		JLabel label2 = new JLabel("Size");
		panel1.add(label1);
		panel1.add(label2);
		
		// ComboBoxs
		cbDifficulty = new JComboBox();
		cbDifficulty.setEditable(false);
		cbDifficulty.addItemListener(this);
        panel1.add(cbDifficulty);
        
		cbSize = new JComboBox();
		cbSize.setEditable(false);
        panel1.add(cbSize);
		
		// Based on library information, add items into comboBox
        Set<Integer> sizeEasy = lib.availableSizesOfDifficulty(Difficulty.EASY);
        Set<Integer> sizeMedium = lib.availableSizesOfDifficulty(Difficulty.MEDIUM);
        Set<Integer> sizeHard = lib.availableSizesOfDifficulty(Difficulty.HARD);
		
		if (sizeEasy.size() != 0) {
			cbDifficulty.addItem(strDiffi[0]);
		}
		if (sizeMedium.size() != 0) {
			cbDifficulty.addItem(strDiffi[1]);
		}
		if (sizeHard.size() != 0) {
			cbDifficulty.addItem(strDiffi[2]);
		}
		
		// if not from SelectLibrary, restore the stored difficulty and size
		if(!afterSelectLib) {
			cbDifficulty.setSelectedItem(storedDiff.name());
			if (storedSize != null) {
				cbSize.setSelectedItem(storedSize + " * " + storedSize);
			} else {
				cbSize.setSelectedItem("");
			}
		}
        
        // Layout
        GroupLayout layout = new GroupLayout(panel1);
        panel1.setLayout(layout);
        
        layout.setHorizontalGroup(
        		layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.TRAILING)
        				.addComponent(label1, 0, 80, 80).addGap(100)
        				.addComponent(label2, 0, 80, 80)).addGap(60)
        			.addGroup(layout.createParallelGroup(Alignment.TRAILING)
        				.addComponent(cbDifficulty, 0, 100, 100)
        				.addComponent(cbSize, 0, 100, 100))
        	);
        
        layout.setVerticalGroup(
        		layout.createSequentialGroup()
        			.addGroup(layout.createParallelGroup(Alignment.CENTER)
        				.addComponent(label1, 0, 20, 20).addGap(50)
        				.addComponent(cbDifficulty, 0, 20, 20)).addGap(10)
        			.addGroup(layout.createParallelGroup(Alignment.CENTER)
        				.addComponent(label2, 0, 20, 20)
        				.addComponent(cbSize, 0, 20, 20))
        	);

        // Panel2
		JPanel panel2 = new JPanel();
		panel2.setBackground(color);
		
		// Buttons
		JButton btnOK = new JButton("OK");
		JButton btnCancel = new JButton("Cancel");
		btnOK.addActionListener(this);
		btnCancel.addActionListener(this);
		
		panel2.add(btnOK, BorderLayout.CENTER);
		panel2.add(btnCancel, BorderLayout.CENTER);
		
		// PanelBlank
		JPanel panelBlank = new JPanel();
		panelBlank.setBackground(color);
		
		this.add(panel1, BorderLayout.PAGE_START);
		this.add(panelBlank, BorderLayout.CENTER);
		this.add(panel2, BorderLayout.PAGE_END);
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("OK")) {
			KenKenPreference preference = new KenKenPreference();
			preference.setPuzzleLibraryLocation(gui.getPuzzleLibrary().getPath());
			preference.setPreferredDifficulty(selectedDiff);
			if (!cbSize.getSelectedItem().toString().equals("")) {
				preference.setPreferredSize(new Integer(cbSize.getSelectedItem().toString().charAt(0)) - '0');
			} else {
				preference.setPreferredSize(null);
			}
			this.gui.setPreference(preference);
		}
		PreferenceGUI.this.dispose();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		String strDifficulty = (String)cbDifficulty.getSelectedItem();
		Difficulty difficulty;
		Set<Integer> sizeSet;
		
		if(strDifficulty.equals(strDiffi[0])) { 
			difficulty = Difficulty.EASY;
		}
		else if(strDifficulty.equals(strDiffi[1])) { 
			difficulty = Difficulty.MEDIUM; 
		}
		else { 
			difficulty = Difficulty.HARD; 
		}
		selectedDiff = difficulty;
		
		sizeSet = lib.availableSizesOfDifficulty(difficulty);
		cbSize.removeAllItems();
		cbSize.addItem("");
		for(int i = 1; i <= 9; i++) {
			if(sizeSet.contains(i)) {
				cbSize.addItem(i + " * " + i);
			}
		}
	}
}