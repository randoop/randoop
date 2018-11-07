package randoop.field;

import java.lang.reflect.Field;
import org.checkerframework.checker.signature.qual.ClassGetName;
import randoop.operation.OperationParseException;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;

/**
 * Recognizes a string representation of a field as part of parsing an {@link
 * randoop.operation.Operation} that is a {@link randoop.operation.FieldGet} or {@link
 * randoop.operation.FieldSet} operation.
 */
public class FieldParser {

  private FieldParser() {
    throw new Error("Do not instantiate");
  }

  /**
   * Recognizes a field from a string description, using class and field name tokens, and returns a
   * {@link AccessibleField} object.
   *
   * @param descr the full string description
   * @param classname the name of the field's declaring class
   * @param fieldname the name of the field
   * @return the {@link AccessibleField} for the given class and field name
   * @throws OperationParseException if either name is malformed or incorrect
   */
  public static AccessibleField parse(
      String descr, @ClassGetName String classname, String fieldname)
      throws OperationParseException {
    String errorPrefix = "Error when parsing field " + descr + ".";
    ClassOrInterfaceType classType;
    try {
      classType = (ClassOrInterfaceType) Type.forName(classname);
    } catch (ClassNotFoundException e) {
      String msg = errorPrefix + " Class for field " + descr + " not found: " + e.getMessage();
      throw new OperationParseException(msg);
    }

    String whitespacePattern = ".*\\s+.*";
    if (fieldname.matches(whitespacePattern)) {
      String msg =
          errorPrefix + " The field name " + fieldname + " has unexpected whitespace characters.";
      throw new OperationParseException(msg);
    }

    Field field = fieldForName(classType.getRuntimeClass(), fieldname);
    if (field == null) {
      String msg =
          errorPrefix
              + " The field name \""
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
   * @param type class object
   * @param fieldName field name for which to search the class
   * @return field of the class with the given name
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
