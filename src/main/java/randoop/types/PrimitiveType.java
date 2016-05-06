package randoop.types;

/**
 * Represents a Java primitive type.
 */
public class PrimitiveType extends GeneralType {

  private final Class<?> runtimeClass;

  public PrimitiveType(Class<?> runtimeClass) {
    assert runtimeClass.isPrimitive() : "must be initialized with primitive type, got " + runtimeClass.getName();
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
   * Specifically implements tests for primitive types as defined in
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.10.1">section 4.10.1 of JLS for JavaSE 8</a>.
   */
  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    return otherType.isPrimitive()
        && PrimitiveTypes.isSubtype(this.getRuntimeClass(), otherType.getRuntimeClass());
  }

  /**
   * {@inheritDoc}
   * Checks for
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.2">primitive widening (section 5.1.2)</a>, and
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.8>unboxing (section 5.1.8)</a> conversions.
   * @return true if this type can be assigned from the source type by primitive widening or unboxing, false otherwise
   */
  @Override
  public boolean isAssignableFrom(GeneralType sourceType) {

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

    if (sourceType.isBoxedPrimitive()){ // unbox then primitive widening conversion
      PrimitiveType primitiveSourceType = ((SimpleClassOrInterfaceType)sourceType).toPrimitive();
      return this.isAssignableFrom(primitiveSourceType);
    }

    return false;
  }

  @Override
  public PrimitiveType toPrimitive() {
    return this;
  }

  @Override
  public ClassOrInterfaceType toBoxedPrimitive() {
    return new SimpleClassOrInterfaceType(PrimitiveTypes.getBoxedType(this.getRuntimeClass()));
  }
}
