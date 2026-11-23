package randoop.types;

import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

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
 * which, unless the bound or the argument is a type variable, treats every type variable in the
 * bound as an unknown.
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
    if (isIdentical(type, otherType) || otherType.isObject()) {
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
      ParameterBound upperBound = ((TypeVariable) type).getResolvedUpperTypeBound();
      if (upperBound.hasUpperBound(otherType, substitution)) {
        return true;
      }
      // Otherwise, a type variable is a subtype only of a type variable whose lower bound it is a
      // subtype of, as for T and a capture of `? super T`.
      if (!otherType.isVariable()) {
        return false;
      }
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
      supertype = supertype.applyCaptureConversion();
      return argumentsContained(supertype, otherInstantiatedType, substitution);
    }
    if (otherType instanceof NonParameterizedType
        && otherType.isParameterized()
        && type instanceof ClassOrInterfaceType) {
      // A member class that declares no type parameters, of a parameterized type, such as
      // Outer<String>.Inner.  isSubtypeOfOrEqualTo would cast it to InstantiatedType, which it is
      // not, so compare the enclosing types here.
      ClassOrInterfaceType supertype =
          findSupertypeOfClass((ClassOrInterfaceType) type, otherType.getRuntimeClass());
      if (supertype == null) {
        return false;
      }
      // As above, capture the wildcard arguments, here those of the enclosing types.
      supertype = supertype.applyCaptureConversion();
      return argumentsContained(supertype, (ClassOrInterfaceType) otherType, substitution);
    }
    return type.isSubtypeOfOrEqualTo(otherType);
  }

  /**
   * Returns the given type or the supertype of it whose runtime class is the given class.
   *
   * @param type a type
   * @param runtimeClass a class
   * @return {@code type} or its supertype whose runtime class is {@code runtimeClass}, or null if
   *     there is none
   */
  private static @Nullable ClassOrInterfaceType findSupertypeOfClass(
      ClassOrInterfaceType type, Class<?> runtimeClass) {
    if (type.runtimeClassIs(runtimeClass)) {
      return type;
    }
    for (ClassOrInterfaceType supertype : type.getSupertypesStrict()) {
      if (supertype.runtimeClassIs(runtimeClass)) {
        return supertype;
      }
    }
    return null;
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
    if (!(argument instanceof ReferenceArgument)) {
      // Capture conversion has replaced wildcards at the top level.  In a nested type argument, a
      // wildcard is contained only by an identical wildcard.
      return otherArgument instanceof WildcardArgument
          && isSameWildcard(argument, (WildcardArgument) otherArgument, substitution);
    }
    ReferenceType type = ((ReferenceArgument) argument).getReferenceType();
    if (otherArgument instanceof ReferenceArgument) {
      return isSameType(type, ((ReferenceArgument) otherArgument).getReferenceType(), substitution);
    }
    WildcardArgument wildcard = (WildcardArgument) otherArgument;
    ReferenceType boundType = getBoundType(wildcard);
    if (boundType == null) {
      return false;
    }
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
   * upper bound or a member of it, if it is an intersection. The result is also true if the
   * unknown's upper bound is an intersection that has a member that is a subtype of {@code type},
   * since every choice for the unknown is a subtype of that member. If {@code lowerType} contains
   * unknowns, as {@code List<B>} does, compares its components with those of {@code type}.
   *
   * @param lowerType the possible subtype, which may be or contain unknowns
   * @param type the possible supertype
   * @param substitution the type substitution, which determines the unknowns
   * @return true if {@code lowerType} is, or can be chosen to be, a subtype of {@code type}
   */
  /*package*/ static boolean canBeSubtype(
      ReferenceType lowerType, ReferenceType type, Substitution substitution) {
    if (!substitution.isUnknown(lowerType)) {
      if (isSubtypeOrEqual(lowerType, type, substitution)) {
        return true;
      }
      // isSubtypeOrEqual recognizes unknowns only in its second argument.
      return hasUnknown(lowerType, substitution)
          && compoundCanBeSubtype(lowerType, type, substitution);
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
    // Every choice for the unknown is a subtype of its upper bound, and of every member of an
    // intersection upper bound.  For `B extends Number & Runnable`, B is a subtype of Runnable,
    // though Runnable, which is not a Number, cannot be B.  For `B extends Comparable<B>`, B is a
    // subtype of Comparable<?>, though neither Comparable<?> nor Comparable<B> can be B.
    if (unknownBound.hasUpperBound(type, substitution)) {
      return true;
    }
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
   * Returns true if the given type contains an unknown.
   *
   * @param type a type
   * @param substitution the type substitution, which determines the unknowns
   * @return true if {@code type} contains an unknown
   */
  /*package*/ static boolean hasUnknown(ReferenceType type, Substitution substitution) {
    for (TypeVariable variable : type.getTypeParameters()) {
      if (substitution.isUnknown(variable)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if {@code lowerType}, an array or class type that contains unknowns, can be chosen
   * to be a subtype of, or equal to, {@code type}, by comparing their component types or type
   * arguments.
   *
   * @param lowerType the possible subtype, which contains unknowns but is not an unknown
   * @param type the possible supertype
   * @param substitution the type substitution, which determines the unknowns
   * @return true if {@code lowerType} can be chosen to be a subtype of {@code type}
   */
  private static boolean compoundCanBeSubtype(
      ReferenceType lowerType, ReferenceType type, Substitution substitution) {
    if (lowerType.isArray() && type.isArray()) {
      Type componentType = ((ArrayType) lowerType).getComponentType();
      Type otherComponentType = ((ArrayType) type).getComponentType();
      if (componentType.isReferenceType() && otherComponentType.isReferenceType()) {
        return canBeSubtype(
            (ReferenceType) componentType, (ReferenceType) otherComponentType, substitution);
      }
      return componentType.equals(otherComponentType);
    }
    if (type instanceof InstantiatedType && lowerType instanceof ClassOrInterfaceType) {
      InstantiatedType supertype =
          ((ClassOrInterfaceType) lowerType)
              .getMatchingSupertype(((InstantiatedType) type).getGenericClassType());
      return supertype != null
          && argumentsCanBeContained(supertype, (ClassOrInterfaceType) type, substitution);
    }
    if (type instanceof NonParameterizedType
        && type.isParameterized()
        && lowerType instanceof ClassOrInterfaceType) {
      // A member class that declares no type parameters, of a parameterized type, such as
      // Outer<U>.Inner.  Its type arguments are those of its enclosing types; compare the enclosing
      // types, as in the same case in isSubtypeOrEqual.
      ClassOrInterfaceType supertype =
          findSupertypeOfClass((ClassOrInterfaceType) lowerType, type.getRuntimeClass());
      return supertype != null
          && argumentsCanBeContained(supertype, (ClassOrInterfaceType) type, substitution);
    }
    return false;
  }

  /**
   * Returns true if each type argument of {@code type} can contain the corresponding type argument
   * of {@code lowerType}, including the type arguments of the enclosing types of member classes.
   * This is {@link #argumentsContained}, but with the unknowns in the contained type arguments. The
   * two types must be instantiations of the same generic class.
   *
   * @param lowerType a type, which may contain unknowns
   * @param type a type
   * @param substitution the type substitution, which determines the unknowns
   * @return true if the type arguments of {@code type} can contain those of {@code lowerType}
   */
  private static boolean argumentsCanBeContained(
      ClassOrInterfaceType lowerType, ClassOrInterfaceType type, Substitution substitution) {
    if (type instanceof InstantiatedType) {
      if (!(lowerType instanceof InstantiatedType)) {
        return false;
      }
      List<TypeArgument> lowerTypeArguments = ((InstantiatedType) lowerType).getTypeArguments();
      List<TypeArgument> typeArguments = ((InstantiatedType) type).getTypeArguments();
      if (lowerTypeArguments.size() != typeArguments.size()) {
        return false;
      }
      for (int i = 0; i < typeArguments.size(); i++) {
        if (!canBeContained(typeArguments.get(i), lowerTypeArguments.get(i), substitution)) {
          return false;
        }
      }
    }
    if (lowerType.isMemberClass() && type.isMemberClass()) {
      return argumentsCanBeContained(lowerType.enclosingType, type.enclosingType, substitution);
    }
    return true;
  }

  /**
   * Returns true if the type argument {@code argument} can contain {@code lowerArgument}, which may
   * contain unknowns (JLS section 4.5.1).
   *
   * @param argument the possibly containing type argument
   * @param lowerArgument the possibly contained type argument, which may contain unknowns
   * @param substitution the type substitution, which determines the unknowns
   * @return true if {@code argument} can contain {@code lowerArgument}
   */
  private static boolean canBeContained(
      TypeArgument argument, TypeArgument lowerArgument, Substitution substitution) {
    if (argument instanceof ReferenceArgument) {
      // A type argument that is a type contains only the same type.
      return lowerArgument instanceof ReferenceArgument
          ? isSameType(
              ((ReferenceArgument) argument).getReferenceType(),
              ((ReferenceArgument) lowerArgument).getReferenceType(),
              substitution)
          : lowerArgument instanceof WildcardArgument
              && isSameWildcard(argument, (WildcardArgument) lowerArgument, substitution);
    }
    WildcardArgument wildcard = (WildcardArgument) argument;
    ReferenceType boundType = getBoundType(wildcard);
    if (boundType == null) {
      return false;
    }
    if (wildcard.hasUpperBound() && boundType.isObject()) {
      return true;
    }
    ReferenceType lowerArgumentType;
    if (lowerArgument instanceof ReferenceArgument) {
      lowerArgumentType = ((ReferenceArgument) lowerArgument).getReferenceType();
    } else {
      // ? extends L is contained by ? extends B if L <: B, and ? super L by ? super B if B <: L.
      WildcardArgument lowerWildcard = (WildcardArgument) lowerArgument;
      ReferenceType lowerBoundType = getBoundType(lowerWildcard);
      if (lowerWildcard.hasUpperBound() != wildcard.hasUpperBound() || lowerBoundType == null) {
        return false;
      }
      lowerArgumentType = lowerBoundType;
    }
    if (wildcard.hasUpperBound()) {
      // ? extends B contains T if T <: B.
      return canBeSubtype(lowerArgumentType, boundType, substitution);
    }
    // ? super B contains T if B <: T.
    return isSubtypeOrEqual(boundType, lowerArgumentType, substitution);
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
    if (isIdentical(type, otherType)) {
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
    if (!(type instanceof ClassOrInterfaceType && otherType instanceof ClassOrInterfaceType)) {
      return false;
    }
    ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
    ClassOrInterfaceType otherClassType = (ClassOrInterfaceType) otherType;
    // For a member class of a generic class, such as Outer<String>.Inner<Integer>, the enclosing
    // types must also be the same.
    if (classType.isMemberClass() != otherClassType.isMemberClass()) {
      return false;
    }
    if (classType.isMemberClass()
        && otherClassType.isMemberClass()
        && !isSameType(classType.enclosingType, otherClassType.enclosingType, substitution)) {
      return false;
    }
    if (type instanceof InstantiatedType != otherType instanceof InstantiatedType) {
      return false;
    }
    if (!(type instanceof InstantiatedType)) {
      return classType.getRuntimeClass().equals(otherClassType.getRuntimeClass());
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
    if (isIdentical(argument, otherWildcard)) {
      return true;
    }
    if (!(argument instanceof WildcardArgument)) {
      return false;
    }
    WildcardArgument wildcard = (WildcardArgument) argument;
    if (wildcard.hasUpperBound() != otherWildcard.hasUpperBound()) {
      return false;
    }
    ReferenceType boundType = getBoundType(wildcard);
    ReferenceType otherBoundType = getBoundType(otherWildcard);
    return boundType != null
        && otherBoundType != null
        && isSameType(boundType, otherBoundType, substitution);
  }

  /**
   * Returns the type of the bound of the given wildcard, or null if the bound is neither a {@link
   * ReferenceBound} nor a {@link LazyParameterBound} that can be converted to one. A lazy bound,
   * such as {@code Comparable<T>} in {@code ? extends Comparable<T>}, is converted so that it can
   * be compared with types.
   *
   * @param wildcard a wildcard type argument
   * @return the type of the bound of {@code wildcard}, or null
   */
  private static @Nullable ReferenceType getBoundType(WildcardArgument wildcard) {
    ParameterBound bound = wildcard.getTypeBound();
    if (bound instanceof LazyParameterBound) {
      bound = ((LazyParameterBound) bound).toReferenceBound();
    }
    return bound instanceof ReferenceBound ? ((ReferenceBound) bound).getBoundType() : null;
  }

  /**
   * Returns true if the two types are equal, including the enclosing types of member classes at any
   * depth. {@link InstantiatedType#equals(Object)} does not compare enclosing types, so it treats
   * {@code Outer<String>.Inner<Integer>} and {@code Outer<Number>.Inner<Integer>} as equal.
   *
   * @param type a type
   * @param otherType another type
   * @return true if the types are equal, including their enclosing types
   */
  /*package*/ static boolean isIdentical(ReferenceType type, ReferenceType otherType) {
    if (!type.equals(otherType)) {
      return false;
    }
    if (type.isArray() && otherType.isArray()) {
      Type componentType = ((ArrayType) type).getComponentType();
      Type otherComponentType = ((ArrayType) otherType).getComponentType();
      return !(componentType.isReferenceType() && otherComponentType.isReferenceType())
          || isIdentical((ReferenceType) componentType, (ReferenceType) otherComponentType);
    }
    if (!(type instanceof ClassOrInterfaceType && otherType instanceof ClassOrInterfaceType)) {
      return true;
    }
    ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
    ClassOrInterfaceType otherClassType = (ClassOrInterfaceType) otherType;
    if (classType.isMemberClass() != otherClassType.isMemberClass()) {
      return false;
    }
    if (classType.isMemberClass()
        && otherClassType.isMemberClass()
        && !isIdentical(classType.enclosingType, otherClassType.enclosingType)) {
      return false;
    }
    if (type instanceof InstantiatedType && otherType instanceof InstantiatedType) {
      List<TypeArgument> typeArguments = ((InstantiatedType) type).getTypeArguments();
      List<TypeArgument> otherTypeArguments = ((InstantiatedType) otherType).getTypeArguments();
      for (int i = 0; i < typeArguments.size(); i++) {
        if (!isIdentical(typeArguments.get(i), otherTypeArguments.get(i))) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Returns true if the two type arguments are equal, including the enclosing types of member
   * classes at any depth; see {@link #isIdentical(ReferenceType, ReferenceType)}.
   *
   * @param argument a type argument
   * @param otherArgument another type argument
   * @return true if the type arguments are equal, including their enclosing types
   */
  private static boolean isIdentical(TypeArgument argument, TypeArgument otherArgument) {
    if (!argument.equals(otherArgument)) {
      return false;
    }
    if (argument instanceof ReferenceArgument && otherArgument instanceof ReferenceArgument) {
      return isIdentical(
          ((ReferenceArgument) argument).getReferenceType(),
          ((ReferenceArgument) otherArgument).getReferenceType());
    }
    if (argument instanceof WildcardArgument && otherArgument instanceof WildcardArgument) {
      ParameterBound bound = ((WildcardArgument) argument).getTypeBound();
      ParameterBound otherBound = ((WildcardArgument) otherArgument).getTypeBound();
      if (bound instanceof ReferenceBound && otherBound instanceof ReferenceBound) {
        return isIdentical(
            ((ReferenceBound) bound).getBoundType(), ((ReferenceBound) otherBound).getBoundType());
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
