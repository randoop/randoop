package randoop.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.plumelib.util.UtilPlume;
import randoop.Globals;

public final class CollectionsExt {

  private CollectionsExt() {
    throw new IllegalStateException("no instances");
  }

  static <T> T getNthIteratedElement(Collection<? extends T> s, int index) {
    if (s == null) throw new IllegalArgumentException("s cannot be null.");
    if (s.isEmpty()) throw new IllegalArgumentException("s cannot be empty.");
    if (index >= s.size()) {
      throw new IllegalArgumentException(
          "Index " + index + " out of bounds for set of size " + s.size());
    }
    return getNthIteratedElement(s.iterator(), index);
  }

  private static <T> T getNthIteratedElement(Iterator<? extends T> iter, int index) {
    if (index < 0) throw new IllegalArgumentException("Index " + index + " out of bounds");
    int counter = 0;
    while (iter.hasNext()) {
      if (counter == index) {
        return iter.next();
      }
      iter.next();
      counter++;
    }
    throw new IllegalArgumentException("index " + index + " out of bounds, size=" + counter);
  }

  /**
   * Returns the String.valueOf() of all elements of the collection, one on each line.
   *
   * @param c the collection of objects to include in string
   * @return the concatenated string of object strings as lines
   */
  static String toStringInLines(Collection<?> c) {
    if (c.isEmpty()) {
      return "";
    }
    return UtilPlume.joinLines(toStringLines(c)) + Globals.lineSep;
  }

  /**
   * List of String.valueOf() of all elements of the collection.
   *
   * @param c the collection of objects to include in string
   * @return the concatenated string of object strings
   */
  private static List<String> toStringLines(Collection<?> c) {
    List<String> lines = new ArrayList<>(c.size());
    for (Object each : c) {
      lines.add(String.valueOf(each));
    }
    return lines;
  }
}
