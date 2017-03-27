import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputWithVariableReassignment {

  // Test input with variable reassignment.
  @Test
  public void test1() throws Throwable {
    int j;
    int i = 3;
    j = i++;
    i = 7;

    // Assertion succeeds.
    org.junit.Assert.assertTrue(i == 7);
  }
}
