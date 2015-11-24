package randoop.operation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import randoop.types.TypeNames;

/**
 * PublicFieldParser defines a parser to recognize a descriptor of a field in a 
 * string, and return an object representing the sort of field it is in the 
 * corresponding class.
 * The descriptor is expected to be in the form <tt>TYPE:FIELD-NAME</tt> where the 
 * <tt>FIELD-NAME</tt> is a fully qualified name of the form 
 * <i>package</i>.<i>class-name</i>.<i>name</i>.
 * The parser first checks that the descriptor is in the right syntactic form, 
 * then extracts the type and field-name and uses reflection to determine if 
 * the field belongs to the class, and whether the types match.
 * 
 */
public class PublicFieldParser {

  /**
   * Parses a type-field pair in a string, and returns the relevant object based 
   * on properties of the field determined by reflection.
   * 
   * @param s  a string in the form of "TYPE:FIELDNAME"
   * @return a reference to a PublicField object represented by the pair in the string.
   * @throws OperationParseException if input string is not expected format.
   * @see PublicField
   */
  public PublicField parse(String s) throws OperationParseException {
    if (s == null) {
      throw new IllegalArgumentException("s cannot be null");
    }
    int colonIdx = s.indexOf(':');
    if (colonIdx < 0) {
      String msg = "A field description must be of the form \"<type>:<field>\"" +
          " but description is \"" + s + "\".";
      throw new OperationParseException(msg);
    }

    String typeName = s.substring(0, colonIdx);
    String qualifiedFieldName = s.substring(colonIdx + 1);

    String errorPrefix = "Error when parsing type-value pair " + s +
        " for a field description of the form <type>:<field-name>.";

    if (typeName.isEmpty()) {
      String msg = errorPrefix + " No type given.";
      throw new OperationParseException(msg);
    }

    if (qualifiedFieldName.isEmpty()) {
      String msg = errorPrefix + " No field name given.";
      throw new OperationParseException(msg);
    }

    int dotPos = qualifiedFieldName.lastIndexOf('.');
    if (dotPos < 0) {
      String msg = errorPrefix + " No class name given in field name \"" + qualifiedFieldName + "\".";
      throw new OperationParseException(msg);
    }
    String className = qualifiedFieldName.substring(0,dotPos);
    String fieldName = qualifiedFieldName.substring(dotPos + 1);

    String whitespacePattern = ".*\\s+.*";
    if (typeName.matches(whitespacePattern)) {
      String msg = errorPrefix + " The type has unexpected whitespace characters.";
      throw new OperationParseException(msg);
    }
    if (qualifiedFieldName.matches(whitespacePattern)) {
      String msg = errorPrefix + " The field name has unexpected whitespace characters.";
      throw new OperationParseException(msg);
    }


    Class<?> type;
    try {
      type = TypeNames.getTypeForName(typeName);
    } catch (ClassNotFoundException e) {
      String msg = errorPrefix + " The type given \"" + typeName +"\" was not recognized.";
      throw new OperationParseException(msg);
    }


    if (className.isEmpty()) {
      String msg = errorPrefix + " The field name given \"" + qualifiedFieldName + "\" has no class name.";
      throw new OperationParseException(msg);
    }

    Class<?> classType;
    try {
      classType = TypeNames.getTypeForName(className);
    } catch (ClassNotFoundException e) {
      String msg = errorPrefix + " The class name \"" + className + "\" of the field name \"" +
          qualifiedFieldName + "\" was not recognized as a class.";
      throw new OperationParseException(msg);
    }

    Field field = fieldForName(classType, fieldName);
    if (field == null) {
      String msg = errorPrefix + " The field name given \"" + fieldName + "\" is not a field of the class " +
          "\"" + className + "\".";
      throw new OperationParseException(msg);
    }
    if (!field.getType().equals(type)) {
      String msg = errorPrefix + " The type of the field \"" + qualifiedFieldName + "\" is " + field.getType().toString() +
          ", but given as " + type.toString() + ".";
      throw new OperationParseException(msg);
    }

    return recognize(field);
  }

  /**
   * Create a {@code PublicField} object based on field given.
   * The field may be an instance field, a static field, or a static final field.
   * 
   * @param field  the {@link Field} object for which to create a wrapper object.
   * @return an object of a subclass of {@link PublicField}.
   */
  public static PublicField recognize(Field field) {
    PublicField pf = null;
    int mods = field.getModifiers();
    if (Modifier.isStatic(mods)) {
      if (Modifier.isFinal(mods)) {
        pf = new StaticFinalField(field);
      } else {
        pf = new StaticField(field);
      }
    } else {
      pf = new InstanceField(field);
    }
    assert pf != null;
    return pf;
  }

  /**
   * Searches the field list of a class for a field that has the given name.
   * 
   * @param type - class object.
   * @param fieldName - field name for which to search the class.
   * @return field of the class with the given name.
   */

  public static Field fieldForName(Class<?> type, String fieldName) {
    for (Field f : type.getDeclaredFields()) {
      if (fieldName.equals(f.getName())) {
        return f;
      }
    }
    return null;
  }

}
