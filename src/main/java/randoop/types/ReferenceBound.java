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
    return bound.hasUpperBound(boundType.substitute(substitution), substitution);
  }

  @Override
  boolean hasUpperBound(ReferenceType upperBound, Substitution substitution) {
    return isSubtypeOrEqual(boundType, upperBound, substitution);
  }

  // The following methods compare types in which some type variables stand for unknown types, as
  // determined by getUnknown, and every other type variable stands for itself.  They differ from
  // ReferenceType.isSubtypeOfOrEqualTo, which treats a raw type as a subtype of every parameterized
  // type that contains a type variable, and from isUpperBound(Type, Substitution), which treats
  // every type variable in the bound as an unknown.  They are approximate:  they do not require two
  // occurrences of an unknown to stand for the same type.

  /**
   * Returns true if {@code type} is a subtype of, or equal to, {@code otherType}.
   *
   * @param type the possible subtype
   * @param otherType the possible supertype, which may contain unknowns
   * @param substitution the type substitution, which determines the unknowns
   * @return true if {@code type} is a subtype of, or equal to, {@code otherType}
   */
  private static boolean isSubtypeOrEqual(
      ReferenceType type, ReferenceType otherType, Substitution substitution) {
    if (isSameVariable(type, otherType) || otherType.isObject()) {
      return true;
    }
    TypeVariable unknown = getUnknown(otherType, substitution);
    if (unknown != null) {
      return satisfiesBound(type, unknown, substitution);
    }
    if (type.isVariable()) {
      // An upper bound of a type variable's upper bound is an upper bound of the type variable.
      return ((TypeVariable) type)
          .getResolvedUpperTypeBound()
          .hasUpperBound(otherType, substitution);
    }
    if (otherType.isVariable()) {
      // Only the type variable itself is a subtype of a type variable that stands for itself.
      return false;
    }
    if (otherType instanceof InstantiatedType && type instanceof ClassOrInterfaceType) {
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
        if (!contains(otherTypeArguments.get(i), typeArguments.get(i), substitution)) {
          return false;
        }
      }
      return true;
    }
    return type.isSubtypeOfOrEqualTo(otherType);
  }

  /**
   * Returns true if the type argument {@code otherArgument} contains {@code argument} (JLS section
   * 4.5.1).
   *
   * @param otherArgument the possibly containing type argument, which may contain unknowns
   * @param argument the possibly contained type argument
   * @param substitution the type substitution, which determines the unknowns
   * @return true if {@code otherArgument} contains {@code argument}
   */
  private static boolean contains(
      TypeArgument otherArgument, TypeArgument argument, Substitution substitution) {
    if (otherArgument instanceof ReferenceArgument) {
      ReferenceType otherType = ((ReferenceArgument) otherArgument).getReferenceType();
      if (!(argument instanceof ReferenceArgument)) {
        return getUnknown(otherType, substitution) != null;
      }
      return isSameType(((ReferenceArgument) argument).getReferenceType(), otherType, substitution);
    }
    WildcardArgument wildcard = (WildcardArgument) otherArgument;
    if (argument instanceof WildcardArgument) {
      return wildcard.contains(argument);
    }
    ReferenceType type = ((ReferenceArgument) argument).getReferenceType();
    ParameterBound bound = wildcard.getTypeBound();
    if (!(bound instanceof ReferenceBound)) {
      return false;
    }
    ReferenceType boundType = ((ReferenceBound) bound).getBoundType();
    if (wildcard.hasUpperBound()) {
      // ? extends B contains T if T <: B.
      return isSubtypeOrEqual(type, boundType, substitution);
    }
    // ? super B contains T if B <: T.
    TypeVariable unknown = getUnknown(boundType, substitution);
    if (unknown != null) {
      return satisfiesBound(type, unknown, substitution);
    }
    return boundType.isSubtypeOfOrEqualTo(type);
  }

  /**
   * Returns true if {@code type} is the same type as {@code otherType}.
   *
   * @param type a type
   * @param otherType a type, which may contain unknowns
   * @param substitution the type substitution, which determines the unknowns
   * @return true if {@code type} is the same type as {@code otherType}
   */
  private static boolean isSameType(
      ReferenceType type, ReferenceType otherType, Substitution substitution) {
    if (isSameVariable(type, otherType)) {
      return true;
    }
    TypeVariable unknown = getUnknown(otherType, substitution);
    if (unknown != null) {
      return satisfiesBound(type, unknown, substitution);
    }
    if (!(type instanceof InstantiatedType && otherType instanceof InstantiatedType)) {
      return false;
    }
    InstantiatedType instantiatedType = (InstantiatedType) type;
    InstantiatedType otherInstantiatedType = (InstantiatedType) otherType;
    if (!instantiatedType
        .getGenericClassType()
        .equals(otherInstantiatedType.getGenericClassType())) {
      return false;
    }
    List<TypeArgument> typeArguments = instantiatedType.getTypeArguments();
    List<TypeArgument> otherTypeArguments = otherInstantiatedType.getTypeArguments();
    for (int i = 0; i < typeArguments.size(); i++) {
      TypeArgument otherTypeArgument = otherTypeArguments.get(i);
      boolean same =
          otherTypeArgument instanceof WildcardArgument
              ? otherTypeArgument.equals(typeArguments.get(i))
              : contains(otherTypeArgument, typeArguments.get(i), substitution);
      if (!same) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if the two types are equal, or are the same declared type variable. Substitution
   * creates a copy of a type variable whose bounds differ, such as by having a lazy part resolved,
   * and {@link ExplicitTypeVariable#equals(Object)} compares bounds.
   *
   * @param type a type
   * @param otherType a type
   * @return true if the two types are equal or are the same declared type variable
   */
  private static boolean isSameVariable(ReferenceType type, ReferenceType otherType) {
    if (type instanceof ExplicitTypeVariable && otherType instanceof ExplicitTypeVariable) {
      return ((ExplicitTypeVariable) type)
          .getReflectionTypeVariable()
          .equals(((ExplicitTypeVariable) otherType).getReflectionTypeVariable());
    }
    return type.equals(otherType);
  }

  /**
   * If the given type is a type variable that stands for an unknown type, returns it, or the
   * equivalent type variable that the substitution maps. Otherwise returns null. A type variable
   * stands for an unknown type if the substitution does not replace it by a different type and does
   * not replace a different type variable by it.
   *
   * @param type a type
   * @param substitution the type substitution
   * @return the type variable, if {@code type} stands for an unknown type; otherwise null
   */
  private static @Nullable TypeVariable getUnknown(ReferenceType type, Substitution substitution) {
    if (!type.isVariable()) {
      return null;
    }
    TypeVariable result = (TypeVariable) type;
    for (TypeVariable variable : substitution.keySet()) {
      ReferenceType replacement = substitution.get(variable);
      boolean isIdentity = replacement != null && isSameVariable(variable, replacement);
      if (isSameVariable(variable, type)) {
        if (!isIdentity) {
          return null;
        }
        result = variable;
      } else if (!isIdentity && replacement != null && isSameVariable(replacement, type)) {
        return null;
      }
    }
    return result;
  }

  /**
   * Returns true if the unknown type that {@code unknown} stands for can be {@code type}: that is,
   * if {@code type} satisfies the upper bound of {@code unknown}.
   *
   * @param type the type
   * @param unknown a type variable that stands for an unknown type
   * @param substitution the type substitution, which does not replace {@code unknown}
   * @return true if {@code type} satisfies the upper bound of {@code unknown}
   */
  private static boolean satisfiesBound(
      ReferenceType type, TypeVariable unknown, Substitution substitution) {
    // Replacing the unknown by the type ensures termination, because there are fewer unknowns.
    return unknown
        .getUpperTypeBound()
        .isUpperBound(new EagerReferenceBound(type), substitution.replace(unknown, type));
  }
}
