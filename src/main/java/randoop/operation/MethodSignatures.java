package randoop.operation;

import java.lang.reflect.Method;

import randoop.reflection.OperationParseVisitor;

/**
 * MethodSignatures provides static methods to write as well as parse a string
 * representation of a method signature.
 */
public class MethodSignatures {

  /**
   * Parses a method signature as produced by getSignature and returns the
   * corresponding reflective {@link Method} object.
   *
   * @param signature
   *          a string representing a method signature.
   * @return reflective {@link Method} object corresponding to signature.
   * @throws OperationParseException
   *           if signature parameter does not match expected format.
   */
  public static void getMethodForSignatureString(String signature, OperationParseVisitor visitor)
      throws OperationParseException {
    if (signature == null) {
      throw new IllegalArgumentException("signature may not be null");
    }

    int openParPos = signature.indexOf('(');
    int closeParPos = signature.indexOf(')');
    // Verify only one open/close paren, and close paren is last char.
    assert openParPos == signature.lastIndexOf('(') : signature;
    assert closeParPos == signature.lastIndexOf(')') : signature;
    assert closeParPos == signature.length() - 1 : signature;
    String prefix = signature.substring(0, openParPos);
    int lastDot = prefix.lastIndexOf('.');
    assert lastDot >= 0 : "there should be at least one period";
    String classname = prefix.substring(0, lastDot);
    String opname = prefix.substring(lastDot + 1);
    String arguments = signature.substring(openParPos + 1, closeParPos);

    visitor.visitMethod(classname, opname, arguments);
  }

  /**
   * Generates a string representation of the signature of the method.
   *
   * @param method
   *          the method.
   * @return string representing the method signature.
   */
  public static String getSignatureString(Method method) {
    StringBuilder sb = new StringBuilder();
    sb.append(method.getDeclaringClass().getName() + ".");
    sb.append(method.getName() + "(");
    Class<?>[] params = method.getParameterTypes();
    TypeArguments.getTypeArgumentString(sb, params);
    sb.append(")");
    return sb.toString();
  }
}
