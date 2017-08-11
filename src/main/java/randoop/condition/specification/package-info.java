/**
 * This package contains classes that represent specifications on operations.
 *
 * <p>Each operation corresponds to an {@code java.lang.reflect.AccessibleObject} that is either a
 * {@code Method} or {@code Constructor}. A specification for this object is represented as a {@link
 * randoop.condition.specification.OperationSpecification}. Randoop loads
 * JSON serialization of specifications with the method {@link
 * randoop.condition.SpecificationCollection#create(java.util.List)}.
 *
 * <p>Programs that create specifications can use the classes in this package to generate the
 * serialization using Gson.
 *
 * <p>Details on how specifications are used are given in {@link randoop.condition}.
 */
package randoop.condition.specification;
