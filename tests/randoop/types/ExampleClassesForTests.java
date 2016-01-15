package randoop.types;

import java.util.List;

class A<T> implements Comparable<T>{
  @Override
  public int compareTo(T o) {
    return 0;
  }
}

class B extends A<String> {}
class C extends A<Integer> {}

class D<S,T> extends A<T> {}
class E<S,T> {}
class F<T,S> extends E<S,T>{}

class G<S> {}
class H<T> extends G<T> implements Comparable<T> {
  @Override
  public int compareTo(T o) {
    return 0;
  }
}

class I {}
class J<T> extends I {}

// these are for arrays

class ArrayHarvest<T> {
  public T[] genericArrayArg1() { return null; }
  public List<T>[] genericArrayArg2() { return null; }
  public int[] concreteArrayArg1() { return null; }
  public List<Integer>[] concreteArrayArg2(){ return null; }
}