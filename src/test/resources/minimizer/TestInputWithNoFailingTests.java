import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputWithNoFailingTests {

  // Test input with no failing tests. The minimizer will remove all lines in the
  // method bodies.
  @Test
  public void test1() throws Throwable {
    int a = 1;
    org.junit.Assert.assertTrue(a == 1);
  }

  @Test
  public void test2() throws Throwable {
    double d = 1.0;
    org.junit.Assert.assertTrue(d < 10.0);
  }

  @Test
  public void test3() throws Throwable {
    String s = "Hello";
    org.junit.Assert.assertTrue(s.equals(s));
  }
}
