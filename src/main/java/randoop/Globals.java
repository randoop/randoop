package randoop;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

/** Various general global variables used throughout Randoop. */
public class Globals {

  public static final String RANDOOP_VERSION = "3.1.5";

  public static final String lineSep = System.getProperty("line.separator");

  public static final String pathSep = System.getProperty("path.separator");

  public static PrintStream blackHole;

  private static final ByteArrayOutputStream bos;

  // Setting the Constant to any number greater than zero will cause models to
  // have a maximal depth MAX_MODEL_DEPTH+1
  public static final int MAX_MODEL_DEPTH = 100;

  private static PrintStream oldStdErr;

  static {
    oldStdErr = System.err;
    bos = new ByteArrayOutputStream();
    blackHole = new PrintStream(bos);
    // System.setErr(blackHole);
  }

  public static class ErrorStreamAssigner {
    public ErrorStreamAssigner(String destination) {
      if (destination.equals("stderr")) {
        System.setErr(oldStdErr);
      } else {
        try {
          System.setErr(new PrintStream(new PrintStream(destination), true));
        } catch (FileNotFoundException e) {
          System.out.println(Globals.lineSep + "Could not create a stream for file " + destination);
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static String getRandoopVersion() {
    return RANDOOP_VERSION;
  }

  public static String getClassPath() {
    return System.getProperty("java.class.path");
  }

  public static final int COLWIDTH = 70;

  public static final int INDENTWIDTH = 8;
}
