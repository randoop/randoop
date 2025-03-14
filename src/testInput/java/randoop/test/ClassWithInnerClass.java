package randoop.test;

/**
 * Input class
 * Inspired by a bug report from Martin Schaef
 */
public class ClassWithInnerClass {
  private int anInt;

  public ClassWithInnerClass(int anInt) {
    this.anInt = anInt;
  }

  public class A {
    public A(String s, int i) {
      this.s = s;
      this.i = i;
    }

    public void zee(String s) {
      this.s = s;
    }

    public String s;
    public int i;
  }

  public void foo(A a) {
    A b = a;
    a.i = 7;
    bar(b);
  }

  private void bar(A a) {
    a.toString();
  }

}
