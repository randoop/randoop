package randoop.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Took me 1hr to generate results on lab unix pc
 * TODO: handling when file already exists, improve efficiency, implement related test cases,
 */
public class WeightedRuntimeChecker {
  private static final int NUMBER_OF_TESTS = 100;
  private static final int MAX_NUMBER_OF_ELEMENTS = 100000;
  private static int sizeOfStructs;

  // Tests runtime of the 2 ADT's methods
  public static void main(String[] args) {
    ArrayList<Double> weightedAddAvgListResults = new ArrayList<Double>();
    ArrayList<Double> weightedRandomAvgListResults = new ArrayList<Double>();
    ArrayList<Double> weightedUpdateAvgListResults = new ArrayList<Double>();
    ArrayList<Double> weightedAddAvgTreeResults = new ArrayList<Double>();
    ArrayList<Double> weightedRandomAvgTreeResults = new ArrayList<Double>();
    ArrayList<Double> weightedUpdateAvgTreeResults = new ArrayList<Double>();
    // initializing the roots
    WeightedRandomSampler<Integer> weightedList = new WeightedList<Integer>();
    WeightedRandomSampler<Integer> weightedTree = new WeightedBalancedTree<Integer>();
    Integer root = new Integer(1);
    weightedList.add(root, 1);
    weightedTree.add(root, 1);

    // Get some results over a range of increasing elements
    for (sizeOfStructs = 1; sizeOfStructs < MAX_NUMBER_OF_ELEMENTS; sizeOfStructs *= 2) {
      System.out.println("Running  methods for ADT's of size: " + sizeOfStructs);
      // fill the ADTs up to the sizeOfStructs
      weightedList = fillWeightedRandomSampler(weightedList);
      weightedTree = fillWeightedRandomSampler(weightedTree);

      long res = 0;
      for (int j = 0; j < NUMBER_OF_TESTS; j++) {
        res += testAdd(new WeightedList<Integer>());
      }
      double weightedAddAvgList = res * 1.0 / NUMBER_OF_TESTS;
      weightedAddAvgListResults.add(weightedAddAvgList);
      //System.out.println("WeightedList Add avg:\n\t" + weightedAddAvgList);

      res = 0;
      for (int j = 0; j < NUMBER_OF_TESTS; j++) {
        res += testGetRandom(weightedList);
      }
      double weightedRandomAvgList = res * 1.0 / NUMBER_OF_TESTS;
      weightedRandomAvgListResults.add(weightedRandomAvgList);
      //System.out.println("WeightedList GetRandom avg:\n\t" + weightedRandomAvgList);

      res = 0;
      for (int j = 0; j < NUMBER_OF_TESTS; j++) {
        res += testUpdateWeightedList(new WeightedList<Integer>());
      }
      double weightedUpdateAvgList = res * 1.0 / NUMBER_OF_TESTS;
      weightedUpdateAvgListResults.add(weightedUpdateAvgList);
      //System.out.println("WeightedList Update avg:\n\t" + weightedUpdateAvgList);

      //System.out.println();

      res = 0;
      for (int j = 0; j < NUMBER_OF_TESTS; j++) {
        res += testAdd(new WeightedBalancedTree<Integer>());
      }
      double weightedAddAvgTree = res * 1.0 / NUMBER_OF_TESTS;
      weightedAddAvgTreeResults.add(weightedAddAvgTree);
      //System.out.println("WeightedBalancedTree Add avg:\n\t" + weightedAddAvgTree);

      res = 0;
      for (int j = 0; j < NUMBER_OF_TESTS; j++) {
        res += testGetRandom(weightedTree);
      }
      double weightedRandomAvgTree = res * 1.0 / NUMBER_OF_TESTS;
      weightedRandomAvgTreeResults.add(weightedRandomAvgTree);
      //System.out.println("WeightedBalancedTree GetRandom avg:\n\t" + weightedRandomAvgTree);

      res = 0;
      for (int j = 0; j < NUMBER_OF_TESTS; j++) {
        res += testUpdateWeightedBalancedTree(new WeightedBalancedTree<Integer>());
      }
      double weightedUpdateAvgTree = res * 1.0 / NUMBER_OF_TESTS;
      weightedUpdateAvgTreeResults.add(weightedUpdateAvgTree);
      //System.out.println("WeightedBalancedTree Update avg:\n\t" + weightedUpdateAvgTree);
    }
    try {
      write(
          weightedAddAvgListResults,
          weightedRandomAvgListResults,
          weightedUpdateAvgListResults,
          weightedAddAvgTreeResults,
          weightedRandomAvgTreeResults,
          weightedUpdateAvgTreeResults);
    } catch (FileNotFoundException e) {
      System.out.println("Could not write to csv file");
      e.printStackTrace();
    }
  }

  // Fills the input with additional objects up to the sizeOfStructs
  public static WeightedRandomSampler<Integer> fillWeightedRandomSampler(
      WeightedRandomSampler<Integer> input) {
    // Construct the struct
    for (int i = input.getSize(); i < sizeOfStructs; i++) {
      int weight = i;
      input.add(new Integer(1), weight);
    }
    return input;
  }

  public static long testAdd(WeightedRandomSampler<Integer> struct) {
    long start = System.nanoTime();
    for (int i = 1; i < sizeOfStructs; i++) {
      int weight = i;
      struct.add(new Integer(1), weight);
    }
    long total = System.nanoTime() - start;
    return total;
  }

  // Tests the update in WeightedBalancedTree
  // This also must create and fill a new WeightedBalancedTree, but the time is only updating
  public static long testUpdateWeightedBalancedTree(WeightedBalancedTree<Integer> struct) {
    // Need to construct the struct, because we need reference to the exact root
    WeightedElement<Integer> testNode = new WeightedElement<>(new Integer(1), 1);
    // Construct the struct
    struct.add(testNode);
    for (int i = 1; i < sizeOfStructs; i++) {
      int weight = i;
      WeightedElement<Integer> node = new WeightedElement<>(new Integer(1), i);
      struct.add(node, weight);
      testNode = node;
    }
    //TODO: Test: root =  new Integer(); -> IllegalArgumentException: Integer is not in set of nodes, but not always verified due to hashmap?

    // Now update
    long start = System.nanoTime();
    for (int i = 1; i < sizeOfStructs; i++) {
      struct.update(testNode, i + 1);
    }
    long total = System.nanoTime() - start;
    return total;
  }

  // Tests the update in WeightedList
  // This also must create and fill a new WeightedList, but the time is only updating
  public static long testUpdateWeightedList(WeightedList<Integer> struct) {
    WeightedElement<Integer> root = new WeightedElement<>(new Integer(1), 1);
    // Need to construct the struct, because we need reference to the exact root
    //TODO: Test: struct.add(root, 1); --> also leads to index out of bounds
    struct.add(root);
    for (int i = 1; i < sizeOfStructs; i++) {
      int weight = i;
      struct.add(new Integer(1), weight);
    }
    //TODO:  Test: root = new WeightedElement<>(new Integer(), 1); --> leads to index out of bounds
    // Now update
    long start = System.nanoTime();
    for (int i = 1; i < sizeOfStructs; i++) {
      int weight = i;
      struct.update(root);
    }
    long total = System.nanoTime() - start;
    return total;
  }

  // Tests the getRandomElement() sizeOfStructs times
  public static long testGetRandom(WeightedRandomSampler<Integer> struct) {
    // Test the random
    long start = System.nanoTime();
    for (int i = 0; i < sizeOfStructs; i++) {
      WeightedElement<Integer> w = struct.getRandomElement();
    }
    long total = System.nanoTime() - start;
    return total;
  }

  // Write the results to csv format
  private static void write(
      ArrayList<Double> weightedAddAvgListResults,
      ArrayList<Double> weightedRandomAvgListResults,
      ArrayList<Double> weightedUpdateAvgListResults,
      ArrayList<Double> weightedAddAvgTreeResults,
      ArrayList<Double> weightedRandomAvgTreeResults,
      ArrayList<Double> weightedUpdateAvgTreeResults)
      throws FileNotFoundException {
    System.out.println("Writing results to ~/runtimeResults.csv");
    PrintWriter pw = new PrintWriter(new File("runtimeResults.csv"));
    StringBuilder sb = new StringBuilder();
    sb.append("sizeOfStructs");
    sb.append(',');
    sb.append("weightedAddAvgListResults (ns)");
    sb.append(',');
    sb.append("weightedRandomAvgListResults (ns)");
    sb.append(',');
    sb.append("weightedUpdateAvgListResults (ns)");
    sb.append(',');
    sb.append("weightedAddAvgTreeResults (nsec)");
    sb.append(',');
    sb.append("weightedRandomAvgTreeResults (ns)");
    sb.append(',');
    sb.append("weightedUpdateAvgTreeResults (ns)");
    sb.append('\n');
    for (int i = 0; i < weightedAddAvgListResults.size(); i++) {
      double n = Math.pow(2, i);
      sb.append(n);
      sb.append(',');
      sb.append(weightedAddAvgListResults.get(i));
      sb.append(',');
      sb.append(weightedRandomAvgListResults.get(i));
      sb.append(',');
      sb.append(weightedUpdateAvgListResults.get(i));
      sb.append(',');
      sb.append(weightedAddAvgTreeResults.get(i));
      sb.append(',');
      sb.append(weightedRandomAvgTreeResults.get(i));
      sb.append(',');
      sb.append(weightedUpdateAvgTreeResults.get(i));
      sb.append('\n');
    }

    // generate second table, same values but in seconds
    sb.append('\n');
    sb.append("sizeOfStructs");
    sb.append(',');
    sb.append("weightedAddAvgListResults (sec)");
    sb.append(',');
    sb.append("weightedRandomAvgListResults (sec)");
    sb.append(',');
    sb.append("weightedUpdateAvgListResults (sec)");
    sb.append(',');
    sb.append("weightedAddAvgTreeResults (sec)");
    sb.append(',');
    sb.append("weightedRandomAvgTreeResults (sec)");
    sb.append(',');
    sb.append("weightedUpdateAvgTreeResults (sec)");
    sb.append('\n');
    for (int i = 0; i < weightedAddAvgListResults.size(); i++) {
      double n = Math.pow(2, i);
      double nanoPerSec = 1000000000.0; // 1e9 ns in a sec
      sb.append(n);
      sb.append(',');
      sb.append(weightedAddAvgListResults.get(i) / nanoPerSec);
      sb.append(',');
      sb.append(weightedRandomAvgListResults.get(i) / nanoPerSec);
      sb.append(',');
      sb.append(weightedUpdateAvgListResults.get(i) / nanoPerSec);
      sb.append(',');
      sb.append(weightedAddAvgTreeResults.get(i) / nanoPerSec);
      sb.append(',');
      sb.append(weightedRandomAvgTreeResults.get(i) / nanoPerSec);
      sb.append(',');
      sb.append(weightedUpdateAvgTreeResults.get(i) / nanoPerSec);
      sb.append('\n');
    }
    pw.write(sb.toString());
    pw.close();
    System.out.println("Done!");
  }
}
