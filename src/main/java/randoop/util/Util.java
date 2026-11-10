package randoop.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.regex.qual.Regex;
import org.plumelib.util.StringsPlume;
import randoop.Globals;

/** Helpers for assertions, and stuff... */
public final class Util {

  /** Do not instantiate. */
  private Util() {
    throw new IllegalStateException("no instance");
  }

  /**
   * Returns true if a and b are equal (both true or both false).
   *
   * @param a first boolean to test
   * @param b second boolean to test
   * @return true if a and b are equal
   */
  public static boolean iff(boolean a, boolean b) {
    return a == b;
  }

  /**
   * Returns true if a is false or b is true.
   *
   * @param a first boolean to test
   * @param b second boolean to test
   * @return true if a is false or b is true
   */
  public static boolean implies(boolean a, boolean b) {
    return !a || b;
  }

  /**
   * Returns true if the string is a legal Java identifier.
   *
   * @param s string to test
   * @return true if the string is a legal Java identifier
   */
  public static boolean isJavaIdentifier(String s) {
    if (s == null || s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) {
      return false;
    }
    for (int i = 1; i < s.length(); i++) {
      if (!Character.isJavaIdentifierPart(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Convert each character to the form "\\uHEXDIGITS".
   *
   * @param unicodeString string to convert
   * @return converted string
   */
  public static String convertToHexString(String unicodeString) {
    char[] chars = unicodeString.toCharArray();
    StringBuilder output = new StringBuilder();
    for (char c : chars) {
      output.append("\\u");
      String hex = Integer.toHexString(c);
      if (hex.length() < 4) {
        output.append('0');
      }
      if (hex.length() < 3) {
        output.append('0');
      }
      if (hex.length() < 2) {
        output.append('0');
      }
      if (hex.length() < 1) {
        output.append('0');
      }

      output.append(hex);
    }
    return output.toString();
  }

  /**
   * Returns the number of times that the pattern appears in the text.
   *
   * @param text string to search
   * @param pattern string pattern to search for
   * @return the number of times the pattern appears in the text
   */
  public static int occurCount(StringBuilder text, String pattern) {
    if (pattern.length() == 0) {
      throw new IllegalArgumentException("empty pattern");
    }
    int i = 0;
    int currIdx = text.indexOf(pattern);
    while (currIdx != -1) {
      i++;
      currIdx = text.indexOf(pattern, currIdx + 1);
    }
    return i;
  }

  /**
   * Format a hanging paragraph: The first line starts at the margin, and every subsequent line
   * starts indented by {@code indentWidth}. Each line is no more than {@code colWidth} characters
   * long.
   *
   * @param string the paragraph to format
   * @param colWidth the full column width
   * @param indentWidth the number of spaces before each line other than the first line
   * @return a string representation of the formatted paragraph, include line separators
   */
  public static String hangingParagraph(String string, int colWidth, int indentWidth) {
    if (string == null) {
      throw new IllegalArgumentException("string cannot be null.");
    }
    if (indentWidth > colWidth) {
      throw new IllegalArgumentException("indentWidth cannot be greater than columnWidth");
    }

    String indentString = new String(new char[indentWidth]).replace("\0", " ");

    StringBuilder b = new StringBuilder();

    boolean firstLine = true;
    while (true) {
      // Determine line's length (exclusive of indent if any)
      int lineLength = firstLine ? colWidth : colWidth - indentWidth;

      if (lineLength >= string.length()) {
        if (!firstLine) {
          b.append(indentString);
        }
        b.append(string);
        return b.toString();
      }

      // i is index of whitespace
      int i = lineLength;
      while (i > 0) {
        if (Character.isWhitespace(string.charAt(i))) {
          break;
        }
        i--;
      }
      if (i == 0) {
        i = lineLength;
      }

      // Add indent.
      if (!firstLine) {
        b.append(indentString);
      }
      b.append(string.substring(0, i));
      b.append(Globals.lineSep);
      string = string.substring(i + 1);
      firstLine = false;
    }
  }

  /**
   * Replace occurrences of words from this map with corresponding replacements. Only performs
   * replacements of full words (using regular expression word delimiters).
   *
   * @param text the text to search for occurrences of names in this map
   * @param replacements the map of replacements to perform
   * @return the text modified by replacing original names with replacement names
   */
  public static String replaceWords(String text, Map<String, String> replacements) {
    @SuppressWarnings({"regex:argument", "regex:assignment"}) // string manipulation
    @Regex(1) Pattern namesPattern =
        Pattern.compile("\\b(" + StringsPlume.join("|", replacements.keySet().toArray()) + ")\\b");
    Matcher namesMatcher = namesPattern.matcher(text);
    StringBuilder b = new StringBuilder();
    int position = 0;
    while (namesMatcher.find(position)) {
      b.append(text.substring(position, namesMatcher.start(1)));
      @SuppressWarnings("nullness:assignment") // https://tinyurl.com/cfissue/4006
      @NonNull String name = namesMatcher.group(1);
      b.append(replacements.get(name));
      position = namesMatcher.end(1);
    }
    b.append(text.substring(position));
    return b.toString();
  }

  /**
   * Returns the path if it is absolute, otherwise returns the path AND the absolute path.
   *
   * @param path a file path
   * @return the path, and the absolute path if different
   */
  public static String pathAndAbsolute(Path path) {
    if (!path.isAbsolute()) {
      return path.toString();
    } else {
      return path + " = " + path.toAbsolutePath();
    }
  }

  /**
   * Returns the file if it is absolute, otherwise returns the file AND the absolute file.
   *
   * @param file a file
   * @return the file, and the absolute file if different
   */
  public static String fileAndAbsolute(File file) {
    return pathAndAbsolute(file.toPath());
  }

  /**
   * Returns the filename if it is absolute, otherwise returns the filename AND the absolute
   * filename.
   *
   * @param filename a file name
   * @return the filename, and the absolute filename if different
   */
  public static String filenameAndAbsolute(String filename) {
    return pathAndAbsolute(Paths.get(filename));
  }

  /**
   * Returns {@link StringsPlume#toStringAndClass(Object)} for the given value: the value's {@code
   * toString()} and its class, as in "hello [class java.lang.String]". For an array, list, or map,
   * prints each element and its class.
   *
   * <p>Use this method to print a value created by code under test, whose {@code toString()} is
   * also code under test. This method does not throw, even if printing the value throws; see {@link
   * #toStringAndClassSafely(Object, boolean)}.
   *
   * @param value a value, possibly null
   * @return a printed representation of the value and its class
   */
  public static String toStringAndClassSafely(@Nullable Object value) {
    return toStringAndClassSafely(value, false);
  }

  /**
   * Returns {@link StringsPlume#toStringAndClass(Object, boolean)} for the given value: the value's
   * {@code toString()} and its class, as in "hello [class java.lang.String]".
   *
   * <p>Use this method to print a value created by code under test, whose {@code toString()} is
   * also code under test. This method does not throw, even if printing the value throws. Instead,
   * if the value's {@code toString()} throws, as {@code HashMap.toString()} throws {@code
   * StackOverflowError} for a map that contains itself indirectly, it returns
   * "exception_when_calling_toString [class java.util.HashMap]: java.lang.StackOverflowError". If
   * printing the elements of an array, list, or map throws, as when iterating over a list throws
   * {@code ConcurrentModificationException}, it returns "exception_when_printing_elements [class
   * java.util.ArrayList]: java.util.ConcurrentModificationException".
   *
   * <p>When {@code toString()} throws an {@code Exception}, plume-util 1.14.0 returns
   * "exception_when_calling_toString [class java.util.HashMap]", without naming the exception. Once
   * Randoop uses a plume-util version that includes plume-lib/plume-util#823, {@code
   * StringsPlume.toStringAndClass} never throws and names the exception, so it can replace this
   * method.
   *
   * @param value a value, possibly null
   * @param shallow if true, do not print the elements of an array, list, or map
   * @return a printed representation of the value and its class
   */
  public static String toStringAndClassSafely(@Nullable Object value, boolean shallow) {
    if (value == null) {
      return "null";
    }
    // TODO: Once Randoop uses a plume-util version that includes plume-lib/plume-util#823,
    // replace the rest of this method body by `return StringsPlume.toStringAndClass(value,
    // shallow);`, or replace calls to this method by calls to StringsPlume.toStringAndClass.
    try {
      return StringsPlume.toStringAndClass(value, shallow);
    } catch (
        @SuppressWarnings("removal")
        ThreadDeath t) {
      // Thread.stop() throws ThreadDeath; let it stop the thread.
      throw t;
    } catch (Throwable t) {
      // Catching every Throwable is necessary:  a value that contains itself can make printing
      // throw StackOverflowError, and printing a huge value can throw OutOfMemoryError.
      boolean printsElements =
          !shallow && (value.getClass().isArray() || value instanceof List || value instanceof Map);
      return printingFailed(
          printsElements ? "exception_when_printing_elements" : "exception_when_calling_toString",
          value,
          t);
    }
  }

  /**
   * Returns the given value's {@code toString()}, as in "java.lang.IllegalStateException: bad
   * state". If the value's {@code toString()} throws, returns a message as {@link
   * #toStringAndClassSafely(Object, boolean)} does, as in "exception_when_calling_toString [class
   * java.lang.IllegalStateException]: java.lang.StackOverflowError".
   *
   * <p>Use this method to print a value created by code under test, whose {@code toString()} is
   * also code under test. For example, {@code Throwable.toString()} calls {@code
   * getLocalizedMessage()}, which code under test can override.
   *
   * @param value a value
   * @return the value's {@code toString()}, or a message that names the value's class and the
   *     thrown class
   */
  public static String toStringSafely(Object value) {
    try {
      return value.toString();
    } catch (
        @SuppressWarnings("removal")
        ThreadDeath t) {
      // Thread.stop() throws ThreadDeath; let it stop the thread.
      throw t;
    } catch (Throwable t) {
      return printingFailed("exception_when_calling_toString", value, t);
    }
  }

  /**
   * Returns a message saying that printing the given value threw, as in
   * "exception_when_calling_toString [class java.util.HashMap]: java.lang.StackOverflowError". If
   * creating that message throws, as when the heap is exhausted, returns {@code kind} itself, which
   * requires no allocation.
   *
   * @param kind what threw, such as "exception_when_calling_toString"
   * @param value the value whose printing threw
   * @param t what printing the value threw
   * @return a message that names {@code kind}, the value's class, and the thrown class
   */
  private static String printingFailed(String kind, Object value, Throwable t) {
    try {
      return String.format("%s [%s]: %s", kind, value.getClass(), t.getClass().getName());
    } catch (
        @SuppressWarnings("removal")
        ThreadDeath t2) {
      // Thread.stop() throws ThreadDeath; let it stop the thread.
      throw t2;
    } catch (Throwable t2) {
      // Formatting can throw OutOfMemoryError if printing the value exhausted the heap.
      return kind;
    }
  }

  /**
   * Returns a printed representation of the given values and their classes, as in "[hello [class
   * java.lang.String], null, [I@1b6d3586 [class [I]]". Prints each value with {@link
   * #toStringAndClassSafely(Object, boolean)}, without printing the elements of an array, list, or
   * map, so the result for a huge array is short.
   *
   * @param values an array of values, each possibly null
   * @return a printed representation of the values and their classes
   */
  public static String elementsToStringAndClassSafely(@Nullable Object[] values) {
    StringJoiner result = new StringJoiner(", ", "[", "]");
    for (Object value : values) {
      result.add(toStringAndClassSafely(value, true));
    }
    return result.toString();
  }
}
