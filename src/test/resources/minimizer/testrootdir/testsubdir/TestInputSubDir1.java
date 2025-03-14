package test.minimizer.testrootdir.testsubdir;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputSubDir1 {
  // Test simplification of a program within a non-default package.
  // This test input file is located in a subdirectory and non-default package.
  // Checks that the minimizer is able to find the input file and minimize it.
  @Test
  public void test1() throws Throwable {
    String s = "String";
    org.junit.Assert.assertTrue(s.equals("String"));

    s = s.replace('i', 'o');

    // Fails, should be 'Strong'
    org.junit.Assert.assertTrue(s.equals("String"));
  }
}
