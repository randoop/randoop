package examples;

@SuppressWarnings("EqualsHashCode")
public class Buggy {

  public Buggy(int i) {
    // Empty body.
  }

  // This should lead to one failing test that fails two contracts,
  // equals-reflexive and equals-to-null.
  public boolean equals(Object o) {
    if (o == null) return true;
    return false;
  }

  // This should not lead to reported failures, because
  // a failure is only a test that leads to an NPE.
  public String toString() {
    throw new RuntimeException(
        "Buggy class is test input class and somehow toString() method has been called outside of contract check");
  }
  // This should not lead to reported failures, because
  // a failure is only a test that leads to an NPE.
  public int hashCode() {
    throw new RuntimeException(
        "Buggy class is test input class and somehow hashCode() has been called outside of contract check");
  }

  public static void throwStackOverflowError() {
    throw new StackOverflowError();
  }

  public static void throwAssertionError() {
    throw new AssertionError();
  }

  /*
   * violates equal-hashcode
   *
   * equality by identity, hashcode mutates
   */
  public static class Buggy3 {

    public boolean equals(Object o) {
      if (o == this) return true;
      return false;
    }

    private static int counter = 0;

    public int hashCode() {
      return counter++;
    }
  }

  /*
   * violates not equals null, reflexive equals
   */
  public static class Buggy4 {

    private static Buggy4 one = new Buggy4();
    private static Buggy4 two = new Buggy4();

    private Buggy4() {}

    public static Buggy4 getOne() {
      return one;
    }

    public static Buggy4 getTwo() {
      return two;
    }

    public boolean equals(Object o) {
      if (this == one) return true;
      return false;
    }
  }

  /*
   *
   */
  public static class Buggy5 {

    private static Buggy5 one = new Buggy5();
    private static Buggy5 two = new Buggy5();

    private Buggy5() {}

    public static Buggy5 getOne() {
      return one;
    }

    public static Buggy5 getTwo() {
      return two;
    }

    public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof Buggy5)) return false;
      if (o == one) {
        return true;
      }
      return false;
    }
  }

  public static class BuggyEqualsTransitive {
    private static BuggyEqualsTransitive one = new BuggyEqualsTransitive();
    private static BuggyEqualsTransitive two = new BuggyEqualsTransitive();
    private static BuggyEqualsTransitive three = new BuggyEqualsTransitive();

    public static BuggyEqualsTransitive getOne() {
      return one;
    }

    public static BuggyEqualsTransitive getTwo() {
      return two;
    }

    public static BuggyEqualsTransitive getThree() {
      return three;
    }

    private BuggyEqualsTransitive() {}

    @Override
    public boolean equals(Object o) {
      // Prevent violations to lower arity contracts
      if (!(o instanceof BuggyEqualsTransitive)) {
        return false;
      }
      if (o == null) {
        return false;
      }
      if (this == one && o == two) {
        return true;
      }
      if (this == two && o == three) {
        return true;
      }
      if (this == one && o == three) {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      // No state to compare
      return 311;
    }
  }

  public static class BuggyCompareToAntiSymmetric
      implements Comparable<BuggyCompareToAntiSymmetric> {
    private static BuggyCompareToAntiSymmetric one = new BuggyCompareToAntiSymmetric();
    private static BuggyCompareToAntiSymmetric two = new BuggyCompareToAntiSymmetric();

    public static BuggyCompareToAntiSymmetric getOne() {
      return one;
    }

    public static BuggyCompareToAntiSymmetric getTwo() {
      return two;
    }

    private BuggyCompareToAntiSymmetric() {}

    @Override
    public int compareTo(BuggyCompareToAntiSymmetric o) {
      if (this == one && o == two) {
        return 1;
      } else if (this == two && o == one) {
        return 1;
      }
      return 0;
    }

    @Override
    public boolean equals(Object o) {
      // Prevent violations to lower arity contracts
      if (!(o instanceof BuggyCompareToAntiSymmetric)) {
        return false;
      }
      if (o == null) {
        return false;
      }
      return this == o;
    }

    @Override
    public int hashCode() {
      // No state to compare
      return 311;
    }
  }

  /** Test for violations to {@code o.compareTo(o) == 0} */
  public static class BuggyCompareToReflexive implements Comparable<BuggyCompareToReflexive> {
    private static BuggyCompareToReflexive one = new BuggyCompareToReflexive();

    public static BuggyCompareToReflexive getOne() {
      return one;
    }

    private BuggyCompareToReflexive() {}

    @Override
    public int compareTo(BuggyCompareToReflexive o) {
      if (this == one && o == one) {
        return -1;
      }
      return 0;
    }

    @Override
    public boolean equals(Object o) {
      // Prevent violations to lower arity contracts
      if (!(o instanceof BuggyCompareToReflexive)) {
        return false;
      }
      if (o == null) {
        return false;
      }
      return this == o;
    }

    @Override
    public int hashCode() {
      // No state to compare
      return 311;
    }
  }

  /** Test for consistency with equals: {@code x.compareTo(y) == 0} whenever {@code x.equals(y)}. */
  public static class BuggyCompareToEquals implements Comparable<BuggyCompareToEquals> {
    private static BuggyCompareToEquals one = new BuggyCompareToEquals();
    private static BuggyCompareToEquals two = new BuggyCompareToEquals();

    public static BuggyCompareToEquals getOne() {
      return one;
    }

    public static BuggyCompareToEquals getTwo() {
      return two;
    }

    private BuggyCompareToEquals() {}

    @Override
    public int compareTo(BuggyCompareToEquals o) {
      if (this == one && o == one) {
        return -1;
      } else if (this == two && o == two) {
        return -1;
      }
      return 0;
    }

    @Override
    public boolean equals(Object o) {
      // Prevent violations to lower arity contracts
      if (!(o instanceof BuggyCompareToEquals)) {
        return false;
      }
      if (o == null) {
        return false;
      }
      return this == o;
    }

    @Override
    public int hashCode() {
      // No state to compare
      return 311;
    }
  }

  public static class BuggyCompareToTransitive implements Comparable<BuggyCompareToTransitive> {
    private static BuggyCompareToTransitive one = new BuggyCompareToTransitive();
    private static BuggyCompareToTransitive two = new BuggyCompareToTransitive();
    private static BuggyCompareToTransitive three = new BuggyCompareToTransitive();

    public static BuggyCompareToTransitive getOne() {
      return one;
    }

    public static BuggyCompareToTransitive getTwo() {
      return two;
    }

    public static BuggyCompareToTransitive getThree() {
      return three;
    }

    private BuggyCompareToTransitive() {}

    @Override
    public int compareTo(BuggyCompareToTransitive o) {
      if (this == one && o == two) {
        return 1;
      } else if (this == two && o == three) {
        return 1;
      } else if (this == one && o == three) {
        return -1;
      }
      return 0;
    }

    @Override
    public boolean equals(Object o) {
      // Prevent violations to lower arity contracts
      if (!(o instanceof BuggyCompareToTransitive)) {
        return false;
      }
      if (o == null) {
        return false;
      }
      return this == o;
    }

    @Override
    public int hashCode() {
      // No state to compare
      return 311;
    }
  }

  public static class BuggyCompareToSubs implements Comparable<BuggyCompareToSubs> {
    private static BuggyCompareToSubs one = new BuggyCompareToSubs();
    private static BuggyCompareToSubs two = new BuggyCompareToSubs();
    private static BuggyCompareToSubs three = new BuggyCompareToSubs();

    public static BuggyCompareToSubs getOne() {
      return one;
    }

    public static BuggyCompareToSubs getTwo() {
      return two;
    }

    public static BuggyCompareToSubs getThree() {
      return three;
    }

    private BuggyCompareToSubs() {}

    @Override
    public int compareTo(BuggyCompareToSubs o) {
      if (this == one && o == two) {
        return 0;
      } else if (this == two && o == three) {
        return 1;
      } else if (this == one && o == three) {
        return -1;
      }
      return 0;
    }

    @Override
    public boolean equals(Object o) {
      // Prevent violations to lower arity contracts
      if (!(o instanceof BuggyCompareToSubs)) {
        return false;
      }
      if (o == null) {
        return false;
      }
      return this == o;
    }

    @Override
    public int hashCode() {
      // No state to compare
      return 311;
    }
  }

  public static class BuggyCompareToNormal implements Comparable<BuggyCompareToNormal> {
    private static BuggyCompareToNormal one = new BuggyCompareToNormal();

    public static BuggyCompareToNormal getOne() {
      return one;
    }

    private BuggyCompareToNormal() {}

    @Override
    public int compareTo(BuggyCompareToNormal o) {
      if (this == one) {
        throw new RuntimeException("compareTo purposely fails here");
      }
      return 0;
    }

    @Override
    public boolean equals(Object o) {
      // Prevent violations to lower arity contracts
      if (!(o instanceof BuggyCompareToNormal)) {
        return false;
      }
      if (o == null) {
        return false;
      }
      return this == o;
    }

    @Override
    public int hashCode() {
      // No state to compare
      return 311;
    }
  }
}
