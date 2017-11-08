package randoop.condition;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import randoop.compile.SequenceClassLoader;
import randoop.compile.SequenceCompiler;
import randoop.condition.specification.OperationSignature;
import randoop.condition.specification.OperationSpecification;
import randoop.reflection.TypeNames;
import randoop.util.Log;
import randoop.util.MultiMap;

/**
 * A collection of {@link OperationSpecification} objects. Also a map from an {@link
 * AccessibleObject} reflection object to the {@link OperationConditions} for the corresponding
 * {@link randoop.operation.TypedClassOperation}.
 *
 * <p>The {@link SpecificationCollection} should be constructed from the specification input before
 * the {@link randoop.reflection.OperationModel} is created.
 *
 * <p>This class stores the {@link OperationSpecification} objects, and only constructs the
 * corresponding {@link OperationConditions} on demand. This lazy-strategy avoids building
 * conditions methods for specifications that are not used.
 */
public class SpecificationCollection {

  /** The map from reflection objects to the corresponding {@link OperationSpecification} */
  private final Map<AccessibleObject, OperationSpecification> specificationMap;

  /** The map from method signatures to methods with that signature and specifications */
  private final MultiMap<OperationSignature, Method> signatureMap;

  /** The map from reflection object to overridden method with specification */
  private final Map<AccessibleObject, Set<Method>> parentMap;

  /** The compiler for creating conditionMethods */
  private final SequenceCompiler compiler;

  /** Map for memoizing conditions for specifications converted by parent search */
  private Map<AccessibleObject, OperationConditions> conditionMap;

  /**
   * Creates a {@link SpecificationCollection} for the given specification map.
   *
   * <p>This constructor is used internally. It is only accessible to allow testing. Clients should
   * use {@link #create(List)} instead.
   *
   * @param specificationMap the map from reflection objects to {@link OperationSpecification}
   * @param signatureMap the multimap from a signature to methods with with the signature
   * @param parentMap the map from a method to methods that it it overrides and that have a
   *     specification
   */
  SpecificationCollection(
      Map<AccessibleObject, OperationSpecification> specificationMap,
      MultiMap<OperationSignature, Method> signatureMap,
      Map<AccessibleObject, Set<Method>> parentMap) {
    this.specificationMap = specificationMap;
    this.signatureMap = signatureMap;
    this.parentMap = parentMap;
    this.conditionMap = new HashMap<>();
    SequenceClassLoader sequenceClassLoader = new SequenceClassLoader(getClass().getClassLoader());
    List<String> options = new ArrayList<>();
    this.compiler = new SequenceCompiler(sequenceClassLoader, options);
  }

  /**
   * Creates a {@link SpecificationCollection} from the list of JSON specification files.
   *
   * @param specificationFiles the files of serialized specifications
   * @return the {@link SpecificationCollection} built from the serialized {@link
   *     OperationSpecification} objects.
   */
  public static SpecificationCollection create(List<File> specificationFiles) {
    if (specificationFiles == null) {
      return null;
    }
    MultiMap<OperationSignature, Method> signatureMap = new MultiMap<>();
    Map<AccessibleObject, OperationSpecification> specificationMap = new LinkedHashMap<>();
    for (File specificationFile : specificationFiles) {

      /*
       * Read the specifications from the file
       */
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
        OperationSignature operation = specification.getOperation();

        // Check for bad input
        if (operation == null) { // deserialization could result in null operation
          continue;
        }
        if (specification.getIdentifiers().hasDuplicatedName()) {
          String msg =
              String.format(
                  "Ignoring specification with identifier name conflict: %s",
                  specification.getOperation().toString());
          System.out.println(msg);
          if (Log.isLoggingOn()) {
            Log.logLine(msg);
          }
          continue;
        }

        AccessibleObject accessibleObject = getAccessibleObject(operation);
        specificationMap.put(accessibleObject, specification);
        if (accessibleObject instanceof Method) {
          OperationSignature signature = OperationSignature.of(accessibleObject);
          signatureMap.add(signature, (Method) accessibleObject);
        }
      }
    }
    Map<AccessibleObject, Set<Method>> parentMap = buildParentMap(signatureMap);
    return new SpecificationCollection(specificationMap, signatureMap, parentMap);
  }

  /**
   * Constructs a map between reflection objects representing override relationships among methods.
   *
   * @param signatureMap the map from a {@link OperationSignature} to methods with that signature
   * @return the map from an {@code AccessibleObject} to methods that it overrides
   */
  private static Map<AccessibleObject, Set<Method>> buildParentMap(
      MultiMap<OperationSignature, Method> signatureMap) {
    Map<AccessibleObject, Set<Method>> parentMap = new HashMap<>();
    for (OperationSignature signature : signatureMap.keySet()) {
      for (Method method : signatureMap.getValues(signature)) {
        Class<?> declaringClass = method.getDeclaringClass();
        Set<Method> parents = findParents(declaringClass, signatureMap.getValues(signature));
        if (!parents.isEmpty()) {
          parentMap.put(method, parents);
        }
      }
    }
    return parentMap;
  }

  /**
   * Finds the methods that are members of the stop set and belong to the supertypes of the given
   * class type.
   *
   * @param classType the class whose supertypes are searched
   * @param stopSet the set of methods
   * @return the set of methods with the signature and in the stop set from lowest supertypes of the
   *     class type
   */
  private static Set<Method> findParents(Class<?> classType, Set<Method> stopSet) {
    Set<Method> parents = new HashSet<>();
    if (classType != null) {
      for (Method method : stopSet) {
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass != classType && declaringClass.isAssignableFrom(classType)) {
          parents.add(method);
        }
      }
    }
    return parents;
  }

  /**
   * Get the {@code java.lang.reflect.AccessibleObject} for the {@link OperationSignature}.
   *
   * @param operation the {@link OperationSignature}
   * @return the {@code java.lang.reflect.AccessibleObject} for {@code operation}
   */
  private static AccessibleObject getAccessibleObject(OperationSignature operation) {
    if (operation.isValid()) {
      List<String> paramTypeNames = operation.getParameterTypeNames();
      Class<?>[] argTypes = new Class<?>[paramTypeNames.size()];
      try {
        for (int i = 0; i < argTypes.length; i++) {
          argTypes[i] = TypeNames.getTypeForName(paramTypeNames.get(i));
        }
        Class<?> declaringClass = TypeNames.getTypeForName(operation.getClassname());
        if (operation.isConstructor()) {
          return declaringClass.getDeclaredConstructor(argTypes);
        } else {
          return declaringClass.getDeclaredMethod(operation.getName(), argTypes);
        }
      } catch (ClassNotFoundException | NoClassDefFoundError e) {
        String msg =
            String.format(
                "Could not load specification operation: %n%s%n" + "Class not found: %s",
                operation.toString(), e.getMessage());
        if (Log.isLoggingOn()) {
          Log.logLine(msg);
        }
        throw new RandoopConditionError(msg, e);
      } catch (NoSuchMethodException e) {
        String msg =
            String.format(
                "Could not load specification operation: %n%s%n" + "No such method: %s",
                operation.toString(), e.getMessage());
        if (Log.isLoggingOn()) {
          Log.logLine(msg);
        }
        throw new RandoopConditionError(msg, e);
      } catch (ExceptionInInitializerError e) {
        String msg =
            String.format(
                "Could not load specification operation: %n%s%n"
                    + "Exception thrown by initializer",
                operation.toString());
        if (Log.isLoggingOn()) {
          Log.logLine(msg);
        }
        throw new RandoopConditionError(msg, e);
      }
    }
    return null;
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
    try (BufferedReader reader = Files.newBufferedReader(specificationFile.toPath(), UTF_8)) {
      specificationList.addAll(
          (List<OperationSpecification>) gson.fromJson(reader, typeToken.getType()));
    }
    return specificationList;
  }

  /**
   * Creates an {@link OperationConditions} object for the given
   * {java.lang.reflect.AccessibleObject}, from its specifications.
   *
   * <p>The translation makes the following conversions:
   *
   * <ul>
   *   <li>{@link randoop.condition.specification.Precondition} to {@link
   *       randoop.condition.BooleanExpression}
   *   <li>{@link randoop.condition.specification.Postcondition} to {@link
   *       randoop.condition.GuardPropertyPair}
   *   <li>{@link randoop.condition.specification.ThrowsCondition} to {@link
   *       randoop.condition.GuardThrowsPair}
   * </ul>
   *
   * @param accessibleObject the reflection object for a constructor or method
   * @return the {@link OperationConditions} for the specifications of the given method or
   *     constructor, {@code null} if there is none
   */
  public OperationConditions getOperationConditions(AccessibleObject accessibleObject) {

    // Check if accessibleObject already has an OperationConditions object
    OperationConditions conditions = conditionMap.get(accessibleObject);
    if (conditions != null) {
      return conditions;
    }

    // Otherwise, build a new one.
    OperationSpecification specification = specificationMap.get(accessibleObject);
    if (specification != null) {
      SpecificationTranslator translator =
          SpecificationTranslator.createTranslator(
              accessibleObject, specification.getIdentifiers(), compiler);
      conditions = translator.createConditions(specification);
    }

    if (conditions == null) {
      conditions = new OperationConditions();
    }

    if (accessibleObject instanceof Method) {
      Method method = (Method) accessibleObject;
      Set<Method> parents = parentMap.get(accessibleObject);
      if (parents == null) {
        Set<Method> sigSet = signatureMap.getValues(OperationSignature.of(method));
        if (sigSet != null) {
          parents = findParents(method.getDeclaringClass(), sigSet);
        }
      }
      if (parents != null) {
        for (Method parent : parents) {
          OperationConditions parentConditions = getOperationConditions(parent);
          conditions.addParent(parentConditions);
        }
      }
    }

    if (!conditions.isEmpty()) {
      conditionMap.put(accessibleObject, conditions);
    }
    return conditions;
  }
}
