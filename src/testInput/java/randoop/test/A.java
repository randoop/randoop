package randoop.test;

public class A {

  public int i;
  public B b;

  public A(int i) {
    this.i = i;
  }

  public A() {}

  public A(B b) {
    this.b = b;
  }

  public A a1() {
    return new A();
  }

  public A a1(A a) {
    this.i = this.i * 10 + 1;
    return new A(a.i + 1);
  }

  public B a2(A a) {
    return new B();
  }

  public int a3(A a, B b) {
    return 1;
  }

  //overloaded function a3
  public int a3(B b) {
    return 3;
  }

  public B a4(A a, B b, int j) {
    return b;
  }

  public int a5(int i, int j) {
    return 1;
  }

  public int a6() {
    return 0;
  }
}
