import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputWithPassingAssertionValue {

  // This test input file has a passing assertion value. The minimized test suite
  // will use the value found in the assertion as a part of a replacement statement.
  @Test
  public void test1() throws Throwable {
    int a = 20;
    int a_squared = a * a;

    // Passing assertion, a equals 400
    org.junit.Assert.assertTrue(a_squared == 400);

    int incorrect_double_a_squared = incorrectDouble(a_squared);
    org.junit.Assert.assertTrue(a_squared * 2 >= incorrect_double_a_squared);
  }

  @Test
  public void test2() throws Throwable {
    int a = 20;
    int a_squared = a * a;

    // Passing assertion, a equals 400
    org.junit.Assert.assertTrue(400 == a_squared);

    int incorrect_double_a_squared = incorrectDouble(a_squared);
    org.junit.Assert.assertTrue(a_squared * 2 >= incorrect_double_a_squared);
  }

  @Test
  public void test3() throws Throwable {
    int a = 20;
    int a_squared = a * a;

    // Passing assertion, a equals 400
    org.junit.Assert.assertEquals(a_squared, 400);

    int incorrect_double_a_squared = incorrectDouble(a_squared);
    org.junit.Assert.assertTrue(a_squared * 2 >= incorrect_double_a_squared);
  }

  @Test
  public void test4() throws Throwable {
    int a = 20;
    int a_squared = a * a;

    // Passing assertion, a equals 400
    org.junit.Assert.assertEquals(400, a_squared);

    int incorrect_double_a_squared = incorrectDouble(a_squared);
    org.junit.Assert.assertTrue(a_squared * 2 >= incorrect_double_a_squared);
  }

  @Test
  public void test5() throws Throwable {
    int j = 0;
    // Orphan comment that belongs nowhere.
    // Set j to 3.
    // Additional comment.
    j = 3;
    // Orphan comment that should remain if statement is not removed.
    // Assertion fails.
    org.junit.Assert.assertEquals("They should be equal", j, 2);
  }

  @Test
  public void test6() throws Throwable {
    int j = 0;
    // Orphan comment that belongs nowhere.
    // Set j to 3.
    // Additional comment.
    j = 3;
    // Orphan comment that should remain if statement is not removed.
    // Assertion fails.
    org.junit.Assert.assertEquals("They should be equal", 2, j);
  }

  public static int incorrectDouble(int a) {
    if (a == 400) {
      return a * 4;
    } else {
      return a * 2;
    }
  }
}
