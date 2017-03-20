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
    org.junit.Assert.assertTrue(a == 400);

    int correct_double_a = a * 2;
    int incorrect_double_a = incorrectDouble(a);
    org.junit.Assert.assertTrue(incorrect_double_a >= correct_double_a);
    org.junit.Assert.assertTrue(incorrect_double_a <= correct_double_a);
  }

  /**
   * Doubles an integer and returns the result. (Incorrectly returns the result if a is 400.)
   *
   * @param a the integer to double
   * @return the value of a, doubled
   */
  public static int incorrectDouble(int a) {
    if (a == 400) {
      return a + 2;
    } else {
      return a * 2;
    }
  }
}
