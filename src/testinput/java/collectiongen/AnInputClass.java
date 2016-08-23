package collectiongen;

import java.util.Objects;

/**
 * An input class for generating collections
 */
public class AnInputClass {
  public final String string;

  public AnInputClass(String string) {
    this.string = string;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AnInputClass)) {
      return false;
    }
    AnInputClass aic = (AnInputClass)obj;
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
