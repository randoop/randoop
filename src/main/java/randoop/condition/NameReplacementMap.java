package randoop.condition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import plume.UtilMDE;

/** A map from a {@code String} name to another name. */
public class NameReplacementMap {

  /** The underlying {@code Map} */
  private final Map<String, String> replacements;

  /** Creates a new {@link NameReplacementMap} with an empty map. */
  NameReplacementMap() {
    this.replacements = new LinkedHashMap<>();
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
    Pattern namesPattern =
        Pattern.compile("\\b(" + UtilMDE.join(replacements.keySet().toArray(), "|") + ")\\b");
    Matcher namesMatcher = namesPattern.matcher(text);
    StringBuilder b = new StringBuilder();
    int position = 0;
    while (namesMatcher.find(position)) {
      b.append(text.substring(position, namesMatcher.start(1)));
      b.append(replacements.get(namesMatcher.group(1)));
      position = namesMatcher.end(1);
    }
    b.append(text.substring(position));
    return b.toString();
  }
}
