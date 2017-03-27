import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputWithWhileLoop {
  // Test simplification of programs with while loops.
  @Test
  public void test1() throws Throwable {
    int a = 10;
    int x = 0;
    while (x < a) {
      x++;
    }
    org.junit.Assert.assertTrue(x == 0);

    int b = 3;
    while (b > 0) {
      b--;
    }
    org.junit.Assert.assertTrue(b == 0);
  }
}
