package randoop.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.main.RandoopBug;

/**
 * A lazy representation of a type bound in which a type variable occurs. Prevents type recognition
 * from having to deal with recursive type bounds. All methods that need to evaluate the bound are
 * provided with a substitution for the variable for which this object is a bound.
 */
class LazyParameterBound extends ParameterBound {

  /** The type for this bound. */
  private final java.lang.reflect.Type boundType;

  /**
   * The type variables in this bound; null if not yet computed. Use {@link #getTypeParameterList()}
   * instead of reading this field.
   */
  // Volatile so that a thread that reads a non-null value also sees the contents of the list.
  private volatile @Nullable List<TypeVariable> typeParameters = null;

  /**
   * Creates a {@code LazyParameterBound} from the given rawtype and type parameters.
   *
   * @param boundType the reflection type for this bound
   */
  /*package*/ LazyParameterBound(java.lang.reflect.Type boundType) {
    super();
    this.boundType = boundType;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if argument is a {@code LazyParameterBound}, and the rawtype and parameters are
   *     identical, false otherwise
   */
  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LazyParameterBound)) {
      return false;
    }
    LazyParameterBound b = (LazyParameterBound) obj;
    return this.boundType.equals(b.boundType);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(boundType);
  }

  @Override
  public String toString() {
    return boundType.toString();
  }

  @Override
  public ParameterBound substitute(Substitution substitution) {
    if (boundType instanceof GenericArrayType || isNonGenericMemberClass(boundType)) {
      // A generic array type, such as `List<String>[]`, or a non-generic member class, such as
      // `Outer<String>.Inner`, need not contain a type variable.  Such a bound is materialized even
      // by an empty substitution, so that it can be compared with types.
      if (!getTypeParameterList().isEmpty() && !instantiatesAny(boundType, substitution)) {
        return this;
      }
      TypeArgument typeArgument = substitute(boundType, substitution);
      if (typeArgument == null) {
        return this;
      }
      return ReferenceBound.forTypeLazyIfGeneric(
          ((ReferenceArgument) typeArgument).getReferenceType());
    }
    if (substitution.isEmpty()) {
      return this;
    }
    if (boundType instanceof java.lang.reflect.TypeVariable) {
      ReferenceType referenceType = substitution.get(boundType);
      if (referenceType != null) {
        return ReferenceBound.forTypeLazyIfGeneric(referenceType);
      }
      if (!instantiatesAny(boundType, substitution)) {
        return this;
      }
      // The substitution changes the bounds of the type variable.
      return ReferenceBound.forTypeLazyIfGeneric(
          TypeVariable.forType(boundType).substitute(substitution));
    }

    if (boundType instanceof ParameterizedType) {
      if (!instantiatesAny(boundType, substitution)) {
        return this;
      }
      // Type variables that the substitution does not map to a type remain type variables, so a
      // partial substitution is preserved rather than discarded.
      TypeArgument typeArgument = substitute(boundType, substitution);
      if (typeArgument == null) {
        return this;
      }
      return ReferenceBound.forTypeLazyIfGeneric(
          ((ReferenceArgument) typeArgument).getReferenceType());
    }

    throw new RandoopBug(
        "lazy parameter bounds should be a type variable, parameterized type, or array type");
  }

  /**
   * Returns a {@link ReferenceBound} that represents the same type as this bound. Each type
   * variable that occurs in this bound remains a type variable in the result.
   *
   * @return a reference bound equivalent to this bound, or null if this bound contains a kind of
   *     type that {@link #substitute(Substitution)} does not handle
   */
  @Nullable ReferenceBound toReferenceBound() {
    List<TypeVariable> parameters = getTypeParameters();
    // Map each type variable to itself, so that substitute() converts this bound without changing
    // the type that it represents.
    ParameterBound result =
        substitute(new Substitution(parameters, new ArrayList<ReferenceType>(parameters)));
    return result instanceof ReferenceBound ? (ReferenceBound) result : null;
  }

  /**
   * Applies a substitution to a reflection type that occurs as an actual argument of a
   * parameterized type bound, to create a type argument to a {@link
   * randoop.types.ParameterizedType}.
   *
   * <p>A type variable that {@code substitution} does not map to a type remains a type variable in
   * the result, with {@code substitution} applied to its bounds.
   *
   * @param type the reflection type
   * @param substitution the type substitution
   * @return the type argument that results from applying {@code substitution} to {@code type}, or
   *     null if {@code type} is not a type variable, parameterized type, class, generic array type,
   *     or wildcard, or is a member class whose owner type cannot be converted
   */
  private static @Nullable TypeArgument substitute(
      java.lang.reflect.Type type, Substitution substitution) {
    if (type instanceof java.lang.reflect.TypeVariable) {
      ReferenceType referenceType = substitution.get(type);
      if (referenceType != null) {
        return TypeArgument.forType(referenceType);
      }
      return TypeArgument.forType(TypeVariable.forType(type).substitute(substitution));
    }

    if (isNonGenericMemberClass(type)) {
      // A member class that declares no type parameters, as in Outer<T>.Inner, gets its type
      // arguments from its owner type, which GenericClassType.forClass would not accept.
      ParameterizedType memberType = (ParameterizedType) type;
      java.lang.reflect.Type owner = memberType.getOwnerType();
      if (owner == null) {
        return null;
      }
      TypeArgument ownerArgument = substitute(owner, substitution);
      if (!(ownerArgument instanceof ReferenceArgument)) {
        return null;
      }
      ReferenceType ownerType = ((ReferenceArgument) ownerArgument).getReferenceType();
      if (!(ownerType instanceof ClassOrInterfaceType)) {
        return null;
      }
      // Do not use NonParameterizedType.forClass, which returns a shared cached object.
      NonParameterizedType result = new NonParameterizedType((Class<?>) memberType.getRawType());
      result.enclosingType = (ClassOrInterfaceType) ownerType;
      return TypeArgument.forType(result);
    }

    if (type instanceof ParameterizedType) {
      java.lang.reflect.Type[] actualTypeArgs = ((ParameterizedType) type).getActualTypeArguments();
      List<TypeArgument> argumentList = new ArrayList<>(actualTypeArgs.length);
      for (java.lang.reflect.Type parameter : actualTypeArgs) {
        TypeArgument typeArgument = substitute(parameter, substitution);
        if (typeArgument == null) {
          return null;
        }
        argumentList.add(typeArgument);
      }

      GenericClassType classType =
          GenericClassType.forClass((Class<?>) ((ParameterizedType) type).getRawType());
      InstantiatedType instantiatedType = new InstantiatedType(classType, argumentList);
      // For a member class such as Outer<String>.Middle<Integer>, keep the owner type, as
      // ParameterizedType.forType does.
      ParameterizedType preservedOwner =
          ClassOrInterfaceType.getPreservedOwnerType((ParameterizedType) type);
      // An owner in declaration form, such as Outer<A> where A is the type parameter of Outer, is
      // not preserved by ParameterizedType.forType.  But the substitution can change it, as when it
      // maps A to String, and then the substituted owner must be kept.
      java.lang.reflect.Type reflectiveOwner = ((ParameterizedType) type).getOwnerType();
      boolean isDeclarationFormOwner =
          preservedOwner == null
              && reflectiveOwner instanceof ParameterizedType
              && !java.lang.reflect.Modifier.isStatic(
                  ((Class<?>) ((ParameterizedType) type).getRawType()).getModifiers());
      ParameterizedType owner =
          isDeclarationFormOwner ? (ParameterizedType) reflectiveOwner : preservedOwner;
      if (owner != null) {
        TypeArgument ownerArgument = substitute(owner, substitution);
        if (!(ownerArgument instanceof ReferenceArgument)) {
          return null;
        }
        ReferenceType ownerType = ((ReferenceArgument) ownerArgument).getReferenceType();
        if (!(ownerType instanceof ClassOrInterfaceType)) {
          return null;
        }
        // Keep the default enclosing type if the substitution leaves a declaration-form owner
        // unchanged.
        if (!isDeclarationFormOwner || !ownerType.equals(ClassOrInterfaceType.forType(owner))) {
          instantiatedType.enclosingType = (ClassOrInterfaceType) ownerType;
        }
      }
      return TypeArgument.forType(instantiatedType);
    }

    if (type instanceof Class) {
      // This call converts an array class such as String[] to an ArrayType.  By contrast,
      // ClassOrInterfaceType.forType would convert it to a NonParameterizedType.
      return TypeArgument.forType(type);
    }

    if (type instanceof GenericArrayType) {
      TypeArgument componentArgument =
          substitute(((GenericArrayType) type).getGenericComponentType(), substitution);
      if (!(componentArgument instanceof ReferenceArgument)) {
        return null;
      }
      ReferenceType componentType = ((ReferenceArgument) componentArgument).getReferenceType();
      return TypeArgument.forType(ArrayType.ofComponentType(componentType));
    }

    if (type instanceof java.lang.reflect.WildcardType) {
      final java.lang.reflect.WildcardType wildcardType = (java.lang.reflect.WildcardType) type;
      java.lang.reflect.Type[] lowerBounds = wildcardType.getLowerBounds();
      if (lowerBounds.length > 0) {
        assert lowerBounds.length == 1 : "a wildcard is defined by the JLS to only have one bound";
        java.lang.reflect.Type lowerBound = lowerBounds[0];
        ParameterBound bound;
        if (lowerBound instanceof java.lang.reflect.TypeVariable) {
          ReferenceType boundType = substitution.get(lowerBound);
          if (boundType != null) {
            bound = forType(boundType);
          } else {
            // The substitution can instantiate type variables in the bounds of `lowerBound`.
            bound = new LazyParameterBound(lowerBound).substitute(substitution);
          }
        } else {
          bound = forType(new HashSet<>(0), lowerBound).substitute(substitution);
        }

        return new WildcardArgument(new WildcardType(bound, false));
      }
      // a wildcard always has an upper bound
      assert wildcardType.getUpperBounds().length == 1
          : "a wildcard is defined by the JLS to only have one bound";
      ParameterBound bound = forTypes(new HashSet<>(0), wildcardType.getUpperBounds());
      bound = bound.substitute(substitution);
      return new WildcardArgument(new WildcardType(bound, true));
    }

    return null;
  }

  /**
   * Returns true if {@code substitution} instantiates some type variable that occurs in {@code
   * type}, or in the bounds of a type variable that occurs in {@code type}, transitively.
   *
   * @param type a reflection type
   * @param substitution a type substitution
   * @return true if {@code substitution} instantiates a type variable that occurs in {@code type}
   *     or in the bounds of the type variables in it
   */
  private static boolean instantiatesAny(java.lang.reflect.Type type, Substitution substitution) {
    return instantiatesAny(type, substitution, new HashSet<>());
  }

  /**
   * Returns true if {@code substitution} instantiates some type variable that occurs in {@code
   * type}, or in the bounds of a type variable that occurs in {@code type}, transitively. Does not
   * examine the bounds of the type variables in {@code visited}.
   *
   * @param type a reflection type
   * @param substitution a type substitution
   * @param visited the type variables whose bounds have been examined; modified by this method
   * @return true if {@code substitution} instantiates a type variable that occurs in {@code type}
   *     or in the bounds of the type variables in it
   */
  private static boolean instantiatesAny(
      java.lang.reflect.Type type,
      Substitution substitution,
      Set<java.lang.reflect.TypeVariable<?>> visited) {
    if (type instanceof java.lang.reflect.TypeVariable) {
      if (substitution.get(type) != null) {
        return true;
      }
      java.lang.reflect.TypeVariable<?> variable = (java.lang.reflect.TypeVariable<?>) type;
      if (!visited.add(variable)) {
        return false;
      }
      for (java.lang.reflect.Type boundType : variable.getBounds()) {
        if (instantiatesAny(boundType, substitution, visited)) {
          return true;
        }
      }
      return false;
    }
    if (isNonGenericMemberClass(type)) {
      java.lang.reflect.Type owner = ((ParameterizedType) type).getOwnerType();
      return owner != null && instantiatesAny(owner, substitution, visited);
    }
    if (type instanceof ParameterizedType) {
      for (java.lang.reflect.Type argType : ((ParameterizedType) type).getActualTypeArguments()) {
        if (instantiatesAny(argType, substitution, visited)) {
          return true;
        }
      }
      java.lang.reflect.Type owner = ((ParameterizedType) type).getOwnerType();
      return owner != null && instantiatesAny(owner, substitution, visited);
    }
    if (type instanceof GenericArrayType) {
      return instantiatesAny(
          ((GenericArrayType) type).getGenericComponentType(), substitution, visited);
    }
    if (type instanceof java.lang.reflect.WildcardType) {
      java.lang.reflect.WildcardType wt = (java.lang.reflect.WildcardType) type;
      for (java.lang.reflect.Type boundType : wt.getUpperBounds()) {
        if (instantiatesAny(boundType, substitution, visited)) {
          return true;
        }
      }
      for (java.lang.reflect.Type boundType : wt.getLowerBounds()) {
        if (instantiatesAny(boundType, substitution, visited)) {
          return true;
        }
      }
      return false;
    }
    return false;
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    return new ArrayList<>(getTypeParameterList());
  }

  /**
   * Returns the type variables in this bound. The result must not be modified.
   *
   * @return the type variables in this bound
   */
  private List<TypeVariable> getTypeParameterList() {
    List<TypeVariable> result = typeParameters;
    if (result == null) {
      result = getTypeParameters(boundType);
      typeParameters = result;
    }
    return result;
  }

  /**
   * Collects the type parameters from the given reflection {@code Type} object.
   *
   * @param type the {@code Type} reference
   * @return the list of type variables in the given type
   */
  private static List<TypeVariable> getTypeParameters(java.lang.reflect.Type type) {
    List<TypeVariable> variableList = new ArrayList<>();
    if (type instanceof java.lang.reflect.TypeVariable) {
      variableList.add(TypeVariable.forType(type));
    } else if (isNonGenericMemberClass(type)) {
      // The type arguments of a member class that declares no type parameters are those of its
      // owner type.
      java.lang.reflect.Type owner = ((ParameterizedType) type).getOwnerType();
      if (owner != null) {
        variableList.addAll(getTypeParameters(owner));
      }
    } else if (type instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) type;
      for (java.lang.reflect.Type argType : pt.getActualTypeArguments()) {
        variableList.addAll(getTypeParameters(argType));
      }
      // The owner type of a member class, as in Outer<T>.Middle<Integer>, can contain type
      // variables too.
      java.lang.reflect.Type owner = pt.getOwnerType();
      if (owner != null) {
        variableList.addAll(getTypeParameters(owner));
      }
    } else if (type instanceof GenericArrayType) {
      variableList.addAll(getTypeParameters(((GenericArrayType) type).getGenericComponentType()));
    } else if (type instanceof java.lang.reflect.WildcardType) {
      java.lang.reflect.WildcardType wt = (java.lang.reflect.WildcardType) type;
      for (java.lang.reflect.Type boundType : wt.getUpperBounds()) {
        variableList.addAll(getTypeParameters(boundType));
      }
      for (java.lang.reflect.Type boundType : wt.getLowerBounds()) {
        variableList.addAll(getTypeParameters(boundType));
      }
    }
    return variableList;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns false, because the bound is a {@code java.lang.reflect.Type}, which cannot contain a
   * capture variable.
   */
  @Override
  public boolean hasCaptureVariable() {
    return false;
  }

  @Override
  public boolean isGeneric(boolean ignoreWildcards) {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * <p>A type variable in this bound that the substitution does not replace stands for an unknown
   * type, as in {@link #isUpperBound(Type, Substitution)}.
   */
  @Override
  public boolean isLowerBound(Type argType, Substitution substitution) {
    ParameterBound b = substituteWithUnknowns(substitution);
    return b != null && b.isLowerBound(argType, substitution);
  }

  @Override
  public boolean isObject() {
    return false;
  }

  @Override
  public boolean isSubtypeOfOrEqualTo(ParameterBound boundType) {
    throw new LazyBoundException();
    // assert false : "LazyParameterBound.isSubtypeOfOrEqualTo is not implemented";
    // return false;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This generic type bound is satisfied by a concrete type if the concrete type formed by
   * applying the substitution to this generic bound is satisfied by the concrete type. A type
   * variable in this bound that the substitution does not replace stands for an unknown type, which
   * may be any type that satisfies its bound.
   */
  @Override
  public boolean isUpperBound(Type argType, Substitution substitution) {
    ParameterBound b = substituteWithUnknowns(substitution);
    return b != null && b.isUpperBound(argType, substitution);
  }

  /**
   * {@inheritDoc}
   *
   * <p>A type variable in this bound that the substitution does not replace stands for an unknown
   * type, as in {@link #isUpperBound(Type, Substitution)}.
   */
  /*package*/
  @Override
  boolean isUpperBound(ParameterBound bound, Substitution substitution) {
    Substitution completeSubstitution = completeSubstitution(substitution);
    ParameterBound b = substituteWithUnknowns(substitution);
    // Compare the instantiated bound directly.  Calling b.isUpperBound would apply the
    // substitution again, which can change b, such as by substituting into the bounds of a type
    // variable that the substitution put in b.
    return b instanceof ReferenceBound
        && bound.hasUpperBound(((ReferenceBound) b).getBoundType(), completeSubstitution);
  }

  /**
   * Returns the given substitution, extended to map to itself each type variable in this bound that
   * it does not map. Such a type variable then stands for an unknown type when the substituted
   * bound is compared with a type.
   *
   * @param substitution a type substitution
   * @return a substitution that maps every type variable in this bound
   */
  private Substitution completeSubstitution(Substitution substitution) {
    return substitution.extendWithIdentity(getTypeParameterList());
  }

  /**
   * Returns this bound with the given substitution applied, after the substitution is extended to
   * map to itself each type variable in this bound that it does not map. Each such type variable
   * stands for an unknown type in the result.
   *
   * <p>Returns null if the result is still this bound, which happens only for a kind of type that
   * {@link #substitute(Substitution)} does not handle; then this method also fails an assertion.
   * Comparing the unchanged bound would recurse forever.
   *
   * @param substitution a type substitution
   * @return this bound with the substitution applied, or null
   */
  @Nullable ParameterBound substituteWithUnknowns(Substitution substitution) {
    Substitution completeSubstitution = completeSubstitution(substitution);
    ParameterBound b = this.substitute(completeSubstitution);
    if (b.equals(this)) {
      assert false : "substitution " + completeSubstitution + " does not instantiate " + this;
      return null;
    }
    return b;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Compares {@code upperBound} with the {@link ReferenceBound} that {@link #toReferenceBound()}
   * converts this bound to. If this bound cannot be converted, returns true only if {@code
   * upperBound} is {@code Object}, which is an upper bound of every reference type, because the
   * type of this bound cannot be determined without a substitution for its type variables.
   */
  @Override
  boolean hasUpperBound(ReferenceType upperBound, Substitution substitution) {
    ReferenceBound converted = toReferenceBound();
    if (converted != null) {
      return converted.hasUpperBound(upperBound, substitution);
    }
    return upperBound.isObject();
  }

  /**
   * {@inheritDoc}
   *
   * <p>A type variable in this bound that the substitution does not replace stands for an unknown
   * type, as in {@link #isUpperBound(Type, Substitution)}.
   */
  @Override
  boolean isLowerBound(ParameterBound bound, Substitution substitution) {
    ParameterBound b = substituteWithUnknowns(substitution);
    return b != null && b.isLowerBound(bound, substitution);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Unlike for other bounds, the substitution is also applied to this bound before {@code
   * lowerBound} is compared with it; a type variable in this bound that the substitution does not
   * instantiate stands for an unknown type, as in {@link #isUpperBound(Type, Substitution)}.
   * Returns true if {@code lowerBound} is the null type, which is a lower bound of every reference
   * type. Otherwise, returns false if the result is not a {@link ReferenceBound}.
   */
  @Override
  boolean hasLowerBound(ParameterBound lowerBound, Substitution substitution) {
    if (lowerBound.isNullType(substitution)) {
      return true;
    }
    ParameterBound converted = substituteWithUnknowns(substitution);
    return converted instanceof ReferenceBound && converted.hasLowerBound(lowerBound, substitution);
  }

  @Override
  public boolean isVariable() {
    return boundType instanceof java.lang.reflect.TypeVariable;
  }

  /** There was an attempt to perform an operation, such as a subtype test, on a lazy bound. */
  /*package*/ static class LazyBoundException extends RuntimeException {
    /** Creates a new LazyBoundException. */
    public LazyBoundException() {
      super();
    }

    /** ID for serializing this class. */
    private static final long serialVersionUID = 20190508;
  }
}
