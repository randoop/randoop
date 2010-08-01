package randoop.util;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import randoop.Globals;
import randoop.Sequence;
import randoop.StatementKind;
import plume.Option;
import plume.OptionGroup;

public final class Log {

  /**
   * Name of a file to which to log lots of information.
   * If not specified, no logging is done.
   */
  @OptionGroup("Logging")
  @Option("<filename> Name of a file to which to log lots of information")
  public static FileWriter log = null;

  private Log() {
    throw new IllegalStateException("no instance");
  }

  public static final ByteArrayOutputStream bos;
  public static final PrintStream systemOutErrStream;
  public static final PrintStream err;
  public static final PrintStream out;

  static {
    bos = new ByteArrayOutputStream();
    systemOutErrStream = new PrintStream(bos);
    err = System.err;
    out = System.out;
  }

  public static void log(String s) {
    if (!isLoggingOn()) return;

    try {
      log.write(s);
      log.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void logLine(String s) {
    if (! isLoggingOn()) return;

    try {
      log.write(s);
      log.write(Globals.lineSep);
      log.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void log(Sequence s) {
    if (!isLoggingOn()) return;

    try {
      log.write(Globals.lineSep + Globals.lineSep);
      log.write(s.toString());
      log.flush();

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void log(List<StatementKind> model) {
    if (! isLoggingOn()) return;

    try {
      log.write("Statements : " + Globals.lineSep);
      for (StatementKind t : model) {
        log.write(t.toString());
        log.write(Globals.lineSep);
        log.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static boolean isLoggingOn() {
    return log != null;
  }
}
