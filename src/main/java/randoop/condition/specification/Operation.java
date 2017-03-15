package randoop.condition.specification;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Created by bjkeller on 3/14/17. */
public class Operation {

  static final String DEFAULT_RECEIVER_NAME = "receiver";
  static final String DEFAULT_RETURN_NAME = "result";
  private final String classname;
  private final String name;
  private final List<String> parameterTypes;
  private final List<String> parameterNames;
  private final String receiverName;
  private final String returnName;

  public Operation(
      String classname,
      String name,
      List<String> parameterTypes,
      List<String> parameterNames,
      String receiverName,
      String returnName) {
    this.classname = classname;
    this.name = name;
    this.parameterTypes = parameterTypes;
    this.parameterNames = parameterNames;
    this.receiverName = receiverName;
    this.returnName = returnName;
  }

  public Operation(
      String classname, String name, List<String> parameterTypes, List<String> parameterNames) {
    this(
        classname,
        name,
        parameterTypes,
        parameterNames,
        DEFAULT_RECEIVER_NAME,
        DEFAULT_RETURN_NAME);
  }

  public static Operation getOperation(
      AccessibleObject op, List<String> parameterNames, String receiverName, String resultName) {
    if (op instanceof Field) {
      return null;
    }

    String classname = null;
    String name = null;
    List<String> parameterTypes = null;
    if (op instanceof Method) {
      Method m = (Method) op;
      classname = m.getDeclaringClass().getCanonicalName();
      name = m.getName();
      parameterTypes = Operation.getTypeNames(m.getParameterTypes());
    } else if (op instanceof Constructor) {
      Constructor<?> constructor = (Constructor<?>) op;
      classname = constructor.getDeclaringClass().getCanonicalName();
      name = constructor.getName();
      parameterTypes = Operation.getTypeNames(constructor.getParameterTypes());
    }
    if (classname != null && name != null && parameterTypes != null) {
      return new Operation(
          classname, name, parameterTypes, parameterNames, receiverName, resultName);
    }
    return null;
  }

  public static Operation getOperation(AccessibleObject op, List<String> parameterNames) {
    return getOperation(op, parameterNames, DEFAULT_RECEIVER_NAME, DEFAULT_RETURN_NAME);
  }

  static List<String> getTypeNames(Class<?>[] classes) {
    List<String> parameterTypes = new ArrayList<>();
    for (Class<?> aClass : classes) {
      parameterTypes.add(aClass.getCanonicalName());
    }
    return parameterTypes;
  }
}
