package randoop.util;

import java.util.ArrayList;
import java.util.List;

import randoop.Globals;
import plume.UtilMDE;


/**
 * Helpers for assertions, and stuff...
 */
public final class Util {

  private Util() {
    throw new IllegalStateException("no instance");
  }

  public static final String newLine = System.getProperty("line.separator");

  public static boolean iff(boolean a, boolean b) {
    return a == b;
  }

  public static boolean implies(boolean a, boolean b) {
    return !a || b;
  }

  /**
   * If both parameters are null, returns true. If one parameter is null and
   * the other isn't, returns false. Otherwise, returns o1.equals(o2).
   * 
   * @param o1
   * @param o2
   */
  public static boolean equalsWithNull(Object o1, Object o2) {
    if (o1 == null) {
      return o2 == null;
    }
    if (o2 == null) {
      return false;
    }
    return (o1.equals(o2));
  }

  public static boolean isJavaIdentifier(String s) {
    if (s == null || s.length() == 0
        || !Character.isJavaIdentifierStart(s.charAt(0))) {
      return false;
    }
    for (int i = 1; i < s.length(); i++) {
      if (!Character.isJavaIdentifierPart(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static String convertToHexString(String unicodeString) {
    char[] chars = unicodeString.toCharArray();
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < chars.length; i++) {
      output.append("\\u");
      String hex = Integer.toHexString(chars[i]);
      if (hex.length() < 4)
        output.append("0");
      if (hex.length() < 3)
        output.append("0");
      if (hex.length() < 2)
        output.append("0");
      if (hex.length() < 1)
        output.append("0");

      output.append(hex);
    }
    return output.toString();
  }

  public static String toNColsStr(String s, int width) {
    StringBuilder b = new StringBuilder();
    for (String line : toNCols(s, width)) {
      b.append(line);
      b.append(System.getProperty("line.separator"));
    }
    return b.toString();
  }

  /**
   *  Splits it into words (whitespace separates words).
   *  Appends words to each other until it reaches a word
   * that would cause the current line to exceed the given width, and then
   * starts a new line.
   */
  public static List<String> toNCols(String s, int width) {
    List<String> ret = new ArrayList<String>();
    StringLineIterator i = new StringLineIterator(s);
    while (i.hasMoreWords()) {
      ret.add(i.nextLine(width));
    }
    return ret;
  }

  public static int occurCount(StringBuilder text, String pattern) {
    if (pattern.length() == 0)
      throw new IllegalArgumentException("empty pattern");
    int i = 0;
    int currIdx = text.indexOf(pattern);
    while (currIdx != -1) {
      i++;
      currIdx = text.indexOf(pattern, currIdx + 1);
    }
    return i;
  }

  public static String hangingParagraph(String string, int colWidth, int indentWidth) {
    if (string == null)
      throw new IllegalArgumentException("string cannot be null.");
    if (indentWidth > colWidth)
      throw new IllegalArgumentException("indentWidth cannot be greater than columnWidth");

    StringBuilder b = new StringBuilder();

    StringLineIterator i = new StringLineIterator(string);
    boolean firstLine = true;
    while (i.hasMoreWords()) {
      // Determine line's length.
      int lineLength;
      if (firstLine)
        lineLength = colWidth;
      else
        lineLength = colWidth - indentWidth;

      String line = i.nextLine(lineLength);
      if (line.length() == 0) {
        // If this happens, it will happen on all subsequence calls to nextLine,
        // which would lead to an infinite loop.
        throw new IllegalArgumentException("column width is too small to create hanging paragraph.");
      }

      // Add indent.
      if (!firstLine) {
        for (int spaces = 0 ; spaces < indentWidth ; spaces++)
          b.append(" ");
      }

      b.append(line);
      b.append(Globals.lineSep);
      firstLine = false;
    }
    return b.toString();
  }

  public static String createArgListJVML(Class<?>[] paramClasses) {
    StringBuilder b = new StringBuilder();
    b.append("(");
    for (int i = 0; i < paramClasses.length; i++) {
      Class<?> cls = paramClasses[i];

      // If primitive, Class.getName() returns the keyword. Convert to JVML.
      if (cls.isPrimitive()) {
        b.append(UtilMDE.primitive_name_to_jvm(cls.getName()));
        continue;
      }

      boolean isArray = cls.isArray();

      // If primitive array, Class.getName() gives the JML representation.      
      if (isArray && cls.isPrimitive()) {
        b.append(cls.getName());
        continue;
      }

      // If object array, Class.getName() returns almost the JVML representation,
      // except for the element class, which uses "." instead of "/" to separate
      // package names.
      if (isArray) {
        b.append(cls.getName().replace('.', '/'));
        continue;
      }

      // Is object, non-array. Class.getName() returns foo.bar.Baz. Convert to JVML.
      b.append(UtilMDE.classnameToJvm(paramClasses[i].getName()));
    }
    b.append(")");
    return b.toString();
  }

}
