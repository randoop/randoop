package misc;

public class NonFaultyClass {
  public NonFaultyClass() {
    /* No code. */
  }

  @Override
  public String toString() {
    return "";
  }

  @Override
  public int hashCode() {
    return 1;
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }
}
