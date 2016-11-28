package randoop.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import plume.UtilMDE;
import randoop.Globals;

public final class CollectionsExt {

  private CollectionsExt() {
    throw new IllegalStateException("no instances");
  }

  static <T> T getNthIteratedElement(Collection<? extends T> s, int index) {
    if (s == null) throw new IllegalArgumentException("s cannot be null.");
    if (s.isEmpty()) throw new IllegalArgumentException("s cannot be empty.");
    if (index >= s.size()) {
      throw new IllegalArgumentException("Index " + index + " invalid for set of size " + s.size());
    }
    return getNthIteratedElement(s.iterator(), index);
  }

  private static <T> T getNthIteratedElement(Iterator<? extends T> iter, int index) {
    if (index < 0) throw new IllegalArgumentException("Index " + index + " invalid");
    int counter = 0;
    while (iter.hasNext()) {
      if (counter == index) {
        return iter.next();
      }
      iter.next();
      counter++;
    }
    throw new IllegalArgumentException("invalid index:" + index + " size:" + counter);
  }

  /**
   * Prints out the String.valueOf() of all elements of the collection,
   * inserting a new line after each element. The order is specified by the
   * collection's iterator.
   *
   * @param c  the collection of objects to include in string
   * @return the concatenated string of object strings as lines
   */
  static String toStringInLines(Collection<?> c) {
    if (c.isEmpty()) return "";
    return UtilMDE.join(toStringLines(c), Globals.lineSep) + Globals.lineSep;
  }

  /**
   * List of String.valueOf() of all elements of the collection. The order is
   * specified by the collection's iterator.
   *
   * @param c  the collection of objects to include in string
   * @return the concatenated string of object strings
   */
  private static List<String> toStringLines(Collection<?> c) {
    List<String> lines = new ArrayList<>(c.size());
    for (Object each : c) {
      lines.add(String.valueOf(each));
    }
    return lines;
  }

  /**
   * Divides the argument into sublists of at most the given length.
   * All sublists except at most one will have length exactly {@code maxLength}.
   * No sublist will be empty.
   *
   * The result list is unmodifiable.
   * It does <em>not</em> copy the list and simply shares it.
   *
   * @param <T> the element type
   * @param list  the list to be partitioned
   * @param maxLength  the maximum length of a list partition
   * @return the partitioned list
   */
  public static <T> List<List<T>> formSublists(List<T> list, int maxLength) {
    if (maxLength <= 0) {
      throw new IllegalArgumentException("maxLength must be > 0 but was " + maxLength);
    }
    int numberOfFullSublists = list.size() / maxLength;

    List<List<T>> result = new ArrayList<>(numberOfFullSublists + 1);
    for (int i = 0; i < numberOfFullSublists; i++) {
      List<T> subList = list.subList(i * maxLength, (i + 1) * maxLength);
      if (subList.size() != maxLength) {
        throw new IllegalStateException(
            "the sublist length:" + subList.size() + " should be " + maxLength);
      }
      result.add(subList);
    }
    List<T> lastSublist = list.subList(numberOfFullSublists * maxLength, list.size());
    if (!lastSublist.isEmpty()) result.add(lastSublist);
    return Collections.unmodifiableList(result);
  }
}
