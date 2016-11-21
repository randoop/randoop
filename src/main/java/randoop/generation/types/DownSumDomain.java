package randoop.generation.types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import randoop.types.ReferenceType;

/**
 * Represents a type domain that is a direct sum of domains determined by upper bound types.
 * Effectively this is a union of domains where each domain is tagged by an upper bound.
 * This translates to a {@code Map} from the upper bound to the restricted domain.
 */
public class DownSumDomain implements TypeDomain {

  private final Map<ReferenceType, TypeDomain> sumMap;

  private DownSumDomain() {
    this(new HashMap<ReferenceType, TypeDomain>());
  }

  private DownSumDomain(Map<ReferenceType, TypeDomain> sumMap) {
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
    return new DownSumDomain(sumMap);
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
