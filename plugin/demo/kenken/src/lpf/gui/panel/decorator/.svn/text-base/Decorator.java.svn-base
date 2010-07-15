package lpf.gui.panel.decorator;

import java.awt.Graphics;
import javax.swing.JComponent;

/**
 * 
 * @author Nam Do
 *
 */
public abstract class Decorator extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -662845268889297519L;
	
	/** The next one in the chain. */
	private JComponent inner;
	
	/**
	 * When constructing a Decorator, you must pass in the next one in the chain.
	 * 
	 * @param inner    The next object along the decorator chain.
	 */
	public Decorator (JComponent inner) {
		this.inner = inner;		
	}
	
	
	/**
	 * When requested to draw state, just do it.
	 * @param Graphics object into which decorated state is drawn.
	 */
	public void paintComponent(Graphics g) {
		inner.paint(g);		
	}
	
}