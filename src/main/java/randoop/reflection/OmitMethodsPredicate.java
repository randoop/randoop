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
 * Tests whether an operation matches a user-specified pattern, indicating that the operation should
 * be omitted from the operation set.
 *
 * <p>An operation matches a pattern if the pattern matches the {@link RawSignature} of the
 * operation or the inherited operation in a superclass. This class provides methods that (1) test
 * the raw signature of an operation, and (2) test the raw signature of an operation and that of the
 * same operation in superclasses.
 */
public class OmitMethodsPredicate {

  /** An OmitMethodsPredicate that does no omission. */
  public static OmitMethodsPredicate NO_OMISSION = new OmitMethodsPredicate(null);

  /** {@code Pattern}s that match operations that should be omitted. */
  private final List<Pattern> omitPatterns;

  /**
   * If {@code omitPatterns} is null, treat it as the empty list.
   *
   * @param omitPatterns a list of regular expressions for method signatures, or null
   */
  public OmitMethodsPredicate(List<Pattern> omitPatterns) {
    if (omitPatterns == null) {
      this.omitPatterns = new ArrayList<>();
    } else {
      this.omitPatterns = new ArrayList<>(omitPatterns);
    }
  }

  /**
   * Returns true if the operation is a constructor or method call and some omit pattern matches the
   * {@link RawSignature} of the operation.
   *
   * @param operation the operation to match against the omit patterns of this predicate
   * @return true if the signature matches an omit pattern, and false otherwise
   */
  boolean shouldOmit(TypedClassOperation operation) {
    // Done if there are no patterns
    if (omitPatterns.isEmpty()) {
      return false;
    }
    // Only match constructors or methods
    if (!operation.isConstructorCall() && !operation.isMethodCall()) {
      return false;
    }

    String signature = operation.getRawSignature().toString();

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
   * declaring class of the operation.
   *
   * <p>It is necessary to search all types from {@code classType} up to {@code
   * operation.getDeclaringType()}, and also to search for superclasses and interfaces of the
   * declaring class that have the method.
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

    /*
     * Search classType and its supertypes that have the method.
     */
    Set<ClassOrInterfaceType> visited = new HashSet<>();
    Queue<ClassOrInterfaceType> typeQueue = new LinkedList<>();
    typeQueue.add(classType);
    while (!typeQueue.isEmpty()) {
      ClassOrInterfaceType type = typeQueue.remove();
      if (!visited.add(type)) {
        continue; // visited already contained type
      }

      Method superclassMethod =
          getMethod(method.getName(), method.getParameterTypes(), type.getRuntimeClass());
      if (superclassMethod != null) {
        TypedClassOperation superTypeOperation = operation.getOperationForType(type);
        if (shouldOmit(superTypeOperation)) {
          return true;
        }
        typeQueue.addAll(getImmediateSupertypes(type));
      }
    }

    return false;
  }

  /**
   * Returns the set of immediate supertypes for the given class including the superclass and
   * interfaces of the type restricted to those that are a subtype of the given upper bound type.
   *
   * @param type the type for which supertypes are collected
   * @param upperBoundType the upper bound type
   * @return the set of immediate supertypes that are subtypes of {@code upperBoundType}
   */
  private static Set<ClassOrInterfaceType> getImmediateSupertypesBelow(
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
   * Returns the set of immediate supertypes for the given class obtained by collecting the
   * superclass and interfaces of the type. (Rather than the transitive set of all supertypes
   * returned by {@link ClassOrInterfaceType#getSuperTypes()}.
   *
   * @param type the type for which supertypes are collected
   * @return the set of immediate supertypes.
   */
  private static Set<ClassOrInterfaceType> getImmediateSupertypes(ClassOrInterfaceType type) {
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
