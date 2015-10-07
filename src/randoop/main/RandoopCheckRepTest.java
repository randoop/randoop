package randoop.main;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class RandoopCheckRepTest {

  public static void main(String[] args)
    throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      JUnitCore junit = new JUnitCore();
      junit.addListener(new TextListener(new PrintStream(baos)));
      Class<?> testClass = Class.forName("CheckRepTest");
      Result testResult = junit.run(testClass);
      
      if (testResult.getFailureCount() == 2) {
        // passed.
      } else {
        StringBuilder b = new StringBuilder("\n\nRANDOOP TEST FAILED: EXPECTED GENERATED UNIT TESTS TO CAUSE 2 FAILURES");
        b.append(baos.toString());
        throw new RuntimeException(b.toString());
      }
    }
  }

}
