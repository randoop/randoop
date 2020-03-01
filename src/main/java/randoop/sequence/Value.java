package randoop.sequence;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import org.plumelib.util.UtilPlume;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.types.JavaTypes;
import randoop.types.NonParameterizedType;
import randoop.types.Type;
import randoop.util.Log;

/** Utility methods to work with values in test sequences. */
public class Value {

  /**
   * Given a primitive, boxed primitive, or String, returns a String that can be used in Java source
   * to represent it.
   *
   * @param value the value to create a String representation for. The value's type must be a
   *     primitive type, a String, or null.
   * @return a string representing code for the given value
   */
  public static String toCodeString(Object value) {

    if (value == null) {
      return "null";
    }

    Type valueType = Type.forClass(value.getClass());
    assert valueType.isNonreceiverType() : "expecting nonreceiver type, have " + valueType;

    if (valueType.isString()) {
      String escaped = UtilPlume.escapeJava(value.toString());
      if (!stringLengthOk(escaped)) {
        throw new StringTooLongException(escaped);
      }
      return "\"" + escaped + "\""; // + "/*length=" + escaped.length() + "*/"
    }

    if (valueType.getRuntimeClass().equals(Class.class)) {
      return ((Class<?>) value).getName() + ".class";
    }

    // conditions below require primitive types
    if (valueType.isBoxedPrimitive()) {
      valueType = ((NonParameterizedType) valueType).toPrimitive();
    }

    if (valueType.equals(JavaTypes.CHAR_TYPE)) {
      // XXX This won't always work!
      if (value.equals(' ')) {
        return "' '";
      }
      return "\'" + UtilPlume.escapeJava(value.toString()) + "\'";
    }

    if (valueType.equals(JavaTypes.BOOLEAN_TYPE)) {
      return value.toString();
    }

    // numeric types
    String rep;
    if (valueType.equals(JavaTypes.DOUBLE_TYPE)) {
      Double d = (Double) value;
      if (d.isNaN()) {
        return "Double.NaN";
      } else if (d.equals(Double.POSITIVE_INFINITY)) {
        return "Double.POSITIVE_INFINITY";
      } else if (d.equals(Double.NEGATIVE_INFINITY)) {
        return "Double.NEGATIVE_INFINITY";
      }
      rep = d.toString();
      assert rep != null;
      rep = rep + "d";
    } else if (valueType.equals(JavaTypes.FLOAT_TYPE)) {
      Float f = (Float) value;
      if (f.isNaN()) {
        return "Float.NaN";
      } else if (f.equals(Float.POSITIVE_INFINITY)) {
        return "Float.POSITIVE_INFINITY";
      } else if (f == Float.NEGATIVE_INFINITY) {
        return "Float.NEGATIVE_INFINITY";
      }
      rep = f.toString();
      assert rep != null;
      rep = rep + "f";
    } else if (valueType.equals(JavaTypes.LONG_TYPE)) {
      rep = value.toString() + "L";
    } else if (valueType.equals(JavaTypes.BYTE_TYPE)) {
      rep = "(byte)" + value.toString();
    } else if (valueType.equals(JavaTypes.SHORT_TYPE)) {
      rep = "(short)" + value.toString();
    } else if (valueType.equals(JavaTypes.INT_TYPE)) {
      rep = value.toString();
    } else {
      throw new RandoopBug("type should be a nonreceiver type: " + valueType);
    }

    // if a negative number parenthesize to avoid problems in casts
    if (rep.charAt(0) == '-') {
      rep = "(" + rep + ")";
    }

    return rep;
  }

  /**
   * Returns true if the value is a string that may NOT be asserted over, because it is (likely to
   * be) nondeterministic or is too long.
   *
   * @param o the value to test, which may or may not be a string
   * @return true if the value is an unassertable string, false if not a string, false if a string
   *     that may be asserted over
   */
  public static boolean isUnassertableString(Object o) {
    if (!(o instanceof String)) {
      return false;
    }
    String str = (String) o;

    // Don't create assertions over strings that look like raw object references.
    if (Value.looksLikeObjectToString(str)) {
      return true;
    }

    // Don't create assertions over long strings.  Long strings can cause the generated unit tests
    // to be unreadable and/or non-compilable due to Java restrictions on String constants.
    if (!Value.escapedStringLengthOk(str)) {
      Log.logPrintf(
          "Ignoring a string that exceeds the maximum length of %d%n",
          GenInputsAbstract.string_maxlen);
      return true;
    }

    return false;
  }

  /**
   * The pattern for strings that look like the output of a call to {@code Object.toString()}.
   *
   * <p>This regex is more efficient than a more complete one that matches against {@code
   * "<em>classname</em>@<em>hex</em>"}. This regex almost always works and is a faster check.
   */
  private static final Pattern OBJECT_TOSTRING_PATTERN = Pattern.compile("@[0-9a-h]{1,8}");

  /**
   * Returns true if the given string looks like it came from a call of Object.toString(); in other
   * words, it contains {@code "@<em>hex</em>"}. Such strings are rarely useful in generation
   * because they contain non-reproducible hash strings.
   *
   * @param s the string
   * @return true if string appears to be default toString output, false otherwise
   */
  public static boolean looksLikeObjectToString(String s) {
    if (s == null) {
      throw new IllegalArgumentException("s is null");
    }
    return OBJECT_TOSTRING_PATTERN.matcher(s).find();
  }

  /**
   * Returns true if the given string is no longer than the --string-maxlen=N parameter.
   *
   * @param s the string
   * @return true if the string length is reasonable for generated tests, false otherwise
   * @see GenInputsAbstract#string_maxlen
   */
  public static boolean stringLengthOk(String s) {
    return s.length() <= GenInputsAbstract.string_maxlen;
  }

  /** Used to increase performance of stringLengthOk method. */
  private static Map<String, Boolean> escapedStringLengthOkCached = new WeakHashMap<>();

  /**
   * Returns true if the given string, when quoted for inclusion in a Java program, is no longer
   * than the --string-maxlen=N parameter.
   *
   * <p>If Randoop generates tests using strings that are too long, this can result in
   * non-compilable tests due to the JVM's limit on the length of a string.
   *
   * @param s the string
   * @return true if the string length is reasonable for generated tests, false otherwise
   * @see GenInputsAbstract#string_maxlen
   */
  public static boolean escapedStringLengthOk(String s) {
    if (s == null) {
      throw new IllegalArgumentException();
    }

    // Optimization: return cached value if available.
    // String caches its hash code, so this is a cheap operation.
    Boolean b = escapedStringLengthOkCached.get(s);
    if (b != null) {
      return b;
    }

    int length = s.length();

    // Optimization: if length greater than maxlen, return false right away.
    if (length > GenInputsAbstract.string_maxlen) {
      return false;
    }

    // Optimization: if the string is definitely short enough, return true right away.
    // If a string's length is less than 1/6 * maxlen, it's definitely short enough, since
    // the worst that could happen is that every character in s is unicode and is
    // expanded to "\u0000" format, blowing up the length to s.length() * 6.
    if (length * 6 < GenInputsAbstract.string_maxlen) {
      return true;
    }

    boolean result = stringLengthOk(UtilPlume.escapeJava(s));
    escapedStringLengthOkCached.put(s, result);
    return result;
  }

  // TODO: Add a command-line  parameter specifically for arrays.
  /**
   * Returns true if the given array is shorter than the --string-maxlen=N parameter.
   *
   * @param a the string
   * @return true if the array length is less than the bound
   */
  public static boolean arrayLengthOk(Object a) {
    if (a == null) {
      throw new IllegalArgumentException();
    }
    return Array.getLength(a) <= GenInputsAbstract.string_maxlen;
  }

  /**
   * Returns true if the given value is not longer than the --string-maxlen=N parameter.
   *
   * @param v a value
   * @return true if the value's size is less than the bound
   */
  public static boolean valueSizeOk(Object v) {
    if (v == null) {
      return true;
    }
    if (v instanceof String) {
      return escapedStringLengthOk((String) v);
    }
    if (v.getClass().isArray()) {
      return arrayLengthOk(v);
    }
    return true;
  }

  /**
   * Returns true if the value returned by the last statement is not too large.
   *
   * @param eseq an executable sequence that has been executed
   * @return true if the final result value's size is less than the bound
   */
  public static boolean lastValueSizeOk(ExecutableSequence eseq) {
    Sequence seq = eseq.sequence;
    int lastIndex = seq.size() - 1;
    ExecutionOutcome lastResult = eseq.getResult(lastIndex);
    if (lastResult instanceof NormalExecution) {
      Object lastValue = ((NormalExecution) lastResult).getRuntimeValue();
      return valueSizeOk(lastValue);
    }
    return true;
  }
}
