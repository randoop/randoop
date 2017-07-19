package randoop.test;

import junit.framework.TestCase;

public class EmptyTest extends TestCase {
  @SuppressWarnings("SelfEquals")
  public void checkSpec(Object o) {
    if (o == null) throw new RuntimeException("null reference passed.");
    o.toString();
    o.hashCode();
    if (o.equals(o) == false) {
      throw new RuntimeException("violated o.equals(o)==true");
    }
  }
}
