package lpf.model;
import junit.framework.*;
import junit.textui.*;

public class KenKenTest extends TestCase {

  public static void main(String[] args) {
    TestRunner runner = new TestRunner();
    TestResult result = runner.doRun(suite(), false);
    if (! result.wasSuccessful()) {
      System.exit(1);
    }
  }

  public KenKenTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite result = new TestSuite();
    return result;
  }

}
