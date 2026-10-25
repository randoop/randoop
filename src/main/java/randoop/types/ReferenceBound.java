package randoop.types;

import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Represents a bound on a type variable where the bound is a {@link ReferenceType}. */
public abstract class ReferenceBound extends ParameterBound {

  /** The type for this bound. */
  private final ReferenceType boundType;

  /**
   * Creates a {@link ReferenceBound} with the given bound type.
   *
   * @param boundType the {@link ReferenceType} of this bound
   */
  ReferenceBound(ReferenceType boundType) {
    this.boundType = boundType;
  }

  /**
   * Returns a bound for the given type, which results from applying a substitution to a bound. The
   * bound is lazy if the type contains a type variable, because the bounds of that type variable
   * might refer back to the type.
   *
   * @param type the type that results from applying a substitution to a bound
   * @return a lazy bound if {@code type} contains a type variable, an eager bound otherwise
   */
  static EagerReferenceBound forSubstitutedType(ReferenceType type) {
    if (type.isGeneric()) {
      return new LazyReferenceBound(type);
    }
    return new EagerReferenceBound(type);
  }

  /**
   * Returns the {@link ReferenceType} bound of this type.
   *
   * @return the type for this bound
   */
  public ReferenceType getBoundType() {
    return boundType;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ReferenceBound)) {
      return false;
    }
    ReferenceBound bound = (ReferenceBound) obj;
    return this.boundType.equals(bound.boundType);
  }

  /**
   * {@inheritDoc}
   *
   * <p>A {@link LazyReferenceBound} can be part of a recursive type, so the hash code is based on
   * the string representation of the bound type to avoid recursive calls on {@code hashCode()}. All
   * subclasses use this implementation, so that equal bounds have equal hash codes regardless of
   * their subclass.
   *
   * @return the hashCode for the string representation of this bound
   */
  @Override
  public int hashCode() {
    return Objects.hash(boundType.toString());
  }

  @Override
  public String toString() {
    return boundType.toString();
  }

  @Override
  public abstract ReferenceBound substitute(Substitution substitution);

  @Override
  public abstract ReferenceBound applyCaptureConversion();

  @Override
  boolean hasWildcard() {
    return boundType.hasWildcard();
  }

  @Override
  public boolean hasCaptureVariable() {
    return boundType.hasCaptureVariable();
  }

  @Override
  public boolean isGeneric(boolean ignoreWildcards) {
    return boundType.isGeneric(ignoreWildcards);
  }

  @Override
  public boolean isObject() {
    return boundType.isObject();
  }

  @Override
  public boolean isVariable() {
    return boundType.isVariable();
  }

  @Override
  public boolean isSubtypeOfOrEqualTo(ParameterBound bound) {
    if (bound instanceof ReferenceBound) {
      return boundType.isSubtypeOfOrEqualTo(((ReferenceBound) bound).getBoundType());
    }
    assert false : "not handling ReferenceBound subtype of other bound type";
    return false;
  }

  @Override
  boolean isUpperBound(ParameterBound bound, Substitution substitution) {
    return bound.hasUpperBound(boundType.substitute(substitution));
  }

  @Override
  boolean hasUpperBound(ReferenceType upperBound) {
    if (boundType.isVariable()) {
      if (boundType.equals(upperBound)) {
        return true;
      }
      // An upper bound of a type variable's upper bound is an upper bound of the type variable.
      TypeVariable variable = (TypeVariable) boundType;
      return TypeVariable.resolveTypeVariables(variable.getUpperTypeBound())
          .hasUpperBound(upperBound);
    }
    return isSubtypeOrEqual(boundType, upperBound);
  }

  /**
   * Returns true if {@code type} is a subtype of, or equal to, {@code otherType}. A type variable
   * in either type stands for itself. This differs from {@link
   * ParameterBound#isUpperBound(Type,Substitution)}, which treats type variables in the bound as
   * unknowns to be instantiated, and from {@link ReferenceType#isSubtypeOfOrEqualTo(Type)}, which
   * treats a raw type as a subtype of a parameterized type.
   *
   * @param type the possible subtype
   * @param otherType the possible supertype
   * @return true if {@code type} is a subtype of, or equal to, {@code otherType}
   */
  private static boolean isSubtypeOrEqual(ReferenceType type, ReferenceType otherType) {
    if (!(otherType instanceof InstantiatedType && type instanceof ClassOrInterfaceType)) {
      return type.isSubtypeOfOrEqualTo(otherType);
    }
    InstantiatedType otherInstantiatedType = (InstantiatedType) otherType;
    InstantiatedType supertype =
        ((ClassOrInterfaceType) type)
            .getMatchingSupertype(otherInstantiatedType.getGenericClassType());
    if (supertype == null) {
      return false;
    }
    List<TypeArgument> typeArguments = supertype.getTypeArguments();
    List<TypeArgument> otherTypeArguments = otherInstantiatedType.getTypeArguments();
    for (int i = 0; i < typeArguments.size(); i++) {
      if (!otherTypeArguments.get(i).contains(typeArguments.get(i))) {
        return false;
      }
    }
    return true;
  }
}
