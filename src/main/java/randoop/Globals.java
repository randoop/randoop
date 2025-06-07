package randoop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;
import org.checkerframework.checker.mustcall.qual.Owning;
import org.checkerframework.checker.regex.qual.Regex;
import org.checkerframework.checker.signedness.qual.PolySigned;
import randoop.main.RandoopBug;

/** Various general global variables used throughout Randoop. */
public class Globals {

  /** The version number for Randoop. */
  public static final String RANDOOP_VERSION = "4.3.4";

  /** The system-specific line separator string. */
  @SuppressWarnings("regex:assignment") // needed with CF 3.49.4 and earlier
  public static final @Regex(0) String lineSep = System.lineSeparator();

  /** A PrintStream whose contents are ignored. */
  public static @Owning PrintStream blackHole = new PrintStream(new NullOutputStream());

  /** Discards anything written to it. */
  private static class NullOutputStream extends OutputStream {
    @Override
    public void write(@PolySigned int b) throws IOException {}
  }

  /**
   * Return the version number for Randoop.
   *
   * @return the version number for Randoop
   */
  public static String getRandoopVersion() {
    Properties prop = new Properties();
    try (InputStream isReleaseStream =
        Globals.class.getResourceAsStream("/this-is-a-randoop-release")) {
      if (isReleaseStream != null) {
        return RANDOOP_VERSION;
      }
    } catch (IOException e) {
      throw new RandoopBug(e);
    }
    try (InputStream inputStream = Globals.class.getResourceAsStream("/git.properties")) {
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
