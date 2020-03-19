package randoop.reflection;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import randoop.operation.TypedClassOperation;
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
// not use the omitmethods patterns.
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
   * @param operation the method or constructor
   * @return true if an omit pattern matches the signature of the method or constructor
   */
  public boolean shouldOmit(final TypedClassOperation operation) {
    if (omitPatterns.isEmpty()) {
      return false;
    }

    if (!operation.isConstructorCall() && !operation.isMethodCall()) {
      throw new IllegalArgumentException(
          String.format("operation = %s [%s]", operation, operation.getClass()));
    }

    if (omitPatterns.isEmpty()) {
      return false;
    }

    if (logOmit) {
      Log.logPrintf("shouldOmit(%s)%n", operation);
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
