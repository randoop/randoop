package randoop.types;

/**
 * Represents a Java primitive type.
 */
public class PrimitiveType extends Type {

  /** The runtime type of the primitive type */
  private final Class<?> runtimeClass;

  /**
   * Creates a primitive type from the given runtime class.
   *
   * @param runtimeClass  the runtime class
   */
  public PrimitiveType(Class<?> runtimeClass) {
    assert runtimeClass.isPrimitive()
        : "must be initialized with primitive type, got " + runtimeClass.getName();
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
   * <p>
   * Checks for
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.2">primitive widening (section 5.1.2)</a>, and
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.8">unboxing (section 5.1.8)</a> conversions.
   * </p>
   *
   * @return true if this type can be assigned from the source type by primitive widening or unboxing, false otherwise
   */
  @Override
  public boolean isAssignableFrom(Type sourceType) {

    // check for void before identity: cannot assign to/from void
    if (this.isVoid() || sourceType.isVoid()) {
      return false;
    }

    if (super.isAssignableFrom(sourceType)) {
      return true;
    }

    // test for primitive widening or unboxing conversion
    if (sourceType.isPrimitive()) { // primitive widening conversion
      return PrimitiveTypes.isAssignable(this.runtimeClass, sourceType.getRuntimeClass());
    }

    if (sourceType.isBoxedPrimitive()) { // unbox then primitive widening conversion
      PrimitiveType primitiveSourceType = ((SimpleClassOrInterfaceType) sourceType).toPrimitive();
      return this.isAssignableFrom(primitiveSourceType);
    }

    return false;
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
   * <p>
   * Specifically implements tests for primitive types as defined in
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.10.1">section 4.10.1 of JLS for JavaSE 8</a>.
   * </p>
   */
  @Override
  public boolean isSubtypeOf(Type otherType) {
    return otherType.isPrimitive()
        && PrimitiveTypes.isSubtype(this.getRuntimeClass(), otherType.getRuntimeClass());
  }

  /**
   * Returns the boxed type for this primitive type.
   *
   * @return the boxed type for this primitive type
   */
  public SimpleClassOrInterfaceType toBoxedPrimitive() {
    return new SimpleClassOrInterfaceType(PrimitiveTypes.toBoxedType(this.getRuntimeClass()));
  }
}
