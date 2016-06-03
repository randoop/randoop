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
class CaptureTypeVariable extends AbstractTypeVariable {

  /** The ID counter for capture conversion variables */
  private static int count = 0;

  /** The integer ID of this capture variable */
  private final int varID;

  /** The wildcard */
  private final WildcardArgument wildcard;

  /** The upper bound */
  private TypeBound upperBound;

  /** The lower bound */
  private ReferenceType lowerBoundType;

  /**
   * Creates a {@link CaptureTypeVariable} for the given wildcard.
   * Created object is not complete until {@link #convert(TypeVariable, Substitution)} is run.
   *
   * @param wildcard  the wildcard argument
   */
  CaptureTypeVariable(WildcardArgument wildcard) {
    this.varID = count++;
    this.wildcard = wildcard;
    upperBound = new ClassOrInterfaceTypeBound(ConcreteTypes.OBJECT_TYPE);
    lowerBoundType = ConcreteTypes.NULL_TYPE;
    if (wildcard.hasUpperBound()) {
      upperBound = TypeBound.forType(wildcard.getBoundType());
    } else {
      lowerBoundType = wildcard.getBoundType();
    }
  }

  @Override
  public String toString() {
    return getName() + " of " + wildcard;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof CaptureTypeVariable)) {
      return false;
    }
    CaptureTypeVariable variable = (CaptureTypeVariable)obj;
    return this.varID == variable.varID
            && this.wildcard.equals(variable.wildcard)
            && this.upperBound.equals(variable.upperBound)
            && this.lowerBoundType.equals(variable.lowerBoundType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(varID, wildcard, upperBound, lowerBoundType);
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
   *
   * @param typeParameter  the formal type parameter of the generic type
   * @param substitution  the capture conversion substitution
   */
  public void convert(TypeVariable typeParameter, Substitution<ReferenceType> substitution) {

    // the lower bound is either the null-type or the wildcard lower bound, so only do upper bound
    ParameterBound parameterBound = typeParameter.getTypeBound().apply(substitution);
    if (upperBound.equals(new ClassOrInterfaceTypeBound(ConcreteTypes.OBJECT_TYPE))) {
      upperBound = parameterBound;
    } else {
      upperBound = new GLBTypeBound(parameterBound, upperBound);
    }
  }

  @Override
  public TypeBound getTypeBound() {
    return upperBound;
  }

  @Override
  public ReferenceType getLowerTypeBound() {
    return lowerBoundType;
  }

  @Override
  public List<AbstractTypeVariable> getTypeParameters() {
    List<AbstractTypeVariable> parameters = new ArrayList<>();
    parameters.add(this);
    return parameters;
  }

}
