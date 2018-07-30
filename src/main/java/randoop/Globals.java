package randoop;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/** Various general global variables used throughout Randoop. */
public class Globals {

  /** The version number for Randoop. */
  public static final String RANDOOP_VERSION = "4.0.4";

  /** The system-specific line separator string. */
  public static final String lineSep = System.getProperty("line.separator");

  /** A PrintStream whose contents are ignored. */
  public static PrintStream blackHole;

  private static final ByteArrayOutputStream bos;

  private static PrintStream oldStdErr;

  static {
    oldStdErr = System.err;
    bos = new ByteArrayOutputStream();
    blackHole = new PrintStream(bos);
    // System.setErr(blackHole);
  }

  /**
   * Return the version number for Randoop.
   *
   * @return the version number for Randoop
   */
  public static String getRandoopVersion() {
    return RANDOOP_VERSION;
  }

  /**
   * Return the Java classpath.
   *
   * @return the Java classpath
   */
  public static String getClassPath() {
    return System.getProperty("java.class.path");
  }

  /** Column width for printing messages. */
  public static final int COLWIDTH = 70;

  /** Number of spaces for leading indentation for printing messages. */
  public static final int INDENTWIDTH = 8;
}
