package randoop.types;

import java.util.Objects;

/**
 * Implements an upper bound on a type variable resulting from a capture conversion (JLS section 5.1.10)
 * in the case that a wildcard has an upper bound other than Object.
 * In particular, each object represents a bound on a variable <code>S<sub>i</sub></code> in a
 * parameterized type
 * <code>C&lt;S<sub>1</sub>,...,S<sub>n</sub>&gt;</code>
 * defined as
 * <code>glb(B<sub>i</sub>, U<sub>i</sub>[A<sub>i</sub>:=S<sub>i</sub>])</code>
 * where
 * <ul>
 *   <li><code>U<sub>i</sub></code> is the upper bound on the type variable <code>A<sub>i</sub></code>
 *   in the declared class <code>C&lt;A<sub>1</sub>,...,A<sub>n</sub>&gt;</code>,</li>
 *   <li><code>glb(S, T)</code> for types <code>S</code> and <code>T</code> is the intersection type
 *   <code>S &amp; T</code>.</li>
 * </ul>
 * The JLS states that if <code>S</code> and <code>T</code> are both class types not related as
 * subtypes, then the greatest lower bound of the two types is a compiler error.
 * Technically it is the null type.
 * <p>
 * Related to {@link IntersectionTypeBound} except the wildcard bound may be an arbitrary reference
 * type.
 */
public class GLBTypeBound extends TypeBound {

  /** The bound on the declaration type variable */
  private final ParameterBound parameterBound;

  /** The wildcard bound */
  private final TypeBound wildcardBound;

  /**
   * Creates a GLB bound from the given parameter and wildcard bound.
   *
   * @param parameterBound  the parameter bound
   * @param wildcardBound  the wildcard bound
   */
  public GLBTypeBound(ParameterBound parameterBound, TypeBound wildcardBound) {
    this.parameterBound = parameterBound;
    this.wildcardBound = wildcardBound;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof GLBTypeBound)) {
      return false;
    }
    GLBTypeBound bound = (GLBTypeBound)obj;
    return this.parameterBound.equals(bound.parameterBound)
            && this.wildcardBound.equals(bound.wildcardBound);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parameterBound, wildcardBound);
  }

  @Override
  public String toString() {
    return parameterBound.toString() + " & " + wildcardBound.toString();
  }

  /**
   * {@inheritDoc}
   * @return true if both bounds are satisfied by the type, false otherwise
   */
  @Override
  public boolean isSatisfiedBy(GeneralType argType, Substitution<ReferenceType> subst) {
    return parameterBound.isSatisfiedBy(argType, subst)
            && wildcardBound.isSatisfiedBy(argType, subst);
  }

  /**
   * {@inheritDoc}
   * @return true if both bounds are satisfied by the type, false otherwise
   */
  @Override
  public boolean isSatisfiedBy(GeneralType argType) {
    return parameterBound.isSatisfiedBy(argType)
            && wildcardBound.isSatisfiedBy(argType);
  }

  /**
   * {@inheritDoc}
   * @return true if both bounds are a subtype of the given type, false otherwise
   */
  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    return parameterBound.isSubtypeOf(otherType)
            && wildcardBound.isSubtypeOf(otherType);
  }

  /**
   * {@inheritDoc}
   * @return the bound formed by applying the substitution to this bound
   */
  @Override
  public TypeBound apply(Substitution<ReferenceType> substitution) {
    return new GLBTypeBound(parameterBound.apply(substitution), wildcardBound.apply(substitution));
  }
}
