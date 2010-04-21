package randoop;

import java.io.Serializable;

/**
 * Two StatNames are considered equal if their long names are equal.
 *
 */
public class StatName implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final int MAX_LENGTH_SHORT_NAME = 10;

  public final String longName;
  public final String shortName;
  public final String explanation;
  public final boolean printable;

  public StatName(String longName, String shortName, String explanation, boolean printable) {
    if (shortName == null || longName == null)
      throw new IllegalArgumentException("parameters cannot be null.");
    if (shortName.length() > MAX_LENGTH_SHORT_NAME)
      throw new IllegalArgumentException("shortName's length must be at most " + MAX_LENGTH_SHORT_NAME + " :" + shortName);
    this.longName = longName;
    this.shortName = shortName;
    this.explanation = explanation;
    this.printable = printable;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof StatName)) return false;
    if (this == o) return true;
    StatName other = (StatName)o;
    return (this.longName.equals(other.longName));
  }

  @Override
  public int hashCode() {
    return this.longName.hashCode();
  }

  @Override
  public String toString() {
    return longName;
  }

}
