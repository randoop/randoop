package randoop.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.regex.qual.Regex;
import org.plumelib.util.StringsPlume;
import randoop.Globals;

/** Helpers for assertions, and stuff... */
public final class Util {

  private Util() {
    throw new IllegalStateException("no instance");
  }

  /**
   * Return true if a and b are equal (both true or both false).
   *
   * @param a first boolean to test
   * @param b second bject to test
   * @return true if a and b are equal
   */
  public static boolean iff(boolean a, boolean b) {
    return a == b;
  }

  /**
   * Return true if a is false or b is true.
   *
   * @param a first boolean to test
   * @param b second bject to test
   * @return true if a is false or b is true
   */
  public static boolean implies(boolean a, boolean b) {
    return !a || b;
  }

  /**
   * Return true if the string is a legal Java identifier.
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
    for (int i = 0; i < chars.length; i++) {
      output.append("\\u");
      String hex = Integer.toHexString(chars[i]);
      if (hex.length() < 4) output.append("0");
      if (hex.length() < 3) output.append("0");
      if (hex.length() < 2) output.append("0");
      if (hex.length() < 1) output.append("0");

      output.append(hex);
    }
    return output.toString();
  }

  /**
   * Return the number of times that the pattern appears in the text.
   *
   * @param text string to search
   * @param pattern string pattern to search for
   * @return the number of times the pattern appears in the text
   */
  public static int occurCount(StringBuilder text, String pattern) {
    if (pattern.length() == 0) throw new IllegalArgumentException("empty pattern");
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
    if (string == null) throw new IllegalArgumentException("string cannot be null.");
    if (indentWidth > colWidth) {
      throw new IllegalArgumentException("indentWidth cannot be greater than columnWidth");
    }

    String indentString = new String(new char[indentWidth]).replace("\0", " ");

    StringBuilder b = new StringBuilder();

    boolean firstLine = true;
    while (true) {
      // Determine line's length (exclusive of intent if any)
      int lineLength = firstLine ? colWidth : colWidth - indentWidth;

      if (lineLength > string.length()) {
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
}
