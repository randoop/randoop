package randoop.reflection.visibilitytest;

@SuppressWarnings({"UnusedMethod", "UnusedNestedClass", "UnusedVariable"})
class PackagePrivateClass {
  public PackagePrivateClass(int i) {}

  PackagePrivateClass(double d) {}

  protected PackagePrivateClass(boolean b) {}

  private PackagePrivateClass(String s) {}

  public String oneMethod(String s) {
    return s;
  }

  int twoMethod(int i) {
    return i;
  }

  protected double threeMethod(double d) {
    return d;
  }

  private boolean fourMethod(boolean b) {
    return b;
  }
  //
  public int oneField;
  int twoField;
  protected int threeField;
  private int fourField;
  //
  public enum oneEnum {
    ONE,
    TWO
  };

  enum twoEnum {
    THREE,
    FOUR
  };

  protected enum threeEnum {
    FIVE,
    SIX
  };

  private enum fourEnum {
    SEVEN,
    EIGHT
  };
}
