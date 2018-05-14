import java.util.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputWithWildcardImport {

  // Test input with wildcard import already included. Redundant import statements will not be
  // added by the minimizer.
  @Test
  public void test1() throws Throwable {
    java.util.List<java.lang.Integer> list = new java.util.ArrayList<java.lang.Integer>();
    list.add(1);
    list.add(2);

    org.junit.Assert.assertTrue(list.size() == 2);

    list.add(3);
    // False, should be 3.
    org.junit.Assert.assertTrue(list.size() == 2);
  }
}
