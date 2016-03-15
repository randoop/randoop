package randoop.test;

import junit.framework.TestCase;

/* This class is a resource for ExtractDuplicatedFailingTestsTests.
 */
public class Test_SomeDuplicates extends TestCase {
  public Test_SomeDuplicates() { //empty
  }

  public void test1() throws Exception {
    Faulty1 f = new Faulty1();
    f.foo();
  }

  public void test1dup1() throws Exception { //same bug as test1
    Faulty1 f = new Faulty1();
    f.toString();
    f.foo();
  }

  public void test1dup2() throws Exception { //same bug as test1
    Faulty1 f = new Faulty1();
    f.toString();
    f.foo1();
  }

  public void test2pass() throws Exception {
    Faulty1 f = new Faulty1();
    f.bar();
  }

  public void test3() throws Exception {
    Faulty1 f = new Faulty1();
    f.baz();
  }

  public void test4failure() throws Exception {
    assertEquals(3, 4);
  }

  public void test4failureDup() throws Exception {
    assertEquals(3, 4);
  }

  static class Faulty1 {
    void foo() {
      throw new NullPointerException();
    }

    void bar() {
      //empty
    }

    void foo1() {
      foo();
    }

    void baz() {
      throw new ArrayIndexOutOfBoundsException();
    }
  }
}
