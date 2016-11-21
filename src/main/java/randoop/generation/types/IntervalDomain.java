package randoop.generation.types;

import java.util.Iterator;
import java.util.Set;

import randoop.types.JavaTypes;
import randoop.types.ReferenceType;

/**
 * Represents a type domain as an interval of reference types determined by a lower and upper bound.
 * Allows bounds to be represented by a set of types, with the least upper bound and greatest lower
 * bound being the formal bounds of the interval.
 * Note that the greatest lower bound is the same as a
 *
 */
public class IntervalDomain implements TypeDomain {

  private final ReferenceType lowerBound;
  private final ReferenceType upperBound;

  /** Creates an empty interval. Not for general use. */
  private IntervalDomain() {
    this.lowerBound = JavaTypes.OBJECT_TYPE;
    this.upperBound = JavaTypes.NULL_TYPE;
  }

  private IntervalDomain(ReferenceType lowerBound, ReferenceType upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  public static TypeDomain createDomain(ReferenceType lowerBound, ReferenceType upperBound) {
    if (lowerBound.isSubtypeOf(upperBound)) {
      return new IntervalDomain(lowerBound, upperBound);
    }
    return EmptyDomain.createDomain();
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public TypeDomain restrictDown(ReferenceType upperBound) {
    return null;
  }

  @Override
  public TypeDomain restrictUp(ReferenceType lowerBound) {
    return null;
  }

  @Override
  public TypeDomain restrictDown(TypeDomain upperDomain) {
    return null;
  }

  @Override
  public TypeDomain restrictUp(TypeDomain lowerDomain) {
    return null;
  }

  @Override
  public Iterator<ReferenceType> iterator() {
    return null;
  }
}
