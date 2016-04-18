package randoop.types;

/**
 * Created by bjkeller on 4/18/16.
 */
public class PrimitiveType extends GeneralType {

  private final Class<?> runtimeClass;

  public PrimitiveType(Class<?> runtimeClass) {
    assert runtimeClass.isPrimitive() : "must be initialized with primitive type";
    this.runtimeClass = runtimeClass;
  }

  /**
   * {@inheritDoc}
   * @return true if the runtime types are the same, false otherwise
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

  @Override
  public boolean isPrimitive() {
    return true;
  }

  @Override
  public GeneralType apply(Substitution substitution) throws RandoopTypeException {
    return this;
  }

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
