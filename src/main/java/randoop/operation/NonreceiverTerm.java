package randoop.operation;

import java.util.List;
import java.util.Objects;
import org.plumelib.util.StringsPlume;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.StringTooLongException;
import randoop.sequence.Value;
import randoop.sequence.Variable;
import randoop.types.JavaTypes;
import randoop.types.NonParameterizedType;
import randoop.types.PrimitiveTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * Represents a value that either cannot (primitive or null values), or we don't care to have
 * (String, Class) be a receiver for a method call as an {@link Operation}.
 *
 * <p>As an {@link Operation}, a value v of type T is formally represented by an operation v : []
 * &rarr; T, with no input types, and the type of the value as the output type. This kind of
 * operation is a <i>ground</i> term &mdash; it requires no inputs.
 *
 * <p>The execution of this {@link Operation} simply returns the value.
 */
public final class NonreceiverTerm extends CallableOperation {

  /** The {@link Type} of this non-receiver term. */
  private final Type type;

  /** The value of this non-receiver term. Must be null, a String, a boxed primitive, or a Class. */
  private final Object value;

  /**
   * Constructs a NonreceiverTerm with type t and value o.
   *
   * @param type the type of the term
   * @param value the value of the term
   */
  public NonreceiverTerm(Type type, Object value) {
    if (type == null) {
      throw new IllegalArgumentException("type should not be null.");
    }

    if (type.isVoid()) {
      throw new IllegalArgumentException("type should not be void.");
    }

    if (type.isPrimitive() || type.isBoxedPrimitive()) {
      if (value == null) {
        if (type.isPrimitive()) {
          throw new IllegalArgumentException("primitive-like values cannot be null.");
        }
      } else {
        if (!type.isAssignableFromTypeOf(value)) {
          throw new IllegalArgumentException(
              "value.getClass()=" + value.getClass() + ",type=" + type);
        }
        if (!NonreceiverTerm.isNonreceiverType(value.getClass())) {
          throw new IllegalArgumentException("value is not a primitive-like value.");
        }
      }
    } else if (type.isString()) {
      String s = (String) value;
      if (value != null && !Value.escapedStringLengthOk(s)) {
        throw new StringTooLongException(s);
      }
    } else if (!type.equals(JavaTypes.CLASS_TYPE)) {
      // If it's not a primitive, string, or Class value, then it must be null.
      if (value != null) {
        throw new IllegalArgumentException(
            "value must be null for type " + type + " but was " + value);
      }
    }

    this.type = type;
    this.value = value;
  }

  /**
   * Determines whether the given {@code Class<?>} is the type of a non-receiver term.
   *
   * @param c the {@code Class<?>} object
   * @return true iff the type is primitive, boxed primitive, {@code String}, or {@code Class}
   */
  public static boolean isNonreceiverType(Class<?> c) {
    return c.isPrimitive()
        || c.equals(String.class)
        || PrimitiveTypes.isBoxedPrimitive(c)
        || c.equals(Class.class);
  }

  /** Indicates whether this object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NonreceiverTerm)) {
      return false;
    }
    NonreceiverTerm other = (NonreceiverTerm) o;

    return this.type.equals(other.type) && Objects.equals(this.value, other.value);
  }

  /** Returns a hash code value for this NonreceiverTerm. */
  @Override
  public int hashCode() {
    return this.type.hashCode() + (this.value == null ? 0 : this.value.hashCode());
  }

  /** Returns string representation of this NonreceiverTerm. */
  @Override
  public String toString() {
    if (type.equals(JavaTypes.CLASS_TYPE)) {
      return ((Class<?>) value).getName() + ".class";
    }
    return Objects.toString(value);
  }

  @Override
  public String getName() {
    return this.toString();
  }

  /**
   * {@inheritDoc}
   *
   * @return {@link NormalExecution} object enclosing value of this non-receiver term
   */
  @Override
  public ExecutionOutcome execute(Object[] statementInput) {
    assert statementInput.length == 0;
    return new NormalExecution(this.value, 0);
  }

  /**
   * {@inheritDoc}
   *
   * <p>For NonreceiverTerm, simply adds a code representation of the value to the string builder.
   * Note: this does not explicitly box primitive values.
   *
   * @param inputVars ignored
   * @param b {@link StringBuilder} to which string representation is appended
   */
  @Override
  public void appendCode(
      Type declaringType,
      TypeTuple inputTypes,
      Type outputType,
      List<Variable> inputVars,
      StringBuilder b) {
    b.append(Value.toCodeString(getValue()));
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
   * Return the type.
   *
   * @return the type
   */
  public Type getType() {
    return this.type;
  }

  /**
   * Returns a NonreceiverTerm holding the zero/false value for the specified class c.
   *
   * <ul>
   *   <li>Returns 'a' for characters.
   *   <li>Returns null for {@link JavaTypes#CLASS_TYPE}.
   *   <li>Returns "" (empty string) for {@link JavaTypes#STRING_TYPE}.
   * </ul>
   *
   * @param type the type of value desired
   * @return a {@link NonreceiverTerm} with a canonical representative of the given type
   */
  static NonreceiverTerm createNullOrZeroTerm(Type type) {
    if (type.isBoxedPrimitive()) {
      type = ((NonParameterizedType) type).toPrimitive();
    }
    if (type.isString()) {
      return new NonreceiverTerm(type, "");
    }
    if (type.equals(JavaTypes.CHAR_TYPE)) {
      return new NonreceiverTerm(type, 'a'); // TODO This is not null or zero...
    }
    if (type.equals(JavaTypes.BYTE_TYPE)) return new NonreceiverTerm(type, (byte) 0);
    if (type.equals(JavaTypes.SHORT_TYPE)) return new NonreceiverTerm(type, (short) 0);
    if (type.equals(JavaTypes.INT_TYPE)) return new NonreceiverTerm(type, 0);
    if (type.equals(JavaTypes.LONG_TYPE)) return new NonreceiverTerm(type, 0L);
    if (type.equals(JavaTypes.FLOAT_TYPE)) return new NonreceiverTerm(type, 0f);
    if (type.equals(JavaTypes.DOUBLE_TYPE)) return new NonreceiverTerm(type, 0d);
    if (type.equals(JavaTypes.BOOLEAN_TYPE)) return new NonreceiverTerm(type, false);
    return new NonreceiverTerm(type, null);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns a string representing this primitive declaration. The string is of the form:<br>
   * {@code TYPE:VALUE}<br>
   * Where TYPE is the type of the primitive declaration, and VALUE is its value. If VALUE is "null"
   * then the value is null (not the String "null"). If TYPE is "char" then
   * (char)Integer.parseInt(VALUE, 16) yields the character value.
   *
   * <p>Examples:
   *
   * <pre>
   * String:null                  represents: String x = null
   * java.lang.String:""          represents: String x = "";
   * String:""                    represents: String x = "";
   * String:" "                   represents: String x = " ";
   * String:"\""                  represents: String x = "\"";
   * String:"\n"                  represents: String x = "\n";
   * String:"\u263A"              represents: String x = "\u263A";
   * java.lang.Object:null        represents: Object x = null;
   * [[Ljava.lang.Object;:null    represents: Object[][] = null;
   * int:0                        represents: int x = 0;
   * boolean:false                represents: boolean x = false;
   * char:20                      represents: char x = ' ';
   * </pre>
   *
   * Note that a string type can be given as both "String" or "java.lang.String".
   *
   * @return string representation of primitive, String or null value
   */
  @Override
  public String toParsableString(Type declaringType, TypeTuple inputTypes, Type outputType) {

    String valStr;
    if (value == null) {
      valStr = "null";
    } else if (type.equals(JavaTypes.CHAR_TYPE)) {
      valStr = Integer.toHexString((Character) value);
    } else if (type.equals(JavaTypes.CLASS_TYPE)) {
      valStr = ((Class<?>) value).getName() + ".class";
    } else {
      valStr = value.toString();
      if (type.isString()) {
        valStr = "\"" + StringsPlume.escapeJava(valStr) + "\"";
      }
    }

    return type.getBinaryName() + ":" + valStr;
  }

  /**
   * Parse a non-receiver value in a string in the form generated by {@link
   * NonreceiverTerm#toParsableString(Type, TypeTuple, Type)}
   *
   * @param s a string representing a value of a non-receiver type
   * @return the non-receiver term for the given string descriptor
   * @throws OperationParseException if string does not represent valid object
   */
  @SuppressWarnings("signature") // parsing
  public static TypedOperation parse(String s) throws OperationParseException {
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
              + "\" contains whitespace characters.";
      throw new OperationParseException(msg);
    }

    // Convert "String" to "java.lang.String"
    if (typeString.equals("String")) {
      typeString = "java.lang.String";
    }

    Type type;
    try {
      type = Type.forName(typeString);
    } catch (ClassNotFoundException | NoClassDefFoundError e1) {
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
    if (type.equals(JavaTypes.CHAR_TYPE)) {
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
                + "\") was not parsable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(JavaTypes.BYTE_TYPE)) {
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
                + "\") was not parsable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(JavaTypes.SHORT_TYPE)) {
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
                + "\") was not parsable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(JavaTypes.INT_TYPE)) {
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
                + "\") was not parsable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(JavaTypes.LONG_TYPE)) {
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
                + "\") was not parsable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(JavaTypes.FLOAT_TYPE)) {
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
                + "\") was not parsable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(JavaTypes.DOUBLE_TYPE)) {
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
                + "\") was not parsable.";
        throw new OperationParseException(msg);
      }
    } else if (type.equals(JavaTypes.BOOLEAN_TYPE)) {
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
                + "\") was not parsable.";
        throw new OperationParseException(msg);
      }
    } else if (type.isString()) {
      if (valString.equals("null")) {
        value = null;
      } else {
        if (valString.charAt(0) != '"' || valString.charAt(valString.length() - 1) != '"') {
          String msg =
              "Error when parsing type/value pair "
                  + s
                  + ". A String value declaration description must be of the form "
                  + "java.lang.String:\"thestring\""
                  + " but the string given was not enclosed in quotation marks.";
          throw new OperationParseException(msg);
        }
        String valStringContent = valString.substring(1, valString.length() - 1);
        if (!Value.stringLengthOk(valStringContent)) {
          throw new OperationParseException(
              String.format(
                  "Error when parsing String; length %d is too large", valStringContent.length()));
        }
        value = StringsPlume.unescapeJava(valStringContent);
      }
    } else {
      if (valString.equals("null")) {
        value = null;
      } else {
        String msg =
            "Error when parsing type/value pair "
                + s
                + ". A primitive value declaration description that is not a primitive value or a"
                + " string must be of the form <type>:null but the string given (\""
                + valString
                + "\") was not of this form.";
        throw new OperationParseException(msg);
      }
    }

    NonreceiverTerm nonreceiverTerm = new NonreceiverTerm(type, value);
    return new TypedTermOperation(nonreceiverTerm, new TypeTuple(), type);
  }

  /**
   * {@inheritDoc}
   *
   * @return true, since all of objects are non-receivers
   */
  @Override
  public boolean isNonreceivingValue() {
    return true;
  }
}
