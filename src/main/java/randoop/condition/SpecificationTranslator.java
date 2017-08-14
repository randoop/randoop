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
import randoop.condition.specification.PostSpecification;
import randoop.condition.specification.PreSpecification;
import randoop.condition.specification.Property;
import randoop.condition.specification.ThrowsSpecification;
import randoop.main.GenInputsAbstract;
import randoop.reflection.RawSignature;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Log;

/**
 * Implements translation of an {@link OperationSpecification} object to an {@link
 * OperationConditions} object.
 */
public class SpecificationTranslator {

  /** The name of dummy variables used by {@link randoop.contract.ObjectContract}. */
  private static final String DUMMY_VARIABLE_NAME = "x";

  /** The map of variable name replacements. */
  private final NameReplacementMap replacementMap;

  /** The {@link SequenceCompiler} for compiling expression methods. */
  private final SequenceCompiler compiler;

  /** The signature string for a guard expression method. */
  private RawSignature guardExpressionSignature;

  /** The parameter declaration string for a guard expression method. */
  private final String guardExpressionDeclaration;

  /** The signature string for a property expression method. */
  private RawSignature propertyExpressionSignature;

  /** The parameter declaration string, with parentheses, for a property expression method. */
  private final String propertyExpressionDeclarations;

  /**
   * Creates a {@link SpecificationTranslator} object in the given package with the signature
   * strings and variable replacementMap.
   *
   * @param guardExpressionSignature the {@link RawSignature} for a guard expression method
   * @param guardExpressionDeclarations the parameter declaration string, with parentheses, for a
   *     guard expression method
   * @param propertyExpressionSignature the {@link RawSignature} for a property expression method
   * @param propertyExpressionDeclarations the parameter declaration string, with parentheses, for a
   *     property expression method
   * @param replacementMap the map of expression identifiers to dummy variables
   * @param compiler the {@link SequenceCompiler} for creating expression methods
   */
  private SpecificationTranslator(
      RawSignature guardExpressionSignature,
      String guardExpressionDeclarations,
      RawSignature propertyExpressionSignature,
      String propertyExpressionDeclarations,
      NameReplacementMap replacementMap,
      SequenceCompiler compiler) {
    this.guardExpressionSignature = guardExpressionSignature;
    this.guardExpressionDeclaration = guardExpressionDeclarations;
    this.propertyExpressionSignature = propertyExpressionSignature;
    this.propertyExpressionDeclarations = propertyExpressionDeclarations;
    this.replacementMap = replacementMap;
    this.compiler = compiler;
  }

  /**
   * Creates a {@link SpecificationTranslator} object to translate the {@link
   * OperationSpecification} of {@code accessibleObject}.
   *
   * @param accessibleObject the {@code java.lang.reflect.AccessibleObject} for the operation with
   *     {@link OperationSpecification} to translate
   * @param identifiers the {@link Identifiers} from the specification to be translated
   * @param compiler the sequence compiler to use to create expression methods
   * @return the translator object to convert the specifications for {@code accessibleObject}
   */
  static SpecificationTranslator createTranslator(
      AccessibleObject accessibleObject, Identifiers identifiers, SequenceCompiler compiler) {
    RawSignature guardExpressionSignature;
    String guardExpressionDeclarations;
    RawSignature propertyExpressionSignature;
    String propertyExpressionDeclarations;
    List<String> parameterNames = new ArrayList<>();
    if (accessibleObject instanceof Method) {
      Method method = (Method) accessibleObject;
      // get expression method signatures
      guardExpressionSignature = getGuardExpressionSignature(method);
      propertyExpressionSignature = getPropertyExpressionSignature(method);

      // get expression method parameter declaration strings
      parameterNames.add(identifiers.getReceiverName());
      parameterNames.addAll(identifiers.getParameterNames());
      guardExpressionDeclarations =
          guardExpressionSignature.getDeclarationArguments(parameterNames);
      parameterNames.add(identifiers.getReturnName());
      propertyExpressionDeclarations =
          propertyExpressionSignature.getDeclarationArguments(parameterNames);
    } else if (accessibleObject instanceof Constructor) {
      Constructor<?> constructor = (Constructor) accessibleObject;
      // get expression method signatures
      guardExpressionSignature = getGuardExpressionSignature(constructor);
      propertyExpressionSignature = getPropertyExpressionSignature(constructor);

      // get expression method parameter declaration strings
      parameterNames.addAll(identifiers.getParameterNames());
      guardExpressionDeclarations =
          guardExpressionSignature.getDeclarationArguments(parameterNames);
      parameterNames.add(identifiers.getReturnName());
      propertyExpressionDeclarations =
          propertyExpressionSignature.getDeclarationArguments(parameterNames);
    } else {
      throw new RandoopConditionError("Specification operation is neither a method or constructor");
    }
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
   * Create the {@link RawSignature} for the expression method for evaluating a guard for the given
   * method or constructor.
   *
   * <p>The parameter types of the RawSignature are the declaring class as the receiver type
   * followed by the parameter types of {@code executable}.
   *
   * <p>Note: The declaring class of the expression method is actually determined by {@link
   * BooleanExpression#createMethod(RawSignature, String, String, SequenceCompiler)}
   *
   * @param executable the method or constructor to which the guard belongs
   * @return the {@link RawSignature} for a guard expression method of {@code executable}
   */
  // The type AccessibleObject should be Executable, but that class was introduced in Java 8
  private static RawSignature getGuardExpressionSignature(AccessibleObject executable) {
    Class<?> declaringClass = getDeclaringClass(executable);
    Class<?>[] parameterTypes = getParameterTypes(executable);
    String packageName = getPackageName(declaringClass.getPackage());
    return BooleanExpression.getRawSignature(packageName, parameterTypes);
  }

  /**
   * Create the {@link RawSignature} for the expression method for evaluating a property expression
   * for the given method or constructor.
   *
   * <p>The parameter types of the RawSignature are the declaring class as the receiver type
   * followed by the parameter types of {@code executable}, and the return type (if {@code
   * executable} is a method).
   *
   * <p>Note: The declaring class of the expression method is actually determined by {@link
   * BooleanExpression#createMethod(RawSignature, String, String, SequenceCompiler)}
   *
   * @param executable the method or constructor to which the property expression belongs
   * @return the {@link RawSignature} for a property expression method of {@code executable}
   */
  // The type AccessibleObject should be Executable, but that class was introduced in Java 8
  private static RawSignature getPropertyExpressionSignature(AccessibleObject executable) {
    boolean isMethod = executable instanceof Method;
    Class<?> declaringClass = getDeclaringClass(executable);
    Class<?>[] parameterTypes = getParameterTypes(executable);
    Class<?> returnType = (isMethod ? ((Method) executable).getReturnType() : declaringClass);
    String packageName = getPackageName(declaringClass.getPackage());
    return PropertyExpression.getRawSignature(
        packageName, declaringClass, parameterTypes, returnType);
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
        getGuardExpressions(specification.getPreSpecifications()),
        getReturnConditions(specification.getPostSpecifications()),
        getThrowsConditions(specification.getThrowsSpecifications()));
  }

  /**
   * Construct the list of {@link BooleanExpression} objects, one for each {@link PreSpecification}.
   *
   * @param preSpecifications the list of {@link PreSpecification} objects that will be converted to
   *     {@link BooleanExpression}
   * @return the list of {@link BooleanExpression} objects obtained by converting each {@link
   *     PreSpecification}
   */
  private List<BooleanExpression> getGuardExpressions(List<PreSpecification> preSpecifications) {
    List<BooleanExpression> guardExpressions = new ArrayList<>();
    for (PreSpecification preSpecification : preSpecifications) {
      try {
        guardExpressions.add(create(preSpecification.getGuard()));
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
   * Construct the list of {@link GuardPropertyExpressionPair} objects, one for each {@link
   * PostSpecification} in {@code postSpecifications}.
   *
   * @param postSpecifications the list of {@link PostSpecification} that will be converted to
   *     {@link GuardPropertyExpressionPair} objects
   * @return the list of {@link GuardPropertyExpressionPair} objects obtained by converting each
   *     {@link PostSpecification}
   */
  private ArrayList<GuardPropertyExpressionPair> getReturnConditions(
      List<PostSpecification> postSpecifications) {
    ArrayList<GuardPropertyExpressionPair> returnConditions = new ArrayList<>();
    for (PostSpecification postSpecification : postSpecifications) {
      try {
        BooleanExpression guardExpression = create(postSpecification.getGuard());
        PropertyExpression propertyExpression = create(postSpecification.getProperty());
        returnConditions.add(new GuardPropertyExpressionPair(guardExpression, propertyExpression));
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
   * Construct the list of {@link GuardExpressionThrowsPair} objects, one for each {@link
   * ThrowsSpecification} in {@code throwsSpecifications}.
   *
   * @param throwsSpecifications the list of {@link ThrowsSpecification} that will be converted to
   *     {@link GuardPropertyExpressionPair} objects
   * @return the list of {@link GuardPropertyExpressionPair} objects obtained by converting each
   *     {@link ThrowsSpecification}
   */
  private ArrayList<GuardExpressionThrowsPair> getThrowsConditions(
      List<ThrowsSpecification> throwsSpecifications) {
    ArrayList<GuardExpressionThrowsPair> throwsPairs = new ArrayList<>();
    for (ThrowsSpecification throwsSpecification : throwsSpecifications) {
      ClassOrInterfaceType exceptionType;
      try {
        exceptionType =
            (ClassOrInterfaceType)
                ClassOrInterfaceType.forName(throwsSpecification.getExceptionTypeName());
      } catch (ClassNotFoundException e) {
        String msg =
            "Error in specification "
                + throwsSpecification
                + ". Cannot find exception type: "
                + e.getMessage();
        if (Log.isLoggingOn()) {
          Log.logLine(msg);
        }
        continue;
      }
      try {
        BooleanExpression guardExpression = create(throwsSpecification.getGuard());
        ThrowsClause throwsClause =
            new ThrowsClause(exceptionType, "// " + throwsSpecification.getDescription());
        throwsPairs.add(new GuardExpressionThrowsPair(guardExpression, throwsClause));
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
   * Creates a {@link BooleanExpression} object for the given {@link Guard} using the guard
   * expression method signature of this {@link SpecificationTranslator}.
   *
   * @param guard the {@link Guard} to be converted
   * @return the {@link BooleanExpression} object for {@code guard}
   */
  private BooleanExpression create(Guard guard) {
    String contractText = replacementMap.replaceNames(guard.getConditionSource());
    return BooleanExpression.createGuardExpression(
        guardExpressionSignature,
        guardExpressionDeclaration,
        guard.getConditionSource(),
        contractText,
        guard.getDescription(),
        compiler);
  }

  /**
   * Creates a {@link PropertyExpression} object for the given {@link Property} using the property
   * expression signature of this {@link SpecificationTranslator}.
   *
   * @param property the {@link Property} to be converted
   * @return the {@link PropertyExpression} object for {@code property}
   */
  public PropertyExpression create(Property property) {
    Method expressionMethod =
        BooleanExpression.createMethod(
            propertyExpressionSignature,
            propertyExpressionDeclarations,
            property.getConditionSource(),
            compiler);
    String comment = property.getDescription();
    String contractText = replacementMap.replaceNames(property.getConditionSource());
    return new PropertyExpression(expressionMethod, comment, contractText);
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
