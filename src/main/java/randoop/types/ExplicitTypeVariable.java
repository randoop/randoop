package randoop.types;

import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Represents a type variable that is a type parameter. (See JLS, section 4.3.) */
public class ExplicitTypeVariable extends TypeVariable {

  /** The type parameter. */
  private final java.lang.reflect.TypeVariable<?> variable;

  /**
   * The type parameters for which {@link #substitute(Substitution)} is executing on the current
   * thread. A type parameter can occur in its own bound, as {@code T} does in {@code T extends
   * Comparable<? extends T>}. When the substitution reaches such an occurrence, it does not apply
   * the substitution to the variable's bound again.
   */
  @SuppressWarnings("nullness:type.argument") // withInitial: get() never returns null
  private static final ThreadLocal<Set<java.lang.reflect.TypeVariable<?>>> substituteIsExecuting =
      ThreadLocal.withInitial(HashSet::new);

  /**
   * Create an {@code ExplicitTypeVariable} for the given type parameter.
   *
   * @param variable the type parameter
   * @param bound the upper bound on the parameter
   */
  /*package*/ ExplicitTypeVariable(
      java.lang.reflect.TypeVariable<?> variable, ParameterBound bound) {
    super(new EagerReferenceBound(JavaTypes.NULL_TYPE), bound);
    this.variable = variable;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Two type parameters are equal if they are the same declared type parameter, even if their
   * bounds differ. Substitution creates a copy of a type parameter whose bounds differ, such as by
   * having a lazy part resolved, and the copy stands for the same type parameter.
   *
   * @return true if the type parameters are the same declared type parameter, false otherwise
   */
  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ExplicitTypeVariable)) {
      return false;
    }
    ExplicitTypeVariable t = (ExplicitTypeVariable) obj;
    return variable.equals(t.variable);
  }

  @Override
  public int hashCode() {
    return variable.hashCode();
  }

  @Override
  public String getFqName() {
    return variable.getName();
  }

  @Override
  public String getBinaryName() {
    return variable.getName();
  }

  @Override
  public String getSimpleName() {
    return this.getFqName();
  }

  /**
   * Returns the reflection type variable that this type variable represents.
   *
   * @return the reflection type variable that this type variable represents
   */
  /*package*/ java.lang.reflect.TypeVariable<?> getReflectionTypeVariable() {
    return this.variable;
  }

  @Override
  public boolean isGeneric(boolean ignoreWildcards) {
    return true;
  }

  @Override
  public ReferenceType substitute(Substitution substitution) {
    ReferenceType type = getSubstitutedType(substitution);
    // A copy of this variable with other bounds has the bounds that the substitution intends.
    if (type != null && (!type.isVariable() || type.equals(this))) {
      return type;
    }
    Set<java.lang.reflect.TypeVariable<?>> substituteIsExecutingSet = substituteIsExecuting.get();
    if (!substituteIsExecutingSet.add(variable)) {
      // This variable occurs in its own bound, which the substitution is already being applied to.
      // Applying the substitution to the bound again would not terminate.  If the enclosing call
      // creates a copy with new bounds, this occurrence in the copy's bound is this variable, not
      // the copy.
      return type != null ? type : this;
    }
    try {
      return substituteIntoBound(type, substitution);
    } finally {
      substituteIsExecutingSet.remove(variable);
    }
  }

  /**
   * Applies the substitution to the upper bound of this type variable, for {@link
   * #substitute(Substitution)}.
   *
   * @param type the type that {@code substitution} maps this variable to, or null if it does not
   *     map this variable or maps it to itself; if non-null, a type variable that is not equal to
   *     this
   * @param substitution the substitution
   * @return the result of applying {@code substitution} to this type variable
   */
  @SuppressWarnings("ReferenceEquality") // detects any change; see Type#substitute
  private ReferenceType substituteIntoBound(
      @Nullable ReferenceType type, Substitution substitution) {
    ParameterBound upperBound = getUpperTypeBound().substitute(substitution);
    if (type == null) {
      if (upperBound != getUpperTypeBound()) {
        return new ExplicitTypeVariable(this.variable, upperBound);
      }
      return this;
    }
    if (upperBound != getUpperTypeBound()) {
      return ((TypeVariable) type).createCopyWithBounds(getLowerTypeBound(), upperBound);
    }
    return type;
  }

  @Override
  public TypeVariable createCopyWithBounds(ParameterBound lowerBound, ParameterBound upperBound) {
    return new ExplicitTypeVariable(this.variable, upperBound);
  }
}
