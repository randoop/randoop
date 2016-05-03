package randoop.types;

import java.util.List;
import java.util.Objects;

import plume.UtilMDE;

/**
 * Represents an intersection type bound on a type parameter in a class,
 * interface, method or constructor (see JLS section 4.4).
 * <p>
 * Java requires that an intersection type bound consist of class and
 * interface types, with at most one class, and if there is a class it appears
 * in the conjunction term first.
 * This class preserves the order of the types, just in case it becomes
 * necessary to dump the bound to compilable code.
 */
public class IntersectionTypeBound extends ClassOrInterfaceBound {

  /** the list of type bounds for the intersection bound */
  private List<ClassOrInterfaceBound> boundList;

  /**
   * Create an intersection type bound from the list of type bounds.
   *
   * @param boundList  the list of type bounds
   */
  public IntersectionTypeBound(List<ClassOrInterfaceBound> boundList) {
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
   * Checks whether the argument type satisfies all of the bounds in this
   * intersection type bound.
   */
  @Override
  public boolean isSatisfiedBy(GeneralType argType, Substitution<ReferenceType> subst) {
    for (ParameterBound b : boundList) {
      if (!b.isSatisfiedBy(argType, subst)) {
        return false;
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   * Checks whether the argument type satisfies all of the bounds in this intersection type bound.
   */
  @Override
  public boolean isSatisfiedBy(GeneralType argType) {
    for (ParameterBound bound : boundList) {
      if (! bound.isSatisfiedBy(argType)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isSubtypeOf(GeneralType otherType) {
    for (ParameterBound b : boundList) {
      if (! b.isSubtypeOf(otherType)) {
        return false;
      }
    }
    return true;
  }

}
