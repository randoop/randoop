import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInputImportsFromSamePackage {

  /* This test input uses ClassA belonging to package dir_a and ClassB belonging
  to package dir_a. The minimizer should simplify the fully-qualified type name
  for instances of either types.
   */

  @Test
  public void test1() throws Throwable {
    test.minimizer.dir_a.ClassA aObject = new test.minimizer.dir_a.ClassA();
    test.minimizer.dir_a.ClassB bObject = new test.minimizer.dir_a.ClassB();

    aObject.setId(1);
    bObject.setId(2);

    org.junit.Assert.assertFalse(aObject.getId() == bObject.getId());

    aObject.setId(100);
    bObject.setId(100);
    // Fails, should be true
    org.junit.Assert.assertFalse(aObject.getId() == bObject.getId());
  }
}
