package randoop.main;

public class TimeOutBehavior {
  private long sleepTime;
  private long delayTime;
  private boolean toggle;

  public TimeOutBehavior(long sleepTime) {
    this.sleepTime = sleepTime;
    this.delayTime = 0;
    this.toggle = false;
  }

  public long getTimeSlowly() throws InterruptedException {
    Thread.sleep(delayTime);
    return sleepTime;
  }

  public long getTime() {
    return sleepTime;
  }

  public void setTimeSlowly(long sleepTime) throws InterruptedException {
    Thread.sleep(delayTime);
    this.sleepTime = sleepTime;
  }

  public void setTime(long sleepTime) {
    this.sleepTime = sleepTime;
  }

  @Override
  public boolean equals(Object obj) {
    if (toggle == true) {
      setDelay();
      toggle = false;
    }
    try {
      Thread.sleep(delayTime);
    } catch (InterruptedException e) {
      System.out.println("rudely woken from my sleep: " + sleepTime);
    }
    if (!(obj instanceof TimeOutBehavior)) {
      return false;
    }
    TimeOutBehavior t = (TimeOutBehavior) obj;
    return this.sleepTime == t.sleepTime;
  }

  @Override
  public int hashCode() {
    try {
      Thread.sleep(delayTime);
    } catch (InterruptedException e) {
      System.out.println("rudely woken from my sleep: " + sleepTime);
    }
    return (int) sleepTime;
  }

  public void setDelay() {
    delayTime = sleepTime * 1000;
  }

  public void resetDelay() {
    delayTime = 0;
  }
}
