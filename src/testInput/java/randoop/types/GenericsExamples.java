package randoop.types;

class GenericsExamples {

static class Variable1<T> {}

static class Variable2<S, T> {
  public <U> T m(Variable1<? extends U> u, T t) {
    return t;
  }
}

static class Variable3<T> extends Variable1<T> {}

static class Variable4 extends Variable1<String> {}

static class Class1<S extends Number> {}

static class Class2<T extends Comparable<Integer>> {}

static class Variable1Ext extends Variable1<Variable1Ext> {} // use as parameter

static class Parameterized1<T extends Variable1<T>> {}

static class Variable1Ext2 extends Variable1<Variable1Ext2> implements Comparable<Variable1Ext2> {
  @Override
  public int compareTo(Variable1Ext2 o) {
    return 0;
  }
}

static class IntersectionBounds<T extends Variable1<T> & Comparable<T>> {}

static class Variable1Ext3 extends Variable1<Variable1Ext4> {}

static class Variable1Ext4 extends Variable1<Variable1Ext3> {}

static class MutuallyRecursive1<S extends Variable1<T>, T extends Variable1<S>> {}

}
