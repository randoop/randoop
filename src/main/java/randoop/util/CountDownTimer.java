package randoop.util;

/** A countdown timer. */
public final class CountDownTimer {

  /** The total time spent. */
  private long totalTime;

  /** The start time. */
  private long startTime;

  /**
   * Creates a new countdown timer that lasts the given number of milliseconds.
   *
   * @param timeMillis the duration of the timer in milliseconds
   */
  private CountDownTimer(long timeMillis) {
    this.totalTime = timeMillis;
    this.startTime = System.currentTimeMillis();
  }

  /**
   * Returns true if this timer's duration has elapsed.
   *
   * @return true if this timer's duration has elapsed
   */
  public boolean reachedZero() {
    return (elapsedTime() >= totalTime);
  }

  /**
   * Returns the number of milliseconds since this timer was started.
   *
   * @return the number of milliseconds since this timer was started
   */
  public long elapsedTime() {
    return System.currentTimeMillis() - this.startTime;
  }

  /**
   * Returns the number of milliseconds until this timer reaches zero, or 0 if it already has.
   *
   * @return the number of milliseconds until this timer reaches zero, or 0 if it already has
   */
  public long remainingTime() {
    long remainingTime = totalTime - elapsedTime();
    if (remainingTime < 0) {
      return 0;
    }
    return remainingTime;
  }

  /**
   * Creates and starts a countdown timer that lasts the given number of milliseconds.
   *
   * @param totalTimeMillis the duration of the timer in milliseconds
   * @return a new countdown timer, which has already started
   */
  public static CountDownTimer createAndStart(long totalTimeMillis) {
    return new CountDownTimer(totalTimeMillis);
  }

  @Override
  public String toString() {
    return "elapsed: " + elapsedTime() + ", remaining: " + remainingTime();
  }
}
