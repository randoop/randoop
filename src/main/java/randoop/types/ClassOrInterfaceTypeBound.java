package randoop.types;

import java.lang.reflect.Type;

/**
 * Represents a parameter bound that is a class or interface.
  */
public class ClassOrInterfaceTypeBound extends ClassOrInterfaceBound {

  /** the type of this bound */
  private final ClassOrInterfaceType boundType;

  /**
   * Creates a bound object for the given class or interface type.
   *
   * @param boundType the class or interface type
   */
  ClassOrInterfaceTypeBound(ClassOrInterfaceType boundType) {
    this.boundType = boundType;
  }

  /**
   * {@inheritDoc}
   * @return true if the type of this bound is assignable from the given type, false otherwise
   */
  @Override
  public boolean isSatisfiedBy(GeneralType generalType) {
    return boundType.isAssignableFrom(generalType);
  }

  /**
   * {@inheritDoc}
   * @return true if the type of this bound, with the substitution applied, is assignable from the
   *         given type, false otherwise
   */
  @Override
  public boolean isSatisfiedBy(GeneralType generalType, Substitution<ReferenceType> substitution) {
    ClassOrInterfaceType boundType = this.boundType.apply(substitution);
    return boundType.isAssignableFrom(generalType);
  }

  /**
   * Creates a bound for the given {@code java.lang.reflect.Type} reference.
   *
   * @param type  the type for the bound
   * @return a {@code ClassOrInterfaceTypeBound} for the given type
   */
  public static ClassOrInterfaceTypeBound forType(Type type) {
    return new ClassOrInterfaceTypeBound(ClassOrInterfaceType.forType(type));
  }

  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    return boundType.isSubtypeOf(otherType);
  }

  @Override
  public ParameterBound apply(Substitution<ReferenceType> substitution) {
    return new ClassOrInterfaceTypeBound(boundType.apply(substitution));
  }
}
