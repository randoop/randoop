package randoop.operation;

import java.lang.reflect.Constructor;

import randoop.reflection.OperationParseVisitor;

/**
 * ConstructorSignatures provides static methods to write as well as parse a
 * string representation of a constructor signature.
 */
public class ConstructorSignatures {

  /**
   * Parses a constructor signature as produced by
   * {@link ConstructorSignatures#getSignatureString(Constructor)} and returns
   * the corresponding reflective {@link Constructor} object.
   *
   * @param signature
   *          a string representing a constructor signature.
   * @return reflective {@link Constructor} method corresponding to signature.
   * @throws OperationParseException
   *           if signature parameter does not match expected format.
   * @throws Error
   *           if there is a reflection exception: the class name is not valid,
   *           there is no method with the name, or there is a security
   *           exception.
   */
  public static void getConstructorForSignatureString(String signature, OperationParseVisitor visitor)
      throws OperationParseException {
    if (signature == null) {
      throw new IllegalArgumentException("signature may not be null");
    }

    int openParPos = signature.indexOf('(');
    int closeParPos = signature.indexOf(')');

    String prefix = signature.substring(0, openParPos);
    int lastDotPos = prefix.lastIndexOf('.');

    assert lastDotPos >= 0;
    String classname = prefix.substring(0, lastDotPos);
    String opname = prefix.substring(lastDotPos + 1);
    assert opname.equals("<init>") : "expected init, saw " + opname;
    String arguments = signature.substring(openParPos + 1, closeParPos);

    visitor.visitConstructor(classname, opname, arguments);
  }

  /**
   * Generates a string representation of the signature of the constructor.
   *
   * @param constructor
   *          for which signature string is to be generated.
   * @return string representing signature of the parameter constructor.
   */
  public static String getSignatureString(Constructor<?> constructor) {
    StringBuilder sb = new StringBuilder();
    sb.append(constructor.getName() + ".<init>(");
    Class<?>[] params = constructor.getParameterTypes();
    TypeArguments.getTypeArgumentString(sb, params);
    sb.append(")");
    return sb.toString();
  }
}
