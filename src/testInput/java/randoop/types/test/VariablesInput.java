package randoop.types.test;

import java.util.List;

public class VariablesInput {
  public <T extends Comparable<T>> void m00(T t) {}

  public <T> void m01(T t) {}

  public <T> void m02(Comparable<? extends T> t) {}

  public <T> void m03(Comparable<? super T> t) {}

  public <U, T extends U> void m04(T t) {}

  public <T> void m05(Comparable<? extends Number> t) {}

  public <T> void m06(Comparable<? super Integer> t) {}

  public <U, W extends U, T extends W> void m07(T t) {}

  public <U extends Number, W extends U, T extends W> void m08(T t) {}

  public <U extends Comparable<U>, W extends U, T extends W> void m09(T t) {}

  public <U extends Comparable<T>, W extends U, T extends W> void m10(T t) {}

  public <O extends Comparable<? super O>> List<O> m11(O a) {
    return null;
  }

  /*
  public void check() {
    m00("s");
    m00(1);
    m01("s");
    m01(1);
    m02("s"); //also testing capture variable
    m02(1);  //also testing capture variable
    m03("s");
    m03(1);
    m04("s");
    //m05 actually look at capture variable
    //m06 actually testing capture variable
    m06(1);
    m07("s");
    m07(1);
    //m08("s");
    m08(1);
    m09("s");
    m09(1);
    m10("s");
    m10(1);
  }
  */
}
