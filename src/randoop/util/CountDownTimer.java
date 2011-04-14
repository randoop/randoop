package randoop.util;

public class CountDownTimer {

  private long totalTime;
  private long startTime;

  private CountDownTimer(long timeMillis) {
    this.totalTime = timeMillis;
    this.startTime = System.currentTimeMillis();
  }

  public boolean reachedZero() {
    return (elapsedTime() >= totalTime);
  }

  public long elapsedTime() {
    return System.currentTimeMillis() - this.startTime;
  }

  public long remainingTime() {
    long remainingTime = totalTime - elapsedTime();
    if (remainingTime < 0) return 0;
    return remainingTime;
  }

  public static CountDownTimer createAndStart(long totalTimeMillis) {
    return new CountDownTimer(totalTimeMillis);
  }

  @Override
  public String toString() {
    return "elapsed:" + elapsedTime() + " remaining:" + remainingTime(); 
  }
}
