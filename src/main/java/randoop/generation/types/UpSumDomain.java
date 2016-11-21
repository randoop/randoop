package randoop.generation.types;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import randoop.types.ReferenceType;

/**
 * Represents a type domain that is a direct sum of domains determined by lower bound types.
 * Effectively this is a union of domains where each domain is tagged by a lower bound.
 * This translates to a {@code Map} from the lower bound to the restricted domain.
 */
public class UpSumDomain implements TypeDomain {

  private final Map<ReferenceType, TypeDomain> sumMap;

  private UpSumDomain() {
    sumMap = new LinkedHashMap<>();
  }

  private UpSumDomain(Map<ReferenceType, TypeDomain> sumMap) {
    this.sumMap = sumMap;
  }

  public static TypeDomain createDomain(Map<ReferenceType, TypeDomain> sumMap) {
    if (sumMap.isEmpty()) {
      return EmptyDomain.createDomain();
    }
    for (Map.Entry<ReferenceType, TypeDomain> entry : sumMap.entrySet()) {
      if (entry.getValue().isEmpty()) {
        return EmptyDomain.createDomain();
      }
    }
    return new UpSumDomain(sumMap);
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
