package randoop.generation;

public class Flaky {

  public Flaky() {}

  public Flaky(int i) {
    throw new NullPointerException("constructor throws NPE");
  }

  public int flaky() {
    throw new NullPointerException("method throws NPE");
  }

  public int getZero() {
    return 0;
  }

  public int flaky(Integer i) {
    if (i == null) {
      throw new NullPointerException("method throws NPE if parameter is null");
    }
    return i;
  }
}
