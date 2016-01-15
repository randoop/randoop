package randoop.types;

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