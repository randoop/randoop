package examples;

import java.util.Objects;

/**
 * Simple class that should behave itself during regression tests.
 */
public class Dummy {

  private int theNumber;

  public Dummy() {
    theNumber = 0;
  }

  public Dummy(int theNumber) {
    this.theNumber = theNumber;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof Dummy)) {
      return false;
    }
    Dummy other = (Dummy)obj;
    return theNumber == other.theNumber;
  }

  public int hashCode() {
    return Objects.hash(theNumber);
  }

  public String toString() {
    return "Dummy(" + theNumber + ")";
  }

  public Dummy multiply(Dummy dummy) {
    return new Dummy(theNumber * dummy.theNumber);
  }
}
