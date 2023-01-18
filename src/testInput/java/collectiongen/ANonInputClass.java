package collectiongen;

import java.util.Objects;

/**
 * A class for testing collection generation
 */
public class ANonInputClass {
  public final String string;

  public ANonInputClass(String string) {
    this.string = string;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ANonInputClass)) {
      return false;
    }
    ANonInputClass aic = (ANonInputClass)obj;
    return this.string.equals(aic.string);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.string);
  }

  @Override
  public String toString() {
    return string;
  }
}
