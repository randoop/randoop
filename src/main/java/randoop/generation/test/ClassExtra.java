package randoop.generation.test;

public class ClassExtra {
  // int0 = 2 : 3
  // int0 = 3 : 1
  // long0 = 2l : 1
  // long0 = 1l : 3
  // string0 = "b" : 1
  // string0 = "c" : 2 ??

  public int a = 2;
  public String b = "b";
  public int e = 50000;
  public long c = 10000l;
  public long d = 10000l;
  public Class<ClassOne> c1 = ClassOne.class;

  enum Enum1 {
    AEnum,
    BEnum,
    CEnum
  };

  public ClassExtra() {
    a = 2;
    //        InnerClass ic = new InnerClass();
    //        ic.setA(2);
    c = 2l;
    d = 2l;
    c1 = ClassOne.class;
    e = 50000;
    a = 50000;
    System.out.println(Enum1.AEnum);
    System.out.println(Enum1.BEnum);
    c1 = ClassOne.class;
  }

  public void setA(int a) {
    this.a = a;
  }

  public void doSomething1() {
    a = 2;
    b = "c";
    System.out.println("c");
    String d = "c";
    System.out.println(d);
    int f = 50000;
    System.out.println(f);
  }

  public void doSomething2() {
    a = 3;
    b = "c";
  }
}
