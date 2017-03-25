package test.minimizer.testrootdir.testsubdir;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputSubDir1 {
  // Test simplification of a program within a non-default package
  @Test
  public void test1() throws Throwable {
    String s = "String";
    org.junit.Assert.assertTrue(s.equals("String"));

    s = s.replace('i', 'o');

    // Fails, should be 'Strong'
    org.junit.Assert.assertTrue(s.equals("String"));
  }
}
