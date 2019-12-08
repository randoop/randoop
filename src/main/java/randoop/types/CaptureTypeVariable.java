package randoop.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a type variable introduced by capture conversion over a wildcard type argument.
 *
 * <p>A {@code CaptureTypeVariable} has both an upper and lower bound determined by combining the
 * wildcard bound with the {@link ParameterBound} on the type parameter. An object is constructed
 * from a wildcard using the wildcard bound to determine the initial upper or lower bound. The
 * {@link #convert(TypeVariable, Substitution)} method is then used to update the bounds to match
 * the definition in JLS section 5.1.10, <a
 * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.10">Capture
 * Conversion</a>.
 */
class CaptureTypeVariable extends TypeVariable {

  /** The ID counter for capture conversion variables. */
  private static int count = 0;

  /** The integer ID of this capture variable. */
  private final int varID;

  /** The wildcard. */
  private final WildcardArgument wildcard;

  /**
   * Creates a {@link CaptureTypeVariable} for the given wildcard. Created object is not complete
   * until {@link #convert(TypeVariable, Substitution)} is run.
   *
   * @param wildcard the wildcard argument
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

  /**
   * Creates a {@link CaptureTypeVariable} with explicitly given {@code ID}, wildcard, and bounds.
   *
   * @param varID the variable ID for the created variable
   * @param wildcard the wildcard for the created variable
   * @param lowerBound the lower type bound of the variable
   * @param upperBound the upper type bound of the variable
   */
  private CaptureTypeVariable(
      int varID, WildcardArgument wildcard, ParameterBound lowerBound, ParameterBound upperBound) {
    super(lowerBound, upperBound);
    this.varID = varID;
    this.wildcard = wildcard;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
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
  public String toString() {
    return getName() + " of " + wildcard;
  }

  /**
   * Converts the bounds on this {@code CaptureTypeVariable} by including those of the formal type
   * parameters of the generic type, and applying the implied substitution between the type
   * parameters and capture conversion argument list. Implements the clauses of the JLS section
   * 5.1.10, <a
   * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.10">Capture
   * Conversion</a>.
   *
   * <p>Creates an upper bound on a type variable resulting from a capture conversion (JLS section
   * 5.1.10) in the case that a wildcard has an upper bound other than Object. In particular, each
   * object represents a bound on a variable {@code Si} in a parameterized type {@code C<S1,...,Sn>}
   * defined as {@code glb(Bi, Ui[Ai:=Si])} where
   *
   * <ul>
   *   <li>{@code Ui} is the upper bound on the type variable {@code Ai} in the declared class
   *       {@code C<A1,...,An>},
   *   <li>{@code glb(S, T)} for types {@code S} and {@code T} is the intersection type {@code S &
   *       T}.
   * </ul>
   *
   * The JLS states that if {@code S} and {@code T} are both class types not related as subtypes,
   * then the greatest lower bound of the two types is a compiler error. Technically it is the null
   * type.
   *
   * @param typeParameter the formal type parameter of the generic type
   * @param substitution the capture conversion substitution
   */
  public void convert(TypeVariable typeParameter, Substitution substitution) {
    // the lower bound is either the null-type or the wildcard lower bound, so only do upper bound
    ParameterBound parameterBound = typeParameter.getUpperTypeBound().substitute(substitution);
    if (getUpperTypeBound().isObject()) {
      setUpperBound(parameterBound);
    } else {
      List<ParameterBound> boundList = new ArrayList<>();
      boundList.add(parameterBound);
      boundList.add(getUpperTypeBound());
      setUpperBound(new IntersectionTypeBound(boundList));
    }
  }

  @Override
  public String getName() {
    return "Capture" + varID;
  }

  @Override
  public String getSimpleName() {
    return this.getName();
  }

  @Override
  public boolean isCaptureVariable() {
    return true;
  }

  @Override
  public boolean isGeneric() {
    return true;
  }

  @Override
  public ReferenceType substitute(Substitution substitution) {
    ReferenceType type = substitution.get(this);
    // if this variable replaced by non-variable, return non-variable
    if (type != null && !type.isVariable()) {
      return type;
    }
    // otherwise, apply to bounds
    ParameterBound lowerBound = getLowerTypeBound().substitute(substitution);
    ParameterBound upperBound = getUpperTypeBound().substitute(substitution);

    if (type == null) {
      // if bounds are affected, return a new copy of this variable with new bounds
      if (!lowerBound.equals(getLowerTypeBound()) || !upperBound.equals(getUpperTypeBound())) {
        WildcardArgument updatedWildcard = wildcard.substitute(substitution);
        return new CaptureTypeVariable(this.varID, updatedWildcard, lowerBound, upperBound);
      }
      return this;
    }

    if (!lowerBound.equals(getLowerTypeBound()) || !upperBound.equals(getUpperTypeBound())) {
      // need a new variable based on type with updated bounds
      return ((TypeVariable) type).createCopyWithBounds(lowerBound, upperBound);
    }
    return type;
  }

  @Override
  public TypeVariable createCopyWithBounds(ParameterBound lowerBound, ParameterBound upperBound) {
    return new CaptureTypeVariable(this.varID, this.wildcard, lowerBound, upperBound);
  }
}
