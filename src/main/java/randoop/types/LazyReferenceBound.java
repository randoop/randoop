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
 * <p>Objects of this class are created by {@link ReferenceBound#forSubstitutedType(ReferenceType)}
 * when applying a substitution to a bound yields a type that contains a type variable.
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
    return forSubstitutedType(referenceType);
  }

  @Override
  public ReferenceBound applyCaptureConversion() {
    return null;
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    Set<TypeVariable> parameters = new LinkedHashSet<>();
    addTypeVariables(getBoundType(), parameters);
    return new ArrayList<>(parameters);
  }

  /**
   * Adds the type variables that occur in the given type to {@code parameters}. Does not examine
   * the bounds of those type variables, which might refer back to this bound.
   *
   * @param type the type
   * @param parameters the set to which to add the type variables
   */
  private static void addTypeVariables(ReferenceType type, Set<TypeVariable> parameters) {
    if (type.isVariable()) {
      parameters.add((TypeVariable) type);
    } else if (type.isArray()) {
      Type componentType = ((ArrayType) type).getComponentType();
      if (componentType.isReferenceType()) {
        addTypeVariables((ReferenceType) componentType, parameters);
      }
    } else if (type instanceof ClassOrInterfaceType) {
      ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
      if (classType.isMemberClass()) {
        addTypeVariables(classType.enclosingType, parameters);
      }
      for (TypeArgument argument : classType.getTypeArguments()) {
        if (argument instanceof ReferenceArgument) {
          addTypeVariables(((ReferenceArgument) argument).getReferenceType(), parameters);
        } else if (argument instanceof WildcardArgument) {
          ParameterBound bound = ((WildcardArgument) argument).getTypeBound();
          if (bound instanceof ReferenceBound) {
            addTypeVariables(((ReferenceBound) bound).getBoundType(), parameters);
          } else {
            parameters.addAll(bound.getTypeParameters());
          }
        }
      }
    }
  }

  @Override
  public boolean isLowerBound(Type argType, Substitution substitution) {
    ReferenceBound b = this.substitute(substitution);
    return !this.equals(b) && b.isLowerBound(argType, substitution);
  }

  @Override
  public boolean isSubtypeOfOrEqualTo(ParameterBound boundType) {
    assert false : "LazyReferenceBound.isSubtypeOfOrEqualTo is not implemented";
    return false;
  }

  @Override
  public boolean isUpperBound(Type argType, Substitution substitution) {
    ReferenceBound b = this.substitute(substitution);
    return !this.equals(b) && b.isUpperBound(argType, substitution);
  }

  @Override
  boolean isUpperBound(ParameterBound bound, Substitution substitution) {
    assert false : "isUpperBound(ParameterBound, Substitution) not implemented";
    return false;
  }
}
