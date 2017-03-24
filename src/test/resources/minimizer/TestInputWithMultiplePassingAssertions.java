import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputWithMultiplePassingAssertions {

  // Test case with runtime exception.
  @Test
  public void test1() throws Throwable {
    int i = 7;

    // Passing assertions.
    org.junit.Assert.assertTrue(i == i);
    org.junit.Assert.assertTrue(i == 7);
    org.junit.Assert.assertTrue(i == i != (i != i));
    org.junit.Assert.assertTrue(i == i == (i == i));
    org.junit.Assert.assertTrue(7 == 7);
    org.junit.Assert.assertTrue(i < 8);
    org.junit.Assert.assertTrue(i != 8);
    org.junit.Assert.assertTrue(i * 8 > 1);

    // Failing assertion.
    org.junit.Assert.assertTrue(i == 8);
  }
}
