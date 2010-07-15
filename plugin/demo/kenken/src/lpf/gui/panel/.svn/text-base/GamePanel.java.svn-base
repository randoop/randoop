package lpf.gui.panel;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import lpf.controller.MouseController;
import lpf.gui.KenKenGUI;
import lpf.gui.panel.decorator.Base;
import lpf.gui.panel.decorator.CageLayer;
import lpf.gui.panel.decorator.CellLayer;
import lpf.gui.panel.decorator.GridLayer;
import lpf.model.kenken.KenKenPuzzle;

/**
 * A panel that displays a game, including grid, cage, cell
 * @author Nam Do
 *
 */
public class GamePanel extends JPanel  {

	private static final long serialVersionUID = -8529023649194986928L;
	
	/** Drawing component*/	 
	private JComponent drawing;

	/** KenKen Gui */
	private KenKenGUI gui;
	
	/**
	 * Constructor for GamePanel
	 * @param gui
	 */
	public GamePanel(KenKenGUI gui){
		super();
		this.gui = gui;		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));		
	}
	
	/**
	 * Set the drawing
	 * @param drawing
	 */
	public void setDrawer(JComponent drawing){
		this.drawing = drawing;
	}
	
	/**
	 * Paint the component
	 */
	public void paintComponent(Graphics g)
	{
		if (drawing == null)
		{
			return;
		}		
		
		this.add(drawing);		
		super.paintComponent(g);		
	}
	
	/**
	 * Paint the GamePanel from the current Puzzle
	 * @param puzzle
	 */
	public void paintGamePanel(KenKenPuzzle puzzle)
	{
		this.removeAll();
		
		int size = (puzzle.getSize() + 1) * Base.CELLSIZE;
		this.setPreferredSize(new Dimension(size, size));
		
		/**
		 * Decorator Patter
		 */
		Base bd = new Base(gui.getMainPanel().getGamePanel().getPreferredSize());			
		GridLayer grl = new GridLayer(bd, puzzle);
		CageLayer cgl = new CageLayer(grl, puzzle);
		CellLayer cll = new CellLayer(cgl, puzzle);
		
		this.setDrawer(cll);
        this.addMouseListener(new MouseController(gui));
        
        if (this.gui.getHistory().canUndo())
		{
			this.gui.getMenu().getMniUndo().setEnabled(true);
		}
		else
		{
			this.gui.getMenu().getMniUndo().setEnabled(false);			
		}
		
		if (this.gui.getHistory().canRedo())
		{
			this.gui.getMenu().getMniRedo().setEnabled(true);
		}
		else
		{
			this.gui.getMenu().getMniRedo().setEnabled(false);
		}
		this.gui.repaint();
	}
}
