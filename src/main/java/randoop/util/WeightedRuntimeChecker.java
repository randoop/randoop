package randoop.util;

public class WeightedRuntimeChecker {
  private static final int NUMBER_OF_TESTS = 100;
  private static final int SIZE_OF_STRUCTS = 10000;

  // Tests gets and sets for integration
  public static void main(String[] args) {
    WeightedRandomSampler<Object> weightedList =
        fillWeightedRandomSampler(new WeightedList<Object>());
    WeightedRandomSampler<Object> weightedTree =
        fillWeightedRandomSampler(new WeightedBalancedTree<Object>());

    long res = 0;
    for (int j = 0; j < NUMBER_OF_TESTS; j++) {
      res += testAdd(new WeightedList<Object>());
    }
    System.out.println("WeightedList Add avg:\n\t" + res * 1.0 / NUMBER_OF_TESTS);

    res = 0;
    for (int j = 0; j < NUMBER_OF_TESTS; j++) {
      res += testGetRandom(weightedList);
    }
    System.out.println("WeightedList GetRandom avg:\n\t" + res * 1.0 / NUMBER_OF_TESTS);

    res = 0;
    for (int j = 0; j < NUMBER_OF_TESTS; j++) {
      res += testUpdateWeightedList(new WeightedList<Object>());
    }
    System.out.println("WeightedList Update avg:\n\t" + res * 1.0 / NUMBER_OF_TESTS);

    System.out.println();

    res = 0;
    for (int j = 0; j < NUMBER_OF_TESTS; j++) {
      res += testAdd(new WeightedBalancedTree<Object>());
    }
    System.out.println("WeightedBalancedTree Add avg:\n\t" + res * 1.0 / NUMBER_OF_TESTS);

    res = 0;
    for (int j = 0; j < NUMBER_OF_TESTS; j++) {
      res += testGetRandom(weightedTree);
    }
    System.out.println("WeightedBalancedTree GetRandom avg:\n\t" + res * 1.0 / NUMBER_OF_TESTS);

    res = 0;
    for (int j = 0; j < NUMBER_OF_TESTS; j++) {
      res += testUpdateWeightedBalancedTree(new WeightedBalancedTree<Object>());
    }
    System.out.println("WeightedBalancedTree Update avg:\n\t" + res * 1.0 / NUMBER_OF_TESTS);
  }

  // Fills the input with objects
  public static WeightedRandomSampler<Object> fillWeightedRandomSampler(
      WeightedRandomSampler<Object> input) {
    Object root = new Object();
    // Construct the struct
    input.add(root, 1);
    for (int i = 1; i < SIZE_OF_STRUCTS; i++) {
      int weight = i;
      input.add(new Object(), weight);
    }
    return input;
  }

  public static long testAdd(WeightedRandomSampler<Object> struct) {
    long start = System.nanoTime();
    for (int i = 1; i < SIZE_OF_STRUCTS; i++) {
      int weight = i;
      struct.add(new Object(), weight);
    }
    long total = System.nanoTime() - start;
    return total;
    // TODO print this to a csv file.
  }

  // Tests the update in WeightedBalancedTree
  // This also must create and fill a new WeightedBalancedTree, but the time is only updating
  public static long testUpdateWeightedBalancedTree(WeightedBalancedTree<Object> struct) {
    // Need to construct the struct, because we need reference to the exact root
    Object root = new Object();
    // Construct the struct
    struct.add(root, 1);
    for (int i = 1; i < SIZE_OF_STRUCTS; i++) {
      int weight = i;
      struct.add(new Object(), weight);
    }
    //TODO: Test: root =  new Object(); -> IllegalArgumentException: Object is not in set of nodes, but not always verified due to hashmap?

    // Now update
    long start = System.nanoTime();
    for (int i = 1; i < SIZE_OF_STRUCTS; i++) {
      struct.update(root, i + 1);
    }
    long total = System.nanoTime() - start;
    return total;
    // TODO print this to a csv file.
  }

  // Tests the update in WeightedList
  // This also must create and fill a new WeightedList, but the time is only updating
  public static long testUpdateWeightedList(WeightedList<Object> struct) {
    WeightedElement<Object> root = new WeightedElement<>(new Object(), 1);
    // Need to construct the struct, because we need reference to the exact root
    //TODO: Test: struct.add(root, 1); --> also leads to index out of bounds
    struct.add(root);
    for (int i = 1; i < SIZE_OF_STRUCTS; i++) {
      int weight = i;
      struct.add(new Object(), weight);
    }
    //TODO:  Test: root = new WeightedElement<>(new Object(), 1); --> leads to index out of bounds
    // Now update
    long start = System.nanoTime();
    for (int i = 1; i < SIZE_OF_STRUCTS; i++) {
      int weight = i;
      struct.update(root);
    }
    long total = System.nanoTime() - start;
    return total;
    // TODO print this to a csv file.
  }

  // Tests the getRandomElement() in WeightedBalancedTree
  public static long testGetRandom(WeightedRandomSampler<Object> struct) {
    // Test the random
    long start = System.nanoTime();
    for (int i = 1; i < SIZE_OF_STRUCTS; i++) {
      WeightedElement<Object> w = struct.getRandomElement();
    }
    long total = System.nanoTime() - start;
    return total;
    // TODO print this to a csv file.
  }
}
