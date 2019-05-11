package randoop.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reference type defined in <a
 * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.3">JLS Section 4.3</a>
 *
 * <pre>
 *   ReferenceType:
 *     ClassOrInterfaceType
 *     TypeVariable
 *     ArrayType
 * </pre>
 *
 * This abstract type is used to mirror the grammar in the JLS.
 */
public abstract class ReferenceType extends Type {

  /**
   * Returns a {@code ReferenceType} object for the given {@code Class} object. Creates arrays,
   * classes, and interfaces. For arrays, calls {@link ArrayType#forClass(Class)}. For other
   * (class/interface) types, calls {@link ClassOrInterfaceType#forClass(Class)}.
   *
   * @param classType the {@code Class} object representing the type
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
   * Creates a {@code ReferenceType} for the given {@code java.lang.reflect.Type}. Specifically,
   * creates
   *
   * <ul>
   *   <li>an {@link ArrayType} if the reference is {@code GenericArrayType} or is a {@code Class}
   *       object representing an array.
   *   <li>a {@link TypeVariable}) if the reference is {@code java.lang.reflect.TypeVariable}.
   *   <li>a {@link ClassOrInterfaceType} if the reference is none of those.
   * </ul>
   *
   * @param type the type reference
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

  /**
   * Applies a substitution to a {@link ReferenceType}. If the type is parameterized then replaces
   * type variables that occur, otherwise returns this type.
   *
   * <p>This abstract method forces typing of substitutions applied to {@link ReferenceType} objects
   * without an explicit cast.
   *
   * @param substitution the type substitution
   * @return the type created by applying the substitution to this type
   */
  @Override
  public abstract ReferenceType substitute(Substitution substitution);

  @Override
  public ReferenceType applyCaptureConversion() {
    return this;
  }

  /**
   * Returns the list of type parameters for this type. Reference types that may have type
   * parameters include {@link ArrayType} (such as {@code E[]}), and subclasses of {@link
   * ParameterizedType}.
   *
   * @return the type parameters for this type
   */
  public List<TypeVariable> getTypeParameters() {
    return new ArrayList<>();
  }

  /**
   * {@inheritDoc}
   *
   * <p>For assignment to {@link ReferenceType}, checks for widening reference conversion when the
   * source type is also a reference type. See <a
   * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.5">section JLS
   * 5.1.5</a> for details.
   */
  @Override
  public boolean isAssignableFrom(Type sourceType) {
    return super.isAssignableFrom(sourceType)
        || (sourceType.isReferenceType() && sourceType.isSubtypeOf(this));
  }

  /**
   * Indicates whether this type is a capture type variable as constructed by {@link
   * InstantiatedType#applyCaptureConversion()}. A capture type variable can only occur as a type
   * argument in an {@link InstantiatedType} constructed by {@link
   * InstantiatedType#applyCaptureConversion()}.
   *
   * @return true if this type is a capture type variable, false otherwise
   */
  public boolean isCaptureVariable() {
    return false;
  }

  /**
   * Indicates whether this type is an instantiation of a more general type.
   *
   * <p>For a general {@link ReferenceType}, this is only true if the other type is the same, or the
   * other type is a type variable for which this type satisfies the bounds. Other cases are handled
   * by the overriding implementations {@link
   * ClassOrInterfaceType#isInstantiationOf(ReferenceType)}, {@link
   * InstantiatedType#isInstantiationOf(ReferenceType)}, and {@link
   * TypeVariable#isInstantiationOf(ReferenceType)}.
   *
   * @param otherType the general reference type
   * @return true if this type instantiates the other reference type, false otherwise
   */
  public boolean isInstantiationOf(ReferenceType otherType) {
    if (this.equals(otherType)) {
      return true;
    }
    if (otherType.isVariable()) {
      TypeVariable variable = (TypeVariable) otherType;
      return variable.canBeInstantiatedBy(this);
    }
    return false;
  }

  /**
   * Computes a substitution that can be applied to the type variables of the generic goal type to
   * instantiate operations of this type, possibly inherited from from the goal type. The
   * substitution will unify this type or a supertype of this type with the given goal type.
   *
   * <p>If there is no unifying substitution, returns {@code null}.
   *
   * @param goalType the generic type for which a substitution is needed
   * @return a substitution unifying this type or a supertype of this type with the goal type
   */
  public Substitution getInstantiatingSubstitution(ReferenceType goalType) {
    return ReferenceType.getInstantiatingSubstitutionforTypeVariable(this, goalType);
  }

  /**
   * Static helper method that does the work of getInstantiatingSubstitution, if goalType is a type
   * variable.
   *
   * @param instantiatedType the first type
   * @param goalType the generic type for which a substitution is needed
   * @return a substitution unifying this first type or a supertype of the first type with the goal
   *     type
   */
  public static Substitution getInstantiatingSubstitutionforTypeVariable(
      ReferenceType instantiatedType, ReferenceType goalType) {
    if (instantiatedType.equals(goalType)) {
      return new Substitution();
    }
    if (goalType.isVariable()) {
      TypeVariable variable = (TypeVariable) goalType;
      Substitution substitution = new Substitution(variable, instantiatedType);
      if (variable.getLowerTypeBound().isLowerBound(instantiatedType, substitution)
          && variable.getUpperTypeBound().isUpperBound(instantiatedType, substitution)) {
        return substitution;
      }
    }
    return null;
  }

  @Override
  public boolean isReferenceType() {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * <p>For {@link ReferenceType}, returns true if {@code otherType} is {@code Object}.
   */
  @Override
  public boolean isSubtypeOf(Type otherType) {
    if (otherType == null) {
      throw new IllegalArgumentException("type may not be null");
    }

    return super.isSubtypeOf(otherType) || otherType.isObject();
  }
}
