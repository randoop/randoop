package randoop.operation;

import java.lang.reflect.Constructor;

/**
 * ConstructorParser provides static methods to write as well as parse 
 * a string representation of a constructor signature.
 */
public class ConstructorSignatures {

  /**
   * Parses a constructor signature as produced by 
   * {@link ConstructorSignatures#getSignatureString(Constructor)} 
   * and returns the corresponding reflective {@link Constructor} object.
   * 
   * @param signature a string representing a constructor signature.
   * @return reflective {@link Constructor} method corresponding to signature.
   * @throws OperationParseException if signature parameter does not match 
   *         expected format.
   */
  public static Constructor<?> getConstructorForSignatureString(String signature) throws OperationParseException {
    if (signature == null) {
      throw new IllegalArgumentException("signature may not be null");
    }
    
    int openPar = signature.indexOf('(');
    int closePar = signature.indexOf(')');
    // Verify only one open/close paren, and close paren is last char.
    assert openPar == signature.lastIndexOf('(') : signature;
    assert closePar == signature.lastIndexOf(')') : signature;
    assert closePar == signature.length() - 1 : signature;
    String clsAndMethod = signature.substring(0, openPar);
    int lastDot = clsAndMethod.lastIndexOf('.');
    // There should be at least one dot, separating class/method name.
    assert lastDot >= 0;
    String clsName = clsAndMethod.substring(0, lastDot);
    String methodName = clsAndMethod.substring(lastDot + 1);
    assert methodName.equals("<init>");
    String argsOneStr = signature.substring(openPar + 1, closePar);
    
    // Extract parameter types.
    Class<?>[] argTypes = TypeArgumentList.getTypeArgumentsForString(argsOneStr);
    
    Class<?> cls;
    try {
      cls = Class.forName(clsName);
      return cls.getDeclaredConstructor(argTypes);
    } catch (ClassNotFoundException e1) {
      throw new Error(e1);
    } catch (NoSuchMethodException e) {
      throw new Error(e);
    } catch (SecurityException e) {
      throw new Error(e);
    }
  }

  
/**
 * Generates a string representation of the signature of the constructor.
 * 
 * @param constructor for which signature string is to be generated.
 * @return string representing signature of the parameter constructor.
 */
  public static String getSignatureString(Constructor<?> constructor) {
    StringBuilder sb = new StringBuilder();
    sb.append(constructor.getName() + ".<init>(");
    Class<?>[] params = constructor.getParameterTypes();
    TypeArgumentList.getTypeArgumentString(sb, params);
    sb.append(")");
    return sb.toString();
  }

}
