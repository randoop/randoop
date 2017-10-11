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
import randoop.util.Log;

/**
 * Tests whether an operation is matched by a user-specified pattern, indicating that the operation
 * should be omitted from the operation set.
 *
 * <p>A pattern matches an operation representing a constructor, if the pattern matches the {@link
 * RawSignature} of the operation. A pattern matches an operation representing a method, if the
 * pattern matches the {@link RawSignature} of an operation for which the declaring class is a
 * supertype of {@link TypedClassOperation#getDeclaringType()} of the operation. * A constructor may
 * If the operation is a method, a pattern matches the operation if This class provides methods that
 * (1) test the raw signature of an operation, and (2) test the raw signature of an operation and,
 * for an inherited method, that of the same operation in superclasses.
 */
public class OmitMethodsPredicate {

  /** An OmitMethodsPredicate that does no omission. */
  public static final OmitMethodsPredicate NO_OMISSION = new OmitMethodsPredicate(null);

  /** {@code Pattern}s to match operations that should be omitted. */
  private final List<Pattern> omitPatterns;

  /**
   * @param omitPatterns a list of regular expressions for method signatures. Null or the empty
   *     least mean to do no omissions.
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
   * <p>This method does not check for matches of the operation in superclasses.
   *
   * @param operation the operation to be matched against the omit patterns of this predicate
   * @return true if the signature matches an omit pattern, and false otherwise
   */
  private boolean shouldOmitExact(TypedClassOperation operation) {
    // Nothing to do if there are no patterns.
    if (omitPatterns.isEmpty()) {
      return false;
    }
    // Only match constructors or methods.
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
   * @param operation the operation for the method
   * @return true if the signature of the method in the current class or a superclass is matched by
   *     an omit pattern, false otherwise
   */
  boolean shouldOmit(final TypedClassOperation operation) {
    // Done if there are no patterns
    if (omitPatterns.isEmpty()) {
      return false;
    }

    RawSignature signature = operation.getRawSignature();

    /*
     * Search the type and its supertypes that have the method.
     */
    Set<ClassOrInterfaceType> visited = new HashSet<>();
    Queue<ClassOrInterfaceType> typeQueue = new LinkedList<>();
    typeQueue.add(operation.getDeclaringType());
    while (!typeQueue.isEmpty()) {
      ClassOrInterfaceType type = typeQueue.remove();
      if (!visited.add(type)) {
        continue; // visited already contains type
      }

      // Try to get the method for type
      Method method;
      try {
        method =
            type.getRuntimeClass().getMethod(signature.getName(), signature.getParameterTypes());
      } catch (NoSuchMethodException e) {
        method = null;
      }

      // If type has the method
      if (method != null) {
        // Create the operation and test whether it is matched by an omit pattern
        TypedClassOperation superTypeOperation = operation.getOperationForType(type);
        if (shouldOmitExact(superTypeOperation)) {
          return true;
        }
        // Otherwise, search supertypes
        typeQueue.addAll(type.getImmediateSupertypes());
      }
    }

    return false;
  }

  @Override
  public String toString() {
    return "OmitMethodsPredicate: " + omitPatterns;
  }
}
