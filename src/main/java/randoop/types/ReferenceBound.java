package randoop.types;

import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.types.LazyParameterBound.LazyBoundException;

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
    // Mapping each type variable that the substitution does not replace to itself makes it an
    // unknown.
    Substitution completeSubstitution = substitution.extendWithIdentity(getTypeParameters());
    return bound.hasUpperBound(boundType.substitute(completeSubstitution), completeSubstitution);
  }

  @Override
  boolean hasUpperBound(ReferenceType upperBound, Substitution substitution) {
    return isSubtypeOrEqual(boundType, upperBound, substitution);
  }

  // The following methods compare types in which some type variables stand for unknown types, as
  // determined by Substitution.isUnknown, and every other type variable stands for itself.  They
  // differ from ReferenceType.isSubtypeOfOrEqualTo, which treats a raw type as a subtype of every
  // parameterized type that contains a type variable, and from
  // EagerReferenceBound.isUpperBound(Type, Substitution), which treats every type variable in the
  // bound as an unknown when neither the bound nor the argument is a type variable.  They are
  // approximate:  they do not require two occurrences of an unknown to stand for the same type.

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
    if (TypeVariable.isSameVariable(type, otherType) || otherType.isObject()) {
      return true;
    }
    if (substitution.isUnknown(otherType)) {
      TypeVariable unknown = (TypeVariable) otherType;
      // The unknown may be the type or any of its supertypes.
      if (satisfiesBound(type, unknown, substitution)) {
        return true;
      }
      if (type instanceof ClassOrInterfaceType) {
        for (ClassOrInterfaceType supertype : ((ClassOrInterfaceType) type).getSupertypesStrict()) {
          if (satisfiesBound(supertype, unknown, substitution)) {
            return true;
          }
        }
        return false;
      }
      if (!type.isVariable()) {
        return false;
      }
      // Try the supertypes of the type variable, below.
    }
    if (type.isVariable()) {
      // An upper bound of a type variable's upper bound is an upper bound of the type variable.
      return ((TypeVariable) type)
          .getResolvedUpperTypeBound()
          .hasUpperBound(otherType, substitution);
    }
    if (otherType.isVariable()) {
      // A type variable that stands for itself is a supertype of its lower bound.
      ParameterBound lowerBound = ((TypeVariable) otherType).getResolvedLowerTypeBound();
      if (!(lowerBound instanceof ReferenceBound)) {
        return false;
      }
      ReferenceType lowerBoundType = ((ReferenceBound) lowerBound).getBoundType();
      return !lowerBoundType.equals(JavaTypes.NULL_TYPE)
          && isSubtypeOrEqual(type, lowerBoundType, substitution);
    }
    if (type.isArray() && otherType.isArray()) {
      Type componentType = ((ArrayType) type).getComponentType();
      Type otherComponentType = ((ArrayType) otherType).getComponentType();
      if (componentType.isReferenceType() && otherComponentType.isReferenceType()) {
        return isSubtypeOrEqual(
            (ReferenceType) componentType, (ReferenceType) otherComponentType, substitution);
      }
      return componentType.equals(otherComponentType);
    }
    if (otherType instanceof InstantiatedType && type instanceof ClassOrInterfaceType) {
      InstantiatedType otherInstantiatedType = (InstantiatedType) otherType;
      InstantiatedType supertype =
          ((ClassOrInterfaceType) type)
              .getMatchingSupertype(otherInstantiatedType.getGenericClassType());
      if (supertype == null) {
        return false;
      }
      // A type with wildcard arguments is a subtype of whatever its capture is a subtype of (JLS
      // section 4.10.2).
      try {
        supertype = supertype.applyCaptureConversion();
      } catch (LazyBoundException e) {
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
    if (!(argument instanceof ReferenceArgument)) {
      // Capture conversion has replaced wildcards at the top level.  In a nested type argument, a
      // wildcard is contained only by an identical wildcard.
      return otherArgument.equals(argument);
    }
    ReferenceType type = ((ReferenceArgument) argument).getReferenceType();
    if (otherArgument instanceof ReferenceArgument) {
      return isSameType(type, ((ReferenceArgument) otherArgument).getReferenceType(), substitution);
    }
    WildcardArgument wildcard = (WildcardArgument) otherArgument;
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
    return canBeSubtype(boundType, type, substitution);
  }

  /**
   * Returns true if {@code lowerType} is a subtype of, or equal to, {@code type}. If {@code
   * lowerType} is an unknown, returns true if some choice for it is a subtype of {@code type}:
   * {@code type} itself, the lower bound of {@code type} if it is a type variable, or the unknown's
   * upper bound.
   *
   * @param lowerType the possible subtype, which may be an unknown
   * @param type the possible supertype
   * @param substitution the type substitution, which determines the unknowns
   * @return true if {@code lowerType} is, or can be chosen to be, a subtype of {@code type}
   */
  private static boolean canBeSubtype(
      ReferenceType lowerType, ReferenceType type, Substitution substitution) {
    if (!substitution.isUnknown(lowerType)) {
      return isSubtypeOrEqual(lowerType, type, substitution);
    }
    TypeVariable unknown = (TypeVariable) lowerType;
    if (satisfiesBound(type, unknown, substitution)) {
      return true;
    }
    if (type.isVariable()) {
      ParameterBound lowerBound = ((TypeVariable) type).getResolvedLowerTypeBound();
      if (lowerBound instanceof ReferenceBound) {
        ReferenceType lowerBoundType = ((ReferenceBound) lowerBound).getBoundType();
        if (!lowerBoundType.equals(JavaTypes.NULL_TYPE)
            && satisfiesBound(lowerBoundType, unknown, substitution)) {
          return true;
        }
      }
    }
    ParameterBound unknownBound = unknown.getResolvedUpperTypeBound();
    if (!(unknownBound instanceof ReferenceBound)) {
      return false;
    }
    ReferenceType unknownBoundType =
        ((ReferenceBound) unknownBound).getBoundType().substitute(substitution);
    return isSubtypeOrEqual(unknownBoundType, type, substitution)
        && satisfiesBound(unknownBoundType, unknown, substitution);
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
    if (TypeVariable.isSameVariable(type, otherType)) {
      return true;
    }
    if (substitution.isUnknown(otherType)) {
      return satisfiesBound(type, (TypeVariable) otherType, substitution);
    }
    if (type.isArray() && otherType.isArray()) {
      Type componentType = ((ArrayType) type).getComponentType();
      Type otherComponentType = ((ArrayType) otherType).getComponentType();
      if (componentType.isReferenceType() && otherComponentType.isReferenceType()) {
        return isSameType(
            (ReferenceType) componentType, (ReferenceType) otherComponentType, substitution);
      }
      return componentType.equals(otherComponentType);
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
   * Returns true if the unknown type that {@code unknown} stands for can be {@code type}: that is,
   * if {@code type} satisfies the upper bound of {@code unknown}.
   *
   * @param type the type
   * @param unknown a type variable that stands for an unknown type
   * @param substitution the type substitution, which maps {@code unknown} to itself
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
