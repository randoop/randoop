package randoop.test;

import junit.framework.TestCase;

/* This class is a resource for ExtractJunitResultsTests.
 */
public class Test_AllFail extends TestCase{
  public Test_AllFail(){ //empty
  }
  public void testA() throws Exception {
    fail();
  }
  public void testB() throws Exception {
    fail();
  } 
}
