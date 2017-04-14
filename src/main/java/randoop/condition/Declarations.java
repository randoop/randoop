package randoop.condition;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import plume.UtilMDE;
import randoop.condition.specification.Identifiers;
import randoop.condition.specification.OperationSpecification;

/** Represents the declaration of identifiers in an {@link OperationSpecification}. */
public class Declarations {
  private final Class<?> returnType;
  private final String returnName;
  private final Class<?> receiverType;
  private final String receiverName;
  private final Class<?>[] parameterTypes;
  private final List<String> parameterNames;
  private final String packageName;

  private Declarations(
      String packageName,
      Class<?> returnType,
      String returnName,
      Class<?> receiverType,
      String receiverName,
      Class<?>[] parameterTypes,
      List<String> parameterNames) {
    this.packageName = packageName;
    this.returnType = returnType;
    this.returnName = returnName;
    this.receiverType = receiverType;
    this.receiverName = receiverName;
    this.parameterTypes = parameterTypes;
    this.parameterNames = parameterNames;
  }

  public static Declarations create(AccessibleObject object, OperationSpecification specification) {
    Identifiers identifiers = specification.getIdentifiers();
    if (object instanceof Method) {
      Method method = (Method) object;
      return new Declarations(
          method.getDeclaringClass().getPackage().getName(),
          method.getReturnType(),
          identifiers.getReturnName(),
          method.getDeclaringClass(),
          identifiers.getReceiverName(),
          method.getParameterTypes(),
          identifiers.getParameterNames());
    } else if (object instanceof Constructor<?>) {
      Constructor<?> constructor = (Constructor<?>) object;
      return new Declarations(
          constructor.getDeclaringClass().getPackage().getName(),
          constructor.getDeclaringClass(),
          identifiers.getReturnName(),
          null,
          null,
          constructor.getParameterTypes(),
          identifiers.getParameterNames());
    }
    return null;
  }

  /**
   * Returns the String representing the parameter types for a pre-condition. Includes the receiver
   * and operation parameters, preserving the order in the {@code java.lang.reflect.Method} or
   * {@code java.lang.reflect.Constructor} classes.
   *
   * @return the String representation of the parameter types for a pre-condition
   */
  String getPreSignature() {
    List<String> paramList = buildPreParamList();
    return "(" + UtilMDE.join(paramList, ", ") + ")";
  }

  /**
   * Returns the {@code String} representing the parameter types for a post-condition with these
   * declarations. Includes the receiver, operation parameters and return type. The receiver and
   * parameters are listed in the order for the reflection object then followed by the declaration
   * for the return type.
   *
   * @return the {@code String} representation of the parameter types for a post-condition with
   *     these declarations.
   */
  String getPostSignature() {
    List<String> paramList = buildPreParamList();
    paramList.add(returnType.getCanonicalName() + " " + returnName);
    return "( " + UtilMDE.join(paramList, ", ") + ")";
  }

  private List<String> buildPreParamList() {
    List<String> paramList = new ArrayList<>();
    if (receiverType != null) {
      paramList.add(receiverType.getCanonicalName() + " " + receiverName);
    }
    for (int i = 0; i < parameterTypes.length; i++) {
      paramList.add(parameterTypes[i].getCanonicalName() + " " + parameterNames.get(i));
    }
    return paramList;
  }

  public String getPackageName() {
    return packageName;
  }

  String replaceWithDummyVariables(String conditionText) {
    int varIndex = 0;
    Map<String, String> replacements = new HashMap<>();
    if (receiverType != null) {
      replacements.put(receiverName, "x" + varIndex++);
    }
    for (String parameterName : parameterNames) {
      replacements.put(parameterName, "x" + varIndex++);
    }
    replacements.put(returnName, "x" + varIndex);

    // make sure that we are replacing from longer to shorter strings to avoid mangled replacement
    Set<String> names =
        new TreeSet<>(
            new Comparator<String>() {
              @Override
              public int compare(String o1, String o2) {
                if (o1.length() < o2.length()) {
                  return 1; // shorter last
                } else if (o1.length() > o2.length()) {
                  return -1; // longer first
                }
                return o1.compareTo(o2);
              }
            });
    names.addAll(replacements.keySet());

    for (String name : names) {
      conditionText = conditionText.replace(name, replacements.get(name));
    }

    return conditionText;
  }
}
