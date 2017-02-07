package randoop.test;

import junit.framework.TestCase;

/* This class is a resource for ExtractJunitResultsTests.
 */
public class Test_SomePass extends TestCase {
  public Test_SomePass() { //empty
  }

  public void testA() throws Exception { //empty
  }

  public void testB() throws Exception {
    fail();
  }
}
