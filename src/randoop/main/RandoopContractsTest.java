package randoop.main;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    TestRunner runner = new TestRunner(new PrintStream(baos));
    TestResult result = runner.doRun(test, false);

    if (result.failureCount() != 7) {
      StringBuilder b = new StringBuilder("RANDOOP TEST FAILED: EXPECTED GENERATED UNIT TESTS TO CAUSE 7 FAILURES BUT GOT " + result.failureCount());
      b.append("\n\nJUNIT OUTPUT ON RANDOOP-GENERATED TESTS:");
      b.append(baos.toString());
      throw new RuntimeException(b.toString());
    }

    System.out.println("Test passed; got 7 failures.");

  }

}
