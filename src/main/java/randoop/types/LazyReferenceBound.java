package randoop.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A lazy representation of a type bound in which a type variable occurs. Similar in purpose to
 * {@link LazyParameterBound}, but this class uses {@link ReferenceType} as the bound instead of
 * {@code java.lang.reflect.Type}.
 *
 * <p>The bounds of a type variable in the bound type might refer back to this bound. This class
 * behaves like {@link EagerReferenceBound}, except that {@link #getTypeParameters()} does not
 * examine the bounds of those type variables, {@link #substitute(Substitution)} does not substitute
 * into this bound again when it reaches this bound through those type variables, and {@link
 * #isUpperBound(ParameterBound, Substitution)} assumes that a check that reaches this bound again
 * succeeds. Any of these would otherwise result in nonterminating calls.
 *
 * <p>Objects of this class are created by {@link ReferenceBound#forSubstitutedType(ReferenceType)}
 * when applying a substitution to a bound yields a type that contains a type variable.
 */
class LazyReferenceBound extends EagerReferenceBound {

  /** The bounds that {@link #substitute(Substitution)} is currently substituting into. */
  private static final ThreadLocal<Set<LazyReferenceBound>> beingSubstituted =
      ThreadLocal.withInitial(() -> Collections.newSetFromMap(new IdentityHashMap<>()));

  /**
   * The checks that {@link #isUpperBound(ParameterBound, Substitution)} is currently performing.
   * Each is a list of the receiver and the two arguments. Substitution creates copies of type
   * variables, so a repeated check is equal to, but not identical to, the earlier check.
   */
  private static final ThreadLocal<Set<List<Object>>> beingChecked =
      ThreadLocal.withInitial(HashSet::new);

  LazyReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  @Override
  public EagerReferenceBound substitute(Substitution substitution) {
    // If the substitution has no effect on this bound, just return this.
    if (substitution.isEmpty()) {
      return this;
    }
    // Substituting into a type variable substitutes into its bounds, which might be this bound.
    Set<LazyReferenceBound> inProgress = beingSubstituted.get();
    if (!inProgress.add(this)) {
      return this;
    }
    ReferenceType referenceType;
    try {
      referenceType = getBoundType().substitute(substitution);
    } finally {
      inProgress.remove(this);
    }

    if (referenceType.equals(getBoundType())) {
      return this;
    }
    return forSubstitutedType(referenceType);
  }

  @Override
  public LazyReferenceBound applyCaptureConversion() {
    ReferenceType referenceType = getBoundType().applyCaptureConversion();
    if (referenceType.equals(getBoundType())) {
      return this;
    }
    return new LazyReferenceBound(referenceType);
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
  boolean isUpperBound(ParameterBound bound, Substitution substitution) {
    // Checking a type variable checks its bounds, which might be this bound. As for any recursive
    // bound, assume that the inner check succeeds.
    Set<List<Object>> inProgress = beingChecked.get();
    List<Object> check = Arrays.asList(this, bound, substitution);
    if (!inProgress.add(check)) {
      return true;
    }
    try {
      return super.isUpperBound(bound, substitution);
    } finally {
      inProgress.remove(check);
    }
  }
}
