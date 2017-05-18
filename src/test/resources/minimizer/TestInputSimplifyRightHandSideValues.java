import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputSimplifyRightHandSideValues {
  // Test simplification of right hand side values.
  @Test
  public void test1() throws Throwable {
    int a = 1;
    int b = 2;
    int c = a + b;

    org.junit.Assert.assertTrue(c == 3);

    int d = 2 * c;

    org.junit.Assert.assertTrue(d == 6);
    org.junit.Assert.assertTrue(d == 7);
  }
}
