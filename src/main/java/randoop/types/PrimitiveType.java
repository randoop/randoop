package randoop.types;

/**
 * Represents a Java primitive type.
 */
public class PrimitiveType extends GeneralType {

  private final Class<?> runtimeClass;

  public PrimitiveType(Class<?> runtimeClass) {
    assert runtimeClass.isPrimitive() : "must be initialized with primitive type";
    this.runtimeClass = runtimeClass;
  }

  /**
   * {@inheritDoc}
   * @return true if the runtime type of this primitive type and the object are the same, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PrimitiveType)) {
      return false;
    }
    PrimitiveType t = (PrimitiveType) obj;
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

  /**
   * {@inheritDoc}
   * @return true since this object represents a primitive type
   */
  @Override
  public boolean isPrimitive() {
    return true;
  }

  /**
   * {@inheritDoc}
   * @return this object
   */
  @Override
  public GeneralType apply(Substitution substitution) throws RandoopTypeException {
    return this;
  }

  /**
   * {@inheritDoc}
   * @return true if this type can be assigned from the source type by primitive widening or uboxing, false otherwise
   */
  @Override
  public boolean isAssignableFrom(GeneralType sourceType) {
    // test for primitive widening or unboxing conversion
    if (sourceType.isPrimitive()) { // primitive widening conversion
      return PrimitiveTypes.isAssignable(this.runtimeClass, sourceType.getRuntimeClass());
    } else { // unbox then widen conversion
      Class<?> tUnboxed = PrimitiveTypes.toUnboxedType(sourceType.getRuntimeClass());
      return tUnboxed != null && PrimitiveTypes.isAssignable(this.runtimeClass, tUnboxed);
    }
  }

  @Override
  public PrimitiveType toPrimitive() {
    return this;
  }

  @Override
  public ClassOrInterfaceType toBoxedPrimitive() {
    return PrimitiveTypes.toBoxedType(this.getRuntimeClass());
  }
}
