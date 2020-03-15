import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputWithComments {

  // Test input with multiple single line comments above the method.
  // The minimizer should not remove these comments.
  @Test
  public void test1() throws Throwable {
    int j = 2;

    // Orphan comment that should be removed if statement is removed.
    // True, j is equal to 2.
    org.junit.Assert.assertTrue(j == 2);

    // Orphan comment that belongs nowhere.

    // Set j to 3.
    j = 3; // Additional comment.

    // Orphan comment that should remain if statement is not removed.
    // Assertion fails.
    org.junit.Assert.assertTrue(j == 2);
  }

  @Test
  public void test2() throws Throwable {
    int j = 2;

    // Orphan comment that should be removed if statement is removed.
    // True, j is equal to 2.
    org.junit.Assert.assertTrue(2 == j);

    // Orphan comment that belongs nowhere.

    // Set j to 3.
    j = 3; // Additional comment.

    // Orphan comment that should remain if statement is not removed.
    // Assertion fails.
    org.junit.Assert.assertTrue(2 == j);
  }

  @Test
  public void test3() throws Throwable {
    int j = 2;

    // Orphan comment that should be removed if statement is removed.
    // True, j is equal to 2.
    org.junit.Assert.assertEquals(j, 2);

    // Orphan comment that belongs nowhere.

    // Set j to 3.
    j = 3; // Additional comment.

    // Orphan comment that should remain if statement is not removed.
    // Assertion fails.
    org.junit.Assert.assertEquals(j, 2);
  }

  @Test
  public void test4() throws Throwable {
    int j = 2;

    // Orphan comment that should be removed if statement is removed.
    // True, j is equal to 2.
    org.junit.Assert.assertEquals(2, j);

    // Orphan comment that belongs nowhere.

    // Set j to 3.
    j = 3; // Additional comment.

    // Orphan comment that should remain if statement is not removed.
    // Assertion fails.
    org.junit.Assert.assertEquals(2, j);
  }
}
