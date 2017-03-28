package randoop.condition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import plume.Pair;
import randoop.compile.SequenceClassLoader;
import randoop.compile.SequenceCompiler;
import randoop.condition.specification.Guard;
import randoop.condition.specification.Operation;
import randoop.condition.specification.OperationSpecification;
import randoop.condition.specification.PostSpecification;
import randoop.condition.specification.PreSpecification;
import randoop.condition.specification.Property;
import randoop.condition.specification.ThrowsSpecification;
import randoop.reflection.TypeNames;
import randoop.test.ExpectedExceptionGenerator;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Log;

/**
 * Represents a collection of preconditions and throws-conditions. Preconditions are represented by
 * {@link Condition} objects, while throws-conditions are represented by (condition, exception-type)
 * pairs.
 */
public class SpecificationCollection {

  /** The map from reflection objects to the corresponding {@link OperationSpecification} */
  private final Map<AccessibleObject, OperationSpecification> specificationMap;

  /** The compiler for creating conditionMethods */
  private final SequenceCompiler compiler;

  /**
   * Creates a {@link SpecificationCollection} for the given specification map.
   *
   * @param specificationMap the map from reflection objects to {@link OperationSpecification}
   */
  SpecificationCollection(Map<AccessibleObject, OperationSpecification> specificationMap) {
    this.specificationMap = specificationMap;
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    SequenceClassLoader sequenceClassLoader = new SequenceClassLoader(getClass().getClassLoader());
    List<String> options = new ArrayList<>();
    this.compiler = new SequenceCompiler(sequenceClassLoader, options, diagnostics);
  }

  /**
   * Creates a {@link SpecificationCollection} from the list of files of serialized specifications.
   *
   * @param specificationFiles the files of serialized specifications
   * @return the {@link SpecificationCollection} built from the serialized {@link
   *     OperationSpecification} objects.
   */
  public static SpecificationCollection create(List<File> specificationFiles) {
    if (specificationFiles == null) {
      return null;
    }
    Map<AccessibleObject, OperationSpecification> specMap = new LinkedHashMap<>();
    for (File specificationFile : specificationFiles) {
      List<OperationSpecification> specificationList;
      try {
        specificationList = readSpecifications(specificationFile);
      } catch (IOException e) {
        String msg = "Unable to read specifications from file";
        if (Log.isLoggingOn()) {
          Log.logLine(msg + ": " + specificationFile);
        }
        throw new RandoopConditionError(msg, e);
      } catch (Throwable e) {
        throw new RandoopConditionError("Bad input", e);
      }

      for (OperationSpecification specification : specificationList) {
        AccessibleObject accessibleObject;
        Operation operation = specification.getOperation();
        if (operation == null) {
          continue;
        }
        try {
          accessibleObject = getReflectionObject(operation);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
          String msg =
              "Error loading operation for specification: "
                  + specification.toString()
                  + ". Exception: "
                  + e.getMessage();
          if (Log.isLoggingOn()) {
            Log.logLine(msg);
          }
          throw new RandoopConditionError(msg, e);
        }
        specMap.put(accessibleObject, specification);
      }
    }
    return new SpecificationCollection(specMap);
  }

  /**
   * Get the {@code java.lang.reflect.AccessibleObject} for the {@link Operation}.
   *
   * @param operation the {@link Operation}
   * @return the {@code java.lang.reflect.AccessibleObject} for the operation
   * @throws ClassNotFoundException if a type in the operation cannot be loaded
   * @throws NoSuchMethodException if there is method/constructor for the operation in the declaring
   *     class
   */
  private static AccessibleObject getReflectionObject(Operation operation)
      throws ClassNotFoundException, NoSuchMethodException {
    List<String> paramTypeNames = operation.getParameterTypeNames();
    Class<?>[] argTypes = new Class<?>[paramTypeNames.size()];
    for (int i = 0; i < argTypes.length; i++) {
      argTypes[i] = TypeNames.getTypeForName(paramTypeNames.get(i));
    }
    Class<?> declaringClass = TypeNames.getTypeForName(operation.getClassname());
    if (operation.isConstructor()) {
      return declaringClass.getConstructor(argTypes);
    } else {
      return declaringClass.getDeclaredMethod(operation.getName(), argTypes);
    }
  }

  /**
   * Reads a list of {@link OperationSpecification} objects from the given file.
   *
   * @param specificationFile the file of serialized {@link OperationSpecification} objects
   * @return the list of {@link OperationSpecification} object from the file
   * @throws IOException if there is an error reading the specification file
   */
  @SuppressWarnings("unchecked")
  private static List<OperationSpecification> readSpecifications(File specificationFile)
      throws IOException {
    List<OperationSpecification> specificationList = new ArrayList<>();
    Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    TypeToken<List<OperationSpecification>> typeToken =
        (new TypeToken<List<OperationSpecification>>() {});
    try (BufferedReader reader = new BufferedReader(new FileReader(specificationFile))) {
      specificationList.addAll(
          (List<OperationSpecification>) gson.fromJson(reader, typeToken.getType()));
    }
    return specificationList;
  }

  /**
   * Creates an {@link OperationConditions} object for the given
   * {java.lang.reflect.AccessibleObject}.
   *
   * @param accessibleObject the reflection object for a constructor or method
   * @return the {@link OperationConditions} for the specifications of the given method or
   *     constructor, {@code null} if there is none
   */
  public OperationConditions getOperationConditions(AccessibleObject accessibleObject) {
    OperationSpecification specification = specificationMap.get(accessibleObject);
    if (specification == null) {
      return null;
    }
    Declarations declarations = Declarations.create(accessibleObject, specification);

    // translate the ParamSpecifications to Condition objects
    List<Condition> paramConditions = new ArrayList<>();
    for (PreSpecification preSpecification : specification.getPreSpecifications()) {
      paramConditions.add(createCondition(preSpecification.getGuard(), declarations));
    }

    // translate the ReturnSpecifications to Condition-PostCondition pairs
    ArrayList<Pair<Condition, PostCondition>> returnConditions = new ArrayList<>();
    for (PostSpecification postSpecification : specification.getPostSpecifications()) {
      Condition preCondition = createCondition(postSpecification.getGuard(), declarations);
      PostCondition postCondition = createCondition(postSpecification.getProperty(), declarations);
      returnConditions.add(new Pair<>(preCondition, postCondition));
    }

    // translate the ThrowsSpecifications to Condition-ExpectedExceptionGenerator pairs
    LinkedHashMap<Condition, ExpectedExceptionGenerator> throwsConditions = new LinkedHashMap<>();
    for (ThrowsSpecification throwsSpecification : specification.getThrowsSpecifications()) {
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
      Condition guardCondition = createCondition(throwsSpecification.getGuard(), declarations);
      ExpectedExceptionGenerator generator =
          new ExpectedExceptionGenerator(
              exceptionType, "// " + throwsSpecification.getDescription());
      throwsConditions.put(guardCondition, generator);
    }

    return new OperationConditions(paramConditions, returnConditions, throwsConditions);
  }

  /**
   * Creates the {@link Condition} object for a given {@link Guard}.
   *
   * @param guard the guard to be converted to a {@link Condition}
   * @param declarations the declarations for the specification the guard belongs to
   * @return the {@link Condition} object for the given {@link Guard}
   */
  private Condition createCondition(Guard guard, Declarations declarations) {
    Method conditionMethod =
        ConditionMethodCreator.create(
            declarations.getPackageName(),
            declarations.getPreSignature(),
            guard.getConditionText(),
            compiler);
    String comment = guard.getDescription();
    String conditionText = declarations.replaceWithDummyVariables(guard.getConditionText());
    return new Condition(conditionMethod, comment, conditionText);
  }

  /**
   * Creates the {@link PostCondition} object for a given {@link Property}.
   *
   * @param property the property to be converted
   * @param declarations the declarations for the specification the guard belongs to
   * @return the {@link PostCondition} object for the given {@link Property}
   */
  private PostCondition createCondition(Property property, Declarations declarations) {
    Method conditionMethod =
        ConditionMethodCreator.create(
            declarations.getPackageName(),
            declarations.getPostSignature(),
            property.getConditionText(),
            compiler);
    String comment = property.getDescription();
    String conditionText = declarations.replaceWithDummyVariables(property.getConditionText());
    return new PostCondition(conditionMethod, comment, conditionText);
  }
}
