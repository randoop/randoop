import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputWithPassingAssertionValue {
  @Test
  public void test1() throws Throwable {
    int a = 20;
    int a_squared = a * a;

    // Passing assertion, a equals 400
    org.junit.Assert.assertTrue(a_squared == 400);

    int incorrect_double_a_squared = incorrectDouble(a_squared);
    org.junit.Assert.assertTrue(a_squared * 2 >= incorrect_double_a_squared);
  }

  public static int incorrectDouble(int a) {
    if (a == 400) {
      return a * 4;
    } else {
      return a * 2;
    }
  }
}
