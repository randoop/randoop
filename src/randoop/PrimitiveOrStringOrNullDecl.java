package randoop;

import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import plume.UtilMDE;
import randoop.util.PrimitiveTypes;
import randoop.util.Reflection;
import randoop.util.StringEscapeUtils;
import randoop.util.Util;
import randoop.main.GenInputsAbstract;

/**
 * Represents a primitive value (including Strings). This type of statement
 * doesn't actually transform any state, but it works out
 * nicely to represent primitives as statements.
 *
 * This decl info is for primitives, strings and nulls (of any type).
 */
public final class PrimitiveOrStringOrNullDecl implements StatementKind, Serializable {

  private static final long serialVersionUID = 20100429; 

  /** ID for parsing purposes (see StatementKinds.parse method) */
  public static final String ID = "prim";

  // State variables.
  private final Class<?> type;
  // This value is guaranteed to be null, a String, or a boxed primitive.
  private final Object value;

  private Object writeReplace() throws ObjectStreamException {
    return new SerializablePrimitiveOrStringOrNullDecl(type, value);
  }
  
  /**
   * Constructs a PrimitiveOrStringOrNullDeclInfo of type t and value o
   */
  public PrimitiveOrStringOrNullDecl(Class<?> t, Object o) {
    if (t == null)
      throw new IllegalArgumentException("t should not be null.");

    if (void.class.equals(t))
      throw new IllegalArgumentException("t should not be void.class.");

    if (t.isPrimitive()) {
      if (o == null)
        throw new IllegalArgumentException("primitive-like values cannot be null.");
      if (!PrimitiveTypes.boxedType(t).equals(o.getClass()))
        throw new IllegalArgumentException("o.getClass()=" + o.getClass() + ",t=" + t);
      if (! PrimitiveTypes.isBoxedOrPrimitiveOrStringType(o.getClass()))
        throw new IllegalArgumentException("o is not a primitive-like value.");
    } else if (t.equals(String.class)) {
      if (!PrimitiveTypes.stringLengthOK((String) o)) {
        throw new IllegalArgumentException("String too long, length = " + ((String) o).length());
      }
    } else {
      // if it's not primitive or string then must be null
      if (o != null) {
        throw new IllegalArgumentException("value must be null for non-primitive, non-string type " + t + " but was " + o);
      }
    }

    this.type = t;
    this.value = o;

  }

  /**
   * Indicates whether this PrimitiveOrStringOrNullDeclInfo is equal to o
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PrimitiveOrStringOrNullDecl))
      return false;
    if (this == o)
      return true;
    PrimitiveOrStringOrNullDecl other = (PrimitiveOrStringOrNullDecl) o;

    return this.type.equals(other.type)
    && Util.equalsWithNull(this.value, other.value);
  }

  /**
   * Returns a hash code value for this PrimitiveOrStringOrNullDeclInfo
   */
  @Override
  public int hashCode() {
    return this.type.hashCode() + (this.value == null ? 0 : this.value.hashCode());
  }

  /**
   * Returns string representation of this PrimitiveOrStringOrNullDeclInfo
   */
  @Override
  public String toString() {
    return toParseableString();
  }

  /**
   * Executes this statement, given the inputs to the statement. Returns
   * the results of execution as an ResultOrException object and can 
   * output results to specified PrintStream.
   */
  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {
    assert statementInput.length == 0;
    return new NormalExecution(this.value, 0);
  }

  /**
   * Extracts the input constraints for this PrimitiveOrStringOrNullDeclInfo
   * @return list of input constraints
   */
  public List<Class<?>> getInputTypes() {
    return Collections.emptyList();
  }

  public void appendCode(Variable newVar, List<Variable> inputVars, StringBuilder b) {

    if (type.isPrimitive()) {

      b.append(PrimitiveTypes.boxedType(type).getName());
      b.append(" ");
      b.append(newVar.getName());
      b.append(" = new ");
      b.append(PrimitiveTypes.boxedType(type).getName());
      b.append("(");
      b.append(PrimitiveTypes.toCodeString(getValue()));
      b.append(");");
      b.append(Globals.lineSep);

    } else {
      b.append(Reflection.getCompilableName(type));
      b.append(" ");
      b.append(newVar.getName());
      b.append(" = ");
      b.append(PrimitiveTypes.toCodeString(getValue()));
      b.append(";");
      b.append(Globals.lineSep);
    }
  }

  /**
   * Returns the value of this PrimitiveOrStringOrNullDeclInfo
   */
  public Object getValue() {
    return value;
  }

  /**
   * @return Returns the type.
   */
  public Class<?> getType() {
    return this.type;
  }

  /**
   * Returns constraint to represent new reference to this statement
   */
  public Class<?> getOutputType() {
    return this.type;
  }

  /**
   * Returns the appropriate PrimitiveOrStringOrNullDeclInfo representative of 
   * the specified class c.
   */
  public static PrimitiveOrStringOrNullDecl nullOrZeroDecl(Class<?> c) {
    if (String.class.equals(c))
      return new PrimitiveOrStringOrNullDecl(String.class, "");
    if (Character.TYPE.equals(c))
      return new PrimitiveOrStringOrNullDecl(Character.TYPE, 'a'); // TODO This is not null or zero...
    if (Byte.TYPE.equals(c))
      return new PrimitiveOrStringOrNullDecl(Byte.TYPE, (byte)0);
    if (Short.TYPE.equals(c))
      return new PrimitiveOrStringOrNullDecl(Short.TYPE, (short)0);
    if (Integer.TYPE.equals(c))
      return new PrimitiveOrStringOrNullDecl(Integer.TYPE, (Integer.valueOf(0)).intValue());
    if (Long.TYPE.equals(c))
      return new PrimitiveOrStringOrNullDecl(Long.TYPE, (Long.valueOf(0)).longValue());
    if (Float.TYPE.equals(c))
      return new PrimitiveOrStringOrNullDecl(Float.TYPE, (Float.valueOf(0)).floatValue());
    if (Double.TYPE.equals(c))
      return new PrimitiveOrStringOrNullDecl(Double.TYPE, (Double.valueOf(0)).doubleValue());
    if (Boolean.TYPE.equals(c))
      return new PrimitiveOrStringOrNullDecl(Boolean.TYPE,false);
    return new PrimitiveOrStringOrNullDecl(c, null);
  }

  public String toParseableString() {

    String valStr = null;
    if (value == null) {
      valStr = "null";
    } else {
      Class<?> valueClass = PrimitiveTypes.primitiveType(value.getClass());

      if (String.class.equals(valueClass)) {
        valStr = "\"" + StringEscapeUtils.escapeJava(value.toString()) + "\"";
      } else if (char.class.equals(valueClass)) {
        valStr = Integer.toHexString((Character) value);
      } else {     
        valStr = value.toString();
      }
    }

    return type.getName() + ":" + valStr;
  }

  /**
   * Returns the sequence corresponding to the given non-null primitive value.
   * 
   * Requires: o != null and o is a boxed primitive or String.
   */
  public static Sequence sequenceForPrimitive(Object o) {
    if (o == null) throw new IllegalArgumentException("o is null");
    Class<?> cls = o.getClass();    
    if (!PrimitiveTypes.isBoxedOrPrimitiveOrStringType(cls)) {
      throw new IllegalArgumentException("o is not a boxed primitive or String");
    }
    if (cls.equals(String.class) && !PrimitiveTypes.stringLengthOK((String)o)) {
      throw new IllegalArgumentException("o is a string of length > " + GenInputsAbstract.string_maxlen);
    }

    return Sequence.create(new PrimitiveOrStringOrNullDecl(PrimitiveTypes.primitiveType(cls), o));
  }

  /**
   * A string representing this primitive declaration. The string is of the form:
   * 
   * TYPE:VALUE
   * 
   * Where TYPE is the type of the primitive declaration, and VALUE is its value.
   * If VALUE is "null" then the value is null (not the String "null"). If
   * TYPE is "char" then (char)Integer.parseInt(VALUE, 16) yields the character value.
   * 
   * Examples:
   * 
   * String:null                  represents: String x = null
   * java.lang.String:""          represents: String x = "";
   * String:""                    represents: String x = "";
   * String:" "                   represents: String x = " ";
   * String:"\""                  represents: String x = "\"";
   * String:"\n"                  represents: String x = "\n";
   * String:"\u0000"              represents: String x = "\u0000";
   * java.lang.Object:null        represents: Object x = null;
   * [[Ljava.lang.Object;:null    represents: Object[][] = null;
   * int:0                        represents: int x = 0;
   * boolean:false                represents: boolean x = false;
   * char:20                      represents: char x = ' ';
   * 
   * Note that a string type can be given as both "String" or "java.lang.String".
   */
  public static PrimitiveOrStringOrNullDecl parse(String s) throws StatementKindParseException {
    if (s == null) throw new IllegalArgumentException("s cannot be null.");
    int colonIdx = s.indexOf(':');
    if (colonIdx == -1) {
      String msg = "A primitive value declaration description must be of the form "
        + "<type>:<value>" + " but the description \"" + s + "\" does not have this form.";
      throw new StatementKindParseException(msg);
    }
    // Extract type and value.
    String typeString = s.substring(0, colonIdx);
    String valString = s.substring(colonIdx+1);
    
    // Basic sanity check: no whitespace in type string.
    if (typeString.matches(".*\\s+.*")) {
      String msg = "Error when parsing type/value pair " + s + ". A primitive value declaration description must be of the form "
        + "<type>:<value>" + " but the <type> description \"" + s + "\" contains invalid whitespace characters.";
      throw new StatementKindParseException(msg);
    }
    
    // Convert "String" to "java.lang.String"
    if (typeString.equals("String")) {
      typeString = "java.lang.String";
    }
    
    Class<?> type = Reflection.classForName(typeString, true);
    if (type == null) {
      String msg = "Error when parsing type/value pair " + s + ". A primitive value declaration description must be of the form "
        + "<type>:<value>" + " but the <type> given (\"" + typeString + "\") was unrecognized.";
      throw new StatementKindParseException(msg);
    }
    Object value = null;

    if (type.equals(char.class)) {
      try {
        value = (char)Integer.parseInt(valString, 16);
      } catch (NumberFormatException e) {
        String msg = "Error when parsing type/value pair " + s + ". A primitive value declaration description must be of the form "
          + "<type>:<value>" + " but the <value> given (\"" + valString + "\") was not parseable.";
        throw new StatementKindParseException(msg);
      }
    } else if (type.equals(byte.class)) {
      try {
        value = Byte.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg = "Error when parsing type/value pair " + s + ". A primitive value declaration description must be of the form "
          + "<type>:<value>" + " but the <value> given (\"" + valString + "\") was not parseable.";
        throw new StatementKindParseException(msg);
      }
    } else if (type.equals(short.class)) {
      try {
        value = Short.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg = "Error when parsing type/value pair " + s + ". A primitive value declaration description must be of the form "
          + "<type>:<value>" + " but the <value> given (\"" + valString + "\") was not parseable.";
        throw new StatementKindParseException(msg);
      }
    } else if (type.equals(int.class)) {
      try {
        value = Integer.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg = "Error when parsing type/value pair " + s +  ". A primitive value declaration description must be of the form "
          + "<type>:<value>" + " but the <value> given (\"" + valString + "\") was not parseable.";
        throw new StatementKindParseException(msg);
      }
    } else if (type.equals(long.class)) {
      try {
        value = Long.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg = "Error when parsing type/value pair " + s +  ". A primitive value declaration description must be of the form "
          + "<type>:<value>" + " but the <value> given (\"" + valString + "\") was not parseable.";
        throw new StatementKindParseException(msg);
      }
    } else if (type.equals(float.class)) {
      try {
        value = Float.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg = "Error when parsing type/value pair " + s +  ". A primitive value declaration description must be of the form "
          + "<type>:<value>" + " but the <value> given (\"" + valString + "\") was not parseable.";
        throw new StatementKindParseException(msg);
      }
    } else if (type.equals(double.class)) {
      try {
        value = Double.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg = "Error when parsing type/value pair " + s +  ". A primitive value declaration description must be of the form "
          + "<type>:<value>" + " but the <value> given (\"" + valString + "\") was not parseable.";
        throw new StatementKindParseException(msg);
      }
    } else if (type.equals(boolean.class)) {
      if (valString.equals("true") || valString.equals("false")) {
        value = Boolean.valueOf(valString);        
      } else {
        String msg = "Error when parsing type/value pair " + s +  ". A primitive value declaration description must be of the form "
          + "<type>:<value>" + " but the <value> given (\"" + valString + "\") was not parseable.";
        throw new StatementKindParseException(msg);
      }
    } else if (type.equals(String.class)) {
      if (valString.equals("null")) {
        value = null;
      } else {
        value = valString;
        if (valString.charAt(0) != '"' || valString.charAt(valString.length() - 1) != '"') {
          String msg = "Error when parsing type/value pair " + s +  ". A String value declaration description must be of the form "
            + "java.lang.String:\"thestring\"" + " but the string given was not enclosed in quotation marks.";
          throw new StatementKindParseException(msg);
        }
        value = UtilMDE.unescapeNonJava(valString.substring(1, valString.length() - 1));
        if (!PrimitiveTypes.stringLengthOK((String)value)) {
          throw new StatementKindParseException("Error when parsing String; length is greater than " + GenInputsAbstract.string_maxlen);
        }
      }
    } else {
      if (valString.equals("null")) {
        value = null;
      } else {
        String msg = "Error when parsing type/value pair " + s +  ". A primitve value declaration description that is not a primitive value or a string must be of the form "
          + "<type>:null but the string given (\"" + valString + "\") was not of this form.";
        throw new StatementKindParseException(msg);
      }
    }

    return new PrimitiveOrStringOrNullDecl(type, value);
  }
}
