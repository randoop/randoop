package randoop.operation;

import java.lang.reflect.Constructor;

/**
 * ConstructorParser recognizes and returns a constructor from a string
 * representation of the constructor's signature.
 * 
 * @author bjkeller
 *
 */
public class ConstructorParser {

  public static Constructor<?> getConstructorForSignature(String signature) throws OperationParseException {
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
    Class<?>[] argTypes = ArgumentParser.recognizeArguments(argsOneStr);
    
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

  
  

}
