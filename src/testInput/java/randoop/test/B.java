package randoop.test;

public class B {

  public B() {}

  public B(B b) {}

  public A b1() {
    return new A();
  }

  public B b2(A a) {
    return new B();
  }

  public int b3(B b, A a) {
    return 1;
  }
}
