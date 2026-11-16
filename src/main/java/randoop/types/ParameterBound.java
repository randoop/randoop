package randoop.types;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.plumelib.util.CollectionsPlume;

/**
 * Represents a type bound on a type variable or wildcard occurring as a type parameter of a generic
 * class, interface, method or constructor.
 *
 * <p>Type bounds for explicitly defined type variables of generic declarations are defined in <a
 * href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.1.2">JLS section
 * 8.1.2</a> as
 *
 * <pre>
 *   TypeBound:
 *     extends TypeVariable
 *     extends ClassOrInterfaceType [ &amp; InterfaceType ]
 * </pre>
 *
 * Type bounds for wildcards may be any reference type, which includes type variables, so the {@link
 * ParameterBound} class hierarchy is simplified to use these {@link ReferenceType} objects as
 * bounds. Intersection types (which include the greatest lower bound construction used in capture
 * conversion) are explicitly represented. And, recursive type bounds are avoided by holding any
 * {@code java.lang.reflect.Type} with variables as a {@link LazyParameterBound}.
 *
 * <p>Type parameters only have upper bounds, but variables introduced by capture conversion can
 * have lower bounds. This class and its subclasses can represent both, with the default lower bound
 * being {@link JavaTypes#NULL_TYPE}, and the default upper bound being {@link
 * JavaTypes#OBJECT_TYPE}.
 *
 * @see EagerReferenceBound
 * @see IntersectionTypeBound
 * @see LazyParameterBound
 */
public abstract class ParameterBound {

  /** Creates a ParameterBound. */
  public ParameterBound() {}

  /**
   * Constructs a parameter bound given a {@link ReferenceType}.
   *
   * @param type the {@link ReferenceType}
   * @return a {@link EagerReferenceBound} with the given type
   */
  public static ParameterBound forType(ReferenceType type) {
    return new EagerReferenceBound(type);
  }

  /**
   * Creates a bound from the array of bounds of a {@code java.lang.reflect.TypeVariable}.
   *
   * <p>The bounds of a type parameter are restricted, but those of a wildcard may be any reference
   * type. See <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-8.html#jls-8.1.2">JLS
   * section 8.1.2</a>.
   *
   * @param variableSet the set of variables affected by this bound
   * @param bounds the type bounds
   * @return the {@code ParameterBound} for the given types
   */
  /*package*/ static ParameterBound forTypes(
      Set<java.lang.reflect.TypeVariable<?>> variableSet, java.lang.reflect.Type[] bounds) {
    if (bounds == null) {
      throw new IllegalArgumentException("bounds must be non-null");
    }

    if (bounds.length == 1) {
      return forType(variableSet, bounds[0]);
    } else {
      List<ParameterBound> boundList =
          CollectionsPlume.mapList(
              (java.lang.reflect.Type type) -> forType(variableSet, type), bounds);
      return new IntersectionTypeBound(boundList);
    }
  }

  /**
   * Creates a {@code ParameterBound} object from a single {@code java.lang.reflect.Type}. The
   * result is an eager bound if the type is a type variable, or if it is a class, parameterized
   * type, or generic array type that has no type variable in the sense of {@link #hasTypeVariable}.
   * Otherwise, the result is a {@link LazyParameterBound}.
   *
   * @param variableSet the set of type variables bound by this type
   * @param type the type for type bound
   * @return a type bound that ensures the given type is satisfied as an upper bound
   */
  /*package*/ static ParameterBound forType(
      Set<java.lang.reflect.TypeVariable<?>> variableSet, java.lang.reflect.Type type) {
    if (isTypeVariable(type)
        || ((type instanceof Class<?>
                || type instanceof java.lang.reflect.ParameterizedType
                || type instanceof java.lang.reflect.GenericArrayType)
            && !hasTypeVariable(type, variableSet))) {
      return new EagerReferenceBound(eagerBoundType(type));
    }
    return new LazyParameterBound(type);
  }

  /**
   * Returns the type of an eager bound for the given (reflection) type. A parameterized type,
   * including one that is the element type of a generic array type, is converted by {@link
   * ParameterizedType#forType}, except that a non-generic member class of a generic class, as in
   * {@code Outer<String>.Inner}, is converted by {@link ClassOrInterfaceType#forType}, which
   * preserves its owner type. {@link ParameterizedType#forType} ignores owner types.
   *
   * @param type a class, parameterized type, generic array type, or type variable
   * @return the type that {@code type} represents
   */
  private static ReferenceType eagerBoundType(java.lang.reflect.Type type) {
    if (isNonGenericMemberClass(type)) {
      return ClassOrInterfaceType.forType(type);
    }
    if (type instanceof java.lang.reflect.ParameterizedType) {
      return ParameterizedType.forType(type);
    }
    if (type instanceof java.lang.reflect.GenericArrayType) {
      return ArrayType.ofComponentType(
          eagerBoundType(((java.lang.reflect.GenericArrayType) type).getGenericComponentType()));
    }
    // A class can be an array class, as in `? extends String[]`.
    return ReferenceType.forType(type);
  }

  /**
   * Returns true if the given (reflection) type is a non-generic member class of a generic class,
   * as in {@code Outer<String>.Inner}. Reflection represents such a type as a parameterized type
   * whose raw type has no type parameters.
   *
   * @param type a reflection type
   * @return true if {@code type} is a non-generic member class of a generic class
   */
  /*package*/ static boolean isNonGenericMemberClass(java.lang.reflect.Type type) {
    return type instanceof java.lang.reflect.ParameterizedType
        && ((Class<?>) ((java.lang.reflect.ParameterizedType) type).getRawType())
                .getTypeParameters()
                .length
            == 0;
  }

  /**
   * Applies the given substitution to this type bound by replacing type variables.
   *
   * <p>If the substitution changes nothing in this bound, the result should be this bound itself,
   * so that a caller can detect a change by comparing the result to this bound with {@code ==}.
   * Comparing with {@code equals} would not detect a change to only the bounds of a type variable,
   * because copies of a type variable are equal even if their bounds differ. A result that is a new
   * object for the same bound is correct, but makes callers create needless copies.
   *
   * @param substitution the type substitution
   * @return this bound with the type after the substitution has been applied
   */
  public abstract ParameterBound substitute(Substitution substitution);

  /**
   * Returns any type parameters in the type of this bound.
   *
   * @return the list of {@code TypeVariable} objects in this bound
   */
  public abstract List<TypeVariable> getTypeParameters();

  /**
   * Returns true if the given (reflection) type has a type variable that requires a lazy bound.
   * Such a type variable is a member of {@code variableSet}, or a type variable whose bound has
   * such a type variable. Either would make an eager bound recursive.
   *
   * <p>Owner types are examined only for a non-generic member class of a generic class, as in
   * {@code Outer<T>.Inner}, which {@link #eagerBoundType} converts with its owner type. It converts
   * each other parameterized type by {@link ParameterizedType#forType}, which ignores owner types.
   *
   * @param type a reflection type
   * @param variableSet the type variables bound by the type
   * @return true if the type has a type variable that requires a lazy bound
   */
  private static boolean hasTypeVariable(
      java.lang.reflect.Type type, Set<java.lang.reflect.TypeVariable<?>> variableSet) {
    if (isTypeVariable(type)) {
      java.lang.reflect.TypeVariable<?> variable = (java.lang.reflect.TypeVariable) type;
      if (variableSet.contains(variable)) {
        return true;
      }
      Set<java.lang.reflect.TypeVariable<?>> recursiveSet = new HashSet<>(variableSet);
      recursiveSet.add(variable);
      for (java.lang.reflect.Type boundType : variable.getBounds()) {
        if (hasTypeVariable(boundType, recursiveSet)) {
          return true;
        }
      }
    }
    if (type instanceof java.lang.reflect.ParameterizedType) {
      java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) type;
      for (java.lang.reflect.Type argType : pt.getActualTypeArguments()) {
        if (hasTypeVariable(argType, variableSet)) {
          return true;
        }
      }
      if (isNonGenericMemberClass(pt) && hasTypeVariable(pt.getOwnerType(), variableSet)) {
        return true;
      }
    }
    if (type instanceof java.lang.reflect.GenericArrayType) {
      return hasTypeVariable(
          ((java.lang.reflect.GenericArrayType) type).getGenericComponentType(), variableSet);
    }
    if (type instanceof java.lang.reflect.WildcardType) {
      java.lang.reflect.WildcardType wt = (java.lang.reflect.WildcardType) type;
      for (java.lang.reflect.Type boundType : wt.getUpperBounds()) {
        if (hasTypeVariable(boundType, variableSet)) {
          return true;
        }
      }
      for (java.lang.reflect.Type boundType : wt.getLowerBounds()) {
        if (hasTypeVariable(boundType, variableSet)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true if the type is a type variable.
   *
   * @param type the type to test
   * @return true if the type is a type variable, false otherwise
   */
  /*package*/ static boolean isTypeVariable(java.lang.reflect.Type type) {
    return type instanceof java.lang.reflect.TypeVariable;
  }

  /**
   * Returns true if the type of this bound has a capture variable.
   *
   * @return true iff this bound has a capture variable
   */
  /*package*/ abstract boolean hasCaptureVariable();

  /**
   * Returns true if the type of this bound is generic.
   *
   * @return true, if this bound type is generic, and false otherwise
   */
  /*package*/ final boolean isGeneric() {
    return isGeneric(false);
  }

  /**
   * Returns true if the type of this bound is generic.
   *
   * @param ignoreWildcards if true, ignore wildcards; that is, treat wildcards as not making the
   *     operation generic
   * @return true, if this bound type is generic, and false otherwise
   */
  public abstract boolean isGeneric(boolean ignoreWildcards);

  /**
   * Returns true if this bound is a lower bound of the given argument type.
   *
   * @param argType the concrete argument type
   * @param subst the substitution
   * @return true if this bound is a subtype of the given type
   */
  public abstract boolean isLowerBound(Type argType, Substitution subst);

  /**
   * Returns true if this bound, after applying the substitution to it, is a lower bound on the type
   * of the given bound. The substitution is not applied to the given bound.
   *
   * @param bound the other bound
   * @param substitution the type substitution to apply to this bound
   * @return true iff this bound is a lower bound on the type of the given bound
   */
  /*package*/ boolean isLowerBound(ParameterBound bound, Substitution substitution) {
    return bound.hasLowerBound(this, substitution);
  }

  /**
   * Returns true if the given bound, after applying the substitution to it, is a lower bound on the
   * type of this bound. The substitution is not applied to this bound. {@code
   * b.hasLowerBound(lowerBound, substitution)} is equivalent to {@code lowerBound.isLowerBound(b,
   * substitution)}.
   *
   * @param lowerBound the possible lower bound
   * @param substitution the type substitution to apply to {@code lowerBound}
   * @return true if {@code lowerBound} is a lower bound on the type of this bound
   */
  abstract boolean hasLowerBound(ParameterBound lowerBound, Substitution substitution);

  /**
   * Returns true if this bound, after applying the substitution to it, is the null type.
   *
   * @param substitution the type substitution to apply to this bound
   * @return true if this bound is the null type
   */
  final boolean isNullType(Substitution substitution) {
    ParameterBound b = this.substitute(substitution);
    return b instanceof ReferenceBound
        && ((ReferenceBound) b).getBoundType().equals(JavaTypes.NULL_TYPE);
  }

  /**
   * Returns true if this bound is {@code Object}.
   *
   * @return true if this bound is {@code Object}, false otherwise
   */
  public abstract boolean isObject();

  /**
   * Returns true if the type of this bound is a subtype of the type of the given bound.
   *
   * @param boundType the other bound
   * @return true if this type is a subtype of the other bound, false otherwise
   */
  public abstract boolean isSubtypeOfOrEqualTo(ParameterBound boundType);

  /**
   * Determines if this bound is an upper bound for the argument type.
   *
   * @param argType the concrete argument type
   * @param subst the substitution
   * @return true if this bound is satisfied by the concrete type when the substitution is used on
   *     the bound, false otherwise
   */
  public abstract boolean isUpperBound(Type argType, Substitution subst);

  /**
   * Returns true if this bound is an upper bound on the type of the given bound with respect to the
   * type substitution.
   *
   * <p>Some type variables in this bound stand for unknown types, which may be any types that
   * satisfy their bounds; see {@link Substitution#isUnknown(ReferenceType)}. An example is another
   * type parameter of the declaration of the variable whose bound this is. Every other type
   * variable, such as one in the given bound, stands for itself.
   *
   * @param bound the other bound
   * @param substitution the type substitution
   * @return true if this bound is an upper bound on the type of the given bound, false otherwise
   */
  /*package*/ abstract boolean isUpperBound(ParameterBound bound, Substitution substitution);

  /**
   * Returns true if the given type is an upper bound on every type that has this bound as an upper
   * bound.
   *
   * @param upperBound the possible upper bound, to which {@code substitution} has been applied
   * @param substitution the type substitution, which determines which type variables in {@code
   *     upperBound} stand for unknown types (see {@link Substitution#isUnknown(ReferenceType)})
   * @return true if {@code upperBound} is an upper bound on every type whose upper bound is this
   */
  abstract boolean hasUpperBound(ReferenceType upperBound, Substitution substitution);

  /**
   * Returns true if this bound is a type variable.
   *
   * @return true if this bound is a type variable, false otherwise
   */
  public boolean isVariable() {
    return false;
  }
}
