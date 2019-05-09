package randoop.types;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import randoop.main.RandoopBug;

/**
 * A lazy representation of a type bound in which a type variable occurs. Prevents type recognition
 * from having to deal with recursive type bounds. All methods that need to evaluate the bound are
 * provided with a substitution for the variable for which this object is a bound.
 */
class LazyParameterBound extends ParameterBound {

  /** the type for this bound */
  private final java.lang.reflect.Type boundType;

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
  public boolean equals(Object obj) {
    if (!(obj instanceof LazyParameterBound)) {
      return false;
    }
    LazyParameterBound b = (LazyParameterBound) obj;
    return this.boundType.equals(b.boundType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(boundType);
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
        if (referenceType.isVariable()) {
          return new LazyReferenceBound(referenceType);
        }
        return new EagerReferenceBound(referenceType);
      }
      return this;
    }

    if (boundType instanceof java.lang.reflect.ParameterizedType) {
      boolean isLazy = false;
      List<TypeArgument> argumentList = new ArrayList<>();
      for (java.lang.reflect.Type parameter :
          ((ParameterizedType) boundType).getActualTypeArguments()) {
        TypeArgument typeArgument = substitute(parameter, substitution);
        if (typeArgument == null) {
          return this;
        }
        isLazy = isTypeVariable(parameter) && typeArgument.isVariable();
        argumentList.add(typeArgument);
      }
      GenericClassType classType =
          GenericClassType.forClass((Class<?>) ((ParameterizedType) boundType).getRawType());
      InstantiatedType instantiatedType = new InstantiatedType(classType, argumentList);
      if (isLazy) {
        return new LazyReferenceBound(instantiatedType);
      }
      return new EagerReferenceBound(instantiatedType);
    }

    throw new RandoopBug(
        "lazy parameter bounds should be either a type variable or parameterized type");
  }

  /**
   * Applies a substitution to a reflection type that occurs as an actual argument of a
   * parameterized type bound, to create a type argument to a {@link
   * randoop.types.ParameterizedType}.
   *
   * @param type the reflection type
   * @param substitution the type substitution
   * @return the type argument
   */
  private static TypeArgument substitute(java.lang.reflect.Type type, Substitution substitution) {
    if (type instanceof java.lang.reflect.TypeVariable) {
      ReferenceType referenceType = substitution.get(type);
      if (referenceType != null) {
        return TypeArgument.forType(referenceType);
      }
      return null;
    }

    if (type instanceof java.lang.reflect.ParameterizedType) {
      List<TypeArgument> argumentList = new ArrayList<>();
      for (java.lang.reflect.Type parameter : ((ParameterizedType) type).getActualTypeArguments()) {
        TypeArgument paramType = substitute(parameter, substitution);
        argumentList.add(paramType);
      }
      GenericClassType classType =
          GenericClassType.forClass((Class<?>) ((ParameterizedType) type).getRawType());
      InstantiatedType instantiatedType = new InstantiatedType(classType, argumentList);
      return TypeArgument.forType(instantiatedType);
    }

    if (type instanceof Class) {
      return TypeArgument.forType(ClassOrInterfaceType.forType(type));
    }

    if (type instanceof java.lang.reflect.WildcardType) {
      final java.lang.reflect.WildcardType wildcardType = (java.lang.reflect.WildcardType) type;
      if (wildcardType.getLowerBounds().length > 0) {
        assert wildcardType.getLowerBounds().length == 1
            : "a wildcard is defined by the JLS to only have one bound";
        java.lang.reflect.Type lowerBound = wildcardType.getLowerBounds()[0];
        ParameterBound bound;
        if (lowerBound instanceof java.lang.reflect.TypeVariable) {
          ReferenceType boundType = substitution.get(lowerBound);
          if (boundType != null) {
            bound = ParameterBound.forType(boundType);
          } else {
            bound = new LazyParameterBound(lowerBound);
          }
        } else {
          bound =
              ParameterBound.forType(new HashSet<java.lang.reflect.TypeVariable<?>>(), lowerBound)
                  .substitute(substitution);
        }

        return new WildcardArgument(new WildcardType(bound, false));
      }
      // a wildcard always has an upper bound
      assert wildcardType.getUpperBounds().length == 1
          : "a wildcard is defined by the JLS to only have one bound";
      ParameterBound bound =
          ParameterBound.forTypes(
              new HashSet<java.lang.reflect.TypeVariable<?>>(), wildcardType.getUpperBounds());
      bound = bound.substitute(substitution);
      return new WildcardArgument(new WildcardType(bound, true));
    }

    return null;
  }

  @Override
  public ParameterBound applyCaptureConversion() {
    throw new LazyBoundException();
    // assert false : "unable to do capture conversion on lazy bound " + this;
    // return this;
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    return getTypeParameters(boundType);
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
    } else if (type instanceof java.lang.reflect.ParameterizedType) {
      java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) type;
      for (java.lang.reflect.Type argType : pt.getActualTypeArguments()) {
        variableList.addAll(getTypeParameters(argType));
      }
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
    if (type instanceof java.lang.reflect.ParameterizedType) {
      java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) type;
      for (java.lang.reflect.Type argType : pt.getActualTypeArguments()) {
        if (hasWildcard(argType)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean isGeneric() {
    return true;
  }

  @Override
  public boolean isLowerBound(Type argType, Substitution substitution) {
    ParameterBound b = this.substitute(substitution);
    if (b.equals(this)) {
      throw new IllegalArgumentException(
          "substitution " + substitution + " does not instantiate " + this);
    }
    return b.isLowerBound(argType, substitution);
  }

  @Override
  public boolean isObject() {
    return false;
  }

  @Override
  public boolean isSubtypeOf(ParameterBound boundType) {
    throw new LazyBoundException();
    // assert false : "LazyParameterBound.isSubtypeOf not implemented";
    // return false;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This generic type bound is satisfied by a concrete type if the concrete type formed by
   * applying the substitution to this generic bound is satisfied by the concrete type.
   */
  @Override
  public boolean isUpperBound(Type argType, Substitution substitution) {
    ParameterBound b = this.substitute(substitution);
    if (b.equals(this)) {
      throw new IllegalArgumentException(
          "substitution " + substitution + " does not instantiate " + this);
    }
    return b.isUpperBound(argType, substitution);
  }

  @Override
  boolean isUpperBound(ParameterBound bound, Substitution substitution) {
    throw new LazyBoundException();
    // assert false : " not quite sure what to do with lazy type bound";
    // return false;
  }

  @Override
  public boolean isVariable() {
    return boundType instanceof java.lang.reflect.TypeVariable;
  }

  /** There was an attempt to perform an operation, such as capture conversion, on a lazy bound. */
  static class LazyBoundException extends RuntimeException {
    private static final long serialVersionUID = 20190508;
  }
}
