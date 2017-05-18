import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestWithNonCompilingTest {
  // Test suite with non-compiling test.
  @Test
  public void test1() throws Throwable {
    // Compilation error here.
    int a = 2.0;
    org.junit.Assert.assertTrue(a == 2);
  }
}
