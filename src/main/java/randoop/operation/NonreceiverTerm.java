package randoop.operation;

import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import plume.UtilMDE;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.PrimitiveTypes;
import randoop.types.TypeNames;
import randoop.util.StringEscapeUtils;
import randoop.util.Util;

/**
 * Represents a value that either cannot (primitive or null values), or we don't
 * care to have (String) be a receiver for a method call as an {@link Operation}
 * .
 *
 * As an {@link Operation} a value v of type T is formally represented by an
 * operation v : [] &rarr; T, with no input types, and the type of the value as
 * the output type. This kind of operation is a <i>ground</i> term &mdash; it
 * requires no inputs.
 *
 * The execution of this {@link Operation} simply returns the value.
 */
public final class NonreceiverTerm extends AbstractOperation implements Operation, Serializable {

  private static final long serialVersionUID = 20100429;

  /**
   * ID for parsing purposes.
   *
   * @see OperationParser#getId(Operation)
   */
  public static final String ID = "prim";

  // State variables.
  private final Class<?> type;
  // This value is guaranteed to be null, a String, or a boxed primitive.
  private final Object value;

  /**
   * Converts this object to a form that can be serialized.
   *
   * @return serializable form of this object
   * @see SerializableNonreceiverTerm
   */
  private Object writeReplace() throws ObjectStreamException {
    return new SerializableNonreceiverTerm(type, value);
  }

  /**
   * Constructs a NonreceiverTerm with type t and value o.
   *
   * @param t
   *          the type of the term
   * @param o
   *          the value of the term
   */
  public NonreceiverTerm(Class<?> t, Object o) {
    if (t == null) throw new IllegalArgumentException("t should not be null.");

    if (void.class.equals(t)) throw new IllegalArgumentException("t should not be void.class.");

    if (t.isPrimitive()) {
      if (o == null) throw new IllegalArgumentException("primitive-like values cannot be null.");
      if (!PrimitiveTypes.toBoxedType(t).equals(o.getClass()))
        throw new IllegalArgumentException("o.getClass()=" + o.getClass() + ",t=" + t);
      if (!PrimitiveTypes.isBoxedOrPrimitiveOrStringType(o.getClass()))
        throw new IllegalArgumentException("o is not a primitive-like value.");
    } else if (t.equals(String.class)) {
      if (!PrimitiveTypes.stringLengthOK((String) o)) {
        throw new IllegalArgumentException("String too long, length = " + ((String) o).length());
      }
    } else {
      // if it's not primitive or string then must be null
      if (o != null) {
        throw new IllegalArgumentException(
            "value must be null for non-primitive, non-string type " + t + " but was " + o);
      }
    }

    this.type = t;
    this.value = o;
  }

  /**
   * Indicates whether this object is equal to o
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NonreceiverTerm)) return false;
    if (this == o) return true;
    NonreceiverTerm other = (NonreceiverTerm) o;

    return this.type.equals(other.type) && Util.equalsWithNull(this.value, other.value);
  }

  /**
   * Returns a hash code value for this NonreceiverTerm
   */
  @Override
  public int hashCode() {
    return this.type.hashCode() + (this.value == null ? 0 : this.value.hashCode());
  }

  /**
   * Returns string representation of this NonreceiverTerm
   */
  @Override
  public String toString() {
    return toParseableString();
  }

  /**
   * {@inheritDoc}
   *
   * @return {@link NormalExecution} object enclosing value of this non-receiver
   *         term.
   */
  @Override
  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {
    assert statementInput.length == 0;
    return new NormalExecution(this.value, 0);
  }

  /**
   * {@inheritDoc}
   *
   * @return empty list.
   */
  @Override
  public List<Class<?>> getInputTypes() {
    return Collections.emptyList();
  }

  /**
   * {@inheritDoc} For NonreceiverTerm, simply adds a code representation of the
   * value to the string builder. Note: this does not explicitly box primitive
   * values.
   *
   * @see Operation#appendCode(List, StringBuilder)
   *
   * @param inputVars
   *          ignored
   * @param b
   *          {@link StringBuilder} to which string representation is appended.
   *
   */
  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    b.append(PrimitiveTypes.toCodeString(getValue()));
  }

  /**
   * {@inheritDoc}
   *
   * @return value of this {@link NonreceiverTerm}
   */
  @Override
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
   * {@inheritDoc}
   *
   * @return type of value.
   */
  @Override
  public Class<?> getOutputType() {
    return this.type;
  }

  /**
   * Returns a NonreceiverTerm holding the zero value for the specified class c.
   * In the case of characters there is no natural zero, so the value 'a' is
   * used.
   *
   * @param c
   *          the type of value desired.
   * @return a {@link NonreceiverTerm} with a canonical representative of the
   *         given type.
   */
  public static NonreceiverTerm createNullOrZeroTerm(Class<?> c) {
    if (String.class.equals(c)) return new NonreceiverTerm(String.class, "");
    if (Character.TYPE.equals(c))
      return new NonreceiverTerm(Character.TYPE, 'a'); // TODO This is not null
    // or zero...
    if (Byte.TYPE.equals(c)) return new NonreceiverTerm(Byte.TYPE, (byte) 0);
    if (Short.TYPE.equals(c)) return new NonreceiverTerm(Short.TYPE, (short) 0);
    if (Integer.TYPE.equals(c))
      return new NonreceiverTerm(Integer.TYPE, (Integer.valueOf(0)).intValue());
    if (Long.TYPE.equals(c)) return new NonreceiverTerm(Long.TYPE, (Long.valueOf(0)).longValue());
    if (Float.TYPE.equals(c))
      return new NonreceiverTerm(Float.TYPE, (Float.valueOf(0)).floatValue());
    if (Double.TYPE.equals(c))
      return new NonreceiverTerm(Double.TYPE, (Double.valueOf(0)).doubleValue());
    if (Boolean.TYPE.equals(c)) return new NonreceiverTerm(Boolean.TYPE, false);
    return new NonreceiverTerm(c, null);
  }

  /**
   * {@inheritDoc} Returns a string representing this primitive declaration. The
   * string is of the form:<br>
   *
   * <code>TYPE:VALUE</code><br>
   *
   * Where TYPE is the type of the primitive declaration, and VALUE is its
   * value. If VALUE is "null" then the value is null (not the String "null").
   * If TYPE is "char" then (char)Integer.parseInt(VALUE, 16) yields the
   * character value.
   * <p>
   * Examples:
   *
   * <pre>
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
   * </pre>
   *
   * Note that a string type can be given as both "String" or
   * "java.lang.String".
   *
   * @return string representation of primitive, String or null value.
   */
  @Override
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
   * Creates a sequence corresponding to the given non-null primitive value.
   *
   * @param o
   *          non-null reference to a primitive or String value
   * @return a {@link Sequence} consisting of a statement created with the
   *         object.
   */
  public static Sequence createSequenceForPrimitive(Object o) {
    if (o == null) throw new IllegalArgumentException("o is null");
    Class<?> cls = o.getClass();
    if (!PrimitiveTypes.isBoxedOrPrimitiveOrStringType(cls)) {
      throw new IllegalArgumentException("o is not a boxed primitive or String");
    }
    if (cls.equals(String.class) && !PrimitiveTypes.stringLengthOK((String) o)) {
      throw new IllegalArgumentException(
          "o is a string of length > " + GenInputsAbstract.string_maxlen);
    }

    return Sequence.create(new NonreceiverTerm(PrimitiveTypes.primitiveType(cls), o));
  }

  /**
   * Parse a non-receiver value in a string in the form generated by
   * {@link NonreceiverTerm#toParseableString()}.
   *
   * @param s
   *          a string representing a value of a non-receiver type.
   * @return a {@link NonreceiverTerm} object containing the recognized value.
   * @throws OperationParseException
   *           if string does not represent valid object.
   */
  public static NonreceiverTerm parse(String s) throws OperationParseException {
    if (s == null) throw new IllegalArgumentException("s cannot be null.");
    int colonIdx = s.indexOf(':');
    if (colonIdx == -1) {
      String msg =
          "A primitive value declaration description must be of the form "
              + "<type>:<value>"
              + " but the description \""
              + s
              + "\" does not have this form.";
      throw new OperationParseException(msg);
    }
    // Extract type and value.
    String typeString = s.substring(0, colonIdx);
    String valString = s.substring(colonIdx + 1);

    // Basic sanity check: no whitespace in type string.
    if (typeString.matches(".*\\s+.*")) {
      String msg =
          "Error when parsing type/value pair "
              + s
              + ". A primitive value declaration description must be of the form "
              + "<type>:<value>"
              + " but the <type> description \""
              + s
              + "\" contains invalid whitespace characters.";
      throw new OperationParseException(msg);
    }

    // Convert "String" to "java.lang.String"
    if (typeString.equals("String")) {
      typeString = "java.lang.String";
    }

    Class<?> type;
    try {
      type = TypeNames.getTypeForName(typeString);
    } catch (ClassNotFoundException e1) {
      String msg =
          "Error when parsing type/value pair "
              + s
              + ". A primitive value declaration description must be of the form "
              + "<type>:<value>"
              + " but the <type> given (\""
              + typeString
              + "\") was unrecognized.";
      throw new OperationParseException(msg);
    }

    Object value;
    if (type.equals(char.class)) {
      try {
        value = (char) Integer.parseInt(valString, 16);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(byte.class)) {
      try {
        value = Byte.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(short.class)) {
      try {
        value = Short.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(int.class)) {
      try {
        value = Integer.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(long.class)) {
      try {
        value = Long.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(float.class)) {
      try {
        value = Float.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(double.class)) {
      try {
        value = Double.valueOf(valString);
      } catch (NumberFormatException e) {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(boolean.class)) {
      if (valString.equals("true") || valString.equals("false")) {
        value = Boolean.valueOf(valString);
      } else {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description must be of the form "
                + "<type>:<value>"
                + " but the <value> given (\""
                + valString
                + "\") was not parseable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(String.class)) {
      if (valString.equals("null")) {
        value = null;
      } else {
        value = valString;
        if (valString.charAt(0) != '"' || valString.charAt(valString.length() - 1) != '"') {
          String msg =
              "Error when parsing type/value pair "
                  + s
                  + ". A String value declaration description must be of the form "
                  + "java.lang.String:\"thestring\""
                  + " but the string given was not enclosed in quotation marks.";
          throw new OperationParseException(msg);
        }
        value = UtilMDE.unescapeNonJava(valString.substring(1, valString.length() - 1));
        if (!PrimitiveTypes.stringLengthOK((String) value)) {
          throw new OperationParseException(
              "Error when parsing String; length is greater than "
                  + GenInputsAbstract.string_maxlen);
        }
      }
    } else {
      if (valString.equals("null")) {
        value = null;
      } else {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitve value declaration description that is not a primitive value or a string must be of the form "
                + "<type>:null but the string given (\""
                + valString
                + "\") was not of this form.";
        throw new OperationParseException(msg);
      }
    }

    return new NonreceiverTerm(type, value);
  }

  /**
   * {@inheritDoc}
   *
   * @return type of value.
   */
  @Override
  public Class<?> getDeclaringClass() {
    return type;
  }

  /**
   * {@inheritDoc}
   *
   * @return true, since all of objects are non-receivers.
   */
  @Override
  public boolean isNonreceivingValue() {
    return true;
  }
}
