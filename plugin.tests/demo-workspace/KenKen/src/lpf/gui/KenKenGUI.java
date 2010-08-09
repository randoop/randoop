package lpf.gui;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;

import lpf.commands.CommandHistory;
import lpf.gui.panel.MainPanel;
import lpf.model.core.Cell;
import lpf.model.kenken.KenKenPuzzle;
import lpf.model.kenken.fileManagement.FileLoader;
import lpf.model.kenken.fileManagement.PuzzleLibrary;
import lpf.model.kenken.preferences.KenKenPreference;

/**
 * 
 * @author Nam Do
 * 
 */
public class KenKenGUI extends JFrame {

	private static final long serialVersionUID = 6077699159694956779L;

	private Menu menu;

	private MainPanel mainPanel;

	private boolean playing;

	private PuzzleLibrary puzzleLibrary;

	private FileLoader fileLoader;

	private KenKenPreference preference;

	private KenKenPuzzle puzzle;

	private CommandHistory history;
	
	private Cell currentCell;

	/**
	 * Constructor for KenKenGUI
	 */
	public KenKenGUI() {
		super();
		initialize();
	}

	/**
	 * Load the store Preference and create a new game based on the Preference
	 * If there is no stored Preference, display a blank panel
	 */
	private void initialize() {
		initializeGUI();

		preference = new KenKenPreference();
		String file = preference.getPuzzleLibraryLocation();
		if ((file != null) && (file != "")) {
			this.puzzleLibrary = new PuzzleLibrary(new File(file));
			this.getMenu().getMniPref().setEnabled(true);
			this.getMenu().getMniNew().setEnabled(true);

			FileLoader fl = this.getPuzzleLibrary().randomPuzzleLoader(
					this.getPreference());
			KenKenPuzzle puzzle = fl.getKenKenPuzzle();

			if (puzzle != null) {
				this.buildNewGame(puzzle);
			}
		}
		else
		{
			this.setSize(new Dimension(300, 300));
		}
	}

	/**
	 * Display a blank panel
	 */
	private void initializeGUI() {
		this.setResizable(true);
		this.setTitle("KenKen");
		this.setResizable(false);
		this.menu = new Menu(this);
		this.setJMenuBar(menu.getMnbMain());
		this.mainPanel = new MainPanel(this);
		this.setContentPane(mainPanel);
		this.pack();
	}

	/**
	 * Build a new game from a puzzle
	 * @param puzzle
	 */
	public void buildNewGame(KenKenPuzzle puzzle) {
		this.getContentPane().removeAll();
		this.setPuzzle(puzzle);
		this.getPuzzle().clearIdentifiedIncorrectCells();
		this.getMenu().getMniRestart().setEnabled(true);
		this.getMenu().getMniTimer().setEnabled(true);
		this.getMenu().getMniHint().setEnabled(true);
		this.getMenu().getMniPrint().setEnabled(true);
		this.initiateCommandHistory();
		this.mainPanel.buildMainPanel();
		this.getMainPanel().getGamePanel().paintGamePanel(puzzle);
		this.getMainPanel().getTimerPanel().restartTimer();
		this.setSize(this.getMainPanel().getPreferredSize());
		this.setPlaying(true);
		this.pack();
	}

	/**
	 * Get Menu
	 * @return
	 */
	public Menu getMenu() {
		return menu;
	}

	/**
	 * Get main panel
	 * @return
	 */
	public MainPanel getMainPanel() {
		return mainPanel;
	}

	/**
	 * Set a new puzzle library
	 * @param lib
	 */
	public void setPuzzleLibrary(PuzzleLibrary lib) {
		this.puzzleLibrary = lib;
	}

	/** 
	 * Get puzzle library
	 * @return
	 */
	public PuzzleLibrary getPuzzleLibrary() {
		return puzzleLibrary;
	}

	/**
	 * Get file loader
	 * @return
	 */
	public FileLoader getFileLoader() {
		return fileLoader;
	}

	/**
	 * Set a new file loader
	 * @param fileLoader
	 */
	public void setFileLoader(FileLoader fileLoader) {
		this.fileLoader = fileLoader;
	}

	/**
	 * Get Preference
	 * @return
	 */
	public KenKenPreference getPreference() {
		return preference;
	}

	/**
	 * Set Preference
	 * @param preference
	 */
	public void setPreference(KenKenPreference preference) {
		this.preference = preference;
	}

	/**
	 * Get current puzzle
	 * @return
	 */
	public KenKenPuzzle getPuzzle() {
		return puzzle;
	}

	/**
	 * Set new puzzle
	 * @param puzzle
	 */
	public void setPuzzle(KenKenPuzzle puzzle) {
		this.puzzle = puzzle;
	}

	/**
	 * Create game history
	 */
	public void initiateCommandHistory() {
		history = new CommandHistory();
	}

	/**
	 * Get game history
	 * @return
	 */
	public CommandHistory getHistory() {
		return history;
	}

	/**
	 * If the game is being played
	 * @return	true if the game is being played
	 * 			false otherwise
	 */
	public boolean isPlaying() {
		return playing;
	}

	/**
	 * 
	 * @param playing
	 */
	public void setPlaying(boolean playing) {
		this.playing = playing;
		this.getMenu().getMniGiveUp().setEnabled(playing);
		this.getMenu().getMniIdIncorrect().setEnabled(playing);
		this.getMenu().getMniRemoveIncorrect().setEnabled(playing);
		
		
		// Block child components to interact
		if (!playing) {
			this.getMainPanel().stopGame();
			this.getMenu().getMniUndo().setEnabled(false);
			this.getMenu().getMniRedo().setEnabled(false);
		}
	}

	/**
	 * Get the current cell that is being selected
	 * @return
	 */
	public Cell getCurrentCell() {
		return currentCell;
	}

	/**
	 * Set the current cell thah is being selected
	 * @param currentCell
	 */
	public void setCurrentCell(Cell currentCell) {
		this.currentCell = currentCell;
	}
}
