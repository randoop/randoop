package randoop.reflection.supertypetest;

/**
 * This enum implements an interface with overloaded methods named {@code alpha} with distinct
 * types. Used to test that Randoop reflection code returns both.
 */
public enum InheritedEnum implements TheSupertype {
  ONE {
    @Override
    public String alpha(Integer i) {
      return "one";
    }

    @Override
    public int alpha(String s) {
      return 1;
    }
  },
  TWO {
    @Override
    public String alpha(Integer i) {
      return "two";
    }

    @Override
    public int alpha(String s) {
      return 2;
    }
  }
}
