package mcdrivertest;

public class Bar {
    private Foo f;
    public boolean toStringBroken = false;

    public Bar(Foo f) {
        this.f = f;
    }

    public void m2() {
        if (f.equalsBroken)
            toStringBroken = true;
    }

    // The bug in toString should not be found because
    // it is always preceded by the equals bug in Foo.
    @Override
    public String toString() {
      if (toStringBroken) throw new RuntimeException();
      return "a Bar.";
    }
}
