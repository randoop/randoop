package randoop.types;

/**
 * {@code ConcreteSimpleType} represents an atomic concrete type: a primitive type,
 * a non-generic class, an enum, or the rawtype of a generic class.
 * It is a wrapper for a {@link Class} object, which is a runtime representation
 * of a type.
 */
public class SimpleClassOrInterfaceType extends ClassOrInterfaceType {

  /** The runtime type of this simple type. */
  private final Class<?> runtimeClass;

  /**
   * Create a {@code ConcreteSimpleType} object for the runtime class
   *
   * @param runtimeType  the runtime class for the type
   */
  public SimpleClassOrInterfaceType(Class<?> runtimeType) {
    assert ! runtimeType.isPrimitive() : "must be reference type";
    this.runtimeClass = runtimeType;
  }

  /**
   * {@inheritDoc}
   * @return true if the runtime types are the same, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SimpleClassOrInterfaceType)) {
      return false;
    }
    SimpleClassOrInterfaceType t = (SimpleClassOrInterfaceType) obj;
    return this.runtimeClass.equals(t.runtimeClass);
  }

  @Override
  public int hashCode() {
    return runtimeClass.hashCode();
  }

  /**
   * {@inheritDoc}
   * @return the name of this type
   */
  @Override
  public String toString() {
    return this.getName();
  }

  /**
   * {@inheritDoc}
   * @return the fully-qualified name of this type
   */
  @Override
  public String getName() {
    return runtimeClass.getCanonicalName();
  }

  /**
   * {@inheritDoc}
   * @return the {@code Class} object for this concrete simple type
   */
  @Override
  public Class<?> getRuntimeClass() {
    return runtimeClass;
  }

  @Override
  public boolean isEnum() {
    return runtimeClass.isEnum();
  }

  @Override
  public boolean isInterface() { return runtimeClass.isInterface(); }

  @Override
  public boolean isBoxedPrimitive() {
    return PrimitiveTypes.isBoxedPrimitiveTypeOrString(runtimeClass)
            && ! this.equals(ConcreteTypes.STRING_TYPE);
  }

  @Override
  public boolean isString() {
    return this.equals(ConcreteTypes.STRING_TYPE);
  }

  /**
   * {@inheritDoc}
   * @return true if the runtime type corresponds to a generic, false otherwise
   */
  @Override
  public boolean isRawtype() {
    return runtimeClass.getTypeParameters().length > 0;
  }

  @Override
  public boolean isVoid() {
    return runtimeClass.equals(void.class);
  }

  @Override
  public SimpleClassOrInterfaceType apply(Substitution<ReferenceType> substitution) {
    return this;
  }

  /**
   * {@inheritDoc}
   * Specifically checks for
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.7">boxing conversion (section 5.1.7)</a>
   */
  @Override
  public boolean isAssignableFrom(GeneralType sourceType) {
    // check identity and reference widening
    if (super.isAssignableFrom(sourceType)) {
      return true;
    }

    // otherwise, check for boxing conversion
    return sourceType.isPrimitive()
        && this.isAssignableFrom(sourceType.toBoxedPrimitive());
  }

  @Override
  public ClassOrInterfaceType getSuperclass() {
    if (this.equals(ConcreteTypes.OBJECT_TYPE)) {
      return this;
    }
    return new SimpleClassOrInterfaceType(this.runtimeClass.getSuperclass());
  }

  @Override
  public PrimitiveType toPrimitive() {
    if (this.isBoxedPrimitive()) {
      return new PrimitiveType(PrimitiveTypes.toUnboxedType(this.getRuntimeClass()));
    }
    throw new IllegalArgumentException("Type must be boxed primitive");
  }

  @Override
  public ClassOrInterfaceType toBoxedPrimitive() {
    if (this.isBoxedPrimitive()) {
      return this;
    }
    throw new IllegalArgumentException("Type must be primitive");
  }
}
