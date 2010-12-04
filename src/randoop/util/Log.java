package randoop.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import randoop.Globals;
import randoop.Sequence;
import randoop.StatementKind;
import randoop.main.GenInputsAbstract;

public final class Log {

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
      GenInputsAbstract.log.write(s);
      GenInputsAbstract.log.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void logLine(String s) {
    if (! isLoggingOn()) return;

    try {
      GenInputsAbstract.log.write(s);
      GenInputsAbstract.log.write(Globals.lineSep);
      GenInputsAbstract.log.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void log(Sequence s) {
    if (!isLoggingOn()) return;

    try {
      GenInputsAbstract.log.write(Globals.lineSep + Globals.lineSep);
      GenInputsAbstract.log.write(s.toString());
      GenInputsAbstract.log.flush();

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void log(List<StatementKind> model) {
    if (! isLoggingOn()) return;

    try {
      GenInputsAbstract.log.write("Statements : " + Globals.lineSep);
      for (StatementKind t : model) {
        GenInputsAbstract.log.write(t.toString());
        GenInputsAbstract.log.write(Globals.lineSep);
        GenInputsAbstract.log.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static boolean isLoggingOn() {
    return GenInputsAbstract.log != null;
  }
}
