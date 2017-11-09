package randoop.condition;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import randoop.compile.SequenceCompiler;
import randoop.condition.specification.Guard;
import randoop.condition.specification.Identifiers;
import randoop.condition.specification.OperationSpecification;
import randoop.condition.specification.Postcondition;
import randoop.condition.specification.Precondition;
import randoop.condition.specification.Property;
import randoop.condition.specification.ThrowsCondition;
import randoop.main.GenInputsAbstract;
import randoop.reflection.RawSignature;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Log;

/** Translates an {@link OperationSpecification} object to an {@link OperationConditions} object. */
public class SpecificationTranslator {

  /** The name of dummy variables used by {@link randoop.contract.ObjectContract}. */
  private static final String DUMMY_VARIABLE_NAME = "x";

  /** The {@link RawSignature} for a guard expression method. */
  private RawSignature guardExpressionSignature;

  /** The parameter declaration string, with parentheses, for a guard expression method. */
  private final String guardExpressionDeclaration;

  /** The {@link RawSignature} for a property expression method. */
  private RawSignature propertyExpressionSignature;

  /** The parameter declaration string, with parentheses, for a property expression method. */
  private final String propertyExpressionDeclarations;

  /** The map of expression identifiers to dummy variables. */
  private final NameReplacementMap replacementMap;

  /** The {@link SequenceCompiler} for compiling expression methods. */
  private final SequenceCompiler compiler;

  /**
   * Creates a {@link SpecificationTranslator} object in the given package with the signature
   * strings and variable replacementMap.
   *
   * @param guardExpressionSignature the {@link RawSignature} for a guard expression method
   * @param guardExpressionDeclaration the parameter declaration string, with parentheses, for a
   *     guard expression method
   * @param propertyExpressionSignature the {@link RawSignature} for a property expression method
   * @param propertyExpressionDeclaration the parameter declaration string, with parentheses, for a
   *     property expression method
   * @param replacementMap the map of expression identifiers to dummy variables
   * @param compiler the {@link SequenceCompiler} for creating expression methods
   */
  private SpecificationTranslator(
      RawSignature guardExpressionSignature,
      String guardExpressionDeclaration,
      RawSignature propertyExpressionSignature,
      String propertyExpressionDeclaration,
      NameReplacementMap replacementMap,
      SequenceCompiler compiler) {
    this.guardExpressionSignature = guardExpressionSignature;
    this.guardExpressionDeclaration = guardExpressionDeclaration;
    this.propertyExpressionSignature = propertyExpressionSignature;
    this.propertyExpressionDeclarations = propertyExpressionDeclaration;
    this.replacementMap = replacementMap;
    this.compiler = compiler;
  }

  /**
   * Creates a {@link SpecificationTranslator} object to translate the {@link
   * OperationSpecification} of {@code executable}.
   *
   * @param executable the {@code java.lang.reflect.AccessibleObject} for the operation with {@link
   *     OperationSpecification} to translate
   * @param identifiers the {@link Identifiers} from the specification to be translated
   * @param compiler the sequence compiler to use to create expression methods
   * @return the translator object to convert the specifications for {@code executable}
   */
  static SpecificationTranslator createTranslator(
      AccessibleObject executable, Identifiers identifiers, SequenceCompiler compiler) {

    // Get expression method signatures.
    RawSignature guardExpressionSignature = getExpressionSignature(executable, false);
    RawSignature propertyExpressionSignature = getExpressionSignature(executable, true);

    List<String> parameterNames = new ArrayList<>();

    // Get expression method parameter declaration strings.
    if (executable instanceof Method) { // TODO: inner class constructors have a receiver
      parameterNames.add(identifiers.getReceiverName());
    }
    parameterNames.addAll(identifiers.getParameterNames());
    String guardExpressionDeclarations =
        guardExpressionSignature.getDeclarationArguments(parameterNames);

    parameterNames.add(identifiers.getReturnName());
    String propertyExpressionDeclarations =
        propertyExpressionSignature.getDeclarationArguments(parameterNames);

    NameReplacementMap replacementMap = createReplacementMap(parameterNames);
    return new SpecificationTranslator(
        guardExpressionSignature,
        guardExpressionDeclarations,
        propertyExpressionSignature,
        propertyExpressionDeclarations,
        replacementMap,
        compiler);
  }

  /**
   * Create the {@link RawSignature} for the expression method for evaluating an expression for the
   * given method or constructor.
   *
   * <p>The parameter types of the RawSignature are the declaring class as the receiver type,
   * followed by the parameter types of {@code executable}, followed by the return type if {@code
   * postState} is true and {@code executable} is not a void method.
   *
   * <p>Note: The declaring class of the expression method is actually determined by {@link
   * ExecutableBooleanExpression#createMethod(RawSignature, String, String, SequenceCompiler)}
   *
   * @param executable the method or constructor to which the expression belongs
   * @param postState if true, include a variable for the return value in the signature
   * @return the {@link RawSignature} for a expression method of {@code executable}
   */
  // The type AccessibleObject should be Executable, but that class was introduced in Java 8
  private static RawSignature getExpressionSignature(
      AccessibleObject executable, boolean postState) {
    boolean isMethod = executable instanceof Method;
    Class<?> declaringClass = getDeclaringClass(executable);
    // TODO: a constructor for an inner class has a receiver (which is not the declaring class)
    Class<?> receiverType = isMethod ? declaringClass : null;
    Class<?>[] parameterTypes = getParameterTypes(executable);
    Class<?> returnType =
        (!postState ? null : (isMethod ? ((Method) executable).getReturnType() : declaringClass));
    String packageName = getPackageName(declaringClass.getPackage());
    return ExecutableBooleanExpression.getRawSignature(
        packageName, receiverType, parameterTypes, returnType);
  }

  // In JDK 8, replace invocations of this by: executable.getDeclaringClass()
  private static Class<?> getDeclaringClass(AccessibleObject executable) {
    if (executable instanceof Method) {
      return ((Method) executable).getDeclaringClass();
    } else {
      return ((Constructor<?>) executable).getDeclaringClass();
    }
  }

  // In JDK 8, replace invocations of this by: executable.getParameterTypes()
  private static Class<?>[] getParameterTypes(AccessibleObject executable) {
    if (executable instanceof Method) {
      return ((Method) executable).getParameterTypes();
    } else {
      return ((Constructor<?>) executable).getParameterTypes();
    }
  }

  /**
   * Gets the name of the package to use for the package of the expression method. If the package
   * name begins with {@code "java"} modifies it to begin with {@code "randoop"} instead, since user
   * classes cannot be added to the {@code java} package.
   *
   * @param aPackage the package to get the name of, may be null
   * @return the name of the package, updated to start with "randoop" if the original begins with
   *     "java"; the empty string if {@code aPackage} is null
   */
  private static String getPackageName(Package aPackage) {
    if (aPackage == null) {
      return null;
    }

    String packageName = aPackage.getName();
    if (packageName.startsWith("java.")) {
      packageName = "randoop." + packageName;
    }
    return packageName;
  }

  /**
   * Creates the replacement map from the given parameter names to numbered dummy variable names as
   * used in {@link randoop.contract.ObjectContract}.
   *
   * @param parameterNames the parameter names of the expression methods
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
   * Create the {@link OperationConditions} object for the given {@link OperationSpecification}
   * using this {@link SpecificationTranslator}.
   *
   * @param specification the specification from which the expressions are to be created
   * @return the {@link OperationConditions} for the given specification
   */
  OperationConditions createConditions(OperationSpecification specification) {

    return new OperationConditions(
        getGuardExpressions(specification.getPreconditions()),
        getReturnConditions(specification.getPostconditions()),
        getThrowsConditions(specification.getThrowsConditions()));
  }

  /**
   * Construct the list of {@link ExecutableBooleanExpression} objects, one for each {@link
   * Precondition}.
   *
   * @param preconditions the list of {@link Precondition} objects that will be converted to {@link
   *     ExecutableBooleanExpression}
   * @return the list of {@link ExecutableBooleanExpression} objects obtained by converting each
   *     {@link Precondition}
   */
  private List<ExecutableBooleanExpression> getGuardExpressions(List<Precondition> preconditions) {
    List<ExecutableBooleanExpression> guardExpressions = new ArrayList<>();
    for (Precondition precondition : preconditions) {
      try {
        guardExpressions.add(create(precondition.getGuard()));
      } catch (RandoopConditionError e) {
        if (GenInputsAbstract.fail_on_condition_error) {
          throw e;
        }
        System.out.println("Warning: discarded uncompilable guard expression: " + e.getMessage());
      }
    }
    return guardExpressions;
  }

  /**
   * Construct the list of {@link GuardPropertyPair} objects, one for each {@link Postcondition} in
   * {@code postconditions}.
   *
   * @param postconditions the list of {@link Postcondition} that will be converted to {@link
   *     GuardPropertyPair} objects
   * @return the list of {@link GuardPropertyPair} objects obtained by converting each {@link
   *     Postcondition}
   */
  private ArrayList<GuardPropertyPair> getReturnConditions(List<Postcondition> postconditions) {
    ArrayList<GuardPropertyPair> returnConditions = new ArrayList<>();
    for (Postcondition postcondition : postconditions) {
      try {
        ExecutableBooleanExpression guardExpression = create(postcondition.getGuard());
        ExecutableBooleanExpression booleanExpression = create(postcondition.getProperty());
        returnConditions.add(new GuardPropertyPair(guardExpression, booleanExpression));
      } catch (RandoopConditionError e) {
        if (GenInputsAbstract.fail_on_condition_error) {
          throw e;
        }
        System.out.println(
            "Warning: discarding uncompilable property expression: " + e.getMessage());
      }
    }
    return returnConditions;
  }

  /**
   * Construct the list of {@link GuardThrowsPair} objects, one for each {@link ThrowsCondition} in
   * {@code throwsConditions}.
   *
   * @param throwsConditions the list of {@link ThrowsCondition} that will be converted to {@link
   *     GuardPropertyPair} objects
   * @return the list of {@link GuardPropertyPair} objects obtained by converting each {@link
   *     ThrowsCondition}
   */
  private ArrayList<GuardThrowsPair> getThrowsConditions(List<ThrowsCondition> throwsConditions) {
    ArrayList<GuardThrowsPair> throwsPairs = new ArrayList<>();
    for (ThrowsCondition throwsCondition : throwsConditions) {
      ClassOrInterfaceType exceptionType;
      try {
        exceptionType =
            (ClassOrInterfaceType)
                ClassOrInterfaceType.forName(throwsCondition.getExceptionTypeName());
      } catch (ClassNotFoundException e) {
        String msg =
            "Error in specification "
                + throwsCondition
                + ". Cannot find exception type: "
                + e.getMessage();
        if (Log.isLoggingOn()) {
          Log.logLine(msg);
        }
        continue;
      }
      try {
        ExecutableBooleanExpression guardExpression = create(throwsCondition.getGuard());
        ThrowsClause throwsClause =
            new ThrowsClause(exceptionType, throwsCondition.getDescription());
        throwsPairs.add(new GuardThrowsPair(guardExpression, throwsClause));
      } catch (RandoopConditionError e) {
        if (GenInputsAbstract.fail_on_condition_error) {
          throw e;
        }
        System.out.println("Warning: discarding uncompilable throws-expression: " + e.getMessage());
      }
    }
    return throwsPairs;
  }

  /**
   * Creates a {@link ExecutableBooleanExpression} object for the given {@link
   * randoop.condition.specification.AbstractBooleanExpression} using the guard expression signature
   * of this {@link SpecificationTranslator}.
   *
   * @param expression the {@link randoop.condition.specification.AbstractBooleanExpression} to be
   *     converted
   * @return the {@link ExecutableBooleanExpression} object for {@code expression}
   */
  private ExecutableBooleanExpression create(Guard expression) {
    String contractText = replacementMap.replaceNames(expression.getConditionSource());
    return new ExecutableBooleanExpression(
        guardExpressionSignature,
        guardExpressionDeclaration,
        expression.getConditionSource(),
        contractText,
        expression.getDescription(),
        compiler);
  }

  /**
   * Creates a {@link ExecutableBooleanExpression} object for the given {@link
   * randoop.condition.specification.AbstractBooleanExpression} using the property expression
   * signature of this {@link SpecificationTranslator}.
   *
   * @param expression the {@link randoop.condition.specification.AbstractBooleanExpression} to be
   *     converted
   * @return the {@link ExecutableBooleanExpression} object for {@code expression}
   */
  public ExecutableBooleanExpression create(Property expression) {
    String contractText = replacementMap.replaceNames(expression.getConditionSource());
    return new ExecutableBooleanExpression(
        propertyExpressionSignature,
        propertyExpressionDeclarations,
        expression.getConditionSource(),
        contractText,
        expression.getDescription(),
        compiler);
  }

  /**
   * Return the guard expression method parameter declaration string. Includes parentheses.
   *
   * <p>Only used for testing.
   *
   * @return the guard expression method parameter declaration string
   */
  String getGuardExpressionDeclaration() {
    return guardExpressionDeclaration;
  }

  /**
   * Return the property expression method parameter declaration string. Includes parentheses.
   *
   * <p>Only used for testing.
   *
   * @return the property expression method parameter declaration string
   */
  String getPropertyExpressionDeclarations() {
    return propertyExpressionDeclarations;
  }

  /**
   * Returns the property expression method signature from this object. This signature includes the
   * parameters of the guard expression signature, plus the return type.
   *
   * <p>Used for testing.
   *
   * @return the property expression method signature for this object
   */
  RawSignature getPropertyExpressionSignature() {
    return propertyExpressionSignature;
  }

  /**
   * Returns the replacement map for the identifiers in the expression to the dummy variables
   * expected in contract assertions
   *
   * @return the replacement map for the identifiers in the expression
   */
  NameReplacementMap getReplacementMap() {
    return replacementMap;
  }
}
