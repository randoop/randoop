package randoop.condition;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import randoop.compile.SequenceCompiler;
import randoop.condition.specification.Guard;
import randoop.condition.specification.Identifiers;
import randoop.condition.specification.Property;
import randoop.reflection.RawSignature;

/** Represents the signature of a condition method for a particular {@code AccessibleObject}. */
public class ConditionSignatures {

  /** The name of the condition method. */
  private static final String CONDITION_METHOD_NAME = "test";

  /** The name of dummy variables used by {@link randoop.contract.ObjectContract}. */
  private static final String DUMMY_VARIABLE_NAME = "x";

  /** The map of variable name replacements */
  private final NameReplacementMap replacementMap;

  /** The {@link SequenceCompiler} for compiling condition methods */
  private final SequenceCompiler compiler;

  /** The signature string for a precondition */
  private RawSignature preConditionSignature;

  /** The parameter declaration string for a precondition method */
  private final String preConditionDeclarations;

  /** The signature string for a post-condition */
  private RawSignature postConditionSignature;

  /** The parameter declaration string, with parentheses, for a post-condition method */
  private final String postConditionDeclarations;

  /**
   * Creates a {@link ConditionSignatures} object in the given package with the signature strings
   * and variable replacementMap.
   *
   * @param preConditionSignature the {@link RawSignature} for a precondition method
   * @param preConditionDeclarations the parameter declaration string, with parentheses, for a
   *     precondition method
   * @param postConditionSignature the {@link RawSignature} for a post-condition method
   * @param postConditionDeclarations the parameter declaration string, with parentheses, for a
   *     post-condition method
   * @param replacementMap the map of condition identifiers to dummy variables
   * @param compiler the {@link SequenceCompiler} for creating condition methods
   */
  private ConditionSignatures(
      RawSignature preConditionSignature,
      String preConditionDeclarations,
      RawSignature postConditionSignature,
      String postConditionDeclarations,
      NameReplacementMap replacementMap,
      SequenceCompiler compiler) {
    this.preConditionSignature = preConditionSignature;
    this.preConditionDeclarations = preConditionDeclarations;
    this.postConditionSignature = postConditionSignature;
    this.postConditionDeclarations = postConditionDeclarations;
    this.replacementMap = replacementMap;
    this.compiler = compiler;
  }

  /**
   * Creates an object representing the condition method signature(s) for the given {@code
   * AccessibleObject} using the identifiers from a {@link
   * randoop.condition.specification.OperationSpecification}.
   *
   * @param object the method or constructor as an {@code AccessibleObject}
   * @param identifiers the {@link Identifiers} for for the signature created
   * @param compiler the {@link SequenceCompiler} for creating condition methods
   * @return the {@link ConditionSignatures} object for the method using the identifiers
   */
  public static ConditionSignatures of(
      AccessibleObject object, Identifiers identifiers, SequenceCompiler compiler) {
    if (object instanceof Method) {
      return of((Method) object, identifiers, compiler);
    } else if (object instanceof Constructor) {
      return of((Constructor<?>) object, identifiers, compiler);
    }
    return null;
  }

  /**
   * Creates a {@link ConditionSignatures} object representing the condition method signatures for
   * the pre- and post-conditions on the given {@code java.lang.reflect.Constructor} where the
   * condition expressions use the given identifiers.
   *
   * @param constructor the {@code java.lang.reflect.Constructor}
   * @param identifiers the condition identifiers
   * @param compiler the {@link SequenceCompiler} for creating condition methods
   * @return the {@link ConditionSignatures} for the condition methods on the constructor
   */
  private static ConditionSignatures of(
      Constructor<?> constructor, Identifiers identifiers, SequenceCompiler compiler) {
    Class<?> declaringClass = constructor.getDeclaringClass();
    String packageName = getPackageName(declaringClass.getPackage());
    int shift = 0; //constructor has no receiver, so don't need to shift parameter types
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    RawSignature preconditionSignature =
        getPreconditionSignature(packageName, declaringClass, parameterTypes, shift);
    RawSignature postconditionSignature =
        getPostconditionSignature(
            packageName, declaringClass, parameterTypes, declaringClass, shift);

    List<String> parameterNames = new ArrayList<>();
    parameterNames.addAll(identifiers.getParameterNames());
    String preconditionDeclarations = preconditionSignature.getDeclarationArguments(parameterNames);
    parameterNames.add(identifiers.getReturnName());
    String postconditionDeclarations =
        postconditionSignature.getDeclarationArguments(parameterNames);
    NameReplacementMap replacementMap = createReplacementMap(parameterNames);

    return new ConditionSignatures(
        preconditionSignature,
        preconditionDeclarations,
        postconditionSignature,
        postconditionDeclarations,
        replacementMap,
        compiler);
  }

  /**
   * Creates a {@link ConditionSignatures} object representing the condition method signatures for
   * the pre- and post-conditions on the given {@code java.lang.reflect.Method}, where the condition
   * expressions use the given identifiers.
   *
   * @param method the {@code java.lang.reflect.Method}
   * @param identifiers the condition identifiers
   * @param compiler the {@link SequenceCompiler} for creating condition methods
   * @return the {@link ConditionSignatures} for the condition methods of the method
   */
  private static ConditionSignatures of(
      Method method, Identifiers identifiers, SequenceCompiler compiler) {
    Class<?> declaringClass = method.getDeclaringClass();
    String packageName = getPackageName(declaringClass.getPackage());

    RawSignature preconditionSignature;
    RawSignature postconditionSignature;
    NameReplacementMap replacementMap;
    String preconditionDeclarations;
    String postconditionDeclarations;
    Class<?>[] parameterTypes = method.getParameterTypes();

    int shift = 1; // need receiver, so shift the parameters
    preconditionSignature =
        getPreconditionSignature(packageName, declaringClass, parameterTypes, shift);
    postconditionSignature =
        getPostconditionSignature(
            packageName, declaringClass, parameterTypes, method.getReturnType(), shift);

    List<String> parameterNames = new ArrayList<>();
    parameterNames.add(identifiers.getReceiverName());
    parameterNames.addAll(identifiers.getParameterNames());
    preconditionDeclarations = preconditionSignature.getDeclarationArguments(parameterNames);
    parameterNames.add(identifiers.getReturnName());
    postconditionDeclarations = postconditionSignature.getDeclarationArguments(parameterNames);
    replacementMap = createReplacementMap(parameterNames);

    return new ConditionSignatures(
        preconditionSignature,
        preconditionDeclarations,
        postconditionSignature,
        postconditionDeclarations,
        replacementMap,
        compiler);
  }

  /**
   * Gets the name of the package, and if it begins with {@code "java"} modifies it to begin with
   * {@code "randoop"} instead.
   *
   * @param aPackage the package to get the name of, may be null
   * @return the name of the package, the empty string if there is none, and, if there is, updated
   *     to start with "randoop" if the original begins with "java"
   */
  private static String getPackageName(Package aPackage) {
    String packageName = "";
    if (aPackage != null) {
      packageName = aPackage.getName();
    }
    if (packageName.startsWith("java.")) {
      packageName = "randoop." + packageName;
    }
    return packageName;
  }

  /**
   * Creates the {@link RawSignature} for the precondition method.
   *
   * <p>if {@code shift == 1}, the parameter types for the condition method have the receiver type
   * first, followed by the parameter types. Otherwise, the condition method parameter types are
   * just the parameter types.
   *
   * @param packageName the package name for the condition class
   * @param receiverType the declaring class of the method or constructor, used as receiver type if
   *     {@code shift == 1}
   * @param parameterTypes the parameter types for the original method
   * @param shift starting position of {@code parameterTypes} in condition method parameter list,
   *     either 0 or 1
   * @return the constructed pre-condition method signature
   */
  private static RawSignature getPreconditionSignature(
      String packageName, Class<?> receiverType, Class<?>[] parameterTypes, int shift) {
    assert shift == 0 || shift == 1;
    Class<?>[] conditionParameterTypes = new Class<?>[parameterTypes.length + shift];
    if (shift == 1) {
      conditionParameterTypes[0] = receiverType;
    }
    System.arraycopy(parameterTypes, 0, conditionParameterTypes, shift, parameterTypes.length);
    return new RawSignature(
        packageName, "RandoopPreConditionClass", CONDITION_METHOD_NAME, conditionParameterTypes);
  }

  /**
   * Creates the {@link RawSignature} for the post-condition method.
   *
   * <p>if {@code shift == 1}, the parameter types for the condition method have the receiver type
   * first, followed by the parameter types. Otherwise, the condition method parameter types are
   * just the parameter types.
   *
   * @param packageName the package name for the condition class
   * @param receiverType the declaring class of the method or constructor, used as receiver type if
   *     {@code shift == 1}
   * @param parameterTypes the parameter types for the original method or constructor
   * @param returnType the return type for the method, or the declaring class for a constructor
   * @param shift starting position of {@code parameterTypes} in condition method parameter list,
   *     either 0 or 1
   * @return the constructed post-condition method signature
   */
  private static RawSignature getPostconditionSignature(
      String packageName,
      Class<?> receiverType,
      Class<?>[] parameterTypes,
      Class<?> returnType,
      int shift) {
    assert shift == 0 || shift == 1;
    Class<?>[] conditionParameterTypes = new Class<?>[parameterTypes.length + shift + 1];
    if (shift == 1) {
      conditionParameterTypes[0] = receiverType;
    }
    conditionParameterTypes[conditionParameterTypes.length - 1] = returnType;
    System.arraycopy(parameterTypes, 0, conditionParameterTypes, shift, parameterTypes.length);
    return new RawSignature(
        packageName, "RandoopPostConditionClass", CONDITION_METHOD_NAME, conditionParameterTypes);
  }

  /**
   * Creates the replacement map from the given parameter names to numbered dummy variable names as
   * used in {@link randoop.contract.ObjectContract}.
   *
   * @param parameterNames the parameter names of the condition methods
   * @return the map from the parameter names to dummy variables
   */
  private static NameReplacementMap createReplacementMap(List<String> parameterNames) {
    int count = 0;
    NameReplacementMap replacementMap = new NameReplacementMap();
    for (String parameterName : parameterNames) {
      replacementMap.put(parameterName, DUMMY_VARIABLE_NAME + count++);
    }
    return replacementMap;
  }

  /**
   * Creates a {@link Condition} object for the given {@link Guard} using the pre-condition
   * signature of this {@link ConditionSignatures}.
   *
   * @param guard the {@link Guard} to be converted
   * @return the {@link Condition} object for {@code guard}
   */
  public Condition create(Guard guard) {
    Method conditionMethod =
        ConditionMethodCreator.create(
            preConditionSignature, preConditionDeclarations, guard.getConditionSource(), compiler);
    String comment = guard.getDescription();
    String conditionText = replacementMap.replaceNames(guard.getConditionSource());
    return new Condition(conditionMethod, comment, conditionText);
  }

  /**
   * Creates a {@link PostCondition} object for the given {@link Property} using the post-condition
   * signature of this {@link ConditionSignatures}.
   *
   * @param property the {@link Property} to be converted
   * @return the {@link PostCondition} object for {@code property}
   */
  public PostCondition create(Property property) {
    Method conditionMethod =
        ConditionMethodCreator.create(
            postConditionSignature,
            postConditionDeclarations,
            property.getConditionSource(),
            compiler);
    String comment = property.getDescription();
    String conditionText = replacementMap.replaceNames(property.getConditionSource());
    return new PostCondition(conditionMethod, comment, conditionText);
  }

  /**
   * Return the pre-condition method parameter declaration string. Includes parentheses.
   *
   * <p>Only used for testing.
   *
   * @return the pre-condition method parameter declaration string.
   */
  String getPreConditionDeclarations() {
    return preConditionDeclarations;
  }

  /**
   * Return the post-condition method parameter declaration string. Includes parentheses.
   *
   * <p>Only used for testing.
   *
   * @return the post-condition method parameter declaration string.
   */
  String getPostConditionDeclarations() {
    return postConditionDeclarations;
  }

  /**
   * Returns the post-condition method signature from this object. This signature includes the
   * parameters of the pre-condition signature, plus the return type.
   *
   * <p>Used for testing.
   *
   * @return the post-condition signature for this object
   */
  RawSignature getPostConditionSignature() {
    return postConditionSignature;
  }

  /**
   * Returns the replacement map for the identifiers in the condition to the dummy variables
   * expected in contract assertions
   *
   * @return the replacement map for the identifiers in the condition
   */
  NameReplacementMap getReplacementMap() {
    return replacementMap;
  }
}
