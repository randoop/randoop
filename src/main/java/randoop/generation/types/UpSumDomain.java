package randoop.generation.types;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import randoop.types.ReferenceType;

/**
 * Represents a type domain that is a direct sum of domains determined by lower bound types.
 * Effectively this is a union of domains where each domain is tagged by a lower bound. This
 * translates to a {@code Map} from the lower bound to the restricted domain.
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
    // if map is a single entry, project down and return domain
    if (sumMap.size() == 1) {
      Map.Entry<ReferenceType, TypeDomain> entry = sumMap.entrySet().iterator().next();
      return entry.getValue().restrictUp(entry.getKey());
    }
    Iterator<Map.Entry<ReferenceType, TypeDomain>> domainIterator = sumMap.entrySet().iterator();
    if (domainIterator.hasNext()) {
      Map.Entry<ReferenceType, TypeDomain> entry = domainIterator.next();
      while (entry.getValue().isEmpty() && domainIterator.hasNext()) {
        entry = domainIterator.next();
      }
      if (entry.getValue().isEmpty() && !domainIterator.hasNext()) {
        return EmptyDomain.createDomain();
      }
    }
    return new UpSumDomain(sumMap);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof UpSumDomain)) {
      return false;
    }
    UpSumDomain other = (UpSumDomain) obj;
    return this.sumMap.equals(other.sumMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sumMap);
  }

  @Override
  public String toString() {
    return sumMap.toString();
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public TypeDomain restrictDown(ReferenceType upperBound) {
    return null;
  }

  @Override
  public TypeDomain restrictDown(TypeDomain upperDomain) {
    return null;
  }

  @Override
  public TypeDomain restrictUp(ReferenceType lowerBound) {
    Map<ReferenceType, TypeDomain> sumMap = new LinkedHashMap<>();
    for (Map.Entry<ReferenceType, TypeDomain> entry : this.sumMap.entrySet()) {
      sumMap.put(entry.getKey(), entry.getValue().restrictUp(lowerBound));
    }
    return createDomain(sumMap);
  }

  @Override
  public TypeDomain restrictUp(TypeDomain lowerDomain) {
    Map<ReferenceType, TypeDomain> sumMap = new LinkedHashMap<>();
    for (Map.Entry<ReferenceType, TypeDomain> entry : this.sumMap.entrySet()) {
      sumMap.put(entry.getKey(), entry.getValue().restrictUp(lowerDomain));
    }
    return createDomain(sumMap);
  }

  @Override
  public boolean hasSupertypeOf(ReferenceType type) {
    for (Map.Entry<ReferenceType, TypeDomain> entry : this.sumMap.entrySet()) {
      if (entry.getValue().hasSupertypeOf(type)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean hasSubtypeOf(ReferenceType type) {
    return false;
  }
}
