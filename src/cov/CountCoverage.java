package cov;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * This class reads in a file with coverage data from a run, and computes
 * coverage information for classes that were instrumented using the coverage
 * instrumenter.
 * 
 * Reads a text file. Parses any lines that look like
 * 
 * COV:MyClass:c:t:n
 * 
 * Where
 * 
 * MyClass is the name of a class c is the number of branches covered in MyClass
 * t is the total number of branches in MyClass n is a unique id for a covered
 * branch, e.g. "26T"
 * 
 * Outputs the percent branch coverage computed from the lines.
 *
 * The cov package implements a basic branch coverage instrumenter that we use
 * for the branch-directed test generation research.
 *
 * This tool is prototype-quality, not for production use. In particular, it is
 * missing a number of features including tracking coverage for switch
 * statements, and lack of support for generics.
 */
public class CountCoverage {

  private static Map<String, Integer> totalBranches = new LinkedHashMap<String, Integer>();

  private static Map<String, Set<String>> coveredBranches = new LinkedHashMap<String, Set<String>>();

  public static void main(String[] args) throws IOException {

    BufferedReader reader = new BufferedReader(new FileReader(args[0]));
    String line = reader.readLine();
    while (line != null) {
      if (line.startsWith("COV")) {
        addCoverage(line);
      }
      line = reader.readLine();
    }
    printCoverage();

  }

  private static void printCoverage() {

    assert totalBranches.keySet().equals(coveredBranches.keySet());
    for (String cls : totalBranches.keySet()) {
      System.out.println("COVERAGE for " + cls + ":" + coveredBranches.get(cls).size() / (double) totalBranches.get(cls));
    }

  }

  private static void addCoverage(String line) {
    if (line == null)
      throw new IllegalArgumentException();
    if (!line.startsWith("COV"))
      throw new IllegalArgumentException(line);
    String[] tokens = line.split(":");
    if (tokens.length != 5)
      throw new IllegalArgumentException(line);
    if (!tokens[0].equals("COV"))
      throw new IllegalArgumentException(line);
    String className = tokens[1];

    int totBr = Integer.parseInt(tokens[3]);
    if (totalBranches.containsKey(className)) {
      assert totBr == totalBranches.get(className).intValue();
    } else {
      totalBranches.put(className, totBr);
    }

    Set<String> covBr = coveredBranches.get(className);
    if (covBr == null) {
      covBr = new LinkedHashSet<String>();
      coveredBranches.put(className, covBr);
    }

    String covUnit = tokens[4];
    covBr.add(covUnit);
  }

}
