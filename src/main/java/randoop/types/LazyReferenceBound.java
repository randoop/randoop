package randoop.types;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A lazy representation of a type bound in which a type variable occurs. Similar in purpose to
 * {@link LazyParameterBound}, but this class uses {@link ReferenceType} as the bound instead of
 * {@code java.lang.reflect.Type}. Also, prevents access to recursive type bounds that would
 * otherwise result in nonterminating calls to {@link #getTypeParameters()}.
 *
 * <p>Objects of this class are created by {@link LazyParameterBound#substitute(Substitution)} when
 * the substitution would replace a type variable with another type variable.
 */
class LazyReferenceBound extends ReferenceBound {

  LazyReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  @Override
  public ReferenceBound substitute(Substitution substitution) {
    // If the substitution has no effect on this bound, just return this.
    if (substitution.isEmpty()) {
      return this;
    }
    for (TypeVariable parameter : getTypeParameters()) {
      if (substitution.get(parameter) == null) {
        return this;
      }
    }

    ReferenceType referenceType = getBoundType().substitute(substitution);

    if (referenceType.equals(getBoundType())) {
      return this;
    }

    if (getBoundType().isVariable()) {
      if (referenceType.isVariable()) {
        return new LazyReferenceBound(referenceType);
      }
      return new EagerReferenceBound(referenceType);
    }

    if (getBoundType().isParameterized()) {
      // XXX technically, need to check if variable argument was replaced by variable
      // if so should return new LazyReferenceBound(referenceType)
      // But highly unlikely so for now only including code to do else case
      return new EagerReferenceBound(referenceType);
    }
    return this;
  }

  @Override
  public ReferenceBound applyCaptureConversion() {
    return null;
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    if (getBoundType().isVariable()) {
      List<TypeVariable> parameters = new ArrayList<>(1);
      parameters.add((TypeVariable) getBoundType());
      return parameters;
    } else if (getBoundType().isParameterized()) {
      List<ReferenceType> referenceArgs =
          ((InstantiatedType) getBoundType()).getReferenceArguments();
      List<TypeVariable> parameters = new ArrayList<>(referenceArgs.size());
      for (ReferenceType argType : referenceArgs) {
        if (argType.isVariable()) {
          parameters.add((TypeVariable) argType);
        }
      }
      return parameters;
    } else {
      return new ArrayList<>(0);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the type variables at any depth of the bound type, without examining their bounds,
   * which might refer to this bound.
   */
  @Override
  List<TypeVariable> getLazyTypeParameters() {
    Set<TypeVariable> parameters = new LinkedHashSet<>();
    addTypeVariables(getBoundType(), parameters);
    return new ArrayList<>(parameters);
  }

  /**
   * Adds the type variables that occur in the given type to {@code parameters}. Does not examine
   * the bounds of the type variables.
   *
   * @param type a type
   * @param parameters the set to which to add the type variables
   */
  private static void addTypeVariables(ReferenceType type, Set<TypeVariable> parameters) {
    if (type.isVariable()) {
      parameters.add((TypeVariable) type);
    } else if (type instanceof InstantiatedType) {
      for (TypeArgument argument : ((InstantiatedType) type).getTypeArguments()) {
        if (argument instanceof ReferenceArgument) {
          addTypeVariables(((ReferenceArgument) argument).getReferenceType(), parameters);
        } else {
          ParameterBound bound = ((WildcardArgument) argument).getTypeBound();
          if (bound instanceof ReferenceBound) {
            addTypeVariables(((ReferenceBound) bound).getBoundType(), parameters);
          }
        }
      }
    } else if (type.isArray()) {
      Type componentType = ((ArrayType) type).getComponentType();
      if (componentType.isReferenceType()) {
        addTypeVariables((ReferenceType) componentType, parameters);
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>The bound type, after substitution, may contain type variables, such as one that the
   * substitution maps to itself. It is compared as an {@link EagerReferenceBound} would be.
   */
  @Override
  public boolean isLowerBound(Type argType, Substitution substitution) {
    return new EagerReferenceBound(getBoundType().substitute(substitution))
        .isLowerBound(argType, substitution);
  }

  @Override
  public boolean isSubtypeOfOrEqualTo(ParameterBound boundType) {
    assert false : "LazyReferenceBound.isSubtypeOfOrEqualTo is not implemented";
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * <p>The bound type, after substitution, may contain type variables, such as one that the
   * substitution maps to itself. It is compared as an {@link EagerReferenceBound} would be.
   */
  @Override
  public boolean isUpperBound(Type argType, Substitution substitution) {
    return new EagerReferenceBound(getBoundType().substitute(substitution))
        .isUpperBound(argType, substitution);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns true only if {@code lowerBound} is the null type, because the type of this bound
   * contains type variables.
   */
  @Override
  boolean hasLowerBound(ParameterBound lowerBound, Substitution substitution) {
    return lowerBound.isNullType(substitution);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns true only if {@code lowerBound} is the null type, because the type of this bound
   * contains type variables.
   */
  @Override
  boolean hasLowerBound(ParameterBound lowerBound, Substitution substitution) {
    return lowerBound.isNullType(substitution);
  }
}
