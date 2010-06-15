package randoop.main;

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
      TestRunner runner = new TestRunner();
      TestResult result = runner.doRun(test, false);
      if (result.failureCount() != 1) {
        throw new RuntimeException("Expected 2 failures but got " + result.failureCount());
      }
      System.out.println("Test passed; got 2 failures.");
    }
  }

}
