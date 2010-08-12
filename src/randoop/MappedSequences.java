package randoop;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import randoop.util.ListOfLists;
import randoop.util.SimpleList;

/**
 * A multimap from keys of type T to sequences. Such a map can be
 * useful to specify sequences that should only be used in specific contexts,
 * for example sequences that should only be used as components when testing a
 * specific class.
 */
public class MappedSequences<T> {

  Map<T, SequenceCollection> map;
  
  public MappedSequences() {
    this.map = new LinkedHashMap<T, SequenceCollection>();
  }

  /**
   * Adds a sequence to the set of sequences associated
   * with the given key.
   */
  public void addSequence(T key, Sequence seq) {
    if (seq == null) throw new IllegalArgumentException("seq is null");
    if (key == null) throw new IllegalArgumentException("key is null");
    SequenceCollection c = map.get(key);
    if (c == null) {
      c = new SequenceCollection();
      map.put(key, c);
    }
    c.add(seq);
  }
  
  /**
   * Returns the set of sequences (as a list) that are associated with
   * the given key and create values of the desiredType.
   */
  public SimpleList<Sequence> getSequences(T key, Class<?> desiredType) {
    if (key == null) throw new IllegalArgumentException("key is null");
    SequenceCollection c = map.get(key);
    if (c == null) {
      return emptyList;
    }
    return map.get(key).getSequencesForType(desiredType, true);
  }

  // Cached empty list used by getSequences method.
  private static final SimpleList<Sequence> emptyList;
  static {
    List<SimpleList<Sequence>> emptyJDKList = Collections.emptyList();
    emptyList = new ListOfLists<Sequence>(emptyJDKList);
  }

}
