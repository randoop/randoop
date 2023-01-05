package randoop.types;

import java.util.ArrayList;
import java.util.List;

class ExampleClassesForTests {

static class A<T> implements Comparable<A<T>> {
  @Override
  public int compareTo(A<T> o) {
    return 0;
  }
}

static class B extends A<String> {}

static class C extends A<Integer> {}

static class D<S, T> extends A<T> {}

static class E<S, T> {}

static class F<T, S> extends E<S, T> {}

static class G<S> {}

static class H<T> extends G<T> implements Comparable<H<T>> {
  @Override
  public int compareTo(H<T> o) {
    return 0;
  }
}

static class I {}

static class J<T> extends I {}

// these are for arrays

static class ArrayHarvest<T> {
  public T[] genericArrayArg1() {
    return null;
  }

  public List<T>[] genericArrayArg2() {
    return null;
  }

  public int[] concreteArrayArg1() {
    return null;
  }

  public List<Integer>[] concreteArrayArg2() {
    return null;
  }
}

static class GenericWithOperations<T> {
  public T theField;
  public List<T> theList;

  public GenericWithOperations(T theValue) {
    this.theField = theValue;
    this.theList = new ArrayList<>();
  }

  public T getTheField() {
    return theField;
  }

  public void setTheField(T theValue) {
    theField = theValue;
  }

  public List<T> getTheList() {
    return theList;
  }

  public void addAll(T[] a) {
    for (T t : a) {
      theList.add(t);
    }
  }
}

static class ConcreteWithOperations extends GenericWithOperations<String> {
  public ConcreteWithOperations(String theValue) {
    super(theValue);
  }
}

interface BaseStream<T, S extends BaseStream<T, S>> {}

interface Stream<T> extends BaseStream<T, Stream<T>> {}

static class GenericWithInnerClass<T> {
  public class InnerClass {
    final T t;

    public InnerClass(T t) {
      this.t = t;
    }
  }

  public InnerClass getAnInnerClass(T param) {
    return new InnerClass(param);
  }

  public static class StaticInnerClass {}

  public StaticInnerClass getAStaticInnerClass() {
    return new StaticInnerClass();
  }

  public class GenericNestedClass<S> {}
}

static class ClassWithGenericInnerClass {
  public class GenericNestedClass<T> {}
}

static class ClassWithInnerClass {
  public class InnerClass {}

  public class OtherInnerClass {}
}

}
