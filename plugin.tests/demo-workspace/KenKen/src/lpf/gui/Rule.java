package lpf.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.GroupLayout.Alignment;

/**
 * 
 * @author Nam Do
 * @author Wang Han
 *
 */
public class Rule extends JDialog {
	
	private static final long serialVersionUID = 219268392164898665L;
	
	private KenKenGUI gui = null;

	/**
	 * Constructor for Rule
	 * @param gui
	 */
	public Rule(KenKenGUI gui){
		super(gui, true);
		this.gui = gui;
		initialize();
	}
	
	/**
	 * Display Rule panel
	 */
	private void initialize(){
		Color color = new java.awt.Color(240, 240, 240);
		
		this.setLocation(gui.getLocation().x + 100, gui.getLocation().y + 100);
		this.setSize(500, 250);
		this.setResizable(false);
		this.setForeground(java.awt.Color.lightGray);
		
		JPanel panel = new JPanel();
		panel.setBackground(color);
		
		// Create Button
		JButton btn = new JButton("OK");
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Rule.this.dispose();
			}
		});
		
		// Create TextArea
		JTextArea txtRule = new JTextArea();  

		txtRule.setBackground(color);		
		txtRule.setEditable(false);
		txtRule.setLineWrap(true);
		txtRule.setFont(new Font("Tahoma", 0, 13));
		txtRule.setText("" +
				"1) Choose a grid size and grade.\n" +
				"2) Fill in the numbers from 1 to grid size\n" +
				"3) Do not repeat a number in any row or column.\n" +
				"4) The numbers in each heavily outlined set of squares, called cages must combine (in any order) to produce the target number in the top corner using the mathematical operation indicated.\n" +
				"5) Cages with just one square should be filled in with the target number in the top corner\n" +
				"6) A number can be repeated within a cage as long as it is not in the same row or column");
		txtRule.setWrapStyleWord(true);
		txtRule.setBackground(color);
		
		// Create bottom Panel
		JPanel btmpanel = new JPanel();
		btmpanel.setBackground(color);
		
        panel.add(txtRule);
        panel.add(btn);
        panel.add(btmpanel);
        
        // Configure Layout
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        
        layout.setHorizontalGroup(
        	layout.createSequentialGroup()
        		.addGroup(layout.createParallelGroup(Alignment.CENTER)
        				.addComponent(txtRule)
        				.addComponent(btn)
                		.addComponent(btmpanel))
        		);
        
        layout.setVerticalGroup(
        	layout.createSequentialGroup()
        		.addComponent(txtRule)
        		.addComponent(btn)
        		.addComponent(btmpanel));
        
        this.add(panel);
		this.setVisible(true);
	}
}
