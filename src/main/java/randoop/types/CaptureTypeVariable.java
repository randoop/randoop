package randoop.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a type variable introduced by capture conversion over a wildcard type argument.
 *
 * A {@code CaptureTypeVariable} has both an upper and lower bound determined by combining the
 * wildcard bound with the {@link ParameterBound} on the type parameter.
 * An object is constructed from a wildcard using the wildcard bound to determine the initial upper
 * or lower bound.
 * The {@link #convert(TypeVariable, Substitution)} method is then used to update the bounds
 * to match the definition in JLS section 5.1.10,
 * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.10">Capture Conversion</a>.
 */
class CaptureTypeVariable extends TypeVariable {

  /** The ID counter for capture conversion variables */
  private static int count = 0;

  /** The integer ID of this capture variable */
  private final int varID;

  /** The wildcard */
  private final WildcardArgument wildcard;

  /**
   * Creates a {@link CaptureTypeVariable} for the given wildcard.
   * Created object is not complete until {@link #convert(TypeVariable, Substitution)} is run.
   *
   * @param wildcard  the wildcard argument
   */
  CaptureTypeVariable(WildcardArgument wildcard) {
    super();
    this.varID = count++;
    this.wildcard = wildcard;

    if (wildcard.hasUpperBound()) {
      setUpperBound(wildcard.getTypeBound());
    } else {
      setLowerBound(wildcard.getTypeBound());
    }
  }

  @Override
  public String toString() {
    return getName() + " of " + wildcard;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof CaptureTypeVariable)) {
      return false;
    }
    CaptureTypeVariable variable = (CaptureTypeVariable) obj;
    return this.varID == variable.varID
        && this.wildcard.equals(variable.wildcard)
        && super.equals(variable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(varID, wildcard, super.hashCode());
  }

  @Override
  boolean isCaptureVariable() {
    return true;
  }

  @Override
  public boolean isGeneric() {
    return true;
  }

  @Override
  public String getName() {
    return "Capture" + varID;
  }

  /**
   * Converts the bounds on this {@code CaptureTypeVariable} by including those of the formal
   * type parameters of the generic type, and applying the implied substitution between the
   * type parameters and capture conversion argument list.
   * Implements the clauses of the JLS section 5.1.10,
   * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.10">Capture Conversion</a>.
   * <p>
   * Creates an upper bound on a type variable resulting from a capture conversion (JLS section 5.1.10)
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
   *
   * @param typeParameter  the formal type parameter of the generic type
   * @param substitution  the capture conversion substitution
   */
  public void convert(TypeVariable typeParameter, Substitution<ReferenceType> substitution) {

    // the lower bound is either the null-type or the wildcard lower bound, so only do upper bound
    ParameterBound parameterBound = typeParameter.getUpperTypeBound().apply(substitution);
    if (getUpperTypeBound().equals(new ReferenceBound(ConcreteTypes.OBJECT_TYPE))) {
      setUpperBound(parameterBound);
    } else {
      List<ParameterBound> boundList = new ArrayList<>();
      boundList.add(parameterBound);
      boundList.add(getUpperTypeBound());
      setUpperBound(new IntersectionTypeBound(boundList));
    }
  }

  /**
   * Returns the type parameters in this type, which is this variable.
   *
   * @return this variable
   */
  @Override
  public List<TypeVariable> getTypeParameters() {
    List<TypeVariable> parameters = new ArrayList<>();
    parameters.add(this);
    return parameters;
  }
}
