package randoop.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import plume.UtilMDE;

/**
 * Represents an intersection type bound on a type parameter in a class,
 * interface, method or constructor (see
 * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.4">JLS section 4.4</a>).
 * Alternatively, in capture conversion, it may also represent the greatest lower bound of two upper bounds (
 * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.10">JLS section 5.1.10</a>).
 * <p>
 * Java requires that an intersection type bound consist of class and
 * interface types, with at most one class, and if there is a class it appears
 * in the conjunction term first. This class preserves the order of the types.
 * In a capture conversion, if both types are classes, one must be a subclass of the other.
 */
class IntersectionTypeBound extends ParameterBound {

  /** the list of type bounds for the intersection bound */
  private List<ParameterBound> boundList;

  /**
   * Create an intersection type bound from the list of type bounds.
   *
   * @param boundList  the list of type bounds
   */
  IntersectionTypeBound(List<ParameterBound> boundList) {
    if (boundList == null) {
      throw new IllegalArgumentException("bounds list may not be null");
    }

    this.boundList = boundList;
  }

  // XXX could be relaxed: only require that the first argument be first, if it is a class (rest can be reordered)
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof IntersectionTypeBound)) {
      return false;
    }
    IntersectionTypeBound b = (IntersectionTypeBound) boundList;
    return this.boundList.equals(b.boundList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(boundList);
  }

  @Override
  public String toString() {
    return UtilMDE.join(boundList, " & ");
  }

  /**
   * {@inheritDoc}
   * @return this bound with the substitution applied to all member bounds
   */
  @Override
  public IntersectionTypeBound apply(Substitution<ReferenceType> substitution) {
    List<ParameterBound> bounds = new ArrayList<>();
    for (ParameterBound bound : this.boundList) {
      bounds.add(bound.apply(substitution));
    }
    return new IntersectionTypeBound(bounds);
  }

  /**
   * {@inheritDoc}
   * @return an intersection bound with capture conversion applied to all member bounds
   */
  @Override
  public ParameterBound applyCaptureConversion() {
    List<ParameterBound> convertedBoundList = new ArrayList<>();
    for (ParameterBound b : boundList) {
      convertedBoundList.add(b.applyCaptureConversion());
    }
    return new IntersectionTypeBound(convertedBoundList);
  }

  /**
   * {@inheritDoc}
   * @return the list of type variables occurring in all of the type bounds of this intersection bound
   */
  @Override
  public List<TypeVariable> getTypeParameters() {
    List<TypeVariable> paramList = new ArrayList<>();
    for (ParameterBound b : boundList) {
      paramList.addAll(b.getTypeParameters());
    }
    return paramList;
  }

  /**
   * {@inheritDoc}
   * @return true if any type in the intersection type bound has a wildcard argument, false otherwise
   */
  @Override
  boolean hasWildcard() {
    for (ParameterBound b : boundList) {
      if (b.hasWildcard()) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * @return true if the argument type has all of types of the member bounds of this object as an upper bound.
   */
  @Override
  public boolean isLowerBound(Type otherType, Substitution<ReferenceType> subst) {
    for (ParameterBound b : boundList) {
      if (!b.isLowerBound(otherType, subst)) {
        return false;
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   * This method should never be tested for {@link IntersectionTypeBound}.
   * Will fail if assertions are enabled.
   * @return false, always
   */
  @Override
  public boolean isSubtypeOf(ParameterBound boundType) {
    assert false : "intersection type bound isSubTypeOf not implemented";
    return false;
  }

  /**
   * {@inheritDoc}
   * @return true if the argument type satisfies all of the bounds in this
   * intersection type bound.
   */
  @Override
  public boolean isUpperBound(Type argType, Substitution<ReferenceType> subst) {
    for (ParameterBound b : boundList) {
      if (!b.isUpperBound(argType, subst)) {
        return false;
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @return true if the argument bound has all of the member bounds of this object as an upper bound.
   */
  @Override
  boolean isUpperBound(ParameterBound bound, Substitution<ReferenceType> substitution) {
    for (ParameterBound b : boundList) {
      if (!b.isUpperBound(bound, substitution)) {
        return false;
      }
    }
    return true;
  }
}
