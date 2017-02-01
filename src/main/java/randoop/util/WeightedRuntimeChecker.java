package randoop.util;

public class WeightedRuntimeChecker {

  // Tests gets and sets for integration
  public static void main(String[] args) {
    System.out.println("Values for WeightedList");
    long res = 0;
    for (int j = 0; j < 100; j++) {
      res += test(new WeightedList<Object>());
    }
    System.out.println("WeightedList avg: " + res * 1.0 / 100);

    System.out.println("Values for Update WeightedList");
    res = 0;
    for (int j = 0; j < 100; j++) {
      res += testUpdateWeightedList(new WeightedList<Object>());
    }
    System.out.println("WeightedList Update avg: " + res * 1.0 / 100);

    System.out.println("Values for WeightedBalancedTree");
    res = 0;
    for (int j = 0; j < 100; j++) {
      res += test(new WeightedBalancedTree<Object>());
    }
    System.out.println("WeightedBalancedTree avg: " + res * 1.0 / 100);

    System.out.println("Values for update WeightedBalancedTree");
    res = 0;
    for (int j = 0; j < 100; j++) {
      res += testUpdateWeightedBalancedTree(new WeightedBalancedTree<Object>());
    }
    System.out.println("WeightedBalancedTree Update avg: " + res * 1.0 / 100);
  }

  public static long test(WeightedRandomSampler<Object> struct) {
    long start = System.nanoTime();
    for (int i = 1; i < 10000; i++) {
      int weight = i;
      struct.add(new Object(), weight);
      WeightedElement<Object> w = struct.getRandomElement();
    }
    long total = System.nanoTime() - start;
    System.out.println(total);
    return total;
    // TODO print this to a csv file.
  }

  // Tests the update in WeightedBalancedTree
  public static long testUpdateWeightedBalancedTree(WeightedBalancedTree<Object> struct) {
    Object root = new Object();
    struct.add(root, 1);
    long start = System.nanoTime();
    for (int i = 1; i < 10000; i++) {
      int weight = i;
      struct.add(new Object(), weight);
      WeightedElement<Object> w = struct.getRandomElement();
      struct.update(root, i + 1);
    }
    long total = System.nanoTime() - start;
    System.out.println(total);
    return total;
    // TODO print this to a csv file.
  }

  // Tests the update in WeightedList
  public static long testUpdateWeightedList(WeightedList<Object> struct) {
    WeightedElement<Object> root = new WeightedElement<>(new Object(), 1);
    struct.add(root);
    long start = System.nanoTime();
    for (int i = 1; i < 10000; i++) {
      int weight = i;
      struct.add(new Object(), weight);
      WeightedElement<Object> w = struct.getRandomElement();
      root.setWeight(i + 1);
      struct.update(root);
    }
    long total = System.nanoTime() - start;
    System.out.println(total);
    return total;
    // TODO print this to a csv file.
  }
}
