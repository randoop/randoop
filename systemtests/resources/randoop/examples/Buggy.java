package examples;

public class Buggy {

  public Buggy(int i) {
    // Empty body.
  }

  // This should lead to one failing test that fails two contracts,
  // equals-reflexive and equals-to-null.
  public boolean equals(Object o) {
    if (o == null)
      return true;
    return false;
  }

  // This should not lead to reported failures, because
  // a failure is only a test that leads to an NPE.
  public String toString() {
    throw new RuntimeException("oops!");
  }
  // This should not lead to reported failures, because
  // a failure is only a test that leads to an NPE.
  public int hashCode() {
    throw new RuntimeException("oops!");
  }

  public static class Buggy3 {

    public boolean equals(Object o) {
      if (o == this)
        return true;
      return false;
    }

    private static int counter = 0;

    public int hashCode() {
      return counter++;
    }
  }

  public static class Buggy4 {

    private static Buggy4 one = new Buggy4();
    private static Buggy4 two = new Buggy4();

    private Buggy4() { }

    public static Buggy4 getOne() {
      return one;
    }

    public static Buggy4 getTwo() {
      return two;
    }

    public boolean equals(Object o) {
      if (this == one)
        return true;
      return false;
    }
  }

  public static class Buggy5 {

    private static Buggy5 one = new Buggy5();
    private static Buggy5 two = new Buggy5();

    private Buggy5() { }

    public static Buggy5 getOne() {
      return one;
    }

    public static Buggy5 getTwo() {
      return two;
    }

    public boolean equals(Object o) {
      if (o == this)
        return true;
      if (!(o instanceof Buggy5))
        return false;
      if (o == one) {
        return true;
      }
      return false;
    }
  }

  public static void StackOverflowError() {
    throw new StackOverflowError();
  }

  public static void AssertionError() {
    throw new AssertionError();
  }

}
