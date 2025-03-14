package randoop.test;

import junit.framework.TestCase;

public class EqualsNotTransitiveTests extends TestCase {

  public void test() {
    // throw new RuntimeException();
  }

  // public void testTo1String() throws Exception {
  // Decoration bc= new EqualsHashcodeContractViolated();
  // bc.toJunitCode("v1", "v2");
  // }

  // public void testToString() throws Exception {
  // EqualsNotTransitive ens= new EqualsNotTransitive();
  // ens.toJunitCode("v1", "v2", "v3");
  // }

  // public void test1() throws Exception {
  // EqualsNotTransitive ens= new EqualsNotTransitive();
  // Decoration checker = ens.evaluate(new Object[]{"x", "y", "z"});
  // assertNull(checker);
  // }

  // public void test2() throws Exception {
  // EqualsNotTransitive ens= new EqualsNotTransitive();
  // Decoration checker = ens.evaluate(new Object[]{"x", "x", "x"});
  // assertNull(checker);
  // }

  // public void test3() throws Exception {
  // class EqualToEveryone{
  // @Override
  // public boolean equals(Object obj) {
  // return true;
  // }
  // }

  // class EqualToAnyNonString{
  // @Override
  // public boolean equals(Object obj) {
  // return ! (obj instanceof String);
  // }
  // }

  // EqualsNotTransitive ens= new EqualsNotTransitive();
  // Decoration checker = ens.evaluate(new Object[]{new EqualToAnyNonString(),
  //                                   new EqualToEveryone(), "y"});
  // assertNotNull(checker);
  // }
}
