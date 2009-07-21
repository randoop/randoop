package cov;

import java.util.List;

/**
 * A class used to test the coverage instrumenter.
 */
public class TestClass {

  public class InnerStatic {
    public int foo2(int x) {
      if (x<0) return 1;
      else return 2;
    }
  }

  public class InnerNonStatic {
    public int foo2(int x) {
      if (x<0) return 1;
      else return 2;
    }
  }
  
  @Foobar("hi") private void bar() {

    Comparable<Integer> c = new Comparable<Integer>() {
      public int compareTo(Integer o) {
        if (o == null) throw new RuntimeException();
        return 0;
      }

    };
  }

  private List<String> l;

  public int foo(int x) {
    if (x<0)
      return 1;
    else
      return 2;
  }

  static public class Foo {
    static boolean b = false;
    static { if (b) b = true; else b = false; }
  }


  @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
  @java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
  public static @interface Foobar {
    String value();
  }
}

class TestClass2  {
  public int m(int x) {
    int y = 1;
    if (x == 0) y++; 
    return x = x>0 ? 1 : -1;
  } 
}