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
    Class<?> testClass = Class.forName("BuggyTest"); // name of generated test
    Result testResult = junit.run(testClass);
    
    /* 
     * it appears that the generation for this class will non-deterministically 
     * generate a set of tests that are in a small range of sizes. It would probably
     * be better to make sure that generated tests involve expected contracts
     */
    // TODO change test so that it looks at which contracts are violated
    int expectedFailures = 81;
    int actualFailures = testResult.getFailureCount();
    if (actualFailures != expectedFailures) {
      StringBuilder b = new StringBuilder("RANDOOP TEST FAILED: EXPECTED GENERATED UNIT TESTS TO CAUSE " + expectedFailures + " FAILURES BUT GOT " + testResult.getFailureCount());
      b.append("\n\nJUNIT OUTPUT ON RANDOOP-GENERATED TESTS:");
      b.append(baos.toString());
      throw new RuntimeException(b.toString());
    }

    System.out.println("Test passed; got " + actualFailures + " expected failures");

  }

}
