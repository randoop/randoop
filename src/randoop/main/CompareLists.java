package randoop.main;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import randoop.Globals;
import randoop.util.InformationalComparator;
import randoop.util.Log;
import randoop.util.SerializationHelper;
import randoop.util.SimpleList;
import randoop.util.InformationalComparator.ComparisonResult;





/**
 * Compares SimpleLists of objects for equality.
 * Prints message to stdout with summary of comparison.
 * If called with more than two arguments, compares the
 * first to each of the rest; it's OK if it only matches one.
 * If suites differ, exists with status 1.
 */
public class CompareLists {

  /**
   * See class comment.
   * @param args
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) {

    if (args.length < 2) {
      Log.out.println("This command takes at least two arguments.");
      printUsage(Log.out);
      System.exit(1);
    }

    String suite1 = args[0];
    SimpleList s1 = (SimpleList)SerializationHelper.readSerialized(suite1);

    List<String> errors = new ArrayList<String>();

    for (int i = 1; i < args.length; i++) {
      String suite2 = args[i];

      SimpleList s2 = (SimpleList)SerializationHelper.readSerialized(suite2);

      InformationalComparator c = new InformationalComparator();

      ComparisonResult r = c.compare(suite1, s1, suite2, s2);

      if (r.listsAreEqual) {
        System.out.println("Lists are equal.");
        System.exit(0);
      } else {
        errors.add(r.message);
      }
    }

    System.out.println("Lists are not equal." + Globals.lineSep + errors);
    System.exit(1);
  }

  private static void printUsage(PrintStream out) {
    out.println("Usage: java randoop.CompareLists <file1> <file>...");
    out.println();
    out.println("<file1> and <file> are files containing");
    out.println("serialized SimpleLists.");
  }

}
