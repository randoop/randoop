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
   * The result of {@link #toReferenceBound()}, which may be null; meaningful only if {@link
   * #referenceBoundComputed} is true.
   */
  private @Nullable ReferenceBound referenceBound = null;

  /** True if {@link #referenceBound} has been computed. */
  // Volatile so that a thread that reads true also sees the value of referenceBound, which is
  // written before this field.
  private volatile boolean referenceBoundComputed = false;

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
      // A generic array type, such as `List<String>[]`, need not contain a type variable.  Such a
      // bound is materialized even by an empty substitution, so that it can be compared with types.
      // A non-generic member class, such as `Outer<T>.Inner`, gets its type arguments from its
      // owner type.
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
      ReferenceType referenceType = replacement(boundType, substitution);
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
    if (referenceBoundComputed) {
      return referenceBound;
    }
    List<TypeVariable> parameters = getTypeParameters();
    // Map each type variable to itself, so that the conversion does not change the type that this
    // bound represents.  This calls the static substitute() method directly, rather than the
    // instance method, because the instance method can return this bound unchanged, as when the
    // substitution instantiates no type variable.
    Substitution identity = new Substitution(parameters, new ArrayList<>(parameters));
    TypeArgument typeArgument = substitute(boundType, identity);
    referenceBound =
        typeArgument instanceof ReferenceArgument
            ? ReferenceBound.forTypeLazyIfGeneric(
                ((ReferenceArgument) typeArgument).getReferenceType())
            : null;
    referenceBoundComputed = true;
    return referenceBound;
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
   *     null if {@code type} is not a type variable, parameterized type (including a non-generic
   *     member class of a generic class), class, generic array type, or wildcard, or is a member
   *     class whose owner type cannot be converted
   */
  private static @Nullable TypeArgument substitute(
      java.lang.reflect.Type type, Substitution substitution) {
    if (type instanceof java.lang.reflect.TypeVariable) {
      ReferenceType referenceType = replacement(type, substitution);
      if (referenceType != null) {
        return TypeArgument.forType(referenceType);
      }
      return TypeArgument.forType(TypeVariable.forType(type).substitute(substitution));
    }

    if (isNonGenericMemberClass(type)) {
      // A member class that declares no type parameters, as in Outer<T>.Inner, gets its type
      // arguments from its owner type, which GenericClassType.forClass would not accept.
      return substituteNonGenericMemberClass((ParameterizedType) type, substitution);
    }

    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      java.lang.reflect.Type[] actualTypeArgs = parameterizedType.getActualTypeArguments();
      List<TypeArgument> argumentList = new ArrayList<>(actualTypeArgs.length);
      for (java.lang.reflect.Type parameter : actualTypeArgs) {
        TypeArgument typeArgument = substitute(parameter, substitution);
        if (typeArgument == null) {
          return null;
        }
        argumentList.add(typeArgument);
      }

      GenericClassType classType =
          GenericClassType.forClass((Class<?>) parameterizedType.getRawType());
      InstantiatedType instantiatedType = new InstantiatedType(classType, argumentList);
      // For a member class such as Outer<String>.Middle<Integer>, keep the owner type, as
      // ParameterizedType.forType does.  ParameterizedType.forType does not preserve an owner type
      // that is the type that the declaration of the enclosing class denotes, such as Outer<T>, but
      // uses the declaration of the enclosing class.  The substitution can instantiate such an
      // owner type, as by mapping T to Integer, so the substituted owner type is kept unless it is
      // still the generic form of its class.
      java.lang.reflect.Type owner = parameterizedType.getOwnerType();
      Class<?> rawType = (Class<?>) parameterizedType.getRawType();
      if (owner instanceof ParameterizedType
          && !java.lang.reflect.Modifier.isStatic(rawType.getModifiers())) {
        boolean preserved = ClassOrInterfaceType.getPreservedOwnerType(parameterizedType) != null;
        if (preserved || instantiatesAny(owner, substitution)) {
          TypeArgument ownerArgument = substitute(owner, substitution);
          if (!(ownerArgument instanceof ReferenceArgument)) {
            return null;
          }
          ReferenceType ownerType = ((ReferenceArgument) ownerArgument).getReferenceType();
          if (!(ownerType instanceof ClassOrInterfaceType)) {
            return null;
          }
          ClassOrInterfaceType ownerClassType = (ClassOrInterfaceType) ownerType;
          if (preserved || !ownerClassType.isGenericForm()) {
            instantiatedType.enclosingType = ownerClassType;
          }
        }
        if (instantiatedType.enclosingType == null) {
          // The substitution leaves a declaration-form owner unchanged.  Represent it as
          // ParameterizedType.forType does, by the declaration of the enclosing class.
          instantiatedType.enclosingType =
              ClassOrInterfaceType.forClass((Class<?>) ((ParameterizedType) owner).getRawType());
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
          // Null if the substitution maps the variable to itself, as completeSubstitution does.
          ReferenceType boundType = replacement(lowerBound, substitution);
          if (boundType != null) {
            bound = forType(boundType);
          } else if (substitution.get(lowerBound) != null) {
            // The substitution maps the variable to itself, so the variable stands for itself, but
            // the substitution can change its bounds, as for `U extends Map<K, List<U>>` when it
            // also maps K.
            bound = forType(TypeVariable.forType(lowerBound).substitute(substitution));
          } else {
            // As for a type variable that is a type argument, the substitution can change the
            // bounds of the type variable, as for `V extends U` when it maps U, as the upper-bound
            // case below also does.
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
   * Applies a substitution to a non-generic member class of a generic class, as in {@code
   * Outer<T>.Inner}, by applying it to the owner type, which supplies the type arguments.
   *
   * @param type a reflection type that is a non-generic member class of a generic class; see {@link
   *     ParameterBound#isNonGenericMemberClass}
   * @param substitution the type substitution
   * @return the type argument that results from applying {@code substitution} to {@code type}, or
   *     null if {@link #substitute(java.lang.reflect.Type, Substitution)} cannot convert the owner
   *     type to a class or interface type
   */
  private static @Nullable TypeArgument substituteNonGenericMemberClass(
      ParameterizedType type, Substitution substitution) {
    Class<?> rawType = (Class<?>) type.getRawType();
    ParameterizedType ownerType = getPreservedOwnerType(type);
    if (ownerType == null) {
      return TypeArgument.forType(ClassOrInterfaceType.forType(type).substitute(substitution));
    }
    // ClassOrInterfaceType.forType preserves the owner type, such as `Outer<String>`, whose type
    // arguments the substitution may change.  The owner type is converted here, rather than by
    // ClassOrInterfaceType.forType, so that type variables in it are treated as in type arguments.
    TypeArgument ownerArgument = substitute(ownerType, substitution);
    if (!(ownerArgument instanceof ReferenceArgument)) {
      return null;
    }
    ReferenceType owner = ((ReferenceArgument) ownerArgument).getReferenceType();
    if (!(owner instanceof ClassOrInterfaceType)) {
      return null;
    }
    // Do not use NonParameterizedType.forClass, which returns a shared cached object.
    NonParameterizedType result = new NonParameterizedType(rawType);
    result.enclosingType = (ClassOrInterfaceType) owner;
    return TypeArgument.forType(result);
  }

  /**
   * Returns the type that the given substitution maps the given type variable to, or null if the
   * substitution does not map it or maps it to itself. A type variable that the substitution maps
   * to itself must still have the substitution applied to its bounds, by {@link
   * TypeVariable#substitute(Substitution)}.
   *
   * @param variable a reflection type variable
   * @param substitution a type substitution
   * @return the type that {@code substitution} maps {@code variable} to, or null if it does not map
   *     {@code variable} or maps it to itself
   */
  private static @Nullable ReferenceType replacement(
      java.lang.reflect.Type variable, Substitution substitution) {
    if (substitution.get(variable) == null) {
      return null;
    }
    return TypeVariable.forType(variable).getSubstitutedType(substitution);
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
      ParameterizedType ownerType = getPreservedOwnerType((ParameterizedType) type);
      return ownerType != null && instantiatesAny(ownerType, substitution, visited);
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
      ParameterizedType ownerType = getPreservedOwnerType(pt);
      if (ownerType != null) {
        variableList.addAll(getTypeParameters(ownerType));
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
    if (isNonGenericMemberClass(boundType) && getTypeParameterList().isEmpty()) {
      // The bound has no type variable, so no substitution changes it.
      return ReferenceType.forType(boundType).isSubtypeOfOrEqualTo(argType);
    }
    // Compare the converted bound, in which each type variable is the one in this bound, so that
    // the comparison applies the substitution exactly once.  Applying the substitution first, and
    // then again in the comparison, would replace a type variable that the substitution put in the
    // bound:  for `T[]` with `T := W` and `W := Integer`, it would compare Integer[], not W[].
    ReferenceBound converted = toReferenceBound();
    return converted != null && converted.isLowerBound(argType, substitution);
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
    if (isNonGenericMemberClass(boundType) && getTypeParameterList().isEmpty()) {
      // The bound has no type variable, so no substitution changes it.
      return argType.isSubtypeOfOrEqualTo(ReferenceType.forType(boundType));
    }
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
   * Comparing the unchanged bound would recurse forever. A bound that has no type variable, such as
   * the non-generic member class {@code Outer<String>.Inner}, is not lazy: {@link
   * ParameterBound#forType(Set, java.lang.reflect.Type)} converts it to an eager bound.
   *
   * @param substitution a type substitution
   * @return this bound with the substitution applied, or null
   */
  @Nullable ParameterBound substituteWithUnknowns(Substitution substitution) {
    Substitution completeSubstitution = completeSubstitution(substitution);
    ParameterBound b = this.substitute(completeSubstitution);
    boolean unchanged = b.equals(this);
    assert !unchanged : "substitution " + completeSubstitution + " does not instantiate " + this;
    return unchanged ? null : b;
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
   * <p>Returns true if {@code lowerBound} is the null type, which is a lower bound of every
   * reference type, even if this bound cannot be converted as described below.
   *
   * <p>As for other bounds, the substitution is applied only to {@code lowerBound}, not to this
   * bound. {@code lowerBound} is compared with the {@link ReferenceBound} that {@link
   * #toReferenceBound()} converts this bound to, in which each type variable remains a type
   * variable. That bound stands for every instantiation of its type variables, so only the null
   * type and that bound's own type are lower bounds of it; see {@link
   * LazyReferenceBound#hasLowerBound}. For example, for this bound {@code T[]} with {@code T :=
   * String}, {@code String[]} is not known to be a subtype of {@code T[]}. Returns false if this
   * bound cannot be converted.
   */
  @Override
  boolean hasLowerBound(ParameterBound lowerBound, Substitution substitution) {
    if (lowerBound.isNullType(substitution)) {
      return true;
    }
    ReferenceBound converted = toReferenceBound();
    return converted != null && converted.hasLowerBound(lowerBound, substitution);
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
