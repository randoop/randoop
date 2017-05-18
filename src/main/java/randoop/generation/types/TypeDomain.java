package randoop.generation.types;

import randoop.types.ReferenceType;

/**
 * The interface for type domains used in propagation of type constraints. A type domain consists of
 * a set of ground (aka, not generic) reference types. Some domains are determined by type bounds,
 * and others are simply sets.
 */
public interface TypeDomain {

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
   * Check that the given {@link ReferenceType} has a supertype in this domain.
   *
   * @param type the type to check for supertypes
   * @return true if this domain has a supertype of the given type, false otherwise
   */
  boolean hasSupertypeOf(ReferenceType type);

  /**
   * Check that the given {@link ReferenceType} has a subtype in this domain.
   *
   * @param type the type to check for subtypes
   * @return true if this domain has a subtype of the given type, false otherwise
   */
  boolean hasSubtypeOf(ReferenceType type);

  /**
   * Returns the domain formed by restricting this domain to elements that have a lower bound in the
   * given domain.
   *
   * @param lowerDomain the domain of lower bounds for restriction
   * @return the subdomain consisting of types in this domain with lower bounds in the given domain
   */
  TypeDomain restrictUp(TypeDomain lowerDomain);
}
