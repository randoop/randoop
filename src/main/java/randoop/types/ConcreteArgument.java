package randoop.types;

/**
 * Created by bjkeller on 4/13/16.
 */
public class ConcreteArgument implements TypeArgument {
  private final ConcreteType type;

  public ConcreteArgument(ConcreteType type) {
    this.type = type;
  }

  @Override
  public TypeBound getBound() {
    return new ConcreteTypeBound(type, new SupertypeOrdering());
  }

  @Override
  public boolean isGeneric() {
    return false;
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
