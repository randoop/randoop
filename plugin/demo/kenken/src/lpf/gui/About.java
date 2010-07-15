package lpf.gui;

import java.awt.Color;

import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 
 * @author Nam Do
 * @author Wang Han
 * 
 */
public class About extends JDialog {
	
	private static final long serialVersionUID = 219268392164898665L;
	
	/** KenKen Gui */
	private KenKenGUI gui = null;

	/**
	 * Constructor for About
	 * @param gui
	 */
	public About(KenKenGUI gui){
		super(gui, true);
		this.gui = gui;
		initialize();
	}
	
	/**
	 * Draw the About panel
	 */
	private void initialize(){
		Color color = new java.awt.Color(240, 240, 240);
		
		this.setLocation(gui.getLocation().x + 100, gui.getLocation().y + 100);
		this.setSize(450, 300);
		this.setResizable(false);
		this.setForeground(java.awt.Color.lightGray);
		
		JPanel panel = new JPanel();
		panel.setBackground(color);
		
		JLabel labelLeft = new JLabel("        ");

		JLabel label1  = new JLabel("<html><tr><td>KenKen" + (char)(174) + "</td></tr>" + 
				"<tr><td>Version 1.0 (Build 1000)</td></tr>" + 
				"<tr><td>Developers:</td></tr>" + 				
				"<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
				"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
				"Do, Nam &lt;namdo&gt;</td></tr>" +				 
				"<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
				"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
				"Kalauskas, Peter &lt;peter.kalauskas&gt;</td></tr>" + 
				"<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
				"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
				"Tak,&nbsp;Jatin &lt;jatint&gt;</td></tr>" + 
				"<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
				"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + 
				"Wang,&nbsp;Han &lt;wanghan&gt;</td></tr></html>" + 
				"<tr><td></td></tr>" + 
				"<tr><td></td></tr>" + 
				"<tr><td>KenKen"  + (char)(174) +  " is a registered trademark of Nextoy, LLC</td></tr>");
		
		panel.add(labelLeft);
		panel.add(label1);
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
					.addComponent(labelLeft)
					.addComponent(label1)
		);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addComponent(labelLeft)
					.addComponent(label1)
		);

		this.add(panel);
		this.setVisible(true);
	}
}
