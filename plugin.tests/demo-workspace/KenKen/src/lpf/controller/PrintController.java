package lpf.controller;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import lpf.gui.KenKenGUI;

/**
 * Listener to handle when the user clicks Print menu item
 * @author Wang Han
 * 
 */
public class PrintController implements ActionListener, Printable  {
	
	/** KenKen Gui */
	private KenKenGUI gui;	

	/**
	 * Constructor for PrintController
	 * @param gui
	 */
	public PrintController(KenKenGUI gui) {
		this.gui = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		 PrinterJob job = PrinterJob.getPrinterJob();
         job.setPrintable(this);
         boolean ok = job.printDialog();
         if (ok) {
             try {
                  job.print();
             } catch (PrinterException ex) {
              /* The job did not successfully complete */
             }
         }
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D)graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        /* Now print the gamePanel and its visible contents */
        Color previousColor = gui.getMainPanel().getGamePanel().getBackground();
        Color printColor = new Color(255, 255, 255);
        gui.getMainPanel().getGamePanel().setBackground(printColor);
        gui.getMainPanel().getGamePanel().print(graphics);
        gui.getMainPanel().getGamePanel().setBackground(previousColor);

        return PAGE_EXISTS;

	}
}
