package randoop.condition.specification;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/** Created by bjkeller on 3/14/17. */
public class Operation {

  private final String classname;
  private final String name;
  private final Signature signature;

  public Operation(String classname, String name, Signature signature) {
    this.classname = classname;
    this.name = name;
    this.signature = signature;
  }

  public static Operation getOperation(AccessibleObject op, List<String> parameterNames) {
    if (op instanceof Field) {
      return null;
    }

    String classname = null;
    String name = null;
    Signature signature = null;
    if (op instanceof Method) {
      Method m = (Method) op;
      classname = m.getDeclaringClass().getCanonicalName();
      name = m.getName();
      signature = Signature.getSignature(m.getParameterTypes(), parameterNames);
    } else if (op instanceof Constructor) {
      Constructor<?> constructor = (Constructor<?>) op;
      classname = constructor.getDeclaringClass().getCanonicalName();
      name = constructor.getName();
      signature = Signature.getSignature(constructor.getParameterTypes(), parameterNames);
    }
    if (classname != null && name != null && signature != null) {
      return new Operation(classname, name, signature);
    }
    return null;
  }
}
