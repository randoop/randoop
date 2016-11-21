package randoop.generation.types;

import java.util.Iterator;

import randoop.types.ReferenceType;

/**
 * Created by bjkeller on 11/18/16.
 */
public class EmptyDomain implements TypeDomain {

  private EmptyDomain() {}

  private static EmptyDomain domain = new EmptyDomain();

  public static EmptyDomain createDomain() {
    return domain;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public TypeDomain restrictDown(ReferenceType upperBound) {
    return this;
  }

  @Override
  public TypeDomain restrictUp(ReferenceType lowerBound) {
    return this;
  }

  @Override
  public TypeDomain restrictDown(TypeDomain upperDomain) {
    return this;
  }

  @Override
  public TypeDomain restrictUp(TypeDomain lowerDomain) {
    return this;
  }

  @Override
  public Iterator<ReferenceType> iterator() {
    return null;
  }
}
