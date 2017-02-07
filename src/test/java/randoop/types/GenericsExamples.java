package randoop.types;

class Variable1<T> {}

class Variable2<S, T> {
  public <U> T m(Variable1<? extends U> u, T t) {
    return t;
  }
}

class Variable3<T> extends Variable1<T> {}

class Variable4 extends Variable1<String> {}

class Class1<S extends Number> {}

class Class2<T extends Comparable<Integer>> {}

class Variable1Ext extends Variable1<Variable1Ext> {} // use as parameter

class Parameterized1<T extends Variable1<T>> {}

class Variable1Ext2 extends Variable1<Variable1Ext2> implements Comparable<Variable1Ext2> {
  @Override
  public int compareTo(Variable1Ext2 o) {
    return 0;
  }
}

class IntersectionBounds<T extends Variable1<T> & Comparable<T>> {}

class Variable1Ext3 extends Variable1<Variable1Ext4> {}

class Variable1Ext4 extends Variable1<Variable1Ext3> {}

class MutuallyRecursive1<S extends Variable1<T>, T extends Variable1<S>> {}
