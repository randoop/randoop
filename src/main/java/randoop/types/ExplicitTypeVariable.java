package randoop.types;

import org.checkerframework.checker.nullness.qual.Nullable;

/** Represents a type variable that is a type parameter. (See JLS, section 4.3.) */
public class ExplicitTypeVariable extends TypeVariable {

  /** The type parameter. */
  private final java.lang.reflect.TypeVariable<?> variable;

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
    if (this.equals(type)) {
      // The substitution maps this type variable to itself.  Keep this copy and its bounds, rather
      // than the substitution's copy.
      type = null;
    }
    if (type != null && !type.isVariable()) {
      return type;
    }
    ParameterBound upperBound = getUpperTypeBound().substitute(substitution);
    if (type == null) {
      if (!upperBound.equals(getUpperTypeBound())) {
        return new ExplicitTypeVariable(this.variable, upperBound);
      }
      return this;
    }
    if (!upperBound.equals(getUpperTypeBound())) {
      return ((TypeVariable) type).createCopyWithBounds(getLowerTypeBound(), upperBound);
    }
    return type;
  }

  @Override
  public TypeVariable createCopyWithBounds(ParameterBound lowerBound, ParameterBound upperBound) {
    return new ExplicitTypeVariable(this.variable, upperBound);
  }
}
