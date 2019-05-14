package randoop.types;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Java primitive type. Corresponds to primitive types as defined in JLS <a
 * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-PrimitiveType">section
 * 4.2</a>.
 */
public class PrimitiveType extends Type {

  /** The runtime class of the primitive type. */
  private final Class<?> runtimeClass;

  /** All the PrimitiveTypes that have been created. */
  private static Map<Class<?>, PrimitiveType> cache = new HashMap<>();

  /**
   * Creates a primitive type from the given runtime class.
   *
   * @param runtimeClass the runtime class
   * @return the PrimitiveType for the given runtime class
   */
  public static PrimitiveType forClass(Class<?> runtimeClass) {
    PrimitiveType cached = cache.get(runtimeClass);
    if (cached == null) {
      cached = new PrimitiveType(runtimeClass);
      cache.put(runtimeClass, cached);
    }
    return cached;
  }

  /**
   * Creates a primitive type from the given runtime class.
   *
   * @param runtimeClass the runtime class
   */
  private PrimitiveType(Class<?> runtimeClass) {
    assert runtimeClass.isPrimitive()
        : "must be initialized with primitive type, got " + runtimeClass.getName();
    assert !runtimeClass.equals(void.class) : "void should be represented by VoidType";
    this.runtimeClass = runtimeClass;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if the runtime class of this primitive type and the object are the same, false
   *     otherwise
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
   *
   * @return the name of this type as the string representation of this type
   */
  @Override
  public String toString() {
    return this.getName();
  }

  /**
   * {@inheritDoc}
   *
   * <p>For primitive types returns the type name: {@code "int"}, {@code "char"}, etc.
   */
  @Override
  public String getName() {
    return runtimeClass.getCanonicalName();
  }

  @Override
  public String getSimpleName() {
    return runtimeClass.getSimpleName();
  }

  /**
   * {@inheritDoc}
   *
   * @return the {@code Class} object for this primitive type
   */
  @Override
  public Class<?> getRuntimeClass() {
    return runtimeClass;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Checks for <a
   * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.2">primitive
   * widening (section 5.1.2)</a>, and <a
   * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.8">unboxing (section
   * 5.1.8)</a> conversions. For a primitive type, returns true if this type can be assigned from
   * the source type by primitive widening or unboxing.
   */
  @Override
  public boolean isAssignableFrom(Type sourceType) {

    if (super.isAssignableFrom(sourceType)) {
      return true;
    }

    // test for primitive widening or unboxing conversion
    if (sourceType.isPrimitive()) { // primitive widening conversion
      return PrimitiveTypes.isAssignable(this.runtimeClass, sourceType.getRuntimeClass());
    }

    if (sourceType.isBoxedPrimitive()) { // unbox then primitive widening conversion
      PrimitiveType primitiveSourceType = ((NonParameterizedType) sourceType).toPrimitive();
      return this.isAssignableFrom(primitiveSourceType);
    }

    return false;
  }

  /**
   * {@inheritDoc}
   *
   * @return true since this object represents a primitive type
   */
  @Override
  public boolean isPrimitive() {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true since this object represents a non-receiver type
   */
  @Override
  public boolean isNonreceiverType() {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Specifically implements tests for primitive types as defined in <a
   * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.10.1">section 4.10.1
   * of JLS for JavaSE 8</a>.
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
  public NonParameterizedType toBoxedPrimitive() {
    return new NonParameterizedType(PrimitiveTypes.toBoxedType(this.getRuntimeClass()));
  }
}
