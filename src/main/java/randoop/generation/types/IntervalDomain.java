package randoop.generation.types;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import randoop.types.JavaTypes;
import randoop.types.ReferenceType;

/**
 * Represents a type domain as an interval of reference types determined by a lower and upper bound.
 * Allows bounds to be represented by a set of types, with the least upper bound and greatest lower
 * bound being the formal bounds of the interval. Note that the greatest lower bound is the same as
 * a
 */
public class IntervalDomain implements TypeDomain {

  private final Set<ReferenceType> lowerBounds;
  private final Set<ReferenceType> upperBounds;

  /** Creates the trivial interval. Not for general use. */
  private IntervalDomain() {
    this.lowerBounds = new HashSet<>();
    this.lowerBounds.add(JavaTypes.NULL_TYPE);
    this.upperBounds = new HashSet<>();
    this.upperBounds.add(JavaTypes.OBJECT_TYPE);
  }

  private IntervalDomain(Set<ReferenceType> lowerBounds, Set<ReferenceType> upperBounds) {
    this.lowerBounds = lowerBounds;
    this.upperBounds = upperBounds;
  }

  public static TypeDomain createDomain(ReferenceType lowerBound, ReferenceType upperBound) {
    if (lowerBound.isSubtypeOf(upperBound)) {
      Set<ReferenceType> lowerBounds = new HashSet<>();
      lowerBounds.add(lowerBound);
      Set<ReferenceType> upperBounds = new HashSet<>();
      upperBounds.add(upperBound);
      return new IntervalDomain(lowerBounds, upperBounds);
    }
    return EmptyDomain.createDomain();
  }

  public static TypeDomain createDomain(Set<ReferenceType> lowerBounds, ReferenceType upperBound) {
    if (isSubtypeOf(lowerBounds, upperBound)) {
      Set<ReferenceType> upperBounds = new HashSet<>();
      upperBounds.add(upperBound);
      return new IntervalDomain(lowerBounds, upperBounds);
    }
    return EmptyDomain.createDomain();
  }

  public static TypeDomain createDomain(ReferenceType lowerBound, Set<ReferenceType> upperBounds) {
    if (isSubtypeOf(lowerBound, upperBounds)) {
      Set<ReferenceType> lowerBounds = new HashSet<>();
      lowerBounds.add(lowerBound);
      return new IntervalDomain(lowerBounds, upperBounds);
    }
    return EmptyDomain.createDomain();
  }

  public static TypeDomain createDomain(
      Set<ReferenceType> lowerBounds, Set<ReferenceType> upperBounds) {
    if (isSubtypeOf(lowerBounds, upperBounds)) {
      return new IntervalDomain(lowerBounds, upperBounds);
    }
    return EmptyDomain.createDomain();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof IntervalDomain)) {
      return false;
    }
    IntervalDomain other = (IntervalDomain) obj;
    return this.lowerBounds.equals(other.lowerBounds) && this.upperBounds.equals(other.upperBounds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.lowerBounds, this.upperBounds);
  }

  @Override
  public String toString() {
    return "[" + lowerBounds + ", " + upperBounds + "]";
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public TypeDomain restrictDown(ReferenceType upperBound) {
    Set<ReferenceType> meetSet = meet(upperBound, this.upperBounds);
    if (meetSet.equals(this.upperBounds)) {
      return this;
    }
    return createDomain(this.lowerBounds, meetSet);
  }

  @Override
  public TypeDomain restrictDown(TypeDomain upperDomain) {
    if (upperDomain instanceof EmptyDomain) {
      return EmptyDomain.createDomain();
    }
    if (upperDomain instanceof SetDomain) {
      return restrictDown((SetDomain) upperDomain);
    }
    if (upperDomain instanceof IntervalDomain) {
      return restrictDown((IntervalDomain) upperDomain);
    }
    if (upperDomain instanceof DownSumDomain) {
      return restrictDown((DownSumDomain) upperDomain);
    }
    if (upperDomain instanceof UpSumDomain) {
      return restrictDown((UpSumDomain) upperDomain);
    }
    return EmptyDomain.createDomain();
  }

  @Override
  public boolean hasSupertypeOf(ReferenceType type) {
    return !this.restrictUp(type).isEmpty();
  }

  @Override
  public boolean hasSubtypeOf(ReferenceType type) {
    return !this.restrictDown(type).isEmpty();
  }

  private TypeDomain restrictDown(SetDomain upperDomain) {
    return EmptyDomain.createDomain();
  }

  private TypeDomain restrictDown(IntervalDomain upperDomain) {
    Set<ReferenceType> joinSet = join(upperDomain.lowerBounds, this.lowerBounds);
    Set<ReferenceType> meetSet = meet(upperDomain.upperBounds, this.upperBounds);
    return IntervalDomain.createDomain(joinSet, meetSet);
  }

  private TypeDomain restrictDown(DownSumDomain upperDomain) {
    return EmptyDomain.createDomain();
  }

  private TypeDomain restrictDown(UpSumDomain upperDomain) {
    return EmptyDomain.createDomain();
  }

  @Override
  public TypeDomain restrictUp(ReferenceType lowerBound) {
    Set<ReferenceType> joinSet = join(lowerBound, this.lowerBounds);
    if (joinSet.equals(this.lowerBounds)) {
      return this;
    }
    return createDomain(joinSet, this.upperBounds);
  }

  @Override
  public TypeDomain restrictUp(TypeDomain lowerDomain) {
    if (lowerDomain instanceof EmptyDomain) {
      return EmptyDomain.createDomain();
    }
    if (lowerDomain instanceof SetDomain) {
      return restrictUp((SetDomain) lowerDomain);
    }
    if (lowerDomain instanceof IntervalDomain) {
      return restrictUp((IntervalDomain) lowerDomain);
    }
    if (lowerDomain instanceof DownSumDomain) {
      return restrictUp(((DownSumDomain) lowerDomain));
    }
    if (lowerDomain instanceof UpSumDomain) {
      return restrictUp((UpSumDomain) lowerDomain);
    }
    return EmptyDomain.createDomain();
  }

  private TypeDomain restrictUp(SetDomain lowerDomain) {
    return EmptyDomain.createDomain();
  }

  private TypeDomain restrictUp(IntervalDomain lowerDomain) {
    Set<ReferenceType> joinSet = join(lowerDomain.lowerBounds, this.lowerBounds);
    Set<ReferenceType> meetSet = meet(lowerDomain.upperBounds, this.upperBounds);
    return IntervalDomain.createDomain(joinSet, meetSet);
  }

  private TypeDomain restrictUp(DownSumDomain lowerDomain) {
    return EmptyDomain.createDomain();
  }

  private TypeDomain restrictUp(UpSumDomain lowerDomain) {
    return EmptyDomain.createDomain();
  }

  private static Set<ReferenceType> join(ReferenceType type, Set<ReferenceType> bounds) {
    if (isSubtypeOf(type, bounds)) { // type <: all bounds
      return bounds;
    }
    Set<ReferenceType> joinSet = new HashSet<>();
    if (!isSubtypeOf(bounds, type)) { // all bounds <: type
      joinSet.addAll(bounds);
    }
    joinSet.add(type);
    return joinSet;
  }

  private static Set<ReferenceType> join(Set<ReferenceType> types, Set<ReferenceType> bounds) {
    if (isSubtypeOf(types, bounds)) {
      return bounds;
    }
    Set<ReferenceType> joinSet = new HashSet<>();
    if (!isSubtypeOf(bounds, types)) {
      joinSet.addAll(bounds);
    }
    joinSet.addAll(types);
    return joinSet;
  }

  // TODO: factor in that for two distinct class types the meet is null unless one is a subtype of the other
  private static Set<ReferenceType> meet(ReferenceType type, Set<ReferenceType> bounds) {
    if (isSubtypeOf(bounds, type)) { // all bounds <: type
      return bounds;
    }
    Set<ReferenceType> meetSet = new HashSet<>();
    if (!isSubtypeOf(type, bounds)) { // type <: all bounds
      meetSet.addAll(bounds);
    }
    meetSet.add(type);
    return meetSet;
  }

  private static Set<ReferenceType> meet(Set<ReferenceType> types, Set<ReferenceType> bounds) {
    if (isSubtypeOf(bounds, types)) {
      return bounds;
    }
    Set<ReferenceType> meetSet = new HashSet<>();
    if (!isSubtypeOf(types, bounds)) {
      meetSet.addAll(bounds);
    }
    meetSet.addAll(types);
    return meetSet;
  }

  private static boolean isSubtypeOf(ReferenceType type, Set<ReferenceType> bounds) {
    for (ReferenceType boundType : bounds) {
      if (!type.isSubtypeOf(boundType)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isSubtypeOf(Set<ReferenceType> bounds, ReferenceType type) {
    for (ReferenceType boundType : bounds) {
      if (!boundType.isSubtypeOf(type)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isSubtypeOf(Set<ReferenceType> types, Set<ReferenceType> bounds) {
    for (ReferenceType type : types) {
      if (!isSubtypeOf(type, bounds)) {
        return false;
      }
    }
    return true;
  }

  private static boolean equalsBound(ReferenceType type, Set<ReferenceType> bounds) {
    return bounds.size() == 1 && bounds.contains(type);
  }
}
