package randoop.field;

import java.lang.reflect.Field;

import randoop.operation.OperationParseException;
import randoop.types.GeneralType;

/**
 * Created by bjkeller on 3/29/16.
 */
public class FieldParser {


  public static AccessibleField parse(String descr, String classname, String fieldname) throws OperationParseException {
    String errorPrefix = "Error when parsing field " + descr + ".";
    GeneralType classType;
    try {
      classType = GeneralType.forName(classname);
    } catch (ClassNotFoundException e) {
      String msg = errorPrefix + " Class for field " + descr + " not found: " + e;
      throw new OperationParseException(msg);
    }

    String whitespacePattern = ".*\\s+.*";
    if (fieldname.matches(whitespacePattern)) {
      String msg = errorPrefix + " The field name "
              + fieldname
              + " has unexpected whitespace characters.";
      throw new OperationParseException(msg);
    }

    Field field = fieldForName(classType.getRuntimeClass(), fieldname);
    if (field == null) {
      String msg =  errorPrefix + " The field name \""
              + fieldname
              + "\" is not a field of the class "
              + "\""
              + classname
              + "\".";
      throw new OperationParseException(msg);
    }

    return new AccessibleField(field, classType);

  }

  /**
   * Searches the field list of a class for a field that has the given name.
   *
   * @param type
   *          - class object.
   * @param fieldName
   *          - field name for which to search the class.
   * @return field of the class with the given name.
   */
  private static Field fieldForName(Class<?> type, String fieldName) {
    for (Field f : type.getDeclaredFields()) {
      if (fieldName.equals(f.getName())) {
        return f;
      }
    }
    return null;
  }
}