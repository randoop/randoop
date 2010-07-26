package lpf.gui.panel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * @author Wang Han
 * @author Nam Do
 *
 */
public class TimerPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -6809460946509588247L;
	public Timer timer;
	private long time = 0;
	private JLabel timerlabel;
    private NumberFormat format;

    /**
     * Constructor for TimerPanel
     */
	public TimerPanel()
	{		
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setPreferredSize(new Dimension(100, 50));
		this.setVisible(true);
		
		// Set format of timer
		format = NumberFormat.getIntegerInstance();
		format.setMinimumIntegerDigits(2);
		
		// use label to display timer
		timerlabel = new JLabel();
		this.add(timerlabel);
		
		
		// set timer tick
		timer = new Timer(1000, this);
	}
	
	/**
	 * Start timing
	 */
	public void startTimer() {
		timer.start();
	}
	
	/**
	 * Stop timing
	 */
	public void stopTimer() {
		timer.stop();
	}
	
	/**
	 * Restart timing
	 */
	public void restartTimer() {
		time = 0;
		stopTimer();
		startTimer();
		timerlabel.setText(timeFormat(time));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		time++;
		timerlabel.setText(timeFormat(time));
	}
	
	/**
	 * Format the time
	 * @param time
	 * @return
	 */
	private String timeFormat(long time) {
		return format.format(time / 60) + ":" + format.format(time % 60);
	}
	
	@Override
	public String toString()
	{
		return timeFormat(time);
		
	}
}
