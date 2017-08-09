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
   * <p>This method is not guaranteed to work correctly if there are replacements that are cyclic or
   * whose application is order dependent, such as {@code "a"=>"b"} and {@code "b"=>"c"}.
   *
   * @param text the text to search for occurrences of names in this map
   * @return the text modified by replacing original names with replacement names
   */
  String replaceNames(String text) {
    String nameString = UtilMDE.join(replacements.keySet().toArray(), "|");
    Pattern namePattern = Pattern.compile("\\b(" + nameString + ")\\b");
    Matcher nameMatcher = namePattern.matcher(text);
    StringBuilder b = new StringBuilder();

    int position = 0;
    while (nameMatcher.find(position)) {
      int previousPosition = position;
      String name = nameMatcher.group(1);
      position = nameMatcher.start(1);
      b.append(text.substring(previousPosition, position)).append(replacements.get(name));
      position += name.length();
    }
    b.append(text.substring(position));
    return b.toString();
  }
}
