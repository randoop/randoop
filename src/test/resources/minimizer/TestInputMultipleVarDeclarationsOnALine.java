import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputMultipleVarDeclarationsOnALine {
  @Test
  public void test1() throws Throwable {
    // Multiple variable declarations on one line.
    int i, j, k;
    i = 1;
    j = 2;
    k = 3;

    double x = 1.0;
    double y;
    double z;
    y = 2.0;
    z = 3.0;

    float a, b = 2.0;
    float c = 3.0;
    a = 1.0;

    org.junit.Assert.assertTrue(i == x);
    org.junit.Assert.assertTrue(a == x);

    org.junit.Assert.assertTrue(j == y);
    org.junit.Assert.assertTrue(b == y);

    org.junit.Assert.assertTrue(k == z);
    org.junit.Assert.assertTrue(c == z);

    org.junit.Assert.assertTrue(a + b + c == x + y + z);
  }
}
