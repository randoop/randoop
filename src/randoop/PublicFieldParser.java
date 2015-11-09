package randoop;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import randoop.util.Reflection;

/**
 * PublicFieldParser defines a parser to recognize a descriptor of a field in a string,
 * and return an object representing the sort of field it is in the corresponding class.
 * The descriptor is expected to be in the form <type>:<field-name> where the <field-name>
 * is a fully qualified name of the form <package>.<class-name>.<name>.
 * The parser first checks that the descriptor is in the right syntactic form, then extracts
 * the type and field-name and uses reflection to determine if the field belongs to the class,
 * and the types match.
 * 
 * @author bjkeller
 *
 */
public class PublicFieldParser {

  /**
   * parse recognizes a type-field pair in a string, and
   * returns the relevant object based on properties of the field
   * determined by reflection.
   * 
   * @param s - a string in the form of "<type>:<field-name>"
   * @return a reference to a PublicField object represented by the pair in the string.
   * @throws StatementKindParseException
   * @see PublicField
   */
  public PublicField parse(String s) throws StatementKindParseException {
    if (s == null) {
      throw new IllegalArgumentException("s cannot be null");
    }
    int colonIdx = s.indexOf(':');
    if (colonIdx < 0) {
      String msg = "A field description must be of the form \"<type>:<field>\"" +
          " but description is \"" + s + "\".";
      throw new StatementKindParseException(msg);
    }

    String typeName = s.substring(0, colonIdx);
    String qualifiedFieldName = s.substring(colonIdx + 1);

    String errorPrefix = "Error when parsing type-value pair " + s +
        " for a field description of the form <type>:<field-name>.";

    if (typeName.isEmpty()) {
      String msg = errorPrefix + " No type given.";
      throw new StatementKindParseException(msg);
    }

    if (qualifiedFieldName.isEmpty()) {
      String msg = errorPrefix + " No field name given.";
      throw new StatementKindParseException(msg);
    }

    int dotPos = qualifiedFieldName.lastIndexOf('.');
    if (dotPos < 0) {
      String msg = errorPrefix + " No class name given in field name \"" + qualifiedFieldName + "\".";
      throw new StatementKindParseException(msg);
    }
    String className = qualifiedFieldName.substring(0,dotPos);
    String fieldName = qualifiedFieldName.substring(dotPos + 1);

    String whitespacePattern = ".*\\s+.*";
    if (typeName.matches(whitespacePattern)) {
      String msg = errorPrefix + " The type has unexpected whitespace characters.";
      throw new StatementKindParseException(msg);
    }
    if (qualifiedFieldName.matches(whitespacePattern)) {
      String msg = errorPrefix + " The field name has unexpected whitespace characters.";
      throw new StatementKindParseException(msg);
    }


    Class<?> type = Reflection.classForName(typeName,true);
    if (type == null) {
      String msg = errorPrefix + " The type given \"" + typeName +"\" was not recognized.";
      throw new StatementKindParseException(msg);
    }


    if (className.isEmpty()) {
      String msg = errorPrefix + " The field name given \"" + qualifiedFieldName + "\" has no class name.";
      throw new StatementKindParseException(msg);
    }

    Class<?> classType = Reflection.classForName(className,true);
    if (classType == null) {
      String msg = errorPrefix + " The class name \"" + className + "\" of the field name \"" +
          qualifiedFieldName + "\" was not recognized as a class.";
      throw new StatementKindParseException(msg);
    }

    Field field = fieldFor(classType, fieldName);
    if (field == null) {
      String msg = errorPrefix + " The field name given \"" + fieldName + "\" is not a field of the class " +
          "\"" + className + "\".";
      throw new StatementKindParseException(msg);
    }
    if (!field.getType().equals(type)) {
      String msg = errorPrefix + " The type of the field \"" + qualifiedFieldName + "\" is " + field.getType().toGenericString() +
          ", but given as " + type.toString() + ".";
      throw new StatementKindParseException(msg);
    }

    return recognize(field);
  }

  /**
   * recognize determines what sort of field is given.
   * Looking for a field to be an instance field, 
   * a static field, or a static final field.
   * 
   * @param field
   * @return an object of a subclass of PublicField.
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
   * fieldFor searches the field list of a class for a field that has the given name.
   * 
   * @param type - class object.
   * @param fieldName - field name for which to search the class.
   * @return field of the class with the given name.
   */

  public static Field fieldFor(Class<?> type, String fieldName) {
    for (Field f : type.getDeclaredFields()) {
      if (fieldName.equals(f.getName())) {
        return f;
      }
    }
    return null;
  }

}
