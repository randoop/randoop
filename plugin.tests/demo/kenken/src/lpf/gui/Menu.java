package lpf.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import lpf.controller.AboutListener;
import lpf.controller.ExitListener;
import lpf.controller.GiveUpListener;
import lpf.controller.HintListener;
import lpf.controller.IdIncorrectListener;
import lpf.controller.LibInfoListener;
import lpf.controller.NewGameListener;
import lpf.controller.PreferenceListener;
import lpf.controller.PrintController;
import lpf.controller.RedoListener;
import lpf.controller.RemoveIncorrectListener;
import lpf.controller.RestartListener;
import lpf.controller.RuleListener;
import lpf.controller.SelectGameListener;
import lpf.controller.SelectLibraryListener;
import lpf.controller.TimerListener;
import lpf.controller.UndoListener;

/**
 * 
 * @author Nam Do
 *
 */
public class Menu extends JMenuBar {

	private static final long serialVersionUID = 1L;

	private KenKenGUI gui; 

	private JMenuBar mnbMain;

	/** File menu */
	private JMenu mnuFile;
	private JMenuItem mniLib;	
	private JMenuItem mniPref;
	private JMenuItem mniPrint;			
	private JMenuItem mniExit;
	private JFileChooser fcLib;

	/** Game menu */
	private JMenu mnuGame;	
	private JMenuItem mniNew;		
	private JMenuItem mniSelect;
	private JFileChooser fcGame;
	private JMenuItem mniGiveUp;
	private JMenuItem mniRestart;	

	/** Edit menu */
	private JMenu mnuEdit;
	private JMenuItem mniUndo;	
	private JMenuItem mniRedo;			
	private JMenuItem mniIdIncorrect;
	private JMenuItem mniRemoveIncorrect;

	/** View menu */
	private JMenu mnuView;	
	private JMenuItem mniHint;	
	private JCheckBoxMenuItem mniTimer;
	private JMenuItem mniLibInfo;

	/** Help menu */
	private JMenu mnuHelp;
	private JMenuItem mniRules;
	private JMenuItem mniAbout;

	/**
	 * Constructor for Menu
	 * @param gui
	 */
	public Menu(KenKenGUI gui) {
		super();
		this.gui = gui;
	}

	public JMenuBar getMnbMain() {
		if (mnbMain == null) {
			mnbMain = new JMenuBar();			
			mnbMain.add(getMnuFile());
			mnbMain.add(getMnuGame());
			mnbMain.add(getMnuEdit());
			mnbMain.add(getMnuView());
			mnbMain.add(getMnuHelp());
		}
		return mnbMain;
	}

	public JMenu getMnuFile() {
		if (mnuFile == null) {
			mnuFile = new JMenu();			
			mnuFile.setText("File");
			mnuFile.setMnemonic(KeyEvent.VK_F);
			mnuFile.add(getMniLib());
			mnuFile.addSeparator();			
			mnuFile.add(getMniPref());
			mnuFile.add(getMniPrint());
			mnuFile.addSeparator();
			mnuFile.add(getMniExit());
		}
		return mnuFile;
	}

	public JMenuItem getMniLib() {
		if (mniLib == null) {
			mniLib = new JMenuItem();
			mniLib.setText("Select Library");
			mniLib.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
			mniLib.addActionListener(new SelectLibraryListener(gui));
		}
		return mniLib;
	}

	public JMenuItem getMniPref() {
		if (mniPref == null) {
			mniPref = new JMenuItem();
			mniPref.setText("Preferences");
			mniPref.setEnabled(false);
			mniPref.addActionListener(new PreferenceListener(gui));
		}
		return mniPref;
	}

	public JMenuItem getMniPrint() {
		if (mniPrint == null) {
			mniPrint = new JMenuItem();
			mniPrint.setText("Print");
			mniPrint.setEnabled(false);
			mniPrint.setMnemonic(KeyEvent.VK_P);
			mniPrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));	
			mniPrint.addActionListener(new PrintController(gui));
		}
		return mniPrint;
	}

	public JMenuItem getMniExit() {
		if (mniExit == null) {
			mniExit = new JMenuItem();
			mniExit.setText("Exit");
			mniExit.setOpaque(false);
			mniExit.setMnemonic(KeyEvent.VK_E);
			mniExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
			mniExit.addActionListener(new ExitListener(gui));
		}
		return mniExit;
	}

	public JFileChooser getFcLib(){
		if (fcLib == null){
			fcLib = new JFileChooser();			
			fcLib.addChoosableFileFilter(new FileFilter(){

				@Override
				public boolean accept(File f) {
					if (f.isDirectory()) {
						return true;
					}
					return f.getName().toLowerCase().endsWith(".xml");					
				}

				@Override
				public String getDescription() {
					return "KenKen Game File (*.xml)";
				}
			});
			fcLib.addChoosableFileFilter(new FileFilter(){

				@Override
				public boolean accept(File f) {
					if (f.isDirectory()) {
						return true;
					}
					return f.getName().toLowerCase().endsWith(".zip");					
				}

				@Override
				public String getDescription() {
					return "KenKen Library File (*.zip)";
				}
			});
			
			fcLib.setAcceptAllFileFilterUsed(false);
			fcLib.setMultiSelectionEnabled(false);
		};
		
		return fcLib;
	}
	
	public JMenu getMnuGame() {
		if (mnuGame == null) {
			mnuGame = new JMenu();
			mnuGame.setText("Game");
			mnuGame.setMnemonic(KeyEvent.VK_G);
			mnuGame.add(getMniNew());
			mnuGame.add(getMniSelect());
			mnuGame.add(getMniGiveUp());
			mnuGame.add(getMniRestart());
		}
		return mnuGame;
	}

	public JMenuItem getMniNew() {
		if (mniNew == null) {
			mniNew = new JMenuItem();
			mniNew.setText("New");
			mniNew.setEnabled(false);
			mniNew.setMnemonic(KeyEvent.VK_N);
			mniNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
			mniNew.addActionListener(new NewGameListener(gui));
		}
		return mniNew;
	}

	public JMenuItem getMniSelect() {
		if (mniSelect == null) {
			mniSelect = new JMenuItem();
			mniSelect.setText("Select");
			mniSelect.addActionListener(new SelectGameListener(gui));
		}
		return mniSelect;
	}
	
	public JFileChooser getFcFile(){
		if (fcGame == null){
			fcGame = new JFileChooser();			
			fcGame.addChoosableFileFilter(new FileFilter(){

				@Override
				public boolean accept(File f) {
					if (f.isDirectory()) {
						return true;
					}
					return f.getName().toLowerCase().endsWith(".xml");					
				}

				@Override
				public String getDescription() {
					return "KenKen Game File (*.xml)";
				}
			});			
			
			fcGame.setAcceptAllFileFilterUsed(false);
			fcGame.setMultiSelectionEnabled(false);
		};
		
		return fcGame;
	}

	public JMenuItem getMniGiveUp() {
		if (mniGiveUp == null) {
			mniGiveUp = new JMenuItem();
			mniGiveUp.setText("Give Up");
			mniGiveUp.setEnabled(false);
			mniGiveUp.addActionListener(new GiveUpListener(gui));
		}
		return mniGiveUp;
	}

	public JMenuItem getMniRestart() {
		if (mniRestart == null) {
			mniRestart = new JMenuItem();
			mniRestart.setText("Restart");
			mniRestart.setEnabled(false);
			mniRestart.addActionListener(new RestartListener(gui));
		}
		return mniRestart;
	}

	public JMenu getMnuEdit() {
		if (mnuEdit == null) {
			mnuEdit = new JMenu();
			mnuEdit.setText("Edit");
			mnuEdit.setMnemonic(KeyEvent.VK_E);
			mnuEdit.add(getMniUndo());
			mnuEdit.add(getMniRedo());
			mnuEdit.addSeparator();
			mnuEdit.add(getMniIdIncorrect());
			mnuEdit.add(getMniRemoveIncorrect());
		}
		return mnuEdit;
	}

	public JMenuItem getMniUndo() {
		if (mniUndo == null) {
			mniUndo = new JMenuItem();
			mniUndo.setText("Undo");
			mniUndo.setEnabled(false);
			mniUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
			mniUndo.addActionListener(new UndoListener(gui));
		}
		return mniUndo;
	}
	
	public JMenuItem getMniRedo() {
		if (mniRedo == null) {
			mniRedo = new JMenuItem();
			mniRedo.setText("Redo");
			mniRedo.setEnabled(false);			
			mniRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
			mniRedo.addActionListener(new RedoListener(gui));
		}
		return mniRedo;
	}

	public JMenuItem getMniIdIncorrect() {
		if (mniIdIncorrect == null) {
			mniIdIncorrect = new JMenuItem();
			mniIdIncorrect.setText("Identify Incorrect");
			mniIdIncorrect.setEnabled(false);
			mniIdIncorrect.addActionListener(new IdIncorrectListener(gui));
		}
		return mniIdIncorrect;
	}

	public JMenuItem getMniRemoveIncorrect() {
		if (mniRemoveIncorrect == null) {
			mniRemoveIncorrect = new JMenuItem();
			mniRemoveIncorrect.setText("Remove Incorrect");
			mniRemoveIncorrect.setEnabled(false);
			mniRemoveIncorrect.addActionListener(new RemoveIncorrectListener(gui));
		}
		return mniRemoveIncorrect;
	}

	public JMenu getMnuView() {
		if (mnuView == null) {
			mnuView = new JMenu();
			mnuView.setText("View");
			mnuView.setMnemonic(KeyEvent.VK_V);
			mnuView.add(getMniHint());
			mnuView.add(getMniTimer());
			mnuView.add(getMniLibInfo());
		}
		return mnuView;
	}

	public JMenuItem getMniHint() {
		if (mniHint == null) {
			mniHint = new JMenuItem();
			mniHint.setText("Hint");
			mniHint.setEnabled(false);
			mniHint.addActionListener(new HintListener(gui));
			mniHint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));			
		}
		return mniHint;
	}

	public JCheckBoxMenuItem getMniTimer() {
		if (mniTimer == null) {
			mniTimer = new JCheckBoxMenuItem();
			mniTimer.setText("Timer");
			mniTimer.setSelected(true);
			mniTimer.setEnabled(false);
			mniTimer.addItemListener(new TimerListener(gui));
		}
		return mniTimer;
	}

	public JMenuItem getMniLibInfo() {
		if (mniLibInfo == null) {
			mniLibInfo = new JMenuItem();
			mniLibInfo.setText("Library Info");
			mniLibInfo.setOpaque(false);
			mniLibInfo.addActionListener(new LibInfoListener(gui));
		}
		return mniLibInfo;
	}

	public JMenu getMnuHelp() {
		if (mnuHelp == null) {
			mnuHelp = new JMenu();
			mnuHelp.setText("Help");
			mnuHelp.setMnemonic(KeyEvent.VK_H);
			mnuHelp.add(getMniRules());
			mnuHelp.add(getMniAbout());
		}
		return mnuHelp;
	}

	public JMenuItem getMniAbout() {
		if (mniAbout == null) {
			mniAbout = new JMenuItem();
			mniAbout.setText("About");
			mniAbout.setOpaque(false);
			mniAbout.addActionListener(new AboutListener(gui));
		}
		return mniAbout;
	}

	public JMenuItem getMniRules() {
		if (mniRules == null) {
			mniRules = new JMenuItem();
			mniRules.setText("Rules");
			mniRules.setOpaque(false);
			mniRules.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
			mniRules.addActionListener(new RuleListener(gui));
		}
		return mniRules;
	}	
}
