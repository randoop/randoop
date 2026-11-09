package randoop.types;

import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.types.LazyParameterBound.LazyBoundException;

/**
 * Subtyping and type-argument containment (JLS sections 4.10 and 4.5.1) for reference types in
 * which some type variables stand for unknown types, as determined by {@link
 * Substitution#isUnknown(ReferenceType)}, and every other type variable stands for itself. With a
 * substitution that has no unknowns, these are the ordinary relations.
 *
 * <p>These relations differ from {@link ReferenceType#isSubtypeOfOrEqualTo(Type)}, which treats a
 * raw type as a subtype of every parameterized type that contains a type variable.
 *
 * <p>These relations also differ from {@link ParameterBound#isUpperBound(Type, Substitution)},
 * which treats every type variable in the bound as an unknown.
 *
 * <p>These relations are approximate: they do not require two occurrences of an unknown to stand
 * for the same type.
 */
final class Subtyping {

  /** Do not instantiate. */
  private Subtyping() {
    throw new Error("Do not instantiate");
  }

  /**
   * Returns true if {@code type} is a subtype of, or equal to, {@code otherType}.
   *
   * @param type the possible subtype
   * @param otherType the possible supertype, which may contain unknowns
   * @param substitution the type substitution, which determines the unknowns
   * @return true if {@code type} is a subtype of, or equal to, {@code otherType}
   */
  static boolean isSubtypeOrEqual(
      ReferenceType type, ReferenceType otherType, Substitution substitution) {
    if (type.equals(otherType) || otherType.isObject()) {
      return true;
    }
    // The null type is a subtype of every reference type (JLS section 4.10.2).
    if (type.equals(JavaTypes.NULL_TYPE)) {
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
      if (((TypeVariable) type)
          .getResolvedUpperTypeBound()
          .hasUpperBound(otherType, substitution)) {
        return true;
      }
      if (!otherType.isVariable()) {
        return false;
      }
      // Try the lower bound of the other type variable, below.  For example, T is a subtype of the
      // capture of "? super T".
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
      return argumentsContained(supertype, otherInstantiatedType, substitution);
    }
    return type.isSubtypeOfOrEqualTo(otherType);
  }

  /**
   * Returns true if each type argument of {@code otherType} contains the corresponding type
   * argument of {@code type}, including the type arguments of the enclosing types of member
   * classes. The two types must be instantiations of the same generic class.
   *
   * @param type a type, to which capture conversion has been applied
   * @param otherType a type, which may contain unknowns
   * @param substitution the type substitution, which determines the unknowns
   * @return true if the type arguments of {@code otherType} contain those of {@code type}
   */
  private static boolean argumentsContained(
      ClassOrInterfaceType type, ClassOrInterfaceType otherType, Substitution substitution) {
    if (otherType instanceof InstantiatedType) {
      if (!(type instanceof InstantiatedType)) {
        return false;
      }
      List<TypeArgument> typeArguments = ((InstantiatedType) type).getTypeArguments();
      List<TypeArgument> otherTypeArguments = ((InstantiatedType) otherType).getTypeArguments();
      if (typeArguments.size() != otherTypeArguments.size()) {
        return false;
      }
      for (int i = 0; i < typeArguments.size(); i++) {
        if (!contains(otherTypeArguments.get(i), typeArguments.get(i), substitution)) {
          return false;
        }
      }
    }
    // For a member class of a generic class, such as Outer<String>.Inner<Integer>, the type
    // arguments of the enclosing type must also be contained.
    if (type.isMemberClass() && otherType.isMemberClass()) {
      return argumentsContained(type.enclosingType, otherType.enclosingType, substitution);
    }
    return true;
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
  static boolean contains(
      TypeArgument otherArgument, TypeArgument argument, Substitution substitution) {
    if (otherArgument instanceof ReferenceArgument) {
      // A type argument that is not a wildcard contains only the same type.
      return argument instanceof ReferenceArgument
          && isSameType(
              ((ReferenceArgument) argument).getReferenceType(),
              ((ReferenceArgument) otherArgument).getReferenceType(),
              substitution);
    }
    WildcardArgument wildcard = (WildcardArgument) otherArgument;
    ReferenceType boundType = getBoundType(wildcard);
    if (boundType == null) {
      return otherArgument.equals(argument);
    }
    if (argument instanceof ReferenceArgument) {
      ReferenceType type = ((ReferenceArgument) argument).getReferenceType();
      if (wildcard.hasUpperBound()) {
        // ? extends B contains T if T <: B.
        return isSubtypeOrEqual(type, boundType, substitution);
      }
      // ? super B contains T if B <: T.
      return canBeSubtype(boundType, type, substitution);
    }
    WildcardArgument argumentWildcard = (WildcardArgument) argument;
    ReferenceType argumentBoundType = getBoundType(argumentWildcard);
    if (argumentBoundType == null) {
      return otherArgument.equals(argument);
    }
    if (wildcard.hasUpperBound()) {
      // ? extends B contains ? extends S if S <: B, and contains ? super S if B is Object.
      ReferenceType argumentUpperBound =
          argumentWildcard.hasUpperBound() ? argumentBoundType : JavaTypes.OBJECT_TYPE;
      return isSubtypeOrEqual(argumentUpperBound, boundType, substitution);
    }
    // ? super B contains ? super S if B <: S, and does not contain ? extends S.
    return !argumentWildcard.hasUpperBound()
        && canBeSubtype(boundType, argumentBoundType, substitution);
  }

  /**
   * Returns the type of the bound of the given wildcard, or null if the bound is not a {@link
   * ReferenceBound}.
   *
   * @param wildcard a wildcard type argument
   * @return the type of the bound of {@code wildcard}, or null
   */
  private static @Nullable ReferenceType getBoundType(WildcardArgument wildcard) {
    ParameterBound bound = wildcard.getTypeBound();
    return bound instanceof ReferenceBound ? ((ReferenceBound) bound).getBoundType() : null;
  }

  /**
   * Returns true if {@code lowerType} is a subtype of, or equal to, {@code type}. If {@code
   * lowerType} is an unknown, returns true if some choice for it is a subtype of {@code type}:
   * {@code type} itself, the lower bound of {@code type} if it is a type variable, or the unknown's
   * upper bound or a member of it, if it is an intersection.
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
    if (unknownBound instanceof ReferenceBound) {
      return boundCanBeSubtype((ReferenceBound) unknownBound, type, unknown, substitution);
    }
    if (unknownBound instanceof IntersectionTypeBound) {
      // Each member of the intersection is a candidate.  For `B extends Number & Serializable`,
      // B can be Number, which is a subtype of Serializable.
      for (ParameterBound member : ((IntersectionTypeBound) unknownBound).getBounds()) {
        if (member instanceof ReferenceBound
            && boundCanBeSubtype((ReferenceBound) member, type, unknown, substitution)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true if the type of the given bound of an unknown is a subtype of, or equal to, {@code
   * type}, and the unknown can be that type.
   *
   * @param bound an upper bound, or a member of an intersection upper bound, of {@code unknown}
   * @param type the possible supertype
   * @param unknown a type variable that stands for an unknown type
   * @param substitution the type substitution, which determines the unknowns
   * @return true if {@code unknown} can be the type of {@code bound}, which is a subtype of {@code
   *     type}
   */
  private static boolean boundCanBeSubtype(
      ReferenceBound bound, ReferenceType type, TypeVariable unknown, Substitution substitution) {
    ReferenceType boundType = bound.getBoundType().substitute(substitution);
    return isSubtypeOrEqual(boundType, type, substitution)
        && satisfiesBound(boundType, unknown, substitution);
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
    if (type.equals(otherType)) {
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
              ? isSameWildcard(
                  typeArguments.get(i), (WildcardArgument) otherTypeArgument, substitution)
              : contains(otherTypeArgument, typeArguments.get(i), substitution);
      if (!same) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if {@code argument} is the same wildcard as {@code otherWildcard}: that is, both
   * are upper-bounded or both are lower-bounded, and their bounds are the same type.
   *
   * @param argument a type argument
   * @param otherWildcard a wildcard, whose bound may contain unknowns
   * @param substitution the type substitution, which determines the unknowns
   * @return true if {@code argument} is the same wildcard as {@code otherWildcard}
   */
  private static boolean isSameWildcard(
      TypeArgument argument, WildcardArgument otherWildcard, Substitution substitution) {
    if (otherWildcard.equals(argument)) {
      return true;
    }
    if (!(argument instanceof WildcardArgument)) {
      return false;
    }
    WildcardArgument wildcard = (WildcardArgument) argument;
    if (wildcard.hasUpperBound() != otherWildcard.hasUpperBound()) {
      return false;
    }
    ParameterBound bound = wildcard.getTypeBound();
    ParameterBound otherBound = otherWildcard.getTypeBound();
    return bound instanceof ReferenceBound
        && otherBound instanceof ReferenceBound
        && isSameType(
            ((ReferenceBound) bound).getBoundType(),
            ((ReferenceBound) otherBound).getBoundType(),
            substitution);
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
