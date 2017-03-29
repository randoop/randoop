import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputWithWrappedTypes {

  // This test input file has wrapped types. The minimizer should attempt to
  // use a zero value right hand side replacement statement.
  @Test
  public void test1() throws Throwable {
    Integer i = 31;
    org.junit.Assert.assertTrue(i == 31);

    Double d = 2.0;
    org.junit.Assert.assertTrue(d - 2.0 < 0.001);

    Float f = 10.0f;
    org.junit.Assert.assertTrue(f - 10.0f < 0.001f);

    Character c = 'c';
    org.junit.Assert.assertTrue(c == 'c');

    // Failing assertion.
    org.junit.Assert.assertTrue(d * i * f * c > 70000.0);
  }
}
