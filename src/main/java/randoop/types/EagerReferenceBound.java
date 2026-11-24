package randoop.types;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a bound on a type variable where the bound is a {@link ReferenceType} that can be used
 * directly. Contrast with {@link LazyReferenceBound}.
 */
class EagerReferenceBound extends ReferenceBound {

  /**
   * The (bound type, argument type) pairs whose {@link #isUpperBound(Type, Substitution)} check of
   * a parameterized bound is executing on the current thread. The check captures the bound, and a
   * capture variable's upper bound can be the bound itself, as for {@code T extends Builder<?>} in
   * {@code Builder<T extends Builder<?>>}. Checking that the argument instantiates the capture
   * variable then repeats the same check. The repeated check returns true: it succeeds if the rest
   * of the original check succeeds.
   *
   * <p>Checks are compared by {@code equals}, which ignores the bounds of a type variable. They
   * cannot be compared by identity: when a bound mentions a type variable, as in {@code D<T, S
   * extends D<T, ?>>}, each repetition of a check has a new bound object, so a check compared by
   * identity would never repeat.
   */
  @SuppressWarnings("nullness:type.argument") // withInitial: get() never returns null
  private static final ThreadLocal<Set<List<Type>>> upperBoundChecksInProgress =
      ThreadLocal.withInitial(HashSet::new);

  /**
   * Creates a bound for the given reference type.
   *
   * @param boundType the reference boundType
   */
  /*package*/ EagerReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  @Override
  public EagerReferenceBound substitute(Substitution substitution) {
    ReferenceType referenceType = getBoundType().substitute(substitution);
    if (Type.sameType(referenceType, getBoundType())) {
      return this;
    }
    return new EagerReferenceBound(referenceType);
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    return getBoundType().getTypeParameters();
  }

  @Override
  public boolean isLowerBound(Type argType, Substitution subst) {
    ReferenceType thisBoundType = this.getBoundType().substitute(subst);
    if (thisBoundType.equals(JavaTypes.NULL_TYPE)) {
      return true;
    }
    if (thisBoundType.isVariable()) {
      return thisBoundType.isSubtypeOfOrEqualTo(argType);
    }
    // A non-generic member class of a generic class, as in `Outer<String>.Inner`, is parameterized
    // but is not an InstantiatedType.  It is compared by the subtype test below, which requires its
    // enclosing type to equal that of the other member class (or that member class to be the
    // generic form of its class), rather than to be contained in it.  For example, the test does
    // not report that `Outer<Integer>.Inner` is within the bound `Outer<? extends Number>.Inner`.
    if (argType instanceof InstantiatedType) {
      if (!(thisBoundType instanceof ClassOrInterfaceType)) {
        return false;
      }
      InstantiatedType argClassType = ((InstantiatedType) argType).applyCaptureConversion();
      InstantiatedType boundSuperType =
          capturedMatchingSupertype(
              (ClassOrInterfaceType) thisBoundType, argClassType.getGenericClassType());
      if (boundSuperType == null) {
        return false;
      }
      // isInstantiationOf does not compare enclosing types, so compare them too.
      return boundSuperType.isInstantiationOf(argClassType)
          && ClassOrInterfaceType.enclosingTypesInstantiate(boundSuperType, argClassType);
    }
    if (isMemberOfParameterizedType(argType)) {
      // Compare the enclosing types by containment, as isSubtypeOfOrEqualTo does not.  The type
      // variables in argType, which the substitution does not apply to, stand for themselves.
      return Subtyping.isSubtypeOrEqual(thisBoundType, (ReferenceType) argType, new Substitution());
    }
    return thisBoundType.isSubtypeOfOrEqualTo(argType);
  }

  /**
   * Returns the supertype of the capture conversion of {@code type} that instantiates {@code
   * goalType}, with capture conversion applied to it too. As in {@link Subtyping}, {@code type} is
   * captured before its supertype is found: if {@code Nested<T>} extends {@code
   * ArrayList<List<T>>}, then the supertype of {@code Nested<?>} is {@code List<List<CAP>>}, not
   * {@code List<List<?>>}. The supertype is captured because its declaration can have wildcard
   * arguments.
   *
   * @param type a type
   * @param goalType a generic class
   * @return the captured supertype of the captured {@code type} that instantiates {@code goalType},
   *     or null if there is none or a bound cannot be evaluated
   */
  private static @Nullable InstantiatedType capturedMatchingSupertype(
      ClassOrInterfaceType type, GenericClassType goalType) {
    try {
      InstantiatedType supertype = type.applyCaptureConversion().getMatchingSupertype(goalType);
      return supertype == null ? null : supertype.applyCaptureConversion();
    } catch (LazyParameterBound.LazyBoundException e) {
      return null;
    }
  }

  @Override
  public boolean isSubtypeOfOrEqualTo(ParameterBound bound) {
    if (bound instanceof ReferenceBound) {
      return this.getBoundType().isSubtypeOfOrEqualTo(((ReferenceBound) bound).getBoundType());
    }
    // TODO: change this into an unconditional exception.
    assert false : "not handling EagerReferenceBound subtype of other bound type";
    return false;
  }

  @Override
  public boolean isUpperBound(Type argType, Substitution subst) {
    ReferenceType thisBoundType = this.getBoundType().substitute(subst);
    if (thisBoundType.equals(JavaTypes.OBJECT_TYPE)) {
      return true;
    }
    if (thisBoundType.isVariable()) {
      // The substitution does not instantiate the variable with a non-variable type, so the
      // variable can still be chosen, within its upper bound.
      TypeVariable variable = (TypeVariable) thisBoundType;
      if (argType.isReferenceType() && !argType.isVariable()) {
        // The type variable can be argType itself or one of its supertypes, as in
        // LazyReferenceBound.isUpperBound.  Checking argType against the upper bound of the type
        // variable instead would leave the type variable unknown within that bound:  for
        // `T extends Comparable<? super T>`, it would accept Comparable<String>, which no choice of
        // T is a supertype of.  The bounds of the type variable are checked with the mappings of
        // `subst`, because they can mention other type variables that `subst` instantiates.
        return variable.canBeSupertypeOf((ReferenceType) argType, subst);
      }
      // `argType` is not a reference type, or is a type variable, so it is not tried as a choice
      // for the variable.  `argType` is within this bound if it is within the variable's upper
      // bound, which the variable can be.
      return variable.getUpperTypeBound().isUpperBound(argType, subst);
    }
    // A non-generic member class of a generic class, as in `Outer<String>.Inner`, is parameterized
    // but is not an InstantiatedType.  It is compared by the subtype test below, which requires its
    // enclosing type to equal that of the other member class (or that member class to be the
    // generic form of its class), rather than to be contained in it.  For example, the test does
    // not report that `Outer<Integer>.Inner` is within the bound `Outer<? extends Number>.Inner`.
    if (thisBoundType instanceof InstantiatedType) {
      if (!(argType instanceof ClassOrInterfaceType)) {
        return false;
      }
      List<Type> check = Arrays.asList(thisBoundType, argType);
      Set<List<Type>> inProgress = upperBoundChecksInProgress.get();
      if (!inProgress.add(check)) {
        // This check depends on itself, so assume it holds. Any other condition still holds or
        // fails on its own.
        return true;
      }
      try {
        InstantiatedType boundClassType =
            ((InstantiatedType) thisBoundType).applyCaptureConversion();
        InstantiatedType argSuperType =
            capturedMatchingSupertype(
                (ClassOrInterfaceType) argType, boundClassType.getGenericClassType());
        if (argSuperType == null) {
          return false;
        }
        // isInstantiationOf does not compare enclosing types, so compare them too:  for the bound
        // Outer<String>.Inner<Integer>, Outer<Number>.Inner<Integer> is not within the bound.
        return argSuperType.isInstantiationOf(boundClassType)
            && ClassOrInterfaceType.enclosingTypesInstantiate(argSuperType, boundClassType);
      } finally {
        inProgress.remove(check);
      }
    }
    if (isMemberOfParameterizedType(thisBoundType) && argType.isReferenceType()) {
      // Compare the enclosing types by containment, as isSubtypeOfOrEqualTo does not.  Each type
      // variable that remains in the bound stands for an unknown, as in the InstantiatedType case
      // above:  for the bound Outer<U>.Inner, Outer<String>.Inner is within the bound.
      return Subtyping.isSubtypeOrEqual(
          (ReferenceType) argType,
          thisBoundType,
          subst.extendWithIdentity(thisBoundType.getTypeParameters()));
    }
    return argType.isSubtypeOfOrEqualTo(thisBoundType);
  }

  /**
   * Returns true if the given type is a member class that declares no type parameters, of a
   * parameterized type, such as {@code Outer<String>.Inner}. Such a type is parameterized, but is
   * not an {@link InstantiatedType}.
   *
   * @param type a type
   * @return true if {@code type} is a non-generic member class of a parameterized type
   */
  private static boolean isMemberOfParameterizedType(Type type) {
    return type instanceof NonParameterizedType && type.isParameterized();
  }
}
