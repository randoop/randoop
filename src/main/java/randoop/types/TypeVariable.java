package randoop.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

/** An abstract class representing type variables. */
public abstract class TypeVariable extends ParameterType {

  /**
   * The checks by {@link #canBeInstantiatedBy(ReferenceType, Substitution)} that are in progress in
   * the current thread, outermost first.
   */
  @SuppressWarnings("nullness:type.argument") // withInitial: get() never returns null
  private static final ThreadLocal<List<InstantiationCheck>> instantiationChecks =
      ThreadLocal.withInitial(ArrayList::new);

  /**
   * A check of whether a type variable can be instantiated by a type, with the other type variables
   * instantiated as by a substitution.
   */
  private static final class InstantiationCheck {
    /** The type variable. */
    final TypeVariable variable;

    /** The type that might instantiate the variable. */
    final ReferenceType type;

    /** The substitution for all the type variables, including {@code variable}. */
    final Substitution substitution;

    /**
     * Creates a check of whether a type variable can be instantiated by a type.
     *
     * @param variable a type variable
     * @param type the type that might instantiate the variable
     * @param substitution the substitution for all the type variables, including {@code variable}
     */
    InstantiationCheck(TypeVariable variable, ReferenceType type, Substitution substitution) {
      this.variable = variable;
      this.type = type;
      this.substitution = substitution;
    }

    /**
     * Returns true if this check asks the same question as the given one: the type variables are
     * identical, including their bounds, and the types and substitutions are equal.
     *
     * @param variable a type variable
     * @param type the type that might instantiate the variable
     * @param substitution the substitution for all the type variables, including {@code variable}
     * @return true if this check asks the same question as the given one
     */
    boolean isSameCheck(TypeVariable variable, ReferenceType type, Substitution substitution) {
      return this.type.equals(type)
          && this.substitution.equals(substitution)
          && this.variable.isIdenticalTo(variable);
    }
  }

  /**
   * The type variable that {@link #forType(java.lang.reflect.Type)} returns for each type
   * parameter, with the bounds that the type parameter declares.
   *
   * <p>Reusing these objects keeps finite the structure that {@link #isIdenticalTo(TypeVariable)}
   * compares: {@link LazyParameterBound#getTypeParameters()} returns the type variables of its
   * bound by calling {@link #forType(java.lang.reflect.Type)}, so creating new objects there would
   * create new bounds to compare, without end, for bounds such as {@code <V extends Map<W, A>, W
   * extends List<V>, A extends V>}.
   */
  private static final Map<java.lang.reflect.TypeVariable<?>, TypeVariable> declaredTypeVariables =
      new ConcurrentHashMap<>();

  /**
   * Creates a type variable with {@link NullReferenceType} as the lower bound, and the {@code
   * Object} type as upper bound.
   */
  /*package*/ TypeVariable() {
    super();
  }

  /**
   * Creates a type variable with the given type bounds. Assumes the bounds are consistent and does
   * not check for the subtype relationship.
   *
   * @param lowerBound the lower type bound on this variable
   * @param upperBound the upper type bound on this variable
   */
  /*package*/ TypeVariable(ParameterBound lowerBound, ParameterBound upperBound) {
    super(lowerBound, upperBound);
  }

  /**
   * Creates a {@code TypeVariable} object for a given {@code java.lang.reflect.Type} reference,
   * which must be a {@code java.lang.reflect.TypeVariable}.
   *
   * @param type the type reference
   * @return the {@code TypeVariable} for the given type
   */
  public static TypeVariable forType(java.lang.reflect.Type type) {
    if (!(type instanceof java.lang.reflect.TypeVariable<?>)) {
      throw new IllegalArgumentException("type must be a type variable, got " + type);
    }
    java.lang.reflect.TypeVariable<?> v = (java.lang.reflect.TypeVariable) type;
    TypeVariable result = declaredTypeVariables.get(v);
    if (result != null) {
      return result;
    }
    Set<java.lang.reflect.TypeVariable<?>> variableSet = new HashSet<>(1);
    variableSet.add(v);
    // Computing the bound can call this method for other type variables, so this method does not
    // use ConcurrentHashMap.computeIfAbsent, which forbids such a recursive update.
    result = new ExplicitTypeVariable(v, ParameterBound.forTypes(variableSet, v.getBounds()));
    TypeVariable previous = declaredTypeVariables.putIfAbsent(v, result);
    return previous != null ? previous : result;
  }

  @Override
  public ReferenceType substitute(Substitution substitution) {
    ReferenceType type = substitution.get(this);
    if (type != null) {
      return type;
    }
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns false, since an uninstantiated type variable may not be assigned to.
   */
  @Override
  public boolean isAssignableFrom(Type sourceType) {
    return false;
  }

  @Override
  public boolean isInstantiationOf(ReferenceType otherType) {
    if (super.isInstantiationOf(otherType)) {
      return true;
    }

    if (otherType.isVariable()) {
      TypeVariable otherVariable = (TypeVariable) otherType;
      Substitution substitution = otherVariable.getSubstitution(this);
      boolean lowerboundOk =
          otherVariable.getLowerTypeBound().isLowerBound(getLowerTypeBound(), substitution);
      boolean upperboundOk =
          otherVariable.getUpperTypeBound().isUpperBound(getUpperTypeBound(), substitution);
      return lowerboundOk && upperboundOk;
    }

    return false;
  }

  @Override
  public boolean isSubtypeOfOrEqualTo(Type otherType) {
    if (super.isSubtypeOfOrEqualTo(otherType)) {
      return true;
    }
    if (otherType.isReferenceType()) {
      // A type variable is a subtype of every supertype of its upper bound.
      return isSubtypeOfOrEqualTo(getUpperTypeBound(), otherType);
    }
    return false;
  }

  /**
   * Returns true if the type of the given bound is a subtype of the given type. An intersection
   * bound is a subtype of the type if any of its members is.
   *
   * <p>This method compares the bound type as written, without substituting for the type variables
   * in it. So, a variable {@code E} whose bound is {@code Enum<E>} is a subtype of {@code Enum<E>}.
   * Substituting {@code otherType} for {@code E} would instead compare {@code Enum<Enum<E>>} with
   * {@code Enum<E>}.
   *
   * <p>A {@link LazyParameterBound}, which can mention type variables other than the bounded one
   * (as in {@code T extends Comparable<U>}), is converted to the equivalent {@link ReferenceBound},
   * in which every type variable remains a type variable.
   *
   * @param upperBound an upper bound of a type variable
   * @param otherType the possible supertype
   * @return true if the type of {@code upperBound} is a subtype of {@code otherType}
   */
  private static boolean isSubtypeOfOrEqualTo(ParameterBound upperBound, Type otherType) {
    if (upperBound instanceof LazyParameterBound) {
      ReferenceBound converted = ((LazyParameterBound) upperBound).toReferenceBound();
      return converted != null && isSubtypeOfOrEqualTo(converted, otherType);
    }
    if (upperBound instanceof ReferenceBound) {
      return ((ReferenceBound) upperBound).getBoundType().isSubtypeOfOrEqualTo(otherType);
    }
    if (upperBound instanceof IntersectionTypeBound) {
      for (ParameterBound member : ((IntersectionTypeBound) upperBound).getBounds()) {
        if (isSubtypeOfOrEqualTo(member, otherType)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns a substitution of the given {@link ReferenceType} for this variable.
   *
   * @param otherType a replacement type
   * @return a substitution that replaces this variable with {@code otherType}
   */
  private Substitution getSubstitution(ReferenceType otherType) {
    return new Substitution(this, otherType);
  }

  @Override
  public boolean isVariable() {
    return true;
  }

  /**
   * Returns true if this {@link TypeVariable} can be instantiated by the {@link ReferenceType}.
   * Does not require that all bounds of this variable be instantiated.
   *
   * <p>A type variable in the bounds that is not instantiated stands for an unknown type, as in
   * {@link LazyParameterBound#isUpperBound(Type, Substitution)}, and each occurrence of it is
   * checked separately. So the result can be true even if no single type for that type variable
   * satisfies every occurrence. For example, for {@code <S extends Map<T, T>, T>}, {@code S} can be
   * instantiated by a class that implements {@code Map<A, B>}. A client that instantiates all the
   * type variables should check the bounds again with all of them instantiated, as {@link
   * randoop.reflection.TypeInstantiator} does.
   *
   * @param otherType the possibly instantiating type, not a variable
   * @return true if the given type can instantiate this variable, false otherwise
   */
  /*package*/ boolean canBeInstantiatedBy(ReferenceType otherType) {
    return canBeInstantiatedBy(otherType, new Substitution());
  }

  /**
   * Returns true if this {@link TypeVariable} can be instantiated by the {@link ReferenceType},
   * when the other type variables are instantiated as by the given substitution. Like {@link
   * #canBeInstantiatedBy(ReferenceType)}, but checks this variable's bounds with the mappings of
   * {@code context} for the type variables in them, rather than treating all of those type
   * variables as unknown.
   *
   * @param otherType the possibly instantiating type, not a variable
   * @param context a substitution for other type variables; any mapping of this variable in it is
   *     ignored
   * @return true if the given type can instantiate this variable, false otherwise
   */
  boolean canBeInstantiatedBy(ReferenceType otherType, Substitution context) {
    Substitution substitution = context.withMapping(this, otherType);
    // Checking a bound can check the same instantiation again, through mutually recursive bounds.
    // As for any recursive bound, assume that the inner check succeeds.  Only an inner check of
    // the same question can be assumed to succeed:  a check with other bounds or another
    // substitution can fail when this one does not.
    List<InstantiationCheck> inProgress = instantiationChecks.get();
    for (InstantiationCheck check : inProgress) {
      if (check.isSameCheck(this, otherType, substitution)) {
        return true;
      }
    }
    inProgress.add(new InstantiationCheck(this, otherType, substitution));
    try {
      return canBeInstantiatedByUnchecked(otherType, substitution);
    } finally {
      inProgress.remove(inProgress.size() - 1);
    }
  }

  /**
   * Returns true if this {@link TypeVariable} can be instantiated by the {@link ReferenceType}.
   * Does not guard against recursion; see {@link #canBeInstantiatedBy(ReferenceType,
   * Substitution)}.
   *
   * @param otherType a possibly instantiating type, not a variable
   * @param substitution a substitution for the other type variables, which maps this variable to
   *     {@code otherType}
   * @return true if the given type can instantiate this variable, false otherwise
   */
  private boolean canBeInstantiatedByUnchecked(ReferenceType otherType, Substitution substitution) {
    if (getLowerTypeBound().isVariable()) {
      if (!variableBoundAllows(getLowerTypeBound(), true, substitution, otherType)) {
        return false;
      }
    } else {
      if (!getLowerTypeBound().isLowerBound(otherType, substitution)) {
        return false;
      }
    }
    if (getUpperTypeBound().isVariable()) {
      if (!variableBoundAllows(getUpperTypeBound(), false, substitution, otherType)) {
        return false;
      }
    } else {
      if (!getUpperTypeBound().isUpperBound(otherType, substitution)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if {@code otherType} is within the given bound of this variable, which is a type
   * variable. If the substitution instantiates that type variable with a type that is not a
   * variable, compares {@code otherType} with that type. Otherwise, that type variable stands for
   * an unknown type. For a lower bound, the result is true if it can be a subtype of {@code
   * otherType}, as determined by {@link #canBeSubtypeOf}; for an upper bound, the result is true if
   * it can be a supertype of {@code otherType}, as determined by {@link #canBeSupertypeOf}.
   *
   * @param bound a bound of this variable that is a type variable
   * @param isLower true if {@code bound} is the lower bound of this variable, false if it is the
   *     upper bound
   * @param substitution the substitution of {@code otherType} for this variable
   * @param otherType the possibly instantiating type, not a variable
   * @return true if {@code otherType} is within {@code bound}
   */
  private boolean variableBoundAllows(
      ParameterBound bound, boolean isLower, Substitution substitution, ReferenceType otherType) {
    ParameterBound substituted = bound.substitute(substitution);
    TypeVariable variable;
    if (substituted instanceof ReferenceBound
        && ((ReferenceBound) substituted).getBoundType().isVariable()) {
      variable = (TypeVariable) ((ReferenceBound) substituted).getBoundType();
    } else if (substituted instanceof LazyParameterBound) {
      // The substitution does not instantiate the type variable.
      variable = substituted.getTypeParameters().get(0);
    } else if (substituted instanceof ReferenceBound) {
      // The substitution instantiates the type variable with a type that is not a variable.
      return isLower
          ? substituted.isLowerBound(otherType, substitution)
          : substituted.isUpperBound(otherType, substitution);
    } else {
      return false;
    }
    return isLower
        ? variable.canBeSubtypeOf(otherType, substitution)
        : variable.canBeSupertypeOf(otherType, substitution);
  }

  /**
   * Returns true if some instantiation of this type variable can be a subtype of the given type,
   * when the other type variables are instantiated as by the given substitution. The instantiations
   * that this considers are {@code otherType} itself, as {@link #canBeInstantiatedBy(ReferenceType,
   * Substitution)} checks, and the upper bound of this variable, which is a subtype of every
   * supertype of it. It considers the upper bound only if the upper bound does not contain this
   * variable, as in {@code T extends Comparable<T>}, because the upper bound is then not a type
   * that this variable can be. So the result can be false even if some other subtype of {@code
   * otherType} can instantiate this variable.
   *
   * <p>For example, for {@code U extends Integer}, the result is true for {@code Number}, because
   * {@code U} can be {@code Integer}.
   *
   * @param otherType a type, not a variable
   * @param context a substitution for other type variables
   * @return true if some instantiation of this variable can be a subtype of {@code otherType}
   */
  boolean canBeSubtypeOf(ReferenceType otherType, Substitution context) {
    if (canBeInstantiatedBy(otherType, context)) {
      return true;
    }
    ParameterBound upperBound = getUpperTypeBound();
    return !upperBound.getTypeParameters().contains(this)
        && upperBound.isLowerBound(otherType, context);
  }

  /**
   * Returns true if some instantiation of this type variable can be a supertype of the given type,
   * when the other type variables are instantiated as by the given substitution. The instantiations
   * that this considers are the lower bound of this variable, which is a supertype of every subtype
   * of it, if it is within the upper bound of this variable; {@code otherType} itself; and each of
   * the strict supertypes of {@code otherType} that {@link #getStrictSupertypes(ReferenceType)}
   * returns, such as {@code Object}. For example, for {@code <T extends Comparable<T>, V extends
   * T>}, {@code java.sql.Date} can instantiate {@code V}, because {@code java.util.Date} can
   * instantiate {@code T}. Each instantiation other than the lower bound is checked as by {@link
   * #canBeInstantiatedBy(ReferenceType, Substitution)}.
   *
   * @param otherType a type, not a variable
   * @param context a substitution for other type variables
   * @return true if some instantiation of this variable can be a supertype of {@code otherType}
   */
  boolean canBeSupertypeOf(ReferenceType otherType, Substitution context) {
    if ((getLowerTypeBound().isUpperBound(otherType, context) && lowerBoundCanInstantiate(context))
        || canBeInstantiatedBy(otherType, context)) {
      return true;
    }
    List<TypeVariable> otherTypeParameters = otherType.getTypeParameters();
    for (ReferenceType supertype : getStrictSupertypes(otherType)) {
      // getStrictSupertypes omits a supertype of a class or interface type that mentions a type
      // variable that the class or interface type does not.  This check also omits such a
      // supertype that arises from the upper bound of a type variable, as for the component type of
      // an array type.  A supertype that mentions only the type variables of otherType, such as
      // List<E> for ArrayList<E> or Parent<String> for `Child<X> extends Parent<String>`, is a
      // supertype of otherType.
      if (otherTypeParameters.containsAll(supertype.getTypeParameters())
          && canBeInstantiatedBy(supertype, context)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true unless the lower bound of this variable is a type, not a type variable, that
   * cannot instantiate this variable when the other type variables are instantiated as by the given
   * substitution. For example, a capture variable with lower bound {@code String} and upper bound
   * {@code K} cannot be instantiated by {@code String} if {@code K} is {@code Integer}, so no type
   * can instantiate it.
   *
   * @param context a substitution for other type variables
   * @return false if the lower bound of this variable is a type that cannot instantiate it
   */
  private boolean lowerBoundCanInstantiate(Substitution context) {
    ParameterBound lowerBound = getLowerTypeBound();
    if (!(lowerBound instanceof ReferenceBound)) {
      return true;
    }
    ReferenceType lowerType = ((ReferenceBound) lowerBound).getBoundType().substitute(context);
    if (lowerType.isVariable() || lowerType instanceof NullReferenceType) {
      return true;
    }
    return canBeInstantiatedBy(lowerType, context);
  }

  /**
   * Returns strict supertypes of the given type, including {@code Object}:
   *
   * <ul>
   *   <li>For a class or interface type, its strict supertypes, as computed by {@link
   *       ClassOrInterfaceType#getSupertypesStrict()}, and {@code Object}, which that method omits
   *       for an interface. For the type and each of those supertypes that is a parameterized type
   *       {@code C<T1, ..., Tn>}, also {@code C<?, ..., ?>}. Other wildcard-widened types, such as
   *       {@code C<? extends T1>}, are omitted.
   *   <li>For an array type {@code C[]}, {@code Object}, {@code Cloneable}, {@code Serializable},
   *       and, if {@code C} is a reference type, {@code S[]} for each strict supertype {@code S} of
   *       {@code C} that this method returns.
   *   <li>For a type variable, the types in its upper bound and their strict supertypes, and {@code
   *       Object}; this lets an array type such as {@code U[]}, for {@code U extends Number}, have
   *       supertypes such as {@code Number[]} and {@code Object[]}.
   *   <li>For any other type, {@code Object}.
   * </ul>
   *
   * <p>A supertype of a class or interface type that mentions a type variable that the class or
   * interface type does not is omitted. The supertypes of a GenericClassType are computed as
   * declared, as {@code Parent<String>} for {@code Child<X> extends Parent<String>}, so they
   * mention only its type variables, but this guards against any supertype that does not preserve
   * the relationship between the type parameters. A supertype that mentions only the type variables
   * of the type, such as {@code List<E>} for {@code ArrayList<E>}, is a supertype of the type. For
   * a class or interface type with a wildcard argument, the supertypes are those of its capture
   * conversion, so the supertypes that mention a capture variable are omitted.
   *
   * @param type a reference type
   * @return strict supertypes of {@code type}
   */
  private static Set<ReferenceType> getStrictSupertypes(ReferenceType type) {
    Set<ReferenceType> result = new LinkedHashSet<>();
    if (type instanceof ClassOrInterfaceType) {
      List<TypeVariable> typeParameters = type.getTypeParameters();
      ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
      if (classType instanceof GenericClassType) {
        // The supertypes of a GenericClassType are those of its raw class, as in Parent<P> for
        // `Child<X> extends Parent<String>`.  The supertypes of the equivalent InstantiatedType,
        // whose type arguments are the class's own type parameters, are as declared, as in
        // Parent<String>.
        GenericClassType genericType = (GenericClassType) classType;
        InstantiatedType instantiated =
            new InstantiatedType(genericType, genericType.getTypeArguments());
        instantiated.enclosingType = genericType.enclosingType;
        classType = instantiated;
      } else if (classType.hasWildcardArgument()) {
        // Computing the supertypes of a type with a wildcard argument would substitute the
        // wildcard for a type parameter, which fails if the type parameter is the component type of
        // an array type in a supertype, as for `C<T> extends Base<T[]>`.  The supertypes of the
        // capture conversion that mention a capture variable are omitted by the test below.
        classType = classType.applyCaptureConversion();
      }
      addWildcardWidening(type, result);
      for (ClassOrInterfaceType supertype : classType.getSupertypesStrict()) {
        if (typeParameters.containsAll(supertype.getTypeParameters())) {
          result.add(supertype);
          addWildcardWidening(supertype, result);
        }
      }
    } else if (type instanceof ArrayType) {
      result.add(JavaTypes.OBJECT_TYPE);
      result.add(JavaTypes.CLONEABLE_TYPE);
      result.add(JavaTypes.SERIALIZABLE_TYPE);
      Type componentType = ((ArrayType) type).getComponentType();
      if (componentType instanceof ReferenceType) {
        // Arrays are covariant:  S[] is a subtype of T[] if S is a subtype of T.
        for (ReferenceType componentSupertype :
            getStrictSupertypes((ReferenceType) componentType)) {
          result.add(ArrayType.ofComponentType(componentSupertype));
        }
      }
    } else if (type instanceof TypeVariable) {
      ParameterBound upperBound = ((TypeVariable) type).getUpperTypeBound();
      for (ReferenceType boundType : getUpperBoundTypes(upperBound)) {
        result.add(boundType);
        result.addAll(getStrictSupertypes(boundType));
      }
    }
    result.add(JavaTypes.OBJECT_TYPE);
    result.remove(type);
    return result;
  }

  /**
   * If the given type is a parameterized type {@code C<T1, ..., Tn>} whose type arguments are not
   * all {@code ?}, adds {@code C<?, ..., ?>}, which is a supertype of it, to the given set. For
   * example, for {@code C<X> implements Comparable<C<?>>}, {@code C<?>} is a supertype of {@code
   * C<String>} that is comparable to itself, though {@code C<String>} is not.
   *
   * @param type a reference type
   * @param result the set to which to add the widened type; side-effected
   */
  private static void addWildcardWidening(ReferenceType type, Set<ReferenceType> result) {
    if (!(type instanceof InstantiatedType)) {
      return;
    }
    InstantiatedType instantiatedType = (InstantiatedType) type;
    List<TypeArgument> wildcards = new ArrayList<>();
    boolean changed = false;
    for (TypeArgument argument : instantiatedType.getTypeArguments()) {
      if (!isUnboundedWildcard(argument)) {
        changed = true;
      }
      wildcards.add(
          new WildcardArgument(
              new WildcardType(new EagerReferenceBound(JavaTypes.OBJECT_TYPE), true)));
    }
    if (!changed) {
      return;
    }
    InstantiatedType widened =
        new InstantiatedType(instantiatedType.getGenericClassType(), wildcards);
    if (instantiatedType.isMemberClass()) {
      widened.enclosingType = instantiatedType.enclosingType;
    }
    result.add(widened);
  }

  /**
   * Returns true if the given type argument is the unbounded wildcard {@code ?}.
   *
   * @param argument a type argument
   * @return true if {@code argument} is {@code ?}
   */
  private static boolean isUnboundedWildcard(TypeArgument argument) {
    if (!(argument instanceof WildcardArgument)) {
      return false;
    }
    WildcardArgument wildcard = (WildcardArgument) argument;
    if (!wildcard.hasUpperBound()) {
      return false;
    }
    ReferenceType boundType = ReferenceBound.asReferenceType(wildcard.getTypeBound());
    return boundType != null && boundType.isObject();
  }

  /**
   * Returns the types in the given upper bound of a type variable: the type of a {@link
   * ReferenceBound}, the equivalent type of a {@link LazyParameterBound}, or the types of the
   * members of an {@link IntersectionTypeBound}.
   *
   * @param upperBound an upper bound of a type variable
   * @return the types in {@code upperBound}
   */
  private static List<ReferenceType> getUpperBoundTypes(ParameterBound upperBound) {
    List<ReferenceType> result = new ArrayList<>();
    if (upperBound instanceof LazyParameterBound) {
      ReferenceBound converted = ((LazyParameterBound) upperBound).toReferenceBound();
      if (converted != null) {
        result.add(converted.getBoundType());
      }
    } else if (upperBound instanceof ReferenceBound) {
      result.add(((ReferenceBound) upperBound).getBoundType());
    } else if (upperBound instanceof IntersectionTypeBound) {
      for (ParameterBound member : ((IntersectionTypeBound) upperBound).getBounds()) {
        result.addAll(getUpperBoundTypes(member));
      }
    }
    return result;
  }

  /**
   * Returns true if this type variable and the given one are the same type variable with identical
   * bounds. Unlike {@link #equals(Object)}, which treats copies of a type variable as equal even if
   * their bounds differ, this compares the bounds, including the bounds of the type variables in
   * them.
   *
   * @param other a type variable
   * @return true if {@code other} is this type variable with identical bounds
   */
  @SuppressWarnings("ReferenceEquality") // fast path for the same object
  boolean isIdenticalTo(TypeVariable other) {
    if (this == other) {
      return true;
    }
    if (!this.equals(other)) {
      return false;
    }
    if (!(this instanceof ExplicitTypeVariable)) {
      // The bounds of a capture variable can refer to the capture variable itself, so comparing
      // them deeply would not terminate.
      return getLowerTypeBound().equals(other.getLowerTypeBound())
          && getUpperTypeBound().equals(other.getUpperTypeBound());
    }
    return areIdentical(getLowerTypeBound(), other.getLowerTypeBound())
        && areIdentical(getUpperTypeBound(), other.getUpperTypeBound());
  }

  /**
   * Returns true if the given bounds represent the same type and the type variables in them are
   * identical, as determined by {@link #isIdenticalTo(TypeVariable)}.
   *
   * @param bound1 a bound
   * @param bound2 a bound
   * @return true if the bounds represent the same type and the type variables in them are identical
   */
  @SuppressWarnings("ReferenceEquality") // fast path for the same object
  static boolean areIdentical(ParameterBound bound1, ParameterBound bound2) {
    if (bound1 == bound2) {
      return true;
    }
    if (bound1 instanceof LazyParameterBound && bound2 instanceof LazyParameterBound) {
      // The type variables in each are created from the same reflection type.
      return bound1.equals(bound2);
    }
    if (bound1 instanceof LazyParameterBound) {
      ParameterBound converted = ((LazyParameterBound) bound1).toReferenceBound();
      return converted != null && areIdentical(converted, bound2);
    }
    if (bound2 instanceof LazyParameterBound) {
      ParameterBound converted = ((LazyParameterBound) bound2).toReferenceBound();
      return converted != null && areIdentical(bound1, converted);
    }
    return bound1.equals(bound2)
        && areIdentical(bound1.getTypeParameters(), bound2.getTypeParameters());
  }

  /**
   * Returns true if the given lists have the same length and their corresponding elements are
   * identical, as determined by {@link #isIdenticalTo(TypeVariable)}.
   *
   * @param variables1 a list of type variables
   * @param variables2 a list of type variables
   * @return true if the lists have identical elements
   */
  private static boolean areIdentical(
      List<TypeVariable> variables1, List<TypeVariable> variables2) {
    if (variables1.size() != variables2.size()) {
      return false;
    }
    for (int i = 0; i < variables1.size(); i++) {
      if (!variables1.get(i).isIdenticalTo(variables2.get(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the type that the given substitution maps this type variable to, or null if the
   * substitution does not map this type variable or maps it to itself, as determined by {@link
   * Substitution#mapsToItself(TypeVariable)}.
   *
   * <p>When the substitution maps this type variable to itself, the substitution's key can be a
   * copy of this type variable with other bounds. Returning null keeps this copy and its bounds,
   * rather than the substitution's copy. When the substitution maps this type variable to a copy of
   * it with other bounds, the result is that copy.
   *
   * @param substitution a substitution
   * @return the type that {@code substitution} maps this type variable to, or null if {@code
   *     substitution} does not map this type variable or maps it to itself
   */
  /*package*/ @Nullable ReferenceType getSubstitutedType(Substitution substitution) {
    ReferenceType type = substitution.get(this);
    if (type != null && substitution.mapsToItself(this)) {
      return null;
    }
    return type;
  }

  /**
   * Returns the type parameters in this type: this variable, followed by the type variables in its
   * bounds.
   *
   * <p>This variable comes first so that it, rather than a copy of it with other bounds that occurs
   * in its bounds, represents it in the result. Copies of a type variable are equal, so the result
   * contains only one of them.
   *
   * @return this variable and the type variables in its bounds
   */
  @Override
  public List<TypeVariable> getTypeParameters() {
    Set<TypeVariable> parameters = new LinkedHashSet<>();
    parameters.add(this);
    parameters.addAll(super.getTypeParameters());
    return new ArrayList<>(parameters);
  }

  /**
   * Returns a copy of this type variable, but with the given bounds. An implementation may ignore
   * {@code lowerBound} if its kind of type variable has a fixed lower bound.
   *
   * @param lowerBound the lower bound for the new type variable
   * @param upperBound the upper bound for the new type variable
   * @return a copy of this type variable with the given bounds
   */
  public abstract TypeVariable createCopyWithBounds(
      ParameterBound lowerBound, ParameterBound upperBound);

  @Override
  public Type getRawtype() {
    return JavaTypes.OBJECT_TYPE;
  }

  @Override
  public String toString() {
    ParameterBound lowerBound = getLowerTypeBound();
    ParameterBound upperBound = getUpperTypeBound();
    StringBuilder result = new StringBuilder(getSimpleName());
    if (!(lowerBound instanceof ReferenceBound
        && ((ReferenceBound) lowerBound).getBoundType() instanceof NullReferenceType)) {
      result.append(" super ").append(lowerBound);
    }
    if (!(upperBound instanceof ReferenceBound
        && ((ReferenceBound) upperBound).getBoundType().isObject())) {
      result.append(" extends ").append(upperBound);
    }
    return result.toString();
  }
}
