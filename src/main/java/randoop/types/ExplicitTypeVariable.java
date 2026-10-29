package randoop.types;

import java.util.Objects;
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
   * <p>Checks that the type parameter is equal. This may be more restrictive than desired because
   * equivalent TypeVariable objects from different instances of the same type may be distinct.
   *
   * @return true if the type parameters are equal, false otherwise
   */
  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ExplicitTypeVariable)) {
      return isAssignableFrom(null);
    }
    ExplicitTypeVariable t = (ExplicitTypeVariable) obj;
    return variable.equals(t.variable) && super.equals(t);
  }

  @Override
  public int hashCode() {
    return Objects.hash(variable, super.hashCode());
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

  /**
   * {@inheritDoc}
   *
   * @return the reflection type variable for this type parameter
   */
  @Override
  Object getIdentity() {
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
