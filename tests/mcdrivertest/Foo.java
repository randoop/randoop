package mcdrivertest;

public class Foo {
    public boolean equalsBroken = false;
    private int timesCalledM1 = 0;

    public Foo(int i) {
        // no body.
    }

    public void m1() {
        timesCalledM1++;
        if (timesCalledM1 > 3) equalsBroken = true;
    }
    
    public Baz makeBaz() {
        return new Baz();
    }
    
    @Override
    public boolean equals(Object o) {
      if (equalsBroken) return false;
      return this == o;
    }

    @Override
    public int hashCode() {
      return 1;
    }
}
