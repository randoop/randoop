package randoop.condition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import randoop.condition.specification.Operation;
import randoop.condition.specification.OperationSpecification;
import randoop.reflection.TypeNames;
import randoop.util.Log;

/**
 * Represents a collection of preconditions and throws-conditions. Preconditions are represented by
 * {@link Condition} objects, while throws-conditions are represented by (condition, exception-type)
 * pairs.
 */
public class SpecificationCollection {

  /** The map from reflection objects to the corresponding {@link OperationSpecification} */
  private final Map<AccessibleObject, OperationSpecification> specificationMap;

  /**
   * Creates a {@link SpecificationCollection} for the given specification map.
   *
   * @param specificationMap the map from reflection objects to {@link OperationSpecification}
   */
  private SpecificationCollection(Map<AccessibleObject, OperationSpecification> specificationMap) {
    this.specificationMap = specificationMap;
  }

  /**
   * Creates a {@link SpecificationCollection} from the list of files of serialized specifications.
   *
   * @param specificationFiles the files of serialized specifications
   * @return the {@link SpecificationCollection} built from the serialized {@link
   *     OperationSpecification} objects.
   */
  public static SpecificationCollection create(List<File> specificationFiles) {
    Map<AccessibleObject, OperationSpecification> specMap = new LinkedHashMap<>();
    for (File specificationFile : specificationFiles) {
      List<OperationSpecification> specificationList;
      try {
        specificationList = readSpecifications(specificationFile);
      } catch (IOException e) {
        String msg =
            "Unable to read specifications from file "
                + specificationFile
                + ". Exception: "
                + e.getMessage();
        if (Log.isLoggingOn()) {
          Log.logLine(msg);
        }
        continue;
      }
      for (OperationSpecification specification : specificationList) {
        AccessibleObject accessibleObject;
        try {
          accessibleObject = getReflectionObject(specification.getOperation());
        } catch (ClassNotFoundException | NoSuchMethodException e) {
          String msg =
              "Error loading operation for specification: "
                  + specification.toString()
                  + ". Exception: "
                  + e.getMessage();
          if (Log.isLoggingOn()) {
            Log.logLine(msg);
          }
          continue;
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
    return OperationConditions.getOperationConditions(specification, declarations);
  }
}
