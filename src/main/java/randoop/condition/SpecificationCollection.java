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
import randoop.operation.OperationConditions;
import randoop.reflection.TypeNames;

/**
 * Represents a collection of preconditions and throws-conditions. Preconditions are represented by
 * {@link Condition} objects, while throws-conditions are represented by (condition, exception-type)
 * pairs.
 */
public class ConditionCollection {

  private final Map<AccessibleObject, OperationSpecification> specificationMap;

  private ConditionCollection(Map<AccessibleObject, OperationSpecification> specificationMap) {
    this.specificationMap = specificationMap;
  }

  public static ConditionCollection create(List<File> specificationFiles) {
    Map<AccessibleObject, OperationSpecification> specMap = new LinkedHashMap<>();
    for (File specificationFile : specificationFiles) {
      List<OperationSpecification> specificationList = null;
      try {
        specificationList = readSpecifications(specificationFile);
      } catch (IOException e) {
        e.printStackTrace(); //XXX add message
        continue;
      }
      for (OperationSpecification specification : specificationList) {
        AccessibleObject accessibleObject;
        try {
          accessibleObject = getReflectionObject(specification.getOperation());
        } catch (ClassNotFoundException | NoSuchMethodException e) {
          e.printStackTrace(); //XXX add message
          continue;
        }

        specMap.put(accessibleObject, specification);
      }
    }
    return null;
  }

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

  public OperationConditions getOperationConditions(AccessibleObject accessibleObject) {
    return null;
  }
}
