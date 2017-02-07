package muse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A test input inspired by MUSE project.
 */
public class SortContainer {
/*
  public static void main(String[] args) {
    List<Integer> list = makeAListOfLengthAtMost100(20);
    List<Integer> sorted = sort(list);
    for (int elem : sorted) {
      System.out.println(elem);
    }
  }
*/
  public static List<Integer> sortIntegerList(List<Integer> unsorted) {
    List<Integer> list = new ArrayList<>(unsorted);
    Collections.sort(list);
    return list;
  }

  public static List<Integer> sortIntegerArray(Integer[] unsorted) {
    List<Integer> list = new ArrayList<>();
    Collections.addAll(list, unsorted);
    Collections.sort(list);
    return list;
  }

  public static List<Integer> sortIntArray(int[] unsorted) {
    List<Integer> list = new ArrayList<>();
    for (int i : unsorted) {
      list.add(i);
    }
    Collections.sort(list);
    return list;
  }

  public static List<String> sortStringArray(String[] unsorted) {
    List<String> list = new ArrayList<>();
    Collections.addAll(list, unsorted);
    Collections.sort(list);
    return list;
  }

  public static <T extends Comparable<T>> List<T> sort(List<T> unsorted) {
    List<T> list = new ArrayList<>(unsorted);
    Collections.sort(list);
    return list;
  }

/*
  public static List<Integer> makeAListOfLengthAtMost100(int length) {
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < length % 100; i++) {
      list.add((i * 5081 + 1) % 2819);
    }
    return list;
  }
  */
}
