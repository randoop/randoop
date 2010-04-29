package randoop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import plume.UtilMDE;

/**
 * Maintains a mapping from StatNames to positive integers; used in various
 * places to maintain statistics about Randoop's behavior.
 */
public class StatsForMethod implements Serializable {

  private static final long serialVersionUID = 1L;

  private Map<StatName, Long> countMap;

  private List<StatName> keys;

  private StatementKind statement;

  /**
   * Create a collection that will hold counts for the given keys. The keys
   * must all be non-null.
   */
  public StatsForMethod(StatementKind statement, StatName... initialKeys) {
    this.countMap = new LinkedHashMap<StatName, Long>();
    this.keys = new ArrayList<StatName>();
    this.statement = statement;
    for (StatName key : initialKeys) {
      if (key == null)
        throw new IllegalArgumentException("no key can be null.");
      addKey(key);
      countMap.put(key, (long) 0);
    }
  }

  public void addKey(StatName newKey) {
    if (newKey == null)
      throw new IllegalArgumentException("newKey cannot be null.");
    if (keys.contains(newKey))
      throw new IllegalArgumentException("key already in key set.");
    keys.add(newKey);
    countMap.put(newKey, (long) 0);
  }

  private static int dummycounter = 0;

  public static StatName getSeparator() {
    String name = "SEP" + dummycounter++;
    return new StatName(name, name, name, true /* always printable */);
  }

  public void addToCount(StatName key, long count) {
    if (key == null)
      throw new IllegalArgumentException("keys cannot be null.");
    if (!keys.contains(key)) {
      throw new IllegalArgumentException("key not in key set: " + keys);
    }
    if (count < 0)
      throw new IllegalArgumentException("count must be > 0.");
    Long i = countMap.get(key);
    if (i == null)
      throw new BugInRandoopException();
    long currentCount = i.longValue();
    countMap.put(key, currentCount + count);
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(UtilMDE.rpad(statement.toString(), 40));
    for (StatName s : getKeys()) {
      if (s.longName.startsWith("SEP")) {
        b.append("| ");
        continue;
      }
      if (!s.printable)
        continue;
      b.append(UtilMDE.rpad(Long.toString(getCount(s)), 7));
    }
    return b.toString();
  }

  public String toStringWithKeys() {
    StringBuilder b = new StringBuilder();
    for (StatName name : keys) {
      if (name.longName.startsWith("SEP")) {
        b.append("| ");
      } else {
        b.append(name.shortName + "=" + countMap.get(name) + " ");
      }
    }
    return b.toString();
  }

  public String getTitle() {
    StringBuilder title = new StringBuilder();
    StringBuilder titleWithLines = new StringBuilder();
    title.append(UtilMDE.rpad("", 40));
    for (StatName n : getKeys()) {
      if (n.longName.startsWith("SEP")) {
        title.append("| ");
        continue;
      }
      if (!n.printable)
        continue;
      title.append(UtilMDE.rpad(n.shortName, 7));
    }
    int length = title.length();
    addLine(titleWithLines, length);
    titleWithLines.append(title);
    titleWithLines.append(Globals.lineSep);
    addLine(titleWithLines, length);
    return titleWithLines.toString();
  }

  public String keyExplanationString() {
    StringBuilder b = new StringBuilder();
    for (StatName n : getKeys()) {
      if (n.longName.startsWith("SEP"))
        continue;
      if (!n.printable)
        continue;
      b.append(UtilMDE.rpad(n.shortName, 8));
      b.append(": ");
      b.append(n.explanation);
      b.append(Globals.lineSep);
    }
    return b.toString();
  }

  public List<StatName> getKeys() {
    return Collections.unmodifiableList(keys);
  }

  /** Returns the values, in the same key order returned by getKeys() */
  public List<Long> getCounts() {
    List<Long> ret = new ArrayList<Long>();
    for (StatName s : getKeys()) {
      ret.add(getCount(s));
    }
    return ret;
  }

  public long getCount(StatName key) {
    if (key == null)
      throw new IllegalArgumentException("keys cannot be null.");
    if (!keys.contains(key))
      throw new IllegalArgumentException("key not in key set: " + keys);
    Long retval = countMap.get(key);
    if (retval == null)
      throw new BugInRandoopException();
    return retval.longValue();
  }

  private void addLine(StringBuilder b, int length) {
    for (int i = 0; i < length; i++) {
      b.append('-');
    }
    b.append(Globals.lineSep);
  }

}
