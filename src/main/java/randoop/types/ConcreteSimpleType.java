package randoop.types;

/**
 * {@code ConcreteSimpleType} represents an atomic concrete type: a primitive type,
 * a non-generic class, an enum, or the rawtype for a generic class.
 * It is a wrapper for a {@link Class} object, which is a runtime representation
 * of a type.
 */
public class ConcreteSimpleType extends ConcreteType {

  /** The runtime type of this simple type. */
  private final Class<?> runtimeClass;

  /**
   * Create a {@code ConcreteSimpleType} object for the runtime class
   *
   * @param runtimeType  the runtime class for the type
   */
  public ConcreteSimpleType(Class<?> runtimeType) {
    this.runtimeClass = runtimeType;
  }

  /**
   * {@inheritDoc}
   * @return true if the runtime types are the same, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ConcreteSimpleType)) {
      return false;
    }
    ConcreteSimpleType t = (ConcreteSimpleType) obj;
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
  public boolean isPrimitive() {
    return runtimeClass.isPrimitive();
  }

  @Override
  public boolean isBoxedPrimitive() {
    return PrimitiveTypes.isBoxedPrimitiveTypeOrString(runtimeClass)
            && ! this.equals(ConcreteType.STRING_TYPE);
  }

  @Override
  public boolean isString() {
    return this.equals(ConcreteType.STRING_TYPE);
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

  /**
   * {@inheritDoc}
   * Tests for assignability to this {@code ConcreteSimpleType}.
   * Does not consider void assignable from/to any type.
   *
   * @param sourceType  the source type
   * @return true if the source type can be assigned to this type by an
   *         assignment conversion, false otherwise
   */
  @Override
  public boolean isAssignableFrom(ConcreteType sourceType) {

    // cannot assign to/from void
    if (this.isVoid() || sourceType.isVoid()) {
      return false;
    }

    // object eats everything by reference widening
    if (this.isObject()) {
      return true;
    }

    // both rawtype and non-generic superclass eat
    // parameterized types by reference widening
    if (sourceType.isParameterized()) {
      return this.runtimeClass.isAssignableFrom(sourceType.getRuntimeClass());
    }

    // to be assignable, other cases must be ConcreteSimpleType to ConcreteSimpleType
    return sourceType instanceof ConcreteSimpleType && isAssignableFrom((ConcreteSimpleType) sourceType);

  }

  /**
   * Tests for assignability to a {@code ConcreteSimpleType} from a
   * {@code ConcreteSimpleType}.
   * Checks for identity, widening reference, widening primitive, boxing, and
   * unboxing conversions.
   *
   * @param sourceType  the source type
   * @return true if a value of {@code sourceType} can be assigned to a variable
   *         of this type
   */
  private boolean isAssignableFrom(ConcreteSimpleType sourceType) {
    // test for identity and reference widening conversions
    if (this.runtimeClass.isAssignableFrom(sourceType.runtimeClass)) {
      return true;
    }

    // test for primitive widening or unboxing conversion
    if (this.isPrimitive()) {
      if (sourceType.isPrimitive()) { // primitive widening conversion
        return PrimitiveTypes.isAssignable(this.runtimeClass, sourceType.runtimeClass);
      } else { // unbox then widen conversion
        Class<?> tUnboxed = PrimitiveTypes.toUnboxedType(sourceType.runtimeClass);
        return tUnboxed != null && PrimitiveTypes.isAssignable(this.runtimeClass, tUnboxed);
      }
    }

    // test for boxing conversion
    if (sourceType.isPrimitive()) {
      Class<?> tBoxed = PrimitiveTypes.getBoxedType(sourceType.runtimeClass);
      return tBoxed != null && this.runtimeClass.isAssignableFrom(tBoxed);
    }

    return false;
  }

  @Override
  public ConcreteType getSuperclass() {
    if (this.equals(ConcreteType.OBJECT_TYPE)) {
      return this;
    }
    return new ConcreteSimpleType(this.runtimeClass.getSuperclass());
  }

  @Override
  public ConcreteType toBoxedPrimitive() {
    if (this.isPrimitive()) {
      return ConcreteType.forClass(PrimitiveTypes.getBoxedType(this.getRuntimeClass()));
    } else if (this.isBoxedPrimitive()) {
      return this;
    }
    throw new IllegalArgumentException("Type must be primitive");
  }

  @Override
  public ConcreteType toPrimitive() {
    if (this.isPrimitive()) {
      return this;
    } else if (this.isBoxedPrimitive()) {
      return ConcreteType.forClass(PrimitiveTypes.toUnboxedType(this.getRuntimeClass()));
    }
    throw new IllegalArgumentException("Type must be primtive");
  }
}
