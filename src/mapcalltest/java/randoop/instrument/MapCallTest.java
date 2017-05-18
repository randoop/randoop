package randoop.instrument;

import static org.junit.Assert.assertTrue;

import com.mypackage.ClassWithCalls;
import org.junit.Test;

/** Tests the mapcall agent. */
public class MapCallTest {
  @Test
  public void manualExampleTest() {
    ClassWithCalls obj = new ClassWithCalls();
    obj.exiting();
    assertTrue("didn't call system.exit()", true);
  }
}
