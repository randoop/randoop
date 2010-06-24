package randoop.main;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.textui.TestRunner;

public class RandoopCheckRepTest {

  @SuppressWarnings("unchecked")
    public static void main(String[] args)
    throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

    {
      Class<TestCase> tstCls = (Class<TestCase>) Class.forName("CheckRepTest");
      Test test = (Test) tstCls.getMethod("suite").invoke(null);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      TestRunner runner = new TestRunner(new PrintStream(baos));
      TestResult result = runner.doRun(test, false);
      if (result.failureCount() == 1 && result.errorCount() == 1) {
        // passed.
      } else {
        StringBuilder b = new StringBuilder("\n\nRANDOOP TEST FAILED: EXPECTED GENERATED UNIT TESTS TO CAUSE 1 FAILURE and 1 ERROR");
        b.append(baos.toString());
        throw new RuntimeException(b.toString());
      }
    }
  }

}
