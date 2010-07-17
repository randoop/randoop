package lpf.gui.panel;

import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JPanel;

import lpf.gui.KenKenGUI;

/**
 * This Panel is the main Content of the frame. It includes GamePanel, ValuePanel, MarkPanel, TimerPanel
 * @author Nam Do
 * 
 */
public class MainPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7820976758501817323L;

	private KenKenGUI gui;

	private GamePanel gamePanel;

	private ValuePanel valuePanel;

	private MarkPanel markPanel;

	private TimerPanel timerPanel;

	/**
	 * Constructor for the MainPanel.
	 * @param gui
	 */
	public MainPanel(KenKenGUI gui) {
		super();
		this.gui = gui;
		this.setSize(new Dimension(300, 300));
	}

	/**
	 * Create a blank Panel
	 */
	public void buildMainPanel() {
		gamePanel = new GamePanel(gui);
		valuePanel = new ValuePanel(gui);
		markPanel = new MarkPanel(gui);
		timerPanel = new TimerPanel();

		GroupLayout layout = new GroupLayout(this.gui.getContentPane());
		this.gui.getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addGap(10, 10, 10).addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.LEADING).addComponent(
								gamePanel, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE).addComponent(
								markPanel, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)).addGap(10, 10, 10)
						.addGroup(
								layout.createParallelGroup(
										GroupLayout.Alignment.LEADING)
										.addComponent(timerPanel,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(valuePanel,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE))
						.addContainerGap(10, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(
				GroupLayout.Alignment.LEADING).addGroup(
				GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup().addGap(10, 10, 10).addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.TRAILING).addComponent(
								valuePanel, GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(gamePanel,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)).addGap(10, 10, 10)
						.addGroup(
								layout.createParallelGroup(
										GroupLayout.Alignment.LEADING)
										.addComponent(markPanel,
												GroupLayout.Alignment.TRAILING,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(timerPanel,
												GroupLayout.Alignment.TRAILING,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE))
						.addContainerGap(10, Short.MAX_VALUE)));

	}

	/**
	 * Get GamePanel
	 * @return
	 */
	public GamePanel getGamePanel() {
		return gamePanel;
	}

	/**
	 * Get ValuePanel
	 * @return
	 */
	public ValuePanel getValuePanel() {
		return valuePanel;
	}

	/**
	 * Get MarkPanel
	 * @return
	 */
	public MarkPanel getMarkPanel() {
		return markPanel;
	}

	/**
	 * Get TimerPanel
	 * @return
	 */
	public TimerPanel getTimerPanel() {
		return timerPanel;
	}

	/**
	 * Stop the game. Disable the interaction with the users when they won.
	 */
	public void stopGame() {
		this.gamePanel.setEnabled(false);
		this.valuePanel.stopGame();
		this.markPanel.stopGame();
		this.timerPanel.stopTimer();
	}
}
