package randoop.test;

import junit.framework.TestCase;
import org.junit.Test;

/** This test just prints the JDK that is being used during testing. */
public class PrintJdkTest extends TestCase {
  @Test
  public void testPrintJdk() {
    String msg = "java.version = " + System.getProperty("java.version");
    System.out.println(msg);
    System.err.println(msg);
  }
}
