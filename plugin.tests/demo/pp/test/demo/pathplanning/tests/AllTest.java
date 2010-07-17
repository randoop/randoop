package demo.pathplanning.tests;
import junit.framework.*;
import junit.textui.*;

public class AllTest extends TestCase {

  public static void main(String[] args) {
    TestRunner runner = new TestRunner();
    TestResult result = runner.doRun(suite(), false);
    if (! result.wasSuccessful()) {
      System.exit(1);
    }
  }

  public AllTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite result = new TestSuite();
    result.addTest(new TestSuite(AllTest0.class));
    result.addTest(new TestSuite(AllTest1.class));
    return result;
  }

}
