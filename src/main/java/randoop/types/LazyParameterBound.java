package randoop.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
  private @Nullable List<TypeVariable> typeParameters = null;

  /**
   * Creates a {@code LazyParameterBound} from the given rawtype and type parameters.
   *
   * @param boundType the reflection type for this bound
   */
  LazyParameterBound(java.lang.reflect.Type boundType) {
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
    if (substitution.isEmpty()) {
      return this;
    }
    if (boundType instanceof java.lang.reflect.TypeVariable) {
      ReferenceType referenceType = substitution.get(boundType);
      if (referenceType != null) {
        return ReferenceBound.forSubstitutedType(referenceType);
      }
      return this;
    }

    if (boundType instanceof ParameterizedType) {
      java.lang.reflect.Type[] actualTypeArgs =
          ((ParameterizedType) boundType).getActualTypeArguments();
      List<TypeArgument> argumentList = new ArrayList<>(actualTypeArgs.length);
      for (java.lang.reflect.Type parameter : actualTypeArgs) {
        TypeArgument typeArgument = substitute(parameter, substitution);
        if (typeArgument == null) {
          return this;
        }
        argumentList.add(typeArgument);
      }
      GenericClassType classType =
          GenericClassType.forClass((Class<?>) ((ParameterizedType) boundType).getRawType());
      return ReferenceBound.forSubstitutedType(new InstantiatedType(classType, argumentList));
    }

    if (boundType instanceof GenericArrayType) {
      TypeArgument typeArgument = substitute(boundType, substitution);
      if (typeArgument == null) {
        return this;
      }
      return ReferenceBound.forSubstitutedType(
          ((ReferenceArgument) typeArgument).getReferenceType());
    }

    throw new RandoopBug(
        "lazy parameter bounds should be a type variable, parameterized type, or array type");
  }

  /**
   * Applies a substitution to a reflection type that occurs as an actual argument of a
   * parameterized type bound, to create a type argument to a {@link
   * randoop.types.ParameterizedType}.
   *
   * @param type the reflection type
   * @param substitution the type substitution
   * @return the type argument, or null if the substitution does not map a type variable in {@code
   *     type} or maps the component type of an array type to the null type
   */
  private static TypeArgument substitute(java.lang.reflect.Type type, Substitution substitution) {
    if (type instanceof java.lang.reflect.TypeVariable) {
      ReferenceType referenceType = substitution.get(type);
      if (referenceType != null) {
        return TypeArgument.forType(referenceType);
      }
      return null;
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
      return TypeArgument.forType(instantiatedType);
    }

    if (type instanceof Class) {
      return TypeArgument.forType(ClassOrInterfaceType.forType(type));
    }

    if (type instanceof GenericArrayType) {
      TypeArgument componentArgument =
          substitute(((GenericArrayType) type).getGenericComponentType(), substitution);
      if (!(componentArgument instanceof ReferenceArgument)) {
        return null;
      }
      ReferenceType componentType = ((ReferenceArgument) componentArgument).getReferenceType();
      // There is no array of the null type.
      if (componentType.equals(JavaTypes.NULL_TYPE)) {
        return null;
      }
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
            bound = new LazyParameterBound(lowerBound);
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

  @Override
  List<TypeVariable> getLazyTypeParameters() {
    return getTypeParameters();
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
    } else if (type instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) type;
      for (java.lang.reflect.Type argType : pt.getActualTypeArguments()) {
        variableList.addAll(getTypeParameters(argType));
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

  @Override
  boolean hasWildcard() {
    return hasWildcard(boundType);
  }

  private static boolean hasWildcard(java.lang.reflect.Type type) {
    if (type instanceof java.lang.reflect.WildcardType) {
      return true;
    }
    if (type instanceof java.lang.reflect.TypeVariable) {
      for (java.lang.reflect.Type bound : ((java.lang.reflect.TypeVariable) type).getBounds()) {
        if (hasWildcard(bound)) {
          return true;
        }
      }
      return false;
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) type;
      for (java.lang.reflect.Type argType : pt.getActualTypeArguments()) {
        if (hasWildcard(argType)) {
          return true;
        }
      }
    }
    if (type instanceof GenericArrayType) {
      return hasWildcard(((GenericArrayType) type).getGenericComponentType());
    }
    return false;
  }

  @Override
  public boolean hasCaptureVariable() {
    return hasCaptureVariable(boundType);
  }

  /**
   * Returns true if the given type has a capture variable.
   *
   * @param type the type to test
   * @return true if the given type has a capture variable
   */
  private static boolean hasCaptureVariable(java.lang.reflect.Type type) {
    if (type instanceof java.lang.reflect.TypeVariable) {
      for (java.lang.reflect.Type bound : ((java.lang.reflect.TypeVariable) type).getBounds()) {
        if (hasCaptureVariable(bound)) {
          return true;
        }
      }
      return false;
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) type;
      for (java.lang.reflect.Type argType : pt.getActualTypeArguments()) {
        if (hasCaptureVariable(argType)) {
          return true;
        }
      }
    }
    if (type instanceof GenericArrayType) {
      return hasCaptureVariable(((GenericArrayType) type).getGenericComponentType());
    }
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
    ParameterBound b = instantiate(completeSubstitution(substitution));
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
    ParameterBound b = instantiate(completeSubstitution(substitution));
    return b != null && b.isUpperBound(argType, substitution);
  }

  /**
   * {@inheritDoc}
   *
   * <p>A type variable in this bound that the substitution does not replace stands for an unknown
   * type, as in {@link #isUpperBound(Type, Substitution)}.
   */
  @Override
  boolean isUpperBound(ParameterBound bound, Substitution substitution) {
    Substitution completeSubstitution = completeSubstitution(substitution);
    ParameterBound b = instantiate(completeSubstitution);
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
   * Applies a substitution that maps every type variable in this bound, as returned by {@link
   * #completeSubstitution(Substitution)}. Returns null if the result is still this bound, which
   * happens if the substitution maps the component type of an array type to the null type, or for a
   * kind of type that {@link #substitute(Substitution)} does not handle. Comparing the unchanged
   * bound would recurse forever.
   *
   * @param completeSubstitution a substitution that maps every type variable in this bound
   * @return this bound with the substitution applied, or null
   */
  private @Nullable ParameterBound instantiate(Substitution completeSubstitution) {
    ParameterBound b = this.substitute(completeSubstitution);
    if (b.equals(this)) {
      return null;
    }
    return b;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns false, because the type of this bound cannot be determined without a substitution
   * for its type variables.
   */
  @Override
  boolean hasUpperBound(ReferenceType upperBound, Substitution substitution) {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns false if the substitution does not instantiate this bound.
   */
  @Override
  boolean isLowerBound(ParameterBound bound, Substitution substitution) {
    ParameterBound b = this.substitute(substitution);
    return !b.equals(this) && b.isLowerBound(bound, substitution);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns true only if {@code lowerBound} is the null type, because the type of this bound
   * cannot be determined without a substitution for its type variables.
   */
  @Override
  boolean hasLowerBound(ParameterBound lowerBound, Substitution substitution) {
    return lowerBound.isNullType(substitution);
  }

  @Override
  public boolean isVariable() {
    return boundType instanceof java.lang.reflect.TypeVariable;
  }

  /** There was an attempt to perform an operation, such as a subtype test, on a lazy bound. */
  static class LazyBoundException extends RuntimeException {
    /** Creates a new LazyBoundException. */
    public LazyBoundException() {}

    /** ID for serializing this class. */
    private static final long serialVersionUID = 20190508;
  }
}
