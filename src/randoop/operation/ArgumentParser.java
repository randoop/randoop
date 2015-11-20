package randoop.operation;

import randoop.types.TypeNames;

/**
 * ArgumentParser recognizes method/constructor arguments in a string
 * and returns the list of argument types as Class<?> objects.
 * @see MethodSignatures
 * @see ConstructorParser
 * 
 * @author bjkeller
 *
 */
public class ArgumentParser {
  public static Class<?>[] recognizeArguments(String argsOneStr) throws OperationParseException {
    Class<?>[] argTypes = new Class<?>[0];
    if (argsOneStr.trim().length() > 0) {
      String[] argsStrs = argsOneStr.split(",");
      argTypes = new Class<?>[argsStrs.length];
      for (int i = 0 ; i < argsStrs.length ; i++) {
        String typeName = argsStrs[i].trim();
        
        Class<?> c;
        try {
          c = TypeNames.recognizeType(typeName);
        } catch (ClassNotFoundException e) {
          throw new OperationParseException("Argument type \"" + typeName + "\" not recognized in arguments " + argsOneStr);
        }
        
        argTypes[i] = c;
      }
    }
    return argTypes;
  }

}
