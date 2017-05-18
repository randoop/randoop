package randoop.generation.types;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import randoop.types.ReferenceType;

/** Represents a type domain that is simply a set of types. */
public class SetDomain implements TypeDomain, Iterable<ReferenceType> {

  private final Set<ReferenceType> types;

  private SetDomain() {
    this(new HashSet<ReferenceType>());
  }

  private SetDomain(Set<ReferenceType> types) {
    this.types = types;
  }

  public static TypeDomain createDomain(Set<ReferenceType> types) {
    if (types.isEmpty()) {
      return EmptyDomain.createDomain();
    }
    return new SetDomain(types);
  }

  public static TypeDomain createDomain(ReferenceType singleton) {
    Set<ReferenceType> types = new HashSet<>();
    types.add(singleton);
    return new SetDomain(types);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SetDomain)) {
      return false;
    }
    SetDomain other = (SetDomain) obj;
    return this.types.equals(other.types);
  }

  @Override
  public int hashCode() {
    return Objects.hash(types);
  }

  @Override
  public String toString() {
    return types.toString();
  }

  public Set<ReferenceType> getTypes() {
    return types;
  }

  public int size() {
    return types.size();
  }

  @Override
  public boolean isEmpty() {
    return types.isEmpty();
  }

  @Override
  public TypeDomain restrictDown(ReferenceType upperBound) {
    boolean isSubset = false;
    Set<ReferenceType> downSet = new HashSet<>();
    for (ReferenceType type : types) {
      if (type.isSubtypeOf(upperBound)) {
        downSet.add(type);
      } else {
        isSubset = true;
      }
    }
    if (downSet.isEmpty()) {
      return EmptyDomain.createDomain();
    }
    if (isSubset) {
      return createDomain(downSet);
    }
    return this;
  }

  @Override
  public TypeDomain restrictDown(TypeDomain upperDomain) {
    if (upperDomain instanceof SetDomain) {
      return restrictDown((SetDomain) upperDomain);
    }
    return EmptyDomain.createDomain();
  }

  private TypeDomain restrictDown(SetDomain upperDomain) {
    boolean isSubset = false;
    Set<ReferenceType> downSet = new HashSet<>();
    for (ReferenceType type : this.types) {
      if (upperDomain.hasSupertypeOf(type)) {
        downSet.add(type);
      } else {
        isSubset = true;
      }
    }
    if (downSet.isEmpty()) {
      return EmptyDomain.createDomain();
    }
    if (isSubset) {
      return createDomain(downSet);
    }
    return this;
  }

  @Override
  public TypeDomain restrictUp(ReferenceType lowerBound) {
    boolean isSubset = false;
    Set<ReferenceType> upSet = new HashSet<>();
    for (ReferenceType type : types) {
      if (lowerBound.isSubtypeOf(type)) {
        upSet.add(type);
      } else {
        isSubset = true;
      }
    }
    if (upSet.isEmpty()) {
      return EmptyDomain.createDomain();
    }
    if (isSubset) {
      return createDomain(upSet);
    }
    return this;
  }

  @Override
  public TypeDomain restrictUp(TypeDomain lowerDomain) {
    boolean isSubset = false;
    Set<ReferenceType> upSet = new HashSet<>();
    for (ReferenceType type : types) {
      if (lowerDomain.hasSubtypeOf(type)) {
        upSet.add(type);
      } else {
        isSubset = true;
      }
    }
    if (upSet.isEmpty()) {
      return EmptyDomain.createDomain();
    }
    if (isSubset) {
      return createDomain(upSet);
    }
    return this;
  }

  @Override
  public boolean hasSupertypeOf(ReferenceType type) {
    for (ReferenceType otherType : this.types) {
      if (type.isSubtypeOf(otherType)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean hasSubtypeOf(ReferenceType type) {
    for (ReferenceType otherType : this.types) {
      if (otherType.isSubtypeOf(type)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Iterator<ReferenceType> iterator() {
    return types.iterator();
  }
}
