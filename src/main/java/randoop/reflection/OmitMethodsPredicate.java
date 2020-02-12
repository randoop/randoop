package randoop.reflection;

import java.util.ArrayList;
import java.util.List;
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

  /** Set to true to produce very voluminous debugging regarding omission. */
  private static boolean logOmit = false;

  /** An OmitMethodsPredicate that does no omission. */
  public static final OmitMethodsPredicate NO_OMISSION =
      new OmitMethodsPredicate(new ArrayList<>());

  /** {@code Pattern}s to match operations that should be omitted. Never side-effected. */
  private final List<Pattern> omitPatterns;

  /**
   * Create a new OmitMethodsPredicate.
   *
   * @param omitPatterns a list of regular expressions for method signatures. May be empty.
   */
  public OmitMethodsPredicate(List<Pattern> omitPatterns) {
    this.omitPatterns = new ArrayList<>(omitPatterns);
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
    if (logOmit) {
      Log.logPrintf("shouldOmitExact(%s)%n", operation);
    }

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
      if (logOmit) {
        Log.logPrintf("shouldOmitExact(%s) with regex %s => %s%n", operation, pattern, result);
        Log.logPrintf("Comparing '%s' against pattern '%s' = %b%n", signature, pattern, result);
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
  @SuppressWarnings("ReferenceEquality")
  public boolean shouldOmit(final TypedClassOperation operation) {
    if (logOmit) {
      Log.logPrintf("shouldOmit: testing %s [%s]%n", operation, operation.getClass());
    }

    // Done if there are no patterns
    if (omitPatterns.isEmpty()) {
      return false;
    }

    RawSignature signature = operation.getRawSignature();

    // It's a method.
    // Search the type and its supertypes that have the method.

    for (ClassOrInterfaceType type : operation.getDeclaringType().getAllSupertypesInclusive()) {
      if (logOmit) {
        Log.logPrintf(
            "shouldOmit looking for %s in %s = %s%n",
            signature.getName(), type.getRuntimeClass(), type);
      }

      if (logOmit) {
        Log.logPrintf(
            "%n operation = %s%n operation.isConstructorCall = %s%n "
                + "signature = %s%n signature.getName() = %s%n signature.getClassname() = %s%n"
                + " type = %s [%s]%n"
                + " type.getRuntimeClass() = %s%n"
                + " type.getRuntimeClass().getSimpleName()) = %s%n type.getRuntimeClass().getname()) = %s%n"
                + " type.getRuntimeClass().getTypeName()) = %s%n",
            operation,
            operation.isConstructorCall(),
            signature,
            signature.getName(),
            signature.getClassname(),
            type,
            type.getClass(),
            type.getRuntimeClass(),
            type.getRuntimeClass().getSimpleName(),
            type.getRuntimeClass().getName(),
            type.getRuntimeClass().getTypeName());
      }

      // Try to get the method for type
      boolean exists;
      try {
        type.getRuntimeClass().getMethod(signature.getName(), signature.getParameterTypes());
        exists = true;
      } catch (NoSuchMethodException e) {
        // This is not necessarily an error (yet); it might be a constructor.
        if (logOmit) {
          Log.logPrintf(
              "no method %s in %stype %s%n",
              signature,
              (type == operation.getDeclaringType()) ? "" : "super",
              type.getRuntimeClass().getSimpleName());
        }
        exists = false;
      }
      // Look for a constructor if the method was not found.
      if (!exists && signature.getName().equals(type.getRuntimeClass().getSimpleName())) {
        try {
          type.getRuntimeClass().getConstructor(signature.getParameterTypes());
          exists = true;
        } catch (NoSuchMethodException e) {
          // nothing to do
          if (logOmit) {
            Log.logPrintf(
                "no constructor for %s in %s%n", signature, type.getRuntimeClass().getSimpleName());
          }
        }
      }

      // If type has the method or constructor
      if (exists) {
        // Create the operation and test whether it is matched by an omit pattern
        TypedClassOperation superTypeOperation = operation.getOperationForType(type);
        if (shouldOmitExact(superTypeOperation)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public String toString() {
    return "OmitMethodsPredicate: " + omitPatterns;
  }
}
