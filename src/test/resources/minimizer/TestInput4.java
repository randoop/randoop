import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestInput4 {
  @Test
  public void test1() throws Throwable {
    test.minimizer.dir_a.ClassA dirAObject = new test.minimizer.dir_a.ClassA();
    test.minimizer.dir_b.ClassA dirBObject = new test.minimizer.dir_b.ClassA();

    dirAObject.setId(1);
    dirBObject.setId(2);

    org.junit.Assert.assertFalse(dirAObject.getId() == dirBObject.getId());

    dirAObject.setId(100);
    dirBObject.setId(100);
    // Fails, should be true
    org.junit.Assert.assertFalse(dirAObject.getId() == dirBObject.getId());
  }
}
