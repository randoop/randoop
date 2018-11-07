package randoop.condition;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import randoop.util.Util;

/**
 * Method {@link #createExecutableSpecification} translates an {@link OperationSpecification} object
 * (which has preconditions, postconditions, and throws conditions) to its executable version,
 * {@link ExecutableSpecification}.
 */
public class SpecificationTranslator {

  /** The base name of dummy variables used by {@link randoop.contract.ObjectContract}. */
  private static final String DUMMY_VARIABLE_BASE_NAME = "x";

  /** The {@link RawSignature} for a prestate (guard) expression method. */
  private RawSignature prestateExpressionSignature;

  /**
   * The parameter part of a method declaration string, with parentheses, for a prestate (guard)
   * expression method.
   */
  private final String prestateExpressionDeclaration;

  /** The {@link RawSignature} for a poststate (property) expression method. */
  private RawSignature poststateExpressionSignature;

  /**
   * The parameter declaration string, with parentheses, for a poststate (property) expression
   * method.
   */
  private final String poststateExpressionDeclarations;

  /** The map of expression identifiers to dummy variables. */
  private final Map<String, String> replacementMap;

  /** The {@link SequenceCompiler} for compiling expression methods. */
  private final SequenceCompiler compiler;

  /**
   * Creates a {@link SpecificationTranslator} object in the given package with the signature
   * strings and variable replacementMap.
   *
   * @param prestateExpressionSignature the {@link RawSignature} for a prestate expression method
   * @param prestateExpressionDeclaration the parameter declaration string, with parentheses, for a
   *     prestate expression method
   * @param poststateExpressionSignature the {@link RawSignature} for a poststate expression method
   * @param poststateExpressionDeclaration the parameter declaration string, with parentheses, for a
   *     poststate expression method
   * @param replacementMap the map of expression identifiers to dummy variables
   * @param compiler the {@link SequenceCompiler} for creating expression methods
   */
  private SpecificationTranslator(
      RawSignature prestateExpressionSignature,
      String prestateExpressionDeclaration,
      RawSignature poststateExpressionSignature,
      String poststateExpressionDeclaration,
      Map<String, String> replacementMap,
      SequenceCompiler compiler) {
    this.prestateExpressionSignature = prestateExpressionSignature;
    this.prestateExpressionDeclaration = prestateExpressionDeclaration;
    this.poststateExpressionSignature = poststateExpressionSignature;
    this.poststateExpressionDeclarations = poststateExpressionDeclaration;
    this.replacementMap = replacementMap;
    this.compiler = compiler;
  }

  /**
   * Creates a {@link SpecificationTranslator} object to translate the {@link
   * OperationSpecification} of {@code executable}.
   *
   * @param executable the {@code java.lang.reflect.AccessibleObject} for the operation with {@link
   *     OperationSpecification} to translate
   * @param specification the specification to be translated
   * @param compiler the sequence compiler to use to create expression methods
   * @return the translator object to convert the specifications for {@code executable}
   */
  static SpecificationTranslator createTranslator(
      AccessibleObject executable,
      OperationSpecification specification,
      SequenceCompiler compiler) {
    Identifiers identifiers = specification.getIdentifiers();

    // Get expression method signatures.
    RawSignature prestateExpressionSignature = getExpressionSignature(executable, false);
    RawSignature poststateExpressionSignature = getExpressionSignature(executable, true);

    // parameterNames is side-effected, then used, then side-effected and used again.
    List<String> parameterNames = new ArrayList<>();

    // Get expression method parameter declaration strings.
    if (executable instanceof Method) { // TODO: inner class constructors have a receiver
      parameterNames.add(identifiers.getReceiverName());
    }
    parameterNames.addAll(identifiers.getParameterNames());
    String prestateExpressionDeclarations =
        prestateExpressionSignature.getDeclarationArguments(parameterNames);

    parameterNames.add(identifiers.getReturnName());
    String poststateExpressionDeclarations =
        poststateExpressionSignature.getDeclarationArguments(parameterNames);

    Map<String, String> replacementMap = createReplacementMap(parameterNames);
    return new SpecificationTranslator(
        prestateExpressionSignature,
        prestateExpressionDeclarations,
        poststateExpressionSignature,
        poststateExpressionDeclarations,
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
  // In Java 8, change the type AccessibleObject to Executable.
  private static RawSignature getExpressionSignature(
      AccessibleObject executable, boolean postState) {
    boolean isMethod = executable instanceof Method;
    Class<?> declaringClass = getDeclaringClass(executable);
    // TODO: a constructor for an inner class has a receiver (which is not the declaring class)
    Class<?> receiverType = isMethod ? declaringClass : null;
    Class<?>[] parameterTypes = getParameterTypes(executable);
    Class<?> returnType =
        (!postState ? null : (isMethod ? ((Method) executable).getReturnType() : declaringClass));
    String packageName = renamedPackage(declaringClass.getPackage());
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
   * Gets the name of the package. If the package name begins with {@code "java."}, prefixes it by
   * {@code "randoop."}, since user classes cannot be added to the {@code java} package.
   *
   * @param aPackage the package to get the name of, may be null
   * @return the name of the package, updated to start with "randoop." if the original begins with
   *     "java."; null if {@code aPackage} is null
   */
  private static String renamedPackage(Package aPackage) {
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
  private static Map<String, String> createReplacementMap(List<String> parameterNames) {
    Map<String, String> replacementMap = new HashMap<>();
    for (int i = 0; i < parameterNames.size(); i++) {
      replacementMap.put(parameterNames.get(i), DUMMY_VARIABLE_BASE_NAME + i);
    }
    return replacementMap;
  }

  /**
   * Create the {@link ExecutableSpecification} object for the given {@link OperationSpecification}
   * using this {@link SpecificationTranslator}.
   *
   * @param executable the {@code java.lang.reflect.AccessibleObject} for the operation to translate
   * @param specification the specification to translate
   * @param compiler the sequence compiler to use to create expression methods
   * @return the {@link ExecutableSpecification} for the given specification
   */
  public static ExecutableSpecification createExecutableSpecification(
      AccessibleObject executable,
      OperationSpecification specification,
      SequenceCompiler compiler) {
    SpecificationTranslator st = createTranslator(executable, specification, compiler);
    return new ExecutableSpecification(
        st.getGuardExpressions(specification.getPreconditions()),
        st.getReturnConditions(specification.getPostconditions()),
        st.getThrowsConditions(specification.getThrowsConditions()));
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
      } catch (RandoopSpecificationError e) {
        if (GenInputsAbstract.ignore_condition_compilation_error) {
          System.out.println("Warning: discarded uncompilable guard expression: " + e.getMessage());
        } else {
          throw e;
        }
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
        ExecutableBooleanExpression guard = create(postcondition.getGuard());
        ExecutableBooleanExpression property = create(postcondition.getProperty());
        returnConditions.add(new GuardPropertyPair(guard, property));
      } catch (RandoopSpecificationError e) {
        if (GenInputsAbstract.ignore_condition_compilation_error) {
          System.out.println(
              "Warning: discarding uncompilable poststate expression: " + e.getMessage());
        } else {
          throw e;
        }
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
        Log.logPrintf("%s%n", msg);
        if (GenInputsAbstract.ignore_condition_compilation_error) {
          continue;
        } else {
          throw new RandoopSpecificationError(msg);
        }
      }
      try {
        ExecutableBooleanExpression guard = create(throwsCondition.getGuard());
        ThrowsClause throwsClause =
            new ThrowsClause(exceptionType, throwsCondition.getDescription());
        throwsPairs.add(new GuardThrowsPair(guard, throwsClause));
      } catch (RandoopSpecificationError e) {
        if (GenInputsAbstract.ignore_condition_compilation_error) {
          System.out.println(
              "Warning: discarding uncompilable throws-expression: " + e.getMessage());
        } else {
          throw e;
        }
      }
    }
    return throwsPairs;
  }

  /**
   * Creates a {@link ExecutableBooleanExpression} object for the given {@link
   * randoop.condition.specification.AbstractBooleanExpression} using the prestate expression
   * signature of this {@link SpecificationTranslator}.
   *
   * @param expression the {@link randoop.condition.specification.AbstractBooleanExpression} to be
   *     converted
   * @return the {@link ExecutableBooleanExpression} object for {@code expression}
   */
  private ExecutableBooleanExpression create(Guard expression) {
    String contractText = Util.replaceWords(expression.getConditionSource(), replacementMap);
    return new ExecutableBooleanExpression(
        prestateExpressionSignature,
        prestateExpressionDeclaration,
        expression.getConditionSource(),
        contractText,
        expression.getDescription(),
        compiler);
  }

  /**
   * Creates a {@link ExecutableBooleanExpression} object for the given {@link
   * randoop.condition.specification.AbstractBooleanExpression} using the poststate expression
   * signature of this {@link SpecificationTranslator}.
   *
   * @param expression the {@link randoop.condition.specification.AbstractBooleanExpression} to be
   *     converted
   * @return the {@link ExecutableBooleanExpression} object for {@code expression}
   */
  public ExecutableBooleanExpression create(Property expression) {
    String contractText = Util.replaceWords(expression.getConditionSource(), replacementMap);
    return new ExecutableBooleanExpression(
        poststateExpressionSignature,
        poststateExpressionDeclarations,
        expression.getConditionSource(),
        contractText,
        expression.getDescription(),
        compiler);
  }

  /**
   * Return the prestate expression method parameter declaration string. Includes parentheses.
   *
   * <p>Only used for testing.
   *
   * @return the prestate expression method parameter declaration string
   */
  String getPrestateExpressionDeclaration() {
    return prestateExpressionDeclaration;
  }

  /**
   * Return the poststate expression method parameter declaration string. Includes parentheses.
   *
   * <p>Only used for testing.
   *
   * @return the poststate expression method parameter declaration string
   */
  String getPoststateExpressionDeclarations() {
    return poststateExpressionDeclarations;
  }

  /**
   * Returns the poststate expression method signature from this object. This signature includes the
   * parameters of the prestate expression signature, plus the return type.
   *
   * <p>Only used for testing.
   *
   * @return the poststate expression method signature for this object
   */
  RawSignature getPoststateExpressionSignature() {
    return poststateExpressionSignature;
  }

  /**
   * Returns the compiler from this object.
   *
   * <p>Only used for testing. ||||||| merged common ancestors
   *
   * <p>Only used for testing.
   *
   * @return the compiler for this object
   */
  SequenceCompiler getCompiler() {
    return compiler;
  }

  /**
   * Returns the replacement map for the identifiers in the expression to the dummy variables
   * expected in contract assertions.
   *
   * @return the replacement map for the identifiers in the expression
   */
  Map<String, String> getReplacementMap() {
    return replacementMap;
  }
}
