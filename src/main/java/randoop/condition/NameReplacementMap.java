package randoop.condition;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** A map from a {@code String} name to another name. */
public class NameReplacementMap {

  /** The underlying {@code Map} */
  private final Map<String, String> replacements;

  /** Creates a new {@link NameReplacementMap} with an empty map. */
  NameReplacementMap() {
    this.replacements = new HashMap<>();
  }

  /**
   * Adds a mapping from {@code originalName} to {@code replacementName}.
   *
   * @param originalName the name to be replaced
   * @param replacementName the replacing name
   * @return the previous value associated with {@code originalName}, or null if there was no such
   *     mapping
   */
  public String put(String originalName, String replacementName) {
    return replacements.put(originalName, replacementName);
  }

  /**
   * Replace occurrences of names from this map with corresponding replacements.
   *
   * @param text the text to search for occurrences of names in this map
   * @return the text modified by replacing original names with replacement names
   */
  String replaceNames(String text) {
    // make sure that we are replacing from longer to shorter strings to avoid mangled replacement
    Set<String> names = new TreeSet<>(new LengthComparator());
    names.addAll(replacements.keySet());
    for (String name : names) {
      String namePattern = "\\b" + name + "\\b";
      text = text.replaceAll(namePattern, replacements.get(name));
    }
    return text;
  }

  /** Comparator to order strings by decreasing length. */
  private static class LengthComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
      if (o1.length() < o2.length()) {
        return 1; // shorter last
      } else if (o1.length() > o2.length()) {
        return -1; // longer first
      }
      return o1.compareTo(o2);
    }
  }
}
