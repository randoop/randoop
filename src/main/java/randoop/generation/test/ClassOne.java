package randoop.generation.test;

public class ClassOne {
  public int a = 2;
  public String b = "b";
  public int c = 10000;
  public int d = 10000;
  public Class<ClassTwo> e = ClassTwo.class;
  public enum EnumOne {
    ENUM_ONE,
    ENUM_TWO,
    ENUM_THREE
  }

  public ClassOne() {
    a = 2;
    //        InnerClass ic = new InnerClass();
    //        ic.setA(2);
    c = 20000;
    d = 20000;
    e = ClassTwo.class;
    System.out.println(e);
  }

  public void setA(int a) {
    this.a = a;
  }

  public void doSomething1() {
    a = 2;
    b = "c";
    System.out.println("c");
    String d = "c";
    c += 50000;
    System.out.println(d);
    Class<ClassThree> f = ClassThree.class;
    System.out.println(f);
    System.out.println(1000000000l);
    System.out.println(200000.0);
  }

  public void doSomething2() {
    a = 3;
    b = "c";
    c = 50000;
    d = 50000;
  }

  //    public static void main(String[] args) {
  //        int a = 1;
  //        long b = 1l;
  //
  ////        System.out.println(a.equals(b));
  ////
  //        Set<Object> s = new HashSet<>();
  //        s.add(a);
  //        s.add(b);
  //        System.out.println(s.size());
  ////
  ////        Object c = (int) 1;
  ////        Object d = (char) 1;
  //
  ////        System.out.println(s.contains(c));
  ////        System.out.println(s.contains(d));
  ////        Long c = 1l;
  //        Character d = '1';
  //        s.add(d);
  ////        char e = '1';
  //        System.out.println(s.contains(d));
  //        System.out.println(s.size());
  //
  //
  //    }
}

// class InnerClass {
//    public int a = 2;
//    public String b = "b";
//
//    public InnerClass() {
//        a = 10000;
//    }
//
//    public void setA(int a) {
//        this.a = a;
//    }
//
//    public void doSomething1() {
//        a = 2;
//        b = "c";
//    }
//
//    public void doSomething2() {
//        a = 3;
//        b = "c";
//    }
// }
