package randoop.main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import randoop.StatementKind;
import randoop.util.Log;
import randoop.util.SerializationHelper;


/**
 * Outputs a model in a textual format to a desired file.
 *
 */
public final class RandoopModelToTxt {

  private RandoopModelToTxt() {
    throw new IllegalStateException("no instance");
  }

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {

    if (args.length != 2) {
      Log.out.println("This command takes exactly two arguments.");
      printUsage(Log.out);
      System.exit(1);
    }

    String modelFileName = args[0];
    String textFileName = args[1];

    PrintStream textStream = null;
    try {
      textStream = new PrintStream(textFileName);
    } catch (FileNotFoundException e) {
      Log.out
      .println("Exception thrown while creating text print stream:");
      e.printStackTrace();
      System.exit(1);
    }

    assert textStream != null;
    List<StatementKind> model = (List<StatementKind>) SerializationHelper.readSerialized(modelFileName);

    textStream.println(model.toString());

    textStream.close();
  }

  private static void printUsage(PrintStream out) {
    out.println("usage: java " + RandoopModelToTxt.class.getCanonicalName() + " <randoop model> <text model name>");
  }

}
