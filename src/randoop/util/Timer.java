package randoop.util;

/**
 * A simple timer implementation. The timer has two
 * states: running and stopped. The timer can be started
 * and stopped multiple times. A newly-constructed timer
 * is in a stopped state.
 *
 * Time is measured in milliseconds, using System.currentTimeMillis().
 *
 */
public class Timer {

  private long startTime;
  private long timeElapsed;
  private boolean running;

  /**
   * Creates a timer. Timer starts in stopped state.
   *
   */
  public Timer() {
    this.timeElapsed = 0;
    this.running = false;
  }

  /**
   * The state of the timer. True means it's running, false means it's stopped.
   */
  public boolean isRunning() {
    return this.running;
  }

  /**
   * Start counting testtime.
   *
   */
  public void startTiming() {
    if (this.running)
      throw new RuntimeException("Timer is already running.");
    this.startTime = System.currentTimeMillis();
    this.running = true;
  }

  /**
   * Stop counting testtime.
   *
   */
  public void stopTiming() {
    if (!this.running)
      throw new RuntimeException("Timer is not running.");
    this.timeElapsed += System.currentTimeMillis() - this.startTime;
    this.running = false;
  }

  /**
   * The total testtime elapsed.
   */
  public long getTimeElapsedMillis() {
    if (this.running) {
      return this.timeElapsed + System.currentTimeMillis() - this.startTime;
    }
    return this.timeElapsed;
  }

}
