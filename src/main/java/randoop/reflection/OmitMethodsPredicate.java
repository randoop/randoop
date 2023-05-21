package randoop.reflection;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import randoop.operation.TypedClassOperation;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Log;

// If a given instance method implementation m is omitted, then all overridden implementations are
// also omitted, so that Randoop doesn't call any method that might dispatch to m at run time.  For
// example, if there is an omit pattern my.package.MyClass.toString method, then Object.toString
// will also be omitted (because otherwise a variable myObject might hold a MyClass, and a call
// myObject.toString() might dispatch to my.package.MyClass.toString).
//
// There is not currently a way to omit just one implementation and not all its overrdden
// implementations.
//
// There is not currently a way to omit a given instance method implementation m, plus also all
// overriding implementations.  That means that if you omit MyClass.m(), Randoop will not output
//   MyClass x = ...
//   x.m()
// but it might output
//   MySubclass x = ...
//   x.m()
//
// The way this code works is a bit gross.  A better implementation would work in two stages.
// 1. Find the method.  It might be defined in this class or inherited.  Throw an error if it cannot
// be found.
// 2. If it is an instance method, find all methods that it overrides and omit them too.  This does
// not use the omit_methods patterns.
//
// Step 1 can be done in this class.
// Step 2 is more naturally done in the client of this class, which can iterate through the methods
// that were omitted and the methods that remain in the model.

/**
 * Tests whether the {@link RawSignature} of an operation is matched by an omit. If so, the
 * operation should be omitted from the operation set.
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

  /** Set to true to produce voluminous debugging regarding omission. */
  private static boolean logOmit = false;

  /** An OmitMethodsPredicate that does no omission. */
  public static final OmitMethodsPredicate NO_OMISSION =
      new OmitMethodsPredicate(new ArrayList<>(0));

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
   * Indicates whether an omit pattern matches the raw signature of the method, either in the
   * declaring class of the method or in a supertype.
   *
   * @param operation the operation for the method or constructor
   * @return true if an omit pattern matches the signature of the method or constructor in the
   *     current class (or, for a method, a superclass that defines the method)
   */
  public boolean shouldOmit(final TypedClassOperation operation) {
    if (logOmit) {
      Log.logPrintf("shouldOmit: testing %s [%s]%n", operation, operation.getClass());
    }

    if (omitPatterns.isEmpty()) {
      return false;
    }

    if (operation.isConstructorCall()) {
      return shouldOmitConstructor(operation);
    }

    if (operation.isMethodCall()) {
      return shouldOmitMethod(operation);
    }

    return false;
  }

  /**
   * Indicates whether an omit pattern matches the raw signature of the constructor.
   *
   * @param operation the operation for the method
   * @return true if the signature of the constructor is matched by an omit pattern, false otherwise
   */
  // * @throws NoSuchMethodException if Randoop can't find the operation (this is a bug in Randoop)
  private boolean shouldOmitConstructor(TypedClassOperation operation) {
    return shouldOmitExact(operation);
  }

  /**
   * Indicates whether an omit pattern matches the raw signature of the method in either the
   * declaring class of the method or a supertype.
   *
   * @param operation the operation for the method
   * @return true if the signature of the method in the current class or a superclass is matched by
   *     an omit pattern, false otherwise
   */
  // * @throws NoSuchMethodException if Randoop can't find the operation (this is a bug in Randoop)
  @SuppressWarnings("ReferenceEquality")
  private boolean shouldOmitMethod(TypedClassOperation operation) {
    if (logOmit) {
      Log.logPrintf("%nshouldOmitMethod(%s)%n", operation);
    }

    RawSignature signature = operation.getRawSignature();

    // Search the type and its supertypes that have the method.

    for (ClassOrInterfaceType type : operation.getDeclaringType().getAllSupertypesInclusive()) {
      if (logOmit) {
        Log.logPrintf("shouldOmit looking in %s for %s%n", type, signature.getName());
      }

      if (logOmit) {
        Log.logPrintf(
            " operation = %s%n"
                + " signature = %s%n"
                + " signature.getName() = %s%n"
                + " signature.getClassname() = %s%n"
                + " type = %s [%s]%n"
                + " type.getRuntimeClass() = %s%n"
                + " type.getRuntimeClass().getSimpleName()) = %s%n"
                + " type.getRuntimeClass().getname()) = %s%n"
                + " type.getRuntimeClass().getTypeName()) = %s%n",
            operation,
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

  /**
   * Returns true if the operation is a constructor or method call and some omit pattern matches the
   * {@link RawSignature} of the operation, in the operation's class.
   *
   * <p>This method does not check for matches of overridden definitions of the operation in
   * superclasses.
   *
   * @param operation the operation to be matched against the omit patterns of this predicate
   * @return true if the signature matches an omit pattern, and false otherwise
   */
  // TODO: Choose a better name for this helper method, that reflects its semantics.
  private boolean shouldOmitExact(TypedClassOperation operation) {
    if (logOmit) {
      Log.logPrintf("shouldOmitExact(%s)%n", operation);
    }

    if (!operation.isConstructorCall() && !operation.isMethodCall()) {
      throw new IllegalArgumentException(
          String.format("operation = %s [%s]", operation, operation.getClass()));
    }

    if (omitPatterns.isEmpty()) {
      return false;
    }

    String signature = operation.getRawSignature().toString();

    for (Pattern pattern : omitPatterns) {
      boolean result = pattern.matcher(signature).find();
      if (logOmit) {
        Log.logPrintf(
            "shouldOmitExact(%s): \"%s\".matches(%s) => %s%n",
            operation, pattern, signature, result);
      }
      if (result) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "OmitMethodsPredicate: " + omitPatterns;
  }
}
