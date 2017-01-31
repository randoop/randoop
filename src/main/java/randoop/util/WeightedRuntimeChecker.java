package randoop.util;

public class WeightedRuntimeChecker {

    // Tests gets and sets for integration
    public static void main(String[] args) {
        System.out.println("Values for WeightedList");
        for (int j = 0; j < 100; j++) {
            long res = test(new WeightedList<Object>());
        }
        System.out.println("Values for Update WeightedList");
        for (int j = 0; j < 100; j++) {
            long res = testUpdate(new WeightedBalancedTree<Object>());
        }
        System.out.println("Values for WeightedBalancedTree");

        for (int j = 0; j < 100; j++) {
            long res = test(new WeightedBalancedTree<Object>());
        }

        System.out.println("Values for update WeightedBalancedTree");
        for (int j = 0; j < 100; j++) {
            long res = testUpdate(new WeightedBalancedTree<Object>());
        }
    }

    public static long test(WeightedRandomSampler<Object> struct) {
        Timer t = new Timer();
        t.startTiming();
        for (int i = 1; i < 10000; i++) {
            int weight = i;
            struct.add(new Object(), i);
            WeightedElement<Object> w = struct.getRandomElement();
        }
        t.stopTiming();
        System.out.println(t.getTimeElapsedMillis());
        return t.getTimeElapsedMillis();
        // TODO print this to a csv file.
    }

    public static long testUpdate(WeightedBalancedTree<Object> struct) {
        Timer t = new Timer();
        Object root = new Object();
        struct.add(root, 1);
        t.startTiming();
        for (int i = 1; i < 10000; i++) {
            int weight = i;
            struct.add(new Object(), i);
            WeightedElement<Object> w = struct.getRandomElement();
            struct.update(root, i + 1);
        }
        t.stopTiming();
        System.out.println(t.getTimeElapsedMillis());
        return t.getTimeElapsedMillis();
        // TODO print this to a csv file.
    }

    public static long testUpdate(WeightedList<Object> struct) {
        Timer t = new Timer();
        WeightedElement<Object> root = new WeightedElement<>(new Object(), 1);
        struct.add(root);
        t.startTiming();
        for (int i = 1; i < 10000; i++) {
            int weight = i;
            struct.add(new Object(), i);
            WeightedElement<Object> w = struct.getRandomElement();
            root.setWeight(i + 1);
            struct.update(root);
        }
        t.stopTiming();
        System.out.println(t.getTimeElapsedMillis());
        return t.getTimeElapsedMillis();
        // TODO print this to a csv file.
    }
}
