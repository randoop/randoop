package randoop.main;

import java.lang.reflect.InvocationTargetException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.textui.TestRunner;

public class RandoopContractsTest {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

    Class<TestCase> tstCls = (Class<TestCase>) Class.forName("BuggyTest");

    Test test = (Test) tstCls.getMethod("suite").invoke(null);

    TestRunner runner = new TestRunner();
    TestResult result = runner.doRun(test, false);

    if (result.failureCount() != 7) {
      throw new RuntimeException("Expected 7 failures but got " + result.failureCount());
    }

    System.out.println("Test passed; got 7 failures.");

  }

}
