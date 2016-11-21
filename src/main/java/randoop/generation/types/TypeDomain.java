package randoop.generation.types;

import java.util.Set;

import randoop.types.ParameterBound;
import randoop.types.ReferenceType;
import randoop.types.Type;

/**
 * The interface for type domains used in propagation of type constraints.
 * A type domain consists of a set of ground (aka, not generic) reference types.
 * Some domains are determined by type bounds, and others are simply sets.
 */
public interface TypeDomain extends Iterable<ReferenceType> {

  /**
   * Indicates whether this type domain is empty not.
   *
   * @return true if this type domain is empty; false, otherwise
   */
  boolean isEmpty();

  TypeDomain restrictDown(ReferenceType upperBound);

  TypeDomain restrictUp(ReferenceType lowerBound);

  /**
   * Returns the domain formed by restricting this domain to elements that have an upper bound in
   * the given domain.
   *
   * @param upperDomain the domain of upper bounds for restriction
   * @return the subdomain consisting of types in this domain with upper bounds in the given domain
   */
  TypeDomain restrictDown(TypeDomain upperDomain);

  /**
   * Returns the domain formed by restricting this domain to elements that have a lower bound in the
   * given domain.
   *
   * @param lowerDomain  the domain of lower bounds for restriction
   * @return the subdomain consisting of types in this domain with lower bounds in the given domain
   */
  TypeDomain restrictUp(TypeDomain lowerDomain);
}
