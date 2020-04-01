package randoop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;
import randoop.main.RandoopBug;

/** Various general global variables used throughout Randoop. */
public class Globals {

  /** The version number for Randoop. */
  public static final String RANDOOP_VERSION = "4.2.3";

  /** The system-specific line separator string. */
  public static final String lineSep = System.lineSeparator();

  /** A PrintStream whose contents are ignored. */
  public static PrintStream blackHole;

  /** Discards anything written to it. */
  private static class NullOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {}
  }

  // private static PrintStream realSystemErr;

  static {
    blackHole = new PrintStream(new NullOutputStream());
    // realSystemErr = System.err;
    // System.setErr(blackHole);
  }

  /**
   * Return the version number for Randoop.
   *
   * @return the version number for Randoop
   */
  public static String getRandoopVersion() {
    Properties prop = new Properties();
    boolean isRelease = Globals.class.getResourceAsStream("/this-is-a-randoop-release") != null;
    if (isRelease) {
      return RANDOOP_VERSION;
    }
    InputStream inputStream = Globals.class.getResourceAsStream("/git.properties");
    try {
      prop.load(inputStream);
    } catch (IOException e) {
      throw new RandoopBug(e);
    }

    String localChanges = prop.getProperty("git.dirty").equals("true") ? ", local changes" : "";
    return "\""
        + String.join(
            ", ",
            RANDOOP_VERSION + localChanges,
            "branch " + prop.getProperty("git.branch"),
            "commit " + prop.getProperty("git.commit.id.abbrev"),
            prop.getProperty("git.commit.time").substring(0, 10))
        + "\"";
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
