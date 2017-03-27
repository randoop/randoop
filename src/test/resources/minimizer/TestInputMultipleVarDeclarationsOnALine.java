import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputMultipleVarDeclarationsOnALine {

  // Test input with multiple variable declarations on a single line. The minimizer
  // will not attempt to simplify these statements.
  @Test
  public void test1() throws Throwable {
    // Multiple variable declarations on one line.
    int i, j = 2;
    int k;

    // True, j is equal to 2.
    org.junit.Assert.assertTrue(j == 2);

    i = 1;
    k = 3;

    // Assertion fails.
    org.junit.Assert.assertTrue(i + j == k - 1);
  }
}
