package randoop.test;

import junit.framework.TestCase;

public class EmptyTest extends TestCase {
  @SuppressWarnings({
    "SelfEquals",
    "PMD.UselessPureMethodCall" // calls methods to see whether they throw
  })
  public void checkSpec(Object o) {
    if (o == null) throw new RuntimeException("null reference passed.");
    o.toString();
    o.hashCode();
    if (!o.equals(o)) {
      throw new RuntimeException("violated o.equals(o)==true");
    }
  }
}
