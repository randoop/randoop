package randoop;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;

import utilpag.Invisible;
import utilpag.Option;

public class Globals {

  public static final String lineSep = System.getProperty("line.separator");

  @Invisible
  @Option("Disable assertions and checkRep methods.")
  public static boolean nochecks = true;

  @Invisible
  @Option("Perform (expensive) checks that tests Randoop (for Randoop developers).")
  public static boolean randooptestrun = false;

  @Invisible
  @Option("Pipe stderr to the given file.")
  public static ErrorStreamAssigner senderr;

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
          System.setErr(new PrintStream(new PrintStream(destination),
              true));
        } catch (FileNotFoundException e) {
          System.out.println(Globals.lineSep + "Could not create a stream for file "
              + destination);
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static String getRandoopVersion() {
    InputStream s = randoop.Globals.class.getResourceAsStream("version.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(s));
    String versionString;
    try {
      versionString = reader.readLine();
      if (versionString == null)
        throw new RuntimeException("version string cannot be null.");
      versionString = versionString.trim();
      if (versionString.trim().equals(""))
        throw new RuntimeException("version string cannot be empty.");
    } catch (IOException e) {
      URL versionFile = randoop.Globals.class.getResource("version.txt");
      String msg = String.format("IO error while reading version file %s : %s ", versionFile.getPath(), e.getMessage());
      throw new RuntimeException(msg);
    }
    return versionString;
  }

  public static final int COLWIDTH = 70;

  public static final int INDENTWIDTH = 8;
}
