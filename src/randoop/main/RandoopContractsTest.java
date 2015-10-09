package randoop.main;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class RandoopContractsTest {

  public static void main(String[] args) throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JUnitCore junit = new JUnitCore();
    junit.addListener(new TextListener(new PrintStream(baos)));
    Class<?> testClass = Class.forName("BuggyTest");
    Result testResult = junit.run(testClass);
    
    /* 
     * JUnit4 Result class does not distinguish b/w failures and exceptions (errors).
     * JUnit3 version looking for 7 failures and 2 errors
     */
    int expectedFailures = 9;


    if (testResult.getFailureCount() != expectedFailures) {
      StringBuilder b = new StringBuilder("RANDOOP TEST FAILED: EXPECTED GENERATED UNIT TESTS TO CAUSE " + expectedFailures + " FAILURES BUT GOT " + testResult.getFailureCount());
      b.append("\n\nJUNIT OUTPUT ON RANDOOP-GENERATED TESTS:");
      b.append(baos.toString());
      throw new RuntimeException(b.toString());
    }

    System.out.println("Test passed; got " + expectedFailures + " failures");

  }

}
