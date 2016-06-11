package randoop.types;

import java.lang.reflect.Type;

/**
 * A reference type in JLS (Section 4.3) is defined as one of Class/Interface type,
 * Array type, or a type variable.
 */
public abstract class ReferenceType extends GeneralType {

  @Override
  public abstract ReferenceType apply(Substitution<ReferenceType> substitution);

  /**
   * {@inheritDoc}
   * <p>
   * For {@link ReferenceType}, checks for widening reference conversion.
   * See <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.5">section 5.1.5 of JLS of JavaSE 8</a>
   * for details.
   * </p>
   */
  public boolean isAssignableFrom(GeneralType sourceType) {
    return super.isAssignableFrom(sourceType)
            || (sourceType.isReferenceType()
                && sourceType.isSubtypeOf(this));
  }

  /**
   * Indicates whether this type is a capture type variable as constructed by {@link InstantiatedType#applyCaptureConversion()}.
   * A capture type variable can only occur as a type argument in an {@link InstantiatedType}
   * constructed this way.
   *
   * @return true if this type is a capture type variable, false otherwise
   */
  boolean isCaptureVariable() {
    return false;
  }

  /**
   * {@inheritDoc}
   * @return true since this is a primitive type
   */
  @Override
  public boolean isReferenceType() {
    return true;
  }

  /**
   * {@inheritDoc}
   * @return true, if {@code otherType} is {@code Object}, false otherwise
   */
  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    if (otherType == null) {
      throw new IllegalArgumentException("type may not be null");
    }

    return otherType.isObject();
  }

  /**
   * {@inheritDoc}
   * Provides the default for reference type objects, which is the {@code Object}
   * type.
   * (Note that this is different behavior than in reflection classes, where {@code null} is
   * returned because {@code Object} has no superclass.)
   *
   * @return the {@code Object} class type
   */
  @Override
  public ReferenceType getSuperclass() {
    return ConcreteTypes.OBJECT_TYPE;
  }

  /**
   * Returns a {@code ReferenceType} object for the given {@code Class} object.
   * Only checks for arrays, classes, and interfaces; since type variables are only represented
   * by {@code Type} references.
   * For arrays, calls {@link ArrayType#forClass(Class)}.
   * For other (class/interface) types, calls {@link ClassOrInterfaceType#forClass(Class)}.
   *
   * @param classType  the {@code Class} object representing the type
   * @return the {@code ReferenceType} object for the given type
   */
  public static ReferenceType forClass(Class<?> classType) {
    if (classType.isPrimitive()) {
      throw new IllegalArgumentException("type must be a reference type");
    }

    if (classType.isArray()) {
      return ArrayType.forClass(classType);
    }

    return ClassOrInterfaceType.forClass(classType);
  }

  /**
   * Creates a {@code ReferenceType} for the given {@code java.lang.reflect.Type}.
   * First, checks that the reference is not one of the direct subtypes of
   * {@code Type} representing reference types ({@code GenericArrayType} or
   * {@code TypeVariable}).
   * Then checks that the reference is not a {@code Class} object representing
   * an array.
   * If the reference is none of those, returns the result of converting the
   * type to a class or interface type.
   *
   * @param type  the type reference
   * @return the {@code ReferenceType} for the given {@code Type}
   */
  public static ReferenceType forType(Type type) {
    if (type instanceof java.lang.reflect.GenericArrayType) {
      return ArrayType.forType(type);
    }

    if (type instanceof java.lang.reflect.TypeVariable) {
      return TypeVariable.forType(type);
    }

    if ((type instanceof Class<?>) && ((Class<?>)type).isArray()) {
      return ArrayType.forType(type);
    }

    return ClassOrInterfaceType.forType(type);

  }


}
