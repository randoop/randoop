package lpf.model.tests.core;
import junit.framework.*;
import junit.textui.*;

public class ModelTest extends TestCase {

  public static void main(String[] args) {
    TestRunner runner = new TestRunner();
    TestResult result = runner.doRun(suite(), false);
    if (! result.wasSuccessful()) {
      System.exit(1);
    }
  }

  public ModelTest(String name) {
    super(name);
  }

  public static Test suite() {
    TestSuite result = new TestSuite();
    result.addTest(new TestSuite(ModelTest0.class));
    result.addTest(new TestSuite(ModelTest1.class));
    result.addTest(new TestSuite(ModelTest2.class));
    result.addTest(new TestSuite(ModelTest3.class));
    result.addTest(new TestSuite(ModelTest4.class));
    result.addTest(new TestSuite(ModelTest5.class));
    result.addTest(new TestSuite(ModelTest6.class));
    result.addTest(new TestSuite(ModelTest7.class));
    result.addTest(new TestSuite(ModelTest8.class));
    result.addTest(new TestSuite(ModelTest9.class));
    result.addTest(new TestSuite(ModelTest10.class));
    result.addTest(new TestSuite(ModelTest11.class));
    result.addTest(new TestSuite(ModelTest12.class));
    result.addTest(new TestSuite(ModelTest13.class));
    result.addTest(new TestSuite(ModelTest14.class));
    result.addTest(new TestSuite(ModelTest15.class));
    result.addTest(new TestSuite(ModelTest16.class));
    result.addTest(new TestSuite(ModelTest17.class));
    result.addTest(new TestSuite(ModelTest18.class));
    return result;
  }

}
