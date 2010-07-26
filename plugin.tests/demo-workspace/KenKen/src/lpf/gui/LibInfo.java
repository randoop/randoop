package lpf.gui;

import java.awt.Color;
import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lpf.model.kenken.preferences.Difficulty;

/**
 * 
 * @author Nam Do
 * @author Wang Han
 * 
 */
public class LibInfo extends JDialog {
	private static final long serialVersionUID = 219268392164898665L;
	
	/** KenKen Gui */
	private KenKenGUI gui = null;

	/**
	 * Constructor for LibInfo
	 * @param gui
	 */
	public LibInfo(KenKenGUI gui){
		super(gui, true);
		this.gui = gui;
		initialize();
	}
	
	/**
	 * Draw the Library Info panel
	 */
	private void initialize(){
		String libPath = "";
		int numEasyNm = 0;
		int numMediumNm = 0;
		int numHardNm = 0;
		int num[] = new int[9];
		int total = 0;
		
		Color color = new java.awt.Color(240, 240, 240);
		
		this.setLocation(gui.getLocation().x + 50, gui.getLocation().y + 100);
		this.setSize(600, 300);
		this.setResizable(false);
		this.setForeground(java.awt.Color.lightGray);
		
		JPanel panel = new JPanel();
		panel.setBackground(color);
		
		if (gui.getPuzzleLibrary() != null) {
			libPath = gui.getPuzzleLibrary().getPath();
			numEasyNm =  gui.getPuzzleLibrary().numPuzzlesOfDifficulty(Difficulty.EASY);
			numMediumNm =  gui.getPuzzleLibrary().numPuzzlesOfDifficulty(Difficulty.MEDIUM);
			numHardNm =  gui.getPuzzleLibrary().numPuzzlesOfDifficulty(Difficulty.HARD);
			
			for(int i = 0; i < 9; i++) {
				num[i] = gui.getPuzzleLibrary().numPuzzlesOfSize(i + 1);
			}
			
			total = gui.getPuzzleLibrary().totalPuzzles();
		}
		
		JLabel labelLeft = new JLabel("        ");
		JLabel label1;

		if (gui.getPuzzleLibrary() != null) {
			label1  = new JLabel(
					"<html><table><tr><td><b>Library Path:</b>&nbsp;&nbsp;&nbsp;" +
									libPath + "</td></tr></table>" + 
					"<table><tr></tr></table>" + 				
					"<table><tr><td><b>Number of Available Games:</b></td><td></td><td></td></tr>" + 
					"<tr><td><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
								"Easy:</b>&nbsp;&nbsp;&nbsp;" + 	
									numEasyNm + "</td><td></td><td align=left>" +
									"<b>3x3:</b>&nbsp;&nbsp;" + 
										num[2] + "</td><td><td align=left>" + 
											"<b>7x7:</b>&nbsp;&nbsp;" + 
												num[6] + "</td></tr>" + 
					"<tr><td><b>Intermediate:</b>&nbsp;&nbsp;&nbsp;" +
									numMediumNm + "</td><td></td><td align=left>" + 
										"<b>4x4:</b>&nbsp;&nbsp;" + 
											num[3] + "</td><td><td align=left>" + 
												"<b>8x8:</b>&nbsp;&nbsp;" + 
													num[7] + "</td></tr>" +
					"<tr><td><b>&nbsp;&nbsp;&nbsp;&nbsp;Advanced:</b>&nbsp;&nbsp;&nbsp;" +
									numHardNm + "</td><td></td><td align=left>" +
										"<b>5x5:</b>&nbsp;&nbsp;" + 
											num[4] + "</td><td><td align=left>" + 
												"<b>9x9:</b>&nbsp;&nbsp;" + 
													num[8] + "</td></tr>" + 
					"<tr><td>&nbsp;&nbsp;&nbsp;</td><td></td><td align=left>" +
										"<b>6x6:</b>&nbsp;&nbsp;" + 
											num[5] + "</td></tr>" + 
					"<tr></tr>" +
					"<tr><td><b>&nbsp;Total Games:</b>&nbsp;&nbsp;&nbsp;" + 
									total + "</td></tr></table></html>");
		} else {
			label1 = new JLabel("<html><tr><td><b>No Library was loaded!</b></td></tr></html>");
		}
		
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
