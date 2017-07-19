package randoop.reflection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;
import randoop.operation.TypedClassOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.util.Log;

/**
 * Predicate to test whether operations match one of a set of patterns indicating that the operation
 * should be omitted from the operation set.
 *
 * <p>An operation matches a pattern if the pattern matches the {@link
 * TypedClassOperation#getRawSignature()} of the operation or the inherited operation in a
 * superclass. This class provides methods that (1) test the raw signature of an operation, and (2)
 * test the raw signature of an operation and that of the same operation in superclasses.
 */
public class OmitMethodsPredicate {

  /** The list of {@code Pattern} objects to omit matching operations */
  private final List<Pattern> omitPatterns;

  public OmitMethodsPredicate() {
    this.omitPatterns = new ArrayList<>();
  }

  public OmitMethodsPredicate(List<Pattern> omitPatterns) {
    this.omitPatterns = omitPatterns;
  }

  /**
   * Indicates whether some omit pattern matches the {@link TypedClassOperation#getRawSignature()}
   * for the given operation.
   *
   * @param operation the constructor or method call to match against the omit patterns of this
   *     extractor
   * @return true if the signature matches an omit pattern, and false otherwise.
   */
  boolean shouldOmit(TypedClassOperation operation) {
    String signature = operation.getRawSignature();
    for (Pattern pattern : omitPatterns) {
      boolean result = pattern.matcher(signature).find();
      if (Log.isLoggingOn()) {
        Log.logLine(
            String.format(
                "Comparing '%s' against pattern '%s' = %b%n", signature, pattern, result));
      }
      if (result) {
        return true;
      }
    }
    return false;
  }

  /**
   * Indicates whether an omit pattern matches the raw signature of the method in either the
   * declaring class of the method or a supertype.
   *
   * <p>Needs to search all supertypes of {@code classType} that have a declared member
   * corresponding to the method. The type {@code classType} is a subtype of or equal to the
   * declaring class of the operation. If non-equal, it is necessary to search all types in the
   * interval between {@code classType} and {@code operation.getDeclaringType()}. Since the method
   * may be an override in the declaring class, it is also necessary to search for superclasses of
   * the declaring class that have the method.
   *
   * @param classType the type to which the method belongs
   * @param operation the operation for the method
   * @param method the reflection object for the method
   * @return true if the signature of the method in the current class or a super class matches an
   *     omit pattern, false otherwise
   */
  boolean shouldOmit(
      ClassOrInterfaceType classType, final TypedClassOperation operation, final Method method) {
    // done if there are no patterns
    if (omitPatterns.isEmpty()) {
      return false;
    }

    // check whether the operation matches an omit pattern directly
    if (shouldOmit(operation)) {
      return true;
    }

    final ClassOrInterfaceType declaringType = operation.getDeclaringType();

    // XXX these two searches are similar, but generalization is awkward.

    /*
     * Search in the interval from classType to declaringType.  These types all have the method.
     */
    Set<ClassOrInterfaceType> visited = new HashSet<>();
    Queue<ClassOrInterfaceType> typeQueue = new LinkedList<>();
    typeQueue.add(classType);
    while (!typeQueue.isEmpty()) {
      ClassOrInterfaceType type = typeQueue.remove();
      if (visited.contains(type)) {
        continue;
      }

      // all subtypes of declaringType have the method
      TypedClassOperation superTypeOperation = operation.getOperationForType(type);
      if (shouldOmit(superTypeOperation)) {
        return true;
      }

      typeQueue.addAll(getBoundSupertypes(type, declaringType));
      visited.add(type);
    }

    /*
     * Search supertypes of declaringType that have the method.
     */
    visited = new HashSet<>();
    typeQueue = new LinkedList<>();
    typeQueue.add(declaringType);
    while (!typeQueue.isEmpty()) {
      ClassOrInterfaceType type = typeQueue.remove();
      if (visited.contains(type)) {
        continue;
      }

      Method superclassMethod =
          getMethod(method.getName(), method.getParameterTypes(), type.getRuntimeClass());
      if (superclassMethod != null) {
        TypedClassOperation superTypeOperation = operation.getOperationForType(type);
        if (shouldOmit(superTypeOperation)) {
          return true;
        }
        typeQueue.addAll(getSupertypes(type));
      }

      visited.add(type);
    }

    return false;
  }

  /**
   * Returns the set of supertypes for the given class including the superclass and interfaces of
   * the type restricted to those that are a subtype of the upper bound type.
   *
   * @param type the type for which supertypes are collected
   * @param upperBoundType the upper bound type
   * @return the set of immediate supertypes that are subtypes of {@code upperBoundType}
   */
  private static Set<ClassOrInterfaceType> getBoundSupertypes(
      ClassOrInterfaceType type, ClassOrInterfaceType upperBoundType) {
    Set<ClassOrInterfaceType> boundedSet = new HashSet<>();

    if (type.equals(upperBoundType) || !type.isSubtypeOf(upperBoundType)) {
      return boundedSet;
    }

    ClassOrInterfaceType supertype = type.getSuperclass();
    if (supertype.isSubtypeOf(upperBoundType)) {
      boundedSet.add(supertype);
    }
    for (ClassOrInterfaceType interfaceType : type.getInterfaces()) {
      if (interfaceType.isSubtypeOf(upperBoundType)) {
        boundedSet.add(interfaceType);
      }
    }
    return boundedSet;
  }

  /**
   * Returns the set of supertypes for the given class obtained by collecting the superclass and
   * interfaces of the type. (Rather than all supertypes returned by {@link
   * ClassOrInterfaceType#getSuperTypes()}.
   *
   * @param type the type for which supertypes are collected
   * @return the set of immediate supertypes.
   */
  private static Set<ClassOrInterfaceType> getSupertypes(ClassOrInterfaceType type) {
    Set<ClassOrInterfaceType> supertypes = new HashSet<>();

    if (type.equals(JavaTypes.OBJECT_TYPE)) {
      return supertypes;
    }

    supertypes.add(type.getSuperclass());
    supertypes.addAll(type.getInterfaces());

    return supertypes;
  }

  /**
   * Returns the {@code java.lang.reflect.Method} with the name and arguments in the given class.
   *
   * @param methodName the method name
   * @param parameterTypes the parameter types of the method
   * @param typeClass the {@code Class} of the method
   * @return the {@code Method} object with the name and parameter types from {@code typeClass},
   *     null if there is no such method
   */
  private static Method getMethod(
      String methodName, Class<?>[] parameterTypes, Class<?> typeClass) {
    try {
      return typeClass.getMethod(methodName, parameterTypes);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }
}
