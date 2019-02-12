package randoop.sequence;

import randoop.types.Type;

/** A dummy variable, to ensure that parameter lists are the expected length. */
public class DummyVariable extends Variable {

  /** The only DummyVariable that exists. */
  public static final DummyVariable DUMMY = new DummyVariable();

  /** Create a DummyVariable. */
  private DummyVariable() {}

  @Override
  public String toString() {
    return "dummy";
  }

  @Override
  public boolean equals(Object o) {
    return o == this;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public Type getType() {
    throw new Error("Not implemented");
  }

  @Override
  public Statement getDeclaringStatement() {
    throw new Error("Not implemented");
  }

  @Override
  public int getDeclIndex() {
    throw new Error("Not implemented");
  }

  @Override
  public String getName() {
    return "dummy";
  }

  @Override
  public boolean shouldInlineLiterals() {
    throw new Error("Not implemented");
  }
}
