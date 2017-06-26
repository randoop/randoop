import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputWithMulitpleTestCases {
  // Test simplification of programs with multiple methods.
  @Test
  public void test1() throws Throwable {
    int a = 1;
    org.junit.Assert.assertTrue(a == 1);

    int b = a + 1;
    int c = a + b;
    int d = c++;

    org.junit.Assert.assertTrue(b == 2);
    org.junit.Assert.assertTrue(c == 3);
    org.junit.Assert.assertTrue(d == 3);
  }

  @Test
  public void test2() throws Throwable {
    String s = "Hello";
    org.junit.Assert.assertEquals(s, "Hello");

    s += " World";
    org.junit.Assert.assertEquals(s, "Hello World");

    String t = s.replace('o', 'y');
    org.junit.Assert.assertEquals(t, "Hello World");
  }
}
