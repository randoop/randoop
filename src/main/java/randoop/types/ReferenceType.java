package randoop.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reference type defined in
 * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.3">JLS Section 4.3</a>
 * <pre>
 *   ReferenceType:
 *     ClassOrInterfaceType
 *     TypeVariable
 *     ArrayType
 * </pre>
 */
public abstract class ReferenceType extends Type {

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
  public static ReferenceType forType(java.lang.reflect.Type type) {
    if (type instanceof java.lang.reflect.GenericArrayType) {
      return ArrayType.forType(type);
    }

    if (type instanceof java.lang.reflect.TypeVariable) {
      return TypeVariable.forType(type);
    }

    if ((type instanceof Class<?>) && ((Class<?>) type).isArray()) {
      return ArrayType.forType(type);
    }

    return ClassOrInterfaceType.forType(type);
  }

  @Override
  public abstract ReferenceType apply(Substitution<ReferenceType> substitution);

  @Override
  public ReferenceType applyCaptureConversion() {
    return this;
  }

  /**
   * Returns the list of type parameters for this type.
   * Allows the construction of substitutions to instantiate a generic type
   * such as a generic array type {@code E[]}, or class type in a generic
   * {@link randoop.operation.TypedClassOperation}.
   * (This is necessary because the operation class does not capture the
   * fact that the underlying method/constructor is generic.)
   *
   * @return the type parameters for this type
   */
  public List<TypeVariable> getTypeParameters() {
    return new ArrayList<>();
  }

  /**
   * {@inheritDoc}
   * <p>
   * For assignment to {@link ReferenceType}, checks for widening reference conversion.
   * See <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.5">section
   * JLS 5.1.5</a>
   * for details.
   * </p>
   */
  public boolean isAssignableFrom(Type sourceType) {
    return super.isAssignableFrom(sourceType)
        || (sourceType.isReferenceType() && sourceType.isSubtypeOf(this));
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
   * Indicates whether this type is an instantiation of a given type.
   * For a general {@link ReferenceType}, this is only true if the other
   * type is the same, or the other type is a type variable for which
   * this type satisfies the bounds.
   *
   * @param otherType  the other reference type
   * @return true if this type instantiates the other reference type,
   * false otherwise
   */
  boolean isInstantiationOf(ReferenceType otherType) {
    if (this.equals(otherType)) {
      return true;
    }
    if (otherType.isVariable()) {
      TypeVariable variable = (TypeVariable) otherType;
      List<TypeVariable> typeParameters = new ArrayList<>();
      typeParameters.add(variable);
      Substitution<ReferenceType> substitution = Substitution.forArgs(typeParameters, this);
      return variable.getLowerTypeBound().isLowerBound(this, substitution)
          && variable.getUpperTypeBound().isUpperBound(this, substitution);
    }
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
  public boolean isSubtypeOf(Type otherType) {
    if (otherType == null) {
      throw new IllegalArgumentException("type may not be null");
    }

    return super.isSubtypeOf(otherType) || otherType.isObject();
  }
}
