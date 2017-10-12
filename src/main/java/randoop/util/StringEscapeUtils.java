/*
 * Package name changed and everything other than Java escaping removed.
 */
/*
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package randoop.util;

import java.io.IOException;
import java.io.Writer;

/**
 * Escapes and unescapes {@code String}s for Java, JavaScript, HTML, XML, and SQL.
 *
 * @author Apache Jakarta Turbine
 * @author GenerationJavaCore library
 * @author Purple Technology
 * @author <a href="mailto:bayard@generationjava.com">Henri Yandell</a>
 * @author <a href="mailto:alex@purpletech.com">Alexander Day Chaffee</a>
 * @author <a href="mailto:cybertiger@cyberiantiger.org">Antony Riley</a>
 * @author Helge Tesgaard
 * @author <a href="sean@boohai.com">Sean Brown</a>
 * @author <a href="mailto:ggregory@seagullsw.com">Gary Gregory</a>
 * @author Phil Steitz
 * @author Pete Gieser
 * @since 2.0
 */
public class StringEscapeUtils {

  /**
   * {@code StringEscapeUtils} instances should NOT be constructed in standard programming.
   *
   * <p>Instead, the class should be used as:
   *
   * <pre>
   * StringEscapeUtils.escapeJava("foo");
   * </pre>
   *
   * <p>This constructor is public to permit tools that require a JavaBean instance to operate.
   */
  public StringEscapeUtils() { // Empty.
  }

  // Java and JavaScript
  // --------------------------------------------------------------------------
  /**
   * Escapes the characters in a {@code String} using Java String rules.
   *
   * <p>Deals correctly with quotes and control-chars (tab, backslash, cr, ff, etc.)
   *
   * <p>So a tab becomes the characters {@code '\\'} and {@code 't'}.
   *
   * <p>The only difference between Java strings and JavaScript strings is that in JavaScript, a
   * single quote must be escaped.
   *
   * <p>Example:
   *
   * <pre>
   * input string: He didn't say, "Stop!"
   * output string: He didn't say, \"Stop!\"
   * </pre>
   *
   * @param str string to escape values in, may be null
   * @return a String with escaped values, {@code null} if input string was null
   */
  public static String escapeJava(String str) {
    return escapeJavaStyleString(str, false);
  }

  /**
   * Escapes the characters in a {@code String} using Java String rules to a {@code Writer}.
   *
   * <p>A {@code null} string input has no effect.
   *
   * @param out writer to write escaped string into
   * @param str string to escape values in, may be null
   * @throws IllegalArgumentException if the Writer is {@code null}
   * @throws IOException if error occurs on underlying Writer
   * @see #escapeJava(java.lang.String)
   */
  public static void escapeJava(Writer out, String str) throws IOException {
    escapeJavaStyleString(out, str, false);
  }

  public static String escapeJavaStyleString(String str, boolean escapeSingleQuotes) {
    if (str == null) {
      return null;
    }
    try {
      StringPrintWriter writer = new StringPrintWriter(str.length() * 2);
      escapeJavaStyleString(writer, str, escapeSingleQuotes);
      return writer.getString();
    } catch (IOException ioe) {
      // this should never ever happen while writing to a StringWriter
      ioe.printStackTrace();
      return null;
    }
  }

  private static void escapeJavaStyleString(Writer out, String str, boolean escapeSingleQuote)
      throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("The Writer must not be null");
    }
    if (str == null) {
      return;
    }

    int sz = str.length();
    for (int i = 0; i < sz; i++) {
      char ch = str.charAt(i);

      // handle unicode
      if (ch > 0xfff) {
        out.write("\\u" + hex(ch));
      } else if (ch > 0xff) {
        out.write("\\u0" + hex(ch));
      } else if (ch > 0x7f) {
        out.write("\\u00" + hex(ch));
      } else if (ch < 32) {
        switch (ch) {
          case '\b':
            out.write('\\');
            out.write('b');
            break;
          case '\n':
            out.write('\\');
            out.write('n');
            break;
          case '\t':
            out.write('\\');
            out.write('t');
            break;
          case '\f':
            out.write('\\');
            out.write('f');
            break;
          case '\r':
            out.write('\\');
            out.write('r');
            break;
          default:
            if (ch > 0xf) {
              out.write("\\u00" + hex(ch));
            } else {
              out.write("\\u000" + hex(ch));
            }
            break;
        }
      } else {
        switch (ch) {
          case '\'':
            if (escapeSingleQuote) {
              out.write('\\');
            }
            out.write('\'');
            break;
          case '"':
            out.write('\\');
            out.write('"');
            break;
          case '\\':
            out.write('\\');
            out.write('\\');
            break;
          default:
            out.write(ch);
            break;
        }
      }
    }
  }

  /**
   * Returns an upper case hexadecimal {@code String} for the given character.
   *
   * @param ch the character to convert
   * @return an upper case hexadecimal {@code String}
   */
  private static String hex(char ch) {
    return Integer.toHexString(ch).toUpperCase();
  }
}
