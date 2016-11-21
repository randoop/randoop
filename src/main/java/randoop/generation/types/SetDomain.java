package randoop.generation.types;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import randoop.types.ReferenceType;

/**
 * Represents a type domain that is simply a set of types.
 */
public class SetDomain implements TypeDomain {

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

  public Set<ReferenceType> getTypes() {
    return null;
  }

  public int size() {
    return 0;
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
    return new SetDomain();
  }

  @Override
  public TypeDomain restrictUp(TypeDomain lowerDomain) {
    return new SetDomain();
  }

  @Override
  public Iterator<ReferenceType> iterator() {
    return null;
  }
}
