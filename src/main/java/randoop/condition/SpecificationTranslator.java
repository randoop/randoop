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
   * Creates a {@link SpecificationTranslator} object in the given package with the signature
   * strings and variable replacementMap.
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
  private SpecificationTranslator(
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
   * Creates a {@link SpecificationTranslator} object to translate the {@link
   * OperationSpecification} of {@code accessibleObject}.
   *
   * @param accessibleObject the {@code java.lang.reflect.AccessibleObject} for the operation with
   *     {@link OperationSpecification} to translate
   * @param identifiers the {@link Identifiers} from the specification to be translated
   * @param compiler the sequence compiler to use to create condition methods
   * @return the translator object to convert the specifications for {@code accessibleObject}
   */
  static SpecificationTranslator createTranslator(
      AccessibleObject accessibleObject, Identifiers identifiers, SequenceCompiler compiler) {
    RawSignature preconditionSignature;
    String preConditionDeclarations;
    RawSignature postconditionSignature;
    String postConditionDeclarations;
    List<String> parameterNames = new ArrayList<>();
    if (accessibleObject instanceof Method) {
      Method method = (Method) accessibleObject;
      // get condition method signatures
      preconditionSignature = getPreConditionSignature(method);
      postconditionSignature = getPostConditionSignature(method);

      // get condition method parameter declaration strings
      parameterNames.add(identifiers.getReceiverName());
      parameterNames.addAll(identifiers.getParameterNames());
      preConditionDeclarations = preconditionSignature.getDeclarationArguments(parameterNames);
      parameterNames.add(identifiers.getReturnName());
      postConditionDeclarations = postconditionSignature.getDeclarationArguments(parameterNames);
    } else if (accessibleObject instanceof Constructor) {
      Constructor<?> constructor = (Constructor) accessibleObject;
      // get condition method signatures
      preconditionSignature = getPreConditionSignature(constructor);
      postconditionSignature = getPostConditionSignature(constructor);

      // get condition method parameter declaration strings
      parameterNames.addAll(identifiers.getParameterNames());
      preConditionDeclarations = preconditionSignature.getDeclarationArguments(parameterNames);
      parameterNames.add(identifiers.getReturnName());
      postConditionDeclarations = postconditionSignature.getDeclarationArguments(parameterNames);
    } else {
      throw new RandoopConditionError("Specification operation is neither a method or constructor");
    }
    NameReplacementMap replacementMap = createReplacementMap(parameterNames);
    return new SpecificationTranslator(
        preconditionSignature,
        preConditionDeclarations,
        postconditionSignature,
        postConditionDeclarations,
        replacementMap,
        compiler);
  }

  /**
   * Create the {@link RawSignature} for the condition method for evaluating a precondition for the
   * given method.
   *
   * <p>The parameter types of the condition method are the declaring class as the receiver type
   * followed by the parameter types of the method.
   *
   * <p>Note: The declaring class of the condition method is actually determined by {@link
   * ConditionMethodCreator#create(RawSignature, String, String, SequenceCompiler)}
   *
   * @param method the method to which the precondition belongs
   * @return the {@link RawSignature} for a pre-condition method of the method
   */
  private static RawSignature getPreConditionSignature(Method method) {
    Class<?> declaringClass = method.getDeclaringClass();
    Class<?>[] parameterTypes = method.getParameterTypes();
    String packageName = getPackageName(declaringClass.getPackage());
    return ConditionMethodCreator.getPreconditionSignature(
        packageName, declaringClass, parameterTypes, true);
  }

  /**
   * Create the {@link RawSignature} for the condition method for evaluating a precondition for the
   * given constructor.
   *
   * <p>The parameter types of the condition method are the parameter types of the constructor.
   *
   * <p>Note: The declaring class of the condition method is actually determined by {@link
   * ConditionMethodCreator#create(RawSignature, String, String, SequenceCompiler)}
   *
   * @param constructor the constructor to which the precondition belongs
   * @return the {@link RawSignature} for a pre-condition method of the constructor
   */
  private static RawSignature getPreConditionSignature(Constructor<?> constructor) {
    Class<?> declaringClass = constructor.getDeclaringClass();
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    String packageName = getPackageName(declaringClass.getPackage());
    return ConditionMethodCreator.getPreconditionSignature(
        packageName, declaringClass, parameterTypes, false);
  }

  /**
   * Create the {@link RawSignature} for the condition method for evaluating a post-condition for
   * the given method.
   *
   * <p>The parameter types of the condition method are the declaring class as the receiver type
   * followed by the parameter types of the method, and the return type of the method.
   *
   * <p>Note: The declaring class of the condition method is actually determined by {@link
   * ConditionMethodCreator#create(RawSignature, String, String, SequenceCompiler)}
   *
   * @param method the method to which the post-condition belongs
   * @return the {@link RawSignature} for a post-condition method of the method
   */
  private static RawSignature getPostConditionSignature(Method method) {
    Class<?> declaringClass = method.getDeclaringClass();
    Class<?>[] parameterTypes = method.getParameterTypes();
    Class<?> returnType = method.getReturnType();
    String packageName = getPackageName(declaringClass.getPackage());
    return ConditionMethodCreator.getPostconditionSignature(
        packageName, declaringClass, parameterTypes, returnType, true);
  }

  /**
   * Create the {@link RawSignature} for the condition method for evaluating a post-condition for
   * the given constructor.
   *
   * <p>The parameter types of the condition method are the the parameter types of the method,
   * followed by the return type of the method.
   *
   * <p>Note: The declaring class of the condition method is actually determined by {@link
   * ConditionMethodCreator#create(RawSignature, String, String, SequenceCompiler)}
   *
   * @param constructor the constructor to which the post-condition belongs
   * @return the {@link RawSignature} for a post-condition method of the method
   */
  private static RawSignature getPostConditionSignature(Constructor<?> constructor) {
    Class<?> declaringClass = constructor.getDeclaringClass();
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    String packageName = getPackageName(declaringClass.getPackage());
    return ConditionMethodCreator.getPostconditionSignature(
        packageName, declaringClass, parameterTypes, declaringClass, false);
  }

  /**
   * Gets the name of the package to use for the package of the condition method. If the package
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
   * Create the {@link OperationConditions} object for the given {@link OperationSpecification}
   * using this {@link SpecificationTranslator}.
   *
   * @param specification the specification from which the conditions are to be created
   * @return the {@link OperationConditions} for the given specification
   */
  OperationConditions createConditions(OperationSpecification specification) {

    return new OperationConditions(
        getPreConditions(specification.getPreSpecifications()),
        getReturnConditions(specification.getPostSpecifications()),
        getThrowsConditions(specification.getThrowsSpecifications()));
  }

  /**
   * Get the list of {@link Condition} objects representing the pre-conditions from {@code
   * specification}.
   *
   * @param preSpecifications the list of {@link PreSpecification} pre-conditions that will be
   *     converted
   * @return the list of {@link Condition} objects obtained by converting the elements of {@code
   *     preSpecifications}
   */
  private List<Condition> getPreConditions(List<PreSpecification> preSpecifications) {
    List<Condition> paramConditions = new ArrayList<>();
    for (PreSpecification preSpecification : preSpecifications) {
      try {
        paramConditions.add(create(preSpecification.getGuard()));
      } catch (RandoopConditionError e) {
        if (GenInputsAbstract.fail_on_condition_error) {
          throw e;
        }
        System.out.println("Warning: discarded uncompilable precondition: " + e.getMessage());
      }
    }
    return paramConditions;
  }

  /**
   * Get the list of pairs of {@link Condition} and {@link PostCondition} representing
   * post-conditions from {@code postSpecifications}.
   *
   * @param postSpecifications the list of {@link PostSpecification} that will be converted
   * @return the list of {@link ConditionPair} objects obtained by converting the elements of {@code
   *     postSpecifications}
   */
  private ArrayList<ConditionPair<PostCondition>> getReturnConditions(
      List<PostSpecification> postSpecifications) {
    ArrayList<ConditionPair<PostCondition>> returnConditions = new ArrayList<>();
    for (PostSpecification postSpecification : postSpecifications) {
      try {
        Condition preCondition = create(postSpecification.getGuard());
        PostCondition postCondition = create(postSpecification.getProperty());
        returnConditions.add(new ConditionPair<>(preCondition, postCondition));
      } catch (RandoopConditionError e) {
        if (GenInputsAbstract.fail_on_condition_error) {
          throw e;
        }
        System.out.println("Warning: discarding uncompilable postcondition: " + e.getMessage());
      }
    }
    return returnConditions;
  }

  /**
   * Get the list of pairs of {@link Condition} and {@link ThrowsClause} representing
   * throws-conditions from {@code throwsSpecifications}.
   *
   * @param throwsSpecifications the list of {@link ThrowsSpecification} that will be converted
   * @return the list of {@link ConditionPair} objects obtained by converting the elements of {@code
   *     }
   */
  private ArrayList<ConditionPair<ThrowsClause>> getThrowsConditions(
      List<ThrowsSpecification> throwsSpecifications) {
    ArrayList<ConditionPair<ThrowsClause>> throwsConditions = new ArrayList<>();
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
        Condition guardCondition = create(throwsSpecification.getGuard());
        ThrowsClause exception =
            new ThrowsClause(exceptionType, "// " + throwsSpecification.getDescription());
        throwsConditions.add(new ConditionPair<>(guardCondition, exception));
      } catch (RandoopConditionError e) {
        if (GenInputsAbstract.fail_on_condition_error) {
          throw e;
        }
        System.out.println("Warning: discarding uncompilable throws-condition: " + e.getMessage());
      }
    }
    return throwsConditions;
  }

  /**
   * Creates a {@link Condition} object for the given {@link Guard} using the pre-condition
   * signature of this {@link SpecificationTranslator}.
   *
   * @param guard the {@link Guard} to be converted
   * @return the {@link Condition} object for {@code guard}
   */
  private Condition create(Guard guard) {
    Method conditionMethod =
        ConditionMethodCreator.create(
            preConditionSignature, preConditionDeclarations, guard.getConditionSource(), compiler);
    String comment = guard.getDescription();
    String conditionText = replacementMap.replaceNames(guard.getConditionSource());
    return new Condition(conditionMethod, comment, conditionText);
  }

  /**
   * Creates a {@link PostCondition} object for the given {@link Property} using the post-condition
   * signature of this {@link SpecificationTranslator}.
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
   * @return the pre-condition method parameter declaration string
   */
  String getPreConditionDeclarations() {
    return preConditionDeclarations;
  }

  /**
   * Return the post-condition method parameter declaration string. Includes parentheses.
   *
   * <p>Only used for testing.
   *
   * @return the post-condition method parameter declaration string
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
