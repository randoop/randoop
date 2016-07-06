package randoop.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import randoop.Globals;
import plume.Hasher;

public final class CollectionsExt {
  private CollectionsExt() {
    throw new IllegalStateException("no instances");
  }

  /**
   * Make a copy of the list with duplicates removed. The first elements of each
   * equivalence class will be retained (and in the original order)
   *
   * @param <T>  the list element type
   * @param lst  the list
   * @return the list of unique elements
   */
  public static <T> List<T> unique(List<T> lst) {
    return new ArrayList<>(new LinkedHashSet<>(lst));
  }

  /**
   * Return a unmodifiable list that has all elements of the given collection.
   *
   * @param <T> the element type
   * @param l  the collection
   * @return the unmodifiable list of objects from the collection
   */
  public static <T> List<T> roCopyList(Collection<T> l) {
    return Collections.unmodifiableList(new ArrayList<>(l));
  }

  @SuppressWarnings({"varargs", "unchecked"}) // heap pollution warning
  public static <T> Set<T> intersection(Set<? extends T>... sets) {
    if (sets.length == 0) return Collections.emptySet();
    Set<T> result = new LinkedHashSet<>();
    result.addAll(sets[0]);
    for (Set<? extends T> s : sets) {
      result.retainAll(s);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T> Set<T> xor(Set<? extends T> s1, Set<? extends T> s2) {
    return union(diff(s1, s2), diff(s2, s1));
  }

  public static <T> Set<T> diff(Set<? extends T> s1, Set<? extends T> s2) {
    Set<T> result = new LinkedHashSet<>();
    result.addAll(s1);
    result.removeAll(s2);
    return result;
  }

  @SuppressWarnings({"varargs", "unchecked"}) // heap pollution warning
  public static <T> Set<T> union(Set<? extends T>... sets) {
    Set<T> result = new LinkedHashSet<>();
    for (Set<? extends T> s : sets) {
      result.addAll(s);
    }
    return result;
  }

  /**
   * Returns an ArrayList that is a concatenation of the arguments. Elements of
   * arguments are copied, the argument lists are NOT shared.
   *
   * @param <T>  the element type
   * @param lists  the list of lists to be concatenated
   * @return the concatenated list
   */
  @SuppressWarnings({"varargs", "unchecked"}) // heap pollution warning
  public static <T> List<T> concat(List<? extends T>... lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists) {
      result.addAll(list);
    }
    return result;
  }

  /**
   * Returns an ArrayList that is a concatenation of the arguments. Elements of
   * arguments are copied, the argument lists are NOT shared.
   *
   * @param <T>  the element type
   * @param lists  the collection of lists to be concatenated
   * @return the concatenated list
   */
  public static <T> List<T> concatAll(Collection<? extends List<? extends T>> lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists) {
      result.addAll(list);
    }
    return result;
  }

  public static <T> T getNthIteratedElement(Collection<? extends T> s, int index) {
    if (s == null) throw new IllegalArgumentException("s cannot be null.");
    if (s.isEmpty()) throw new IllegalArgumentException("s cannot be empty.");
    if (index >= s.size())
      throw new IllegalArgumentException("Index " + index + " invalid for set of size " + s.size());
    return getNthIteratedElement(s.iterator(), index);
  }

  public static <T> T getNthIteratedElement(Iterator<? extends T> iter, int index) {
    if (index < 0) throw new IllegalArgumentException("Index " + index + " invalid");
    int counter = 0;
    for (Iterator<? extends T> i = iter; i.hasNext(); ) {
      if (counter == index) {
        return i.next();
      }
      i.next();
      counter++;
    }
    throw new IllegalArgumentException("invalid index:" + index + " size:" + counter);
  }

  public static SortedSet<Integer> findAll(List<?> list, Object elem) {
    if (list == null) throw new IllegalArgumentException("list cannot be null.");
    SortedSet<Integer> result = new TreeSet<>();
    for (int i = 0, n = list.size(); i < n; i++) {
      if (list.get(i).equals(elem)) {
        result.add(i);
      }
    }

    return Collections.unmodifiableSortedSet(result);
  }

  /**
   * Prints out the String.valueOf() of all elements of the collection,
   * inserting a new line after each element. The order is specified by the
   * collection's iterator.
   *
   * @param c  the collection of objects to include in string
   * @return the concatenated string of object strings as lines
   */
  public static String toStringInLines(Collection<?> c) {
    if (c.isEmpty()) return "";
    return join(Globals.lineSep, toStringLines(c)) + Globals.lineSep;
  }

  /**
   * Prints out the elements of the collection in lines, in lexicographic order
   * of String.valueOf called on each element.
   *
   * @param c  the collection of objects to include in string
   * @return the concatenated string of object strings as lines sorted lexicographically
   */
  public static String toStringInSortedLines(Collection<?> c) {
    if (c.isEmpty()) return "";
    return join(Globals.lineSep, sort(toStringLines(c))) + Globals.lineSep;
  }

  /**
   * List of String.valueOf() of all elements of the collection. The order is
   * specified by the collection's iterator.
   *
   * @param c  the collection of objects to include in string
   * @return the concatenated string of object strings
   */
  public static List<String> toStringLines(Collection<?> c) {
    List<String> lines = new ArrayList<>(c.size());
    for (Object each : c) {
      lines.add(String.valueOf(each));
    }
    return lines;
  }

  /**
   * Sort and return the list. Useful for chaining the call.
   *
   * @param strings  the list of strings
   * @return the sorted list of strings
   */
  public static List<String> sort(List<String> strings) {
    Collections.sort(strings);
    return strings;
  }

  /**
   * Divides the argument into chunks of at most the given length. All chunks
   * except at most one will have length exactly maxLength. No chunks are empty.
   *
   * The result list is unmodifiable. It does <em>not</em> copy the list and
   * simply shares it.
   *
   * @param <T> the element type
   * @param list  the list to be partitioned
   * @param maxLength  the maximum length of a list partition
   * @return the partitioned list
   */
  public static <T> List<List<T>> chunkUp(List<T> list, int maxLength) {
    if (maxLength <= 0)
      throw new IllegalArgumentException("maxLength must be > 0 but was " + maxLength);
    int fullChunks = list.size() / maxLength;

    List<List<T>> result = new ArrayList<>(fullChunks + 1);
    for (int i = 0; i < fullChunks; i++) {
      List<T> subList = list.subList(i * maxLength, (i + 1) * maxLength);
      if (subList.size() != maxLength)
        throw new IllegalStateException("bogus length:" + subList.size() + " not " + maxLength);
      result.add(subList);
    }
    List<T> lastChunk = list.subList(fullChunks * maxLength, list.size());
    if (!lastChunk.isEmpty()) result.add(lastChunk);
    return Collections.unmodifiableList(result);
  }

  public static <T> Set<T> getAll(Enumeration<T> e) {
    Set<T> result = new LinkedHashSet<>();
    while (e.hasMoreElements()) result.add(e.nextElement());
    return result;
  }

  /**
   * Removed from the collection all strings that match the given pattern.
   * Returns the modified collection, for easier chaining.
   *
   * @param <T> the string collection type
   * @param pattern  the string pattern
   * @param strings  the string collection
   * @return the filtered string collection
   */
  public static <T extends Collection<String>> T removeMatching(String pattern, T strings) {
    for (Iterator<String> iter = strings.iterator(); iter.hasNext(); ) {
      String s = iter.next();
      if (s.matches(pattern)) iter.remove();
    }
    return strings;
  }

  /**
   * Reverse of String.split. Glues together the strings and inserts the
   * separator between each consecutive pair.
   *
   * @param separator  the separator between consecutive strings
   * @param strings  the strings to be joined
   * @return the concatenated string
   */
  public static String join(String separator, List<String> strings) {
    StringBuilder sb = new StringBuilder();
    for (Iterator<String> iter = strings.iterator(); iter.hasNext(); ) {
      String s = iter.next();
      sb.append(s);
      if (iter.hasNext()) sb.append(separator);
    }
    return sb.toString();
  }

  /**
   * Adds prefix to each line.
   *
   * @param prefix  the prefix to add
   * @param lines  the lines
   * @return the lines with prefix added
   */
  public static List<String> prefix(String prefix, List<String> lines) {
    List<String> result = new ArrayList<>(lines.size());
    for (String line : lines) {
      result.add(prefix + line);
    }
    return result;
  }

  /**
   * Returns whether the sets are disjoint.
   *
   * XXX bogus for empty sets
   *
   * @param ss  the sets
   * @return true if the sets are disjoint, false otherwise
   */
  public static boolean areDisjoint(Set<?>... ss) {
    if (ss == null || ss.length == 0) return true;
    int elementCount = 0;
    for (Set<?> set : ss) {
      elementCount += set.size();
    }
    return CollectionsExt.union(ss).size() == elementCount;
  }

  /**
   * Creates a set of all sequences of length max of objects. The order matters
   * and elements may be repeated. The set is grouped by the number of objects
   * in the array. The result is a map from arities to object sequences.
   *
   * NOTE: This is done with arrays rather than lists because in JOE we don't
   * want to execute any code of the objects.
   *
   * @param objects  the objects
   * @param max  the maximum length of generated sets
   * @return the map from cardinality to sets of that size
   */
  public static Map<Integer, Set<Object[]>> createPerArityGroups(Object[] objects, int max) {
    Map<Integer, Set<Object[]>> result = new LinkedHashMap<>();
    result.put(0, Collections.singleton(new Object[0]));
    for (int i = 1; i <= max; i++) {
      Set<Object[]> newSet = new LinkedHashSet<>();
      // add each object to each one smaller
      for (Object[] oneSmaller : result.get(i - 1)) {
        for (Object object : objects) {
          newSet.add(CollectionsExt.addToArray(oneSmaller, object));
        }
      }
      result.put(i, newSet);
    }
    return result;
  }

  /**
   * Creates and returns an array that contains all elements of the array
   * parameter and has the el parameter appened.
   *
   * The runtime type of the resulting array is the same as the type of the
   * argument array.
   *
   * @param <T>  the element type
   * @param array  the array
   * @param el  the object to append
   * @return the array with the new object appended
   */
  @SuppressWarnings("unchecked") // nothing we can do here, we must cast
  public static <T> T[] addToArray(T[] array, T el) {
    T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
    System.arraycopy(array, 0, newArray, 0, array.length);
    newArray[array.length] = el;
    return newArray;
  }

  /**
   * Returns whether all objects are instances of a given class.
   *
   * @param clazz the classes to check
   * @param objs  the objects to check
   * @return true if each object is an instance of one of the given classes
   */
  public static boolean allInstancesOf(Class<?> clazz, Object[] objs) {
    for (Object obj : objs) {
      if (!clazz.isInstance(obj)) return false;
    }
    return true;
  }

  /**
   * For a given list of list of objects, create and return the (exponential in
   * size) list of list of objects that comes from combining the every element
   * from the first list with every element from the second list etc. Eg for
   * input [[1,2],[a,b]] this returns [[1,a][1,b],[2,a],[2,b]]
   *
   * For empty input, returns a list with one element: empty list. Requires: all
   * lists non-empty
   *
   * @param <T>  the list type
   * @param inputs  the list of lists
   * @return product of the lists
   */
  public static <T> List<List<T>> allCombinations(List<List<T>> inputs) {
    if (inputs.isEmpty()) {
      ArrayList<List<T>> result = new ArrayList<>(1);
      result.add(new ArrayList<T>(1));
      return result;
    }
    List<T> lastList = inputs.get(inputs.size() - 1);

    List<List<T>> result = new ArrayList<>();
    for (int i = 0; i < lastList.size(); i++) {
      List<List<T>> tails = allCombinations(inputs.subList(0, inputs.size() - 1));
      T x = lastList.get(i);
      for (List<T> tail : tails) {
        tail.add(x);
      }
      result.addAll(tails);
    }
    return result;
  }

  /**
   * Returns true iff the collection contains the element. Objects are compared
   * by identity (==).
   *
   * @param <T>  the collection type
   * @param c  the collection of Objects
   * @param o the object for which to search
   * @return true if the object occurs in the collection
   */
  public static <T> boolean containsIdentical(Collection<T> c, Object o) {
    for (T t : c) {
      if (t == o) return true;
    }
    return false;
  }

  /**
   * Returns true iff the collection contains an element equivalent to the given
   * one, as decided by the hasher.
   *
   * @param <T>  the collection type
   * @param c  the collection
   * @param o  the object
   * @param h  the hasher
   * @return true if the collection contains the hashed value of the object
   */
  public static <T> boolean containsEquivalent(Collection<T> c, Object o, Hasher h) {
    for (T t : c) {
      if (h.equals(t, o)) return true;
    }
    return false;
  }

  /**
   * Maps all values to new values. Returns a new list.
   *
   * @param <T>  the collection type
   * @param values  the list to map
   * @param map  the map to apply
   * @return the list formed by mapping values of original list
   */
  public static <T> List<T> map(List<T> values, Map<T, T> map) {
    List<T> result = new ArrayList<>(values.size());
    for (T t : values) {
      result.add(map.get(t));
    }
    return result;
  }
}
