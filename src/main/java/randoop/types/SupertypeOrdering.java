package randoop.types;


import java.util.Objects;

/**
 * Created by bjkeller on 4/11/16.
 */
public final class SupertypeOrdering implements TypeOrdering {
  @Override
  public boolean isLessThanOrEqualTo(ConcreteType t1, ConcreteType t2) {
    return t1.isAssignableFrom(t2);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof SupertypeOrdering);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getClass());
  }
}
