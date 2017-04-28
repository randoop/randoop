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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import plume.Pair;
import plume.UtilMDE;
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

  /** The map from method signatures to methods with that signature and specifications */
  private final Map<Signature, Set<Method>> signatureMap;

  /** The map from reflection object to overridden method with specification */
  private final Map<AccessibleObject, Set<Method>> parentMap;

  /** The compiler for creating conditionMethods */
  private final SequenceCompiler compiler;

  /** Map for memoizing conditions for specifications coverted by parent search */
  private Map<AccessibleObject, OperationConditions> conditionMap;

  /**
   * Creates a {@link SpecificationCollection} for the given specification map.
   *
   * @param specificationMap the map from reflection objects to {@link OperationSpecification}
   * @param signatureMap the map from signatures to methods with specifications
   * @param parentMap the map to overridden method with specification
   */
  SpecificationCollection(
      Map<AccessibleObject, OperationSpecification> specificationMap,
      Map<Signature, Set<Method>> signatureMap,
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
    Map<Signature, Set<Method>> sigMap = new LinkedHashMap<>();
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
        if (specification.getIdentifiers().hasNameConflict()) {
          String msg =
              String.format(
                  "Ignoring specification with identifer name conflict: %s",
                  specification.getOperation().toString());
          System.out.println(msg);
          if (Log.isLoggingOn()) {
            Log.logLine(msg);
          }
          continue;
        }
        try {
          accessibleObject = getReflectionObject(operation);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
          String msg =
              String.format(
                  "Could not load operation for specification: %n%s%n" + "Class not found: %s",
                  specification.getOperation().toString(), e.getMessage());

          if (Log.isLoggingOn()) {
            Log.logLine(msg);
          }
          throw new RandoopConditionError(msg, e);
        } catch (NoSuchMethodException e) {
          String msg =
              String.format(
                  "Could not load operation for specification: %n%s%n" + "No such method: %s",
                  specification.getOperation().toString(), e.getMessage());
          if (Log.isLoggingOn()) {
            Log.logLine(msg);
          }
          throw new RandoopConditionError(msg, e);
        }
        specMap.put(accessibleObject, specification);
        if (accessibleObject instanceof Method) {
          Signature signature = Signature.create((Method) accessibleObject);
          Set<Method> objectSet = sigMap.get(signature);
          if (objectSet == null) {
            objectSet = new HashSet<>();
          }
          objectSet.add((Method) accessibleObject);
          sigMap.put(signature, objectSet);
        }
      }
    }
    Map<AccessibleObject, Set<Method>> parentMap = buildParentMap(sigMap);
    return new SpecificationCollection(specMap, sigMap, parentMap);
  }

  private static Map<AccessibleObject, Set<Method>> buildParentMap(
      Map<Signature, Set<Method>> sigMap) {
    Map<AccessibleObject, Set<Method>> parentMap = new HashMap<>();
    for (Map.Entry<Signature, Set<Method>> entry : sigMap.entrySet()) {
      for (Method method : entry.getValue()) {
        Class<?> declaringClass = method.getDeclaringClass();
        Set<Method> parents = findParents(declaringClass, entry.getValue());
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
   *     class type.
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
      return declaringClass.getDeclaredConstructor(argTypes);
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
    OperationConditions conditions = conditionMap.get(accessibleObject);
    if (conditions != null) {
      return conditions;
    }

    OperationSpecification specification = specificationMap.get(accessibleObject);
    if (specification != null) {
      Declarations declarations = Declarations.create(accessibleObject, specification);
      conditions = createConditions(specification, declarations);
    } else {
      conditions = new OperationConditions();
    }

    if (accessibleObject instanceof Method) {
      Method method = (Method) accessibleObject;
      Set<Method> parents = parentMap.get(accessibleObject);
      if (parents == null) {
        Set<Method> sigSet = signatureMap.get(Signature.create(method));
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

  /**
   * Create the {@link OperationConditions} object for the given {@link OperationSpecification}
   * using the {@link Declarations}.
   *
   * @param specification the specification from which the conditions are to be created
   * @param declarations the declarations to be used in the conditions
   * @return the {@link OperationConditions} for the given specification
   */
  private OperationConditions createConditions(
      OperationSpecification specification, Declarations declarations) {
    OperationConditions conditions; // translate the ParamSpecifications to Condition objects
    List<Condition> paramConditions = new ArrayList<>();
    for (PreSpecification preSpecification : specification.getPreSpecifications()) {
      try {
        paramConditions.add(createCondition(preSpecification.getGuard(), declarations));
      } catch (RandoopConditionError e) {
        System.out.println("Warning: discarded uncompilable precondition: " + e.getMessage());
      }
    }

    // translate the ReturnSpecifications to Condition-PostCondition pairs
    ArrayList<Pair<Condition, PostCondition>> returnConditions = new ArrayList<>();
    for (PostSpecification postSpecification : specification.getPostSpecifications()) {
      try {
        Condition preCondition = createCondition(postSpecification.getGuard(), declarations);
        PostCondition postCondition =
            createCondition(postSpecification.getProperty(), declarations);
        returnConditions.add(new Pair<>(preCondition, postCondition));
      } catch (RandoopConditionError e) {
        System.out.println("Warning: discarding uncompilable postcondition: " + e.getMessage());
      }
    }

    // translate the ThrowsSpecifications to Condition-ExpectedExceptionGenerator pairs
    LinkedHashMap<Condition, ExpectedException> throwsConditions = new LinkedHashMap<>();
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
      try {
        Condition guardCondition = createCondition(throwsSpecification.getGuard(), declarations);
        ExpectedException exception =
            new ExpectedException(exceptionType, "// " + throwsSpecification.getDescription());
        throwsConditions.put(guardCondition, exception);
      } catch (RandoopConditionError e) {
        System.out.println("Warning: discarding uncompilable throws-condition: " + e.getMessage());
      }
    }

    conditions = new OperationConditions(paramConditions, returnConditions, throwsConditions);
    return conditions;
  }

  /**
   * Creates the {@link Condition} object for a given {@link Guard}.
   *
   * @param guard the guard to be converted to a {@link Condition}
   * @param declarations the declarations for the specification the guard belongs to
   * @return the {@link Condition} object for the given {@link Guard}
   */
  private Condition createCondition(Guard guard, Declarations declarations) {
    Method conditionMethod;
    try {
      conditionMethod =
          ConditionMethodCreator.create(
              declarations.getPackageName(),
              declarations.getPreSignature(),
              guard.getConditionText(),
              compiler);
    } catch (RandoopConditionError e) {
      throw new RandoopConditionError("guard condition " + guard.getConditionText(), e);
    }
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
    Method conditionMethod;
    try {
      conditionMethod =
          ConditionMethodCreator.create(
              declarations.getPackageName(),
              declarations.getPostSignature(),
              property.getConditionText(),
              compiler);
    } catch (RandoopConditionError e) {
      throw new RandoopConditionError("property condition " + property.getConditionText(), e);
    }
    String comment = property.getDescription();
    String conditionText = declarations.replaceWithDummyVariables(property.getConditionText());
    return new PostCondition(conditionMethod, comment, conditionText);
  }

  /**
   * Represents a method signature. Used to manage groups of methods likely to have
   * override/implementation relationships.
   */
  static class Signature {
    private final String name;
    private final Class<?>[] parameterTypes;

    Signature(String name, Class<?>[] parameterTypes) {
      this.name = name;
      this.parameterTypes = parameterTypes;
    }

    public static Signature create(Method method) {
      return new Signature(method.getName(), method.getParameterTypes());
    }

    @Override
    public boolean equals(Object object) {
      if (!(object instanceof Signature)) {
        return false;
      }
      Signature signature = (Signature) object;
      if (!this.name.equals(signature.name)) {
        return false;
      }
      if (this.parameterTypes.length != signature.parameterTypes.length) {
        return false;
      }
      for (int i = 0; i < parameterTypes.length; i++) {
        if (!this.parameterTypes[i].equals(signature.parameterTypes[i])) {
          return false;
        }
      }
      return true;
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, Arrays.hashCode(parameterTypes));
    }

    @Override
    public String toString() {
      List<String> typeNames = new ArrayList<>();
      for (Class<?> type : parameterTypes) {
        typeNames.add(type.getName());
      }
      return name + "(" + UtilMDE.join(typeNames, ",") + ")";
    }
  }
}
