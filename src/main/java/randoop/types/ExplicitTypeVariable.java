package randoop.types;

import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Represents a type variable that is a type parameter. (See JLS, section 4.3.) */
public class ExplicitTypeVariable extends TypeVariable {

  /** The type parameter. */
  private final java.lang.reflect.TypeVariable<?> variable;

  /**
   * The type parameters for which {@link #substitute(Substitution)} is in progress, in the current
   * thread.
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
  ExplicitTypeVariable(java.lang.reflect.TypeVariable<?> variable, ParameterBound bound) {
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
  java.lang.reflect.TypeVariable<?> getReflectionTypeVariable() {
    return this.variable;
  }

  @Override
  public boolean isGeneric(boolean ignoreWildcards) {
    return true;
  }

  @Override
  public ReferenceType substitute(Substitution substitution) {
    ReferenceType type = substitution.get(this);
    if (type != null && substitution.mapsToItself(this)) {
      // The substitution maps this type variable to itself.  Keep this copy and its bounds, rather
      // than the substitution's copy.  If the substitution maps this type variable to a copy of it
      // with other bounds, that copy is the result.
      type = null;
    }
    if (type != null && !type.isVariable()) {
      return type;
    }
    // A type variable can occur in its own bound, directly as in `T extends Comparable<T>` or
    // through the bounds of other type variables.  Such an occurrence remains as it is.
    Set<java.lang.reflect.TypeVariable<?>> inProgress = substituteIsExecuting.get();
    if (!inProgress.add(variable)) {
      return type == null ? this : type;
    }
    ParameterBound upperBound;
    try {
      upperBound = getUpperTypeBound().substitute(substitution);
    } finally {
      inProgress.remove(variable);
    }
    if (type == null) {
      if (!isSameAsUpperBound(upperBound)) {
        return new ExplicitTypeVariable(this.variable, upperBound);
      }
      return this;
    }
    if (!isSameAsUpperBound(upperBound)) {
      return ((TypeVariable) type).createCopyWithBounds(getLowerTypeBound(), upperBound);
    }
    return type;
  }

  /**
   * Returns true if the given bound represents the same type as the upper bound of this variable,
   * with identical type variables, as determined by {@link
   * TypeVariable#areIdentical(ParameterBound, ParameterBound)}. The two can differ in
   * representation: {@link LazyParameterBound#substitute}, given a substitution that maps each type
   * variable in the bound to itself, returns a {@link ReferenceBound} for the same type.
   *
   * <p>Comparing the bounds with {@link ParameterBound#equals(Object)} instead would discard a
   * substitution that changes only the bounds of a type variable in the bound, because copies of a
   * type variable are equal even if their bounds differ.
   *
   * <p>Treating such bounds as different would make {@link #substitute(Substitution)} create a copy
   * of this variable that is not equal to it. Then {@link TypeVariable#isInstantiationOf} on a
   * variable that occurs in its own bound, such as {@code T extends Map<T, String>}, would compare
   * the variable to such a copy, which creates another copy, and would not terminate.
   *
   * @param bound a bound
   * @return true if {@code bound} represents the same type as the upper bound of this variable
   */
  private boolean isSameAsUpperBound(ParameterBound bound) {
    return areIdentical(bound, getUpperTypeBound());
  }

  @Override
  public TypeVariable createCopyWithBounds(ParameterBound lowerBound, ParameterBound upperBound) {
    return new ExplicitTypeVariable(this.variable, upperBound);
  }
}
