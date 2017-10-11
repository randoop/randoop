package randoop.sequence;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import randoop.types.Type;
import randoop.util.ListOfLists;
import randoop.util.SimpleList;

/**
 * A multimap from keys of type T to sequences. Such a map can be useful to specify sequences that
 * should only be used in specific contexts, for example sequences that should only be used as
 * components when testing a specific class.
 */
public class MappedSequences<T> {

  private Map<T, SequenceCollection> map;

  public MappedSequences() {
    this.map = new LinkedHashMap<>();
  }

  /**
   * Adds a sequence to the set of sequences associated with the given key.
   *
   * @param key the key value
   * @param seq the sequence
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
   * Returns the set of sequences (as a list) that are associated with the given key and create
   * values of the desiredType.
   *
   * @param key the key value
   * @param desiredType the query type
   * @return the list of sequences for the key and query type
   */
  public SimpleList<Sequence> getSequences(T key, Type desiredType) {
    if (key == null) {
      throw new IllegalArgumentException("key is null");
    }
    SequenceCollection c = map.get(key);
    if (c == null) {
      return emptyList;
    }
    return map.get(key).getSequencesForType(desiredType, true, false);
  }

  // Cached empty list used by getSequences method.
  private static final SimpleList<Sequence> emptyList;

  static {
    List<SimpleList<Sequence>> emptyJDKList = Collections.emptyList();
    emptyList = new ListOfLists<>(emptyJDKList);
  }

  /**
   * Returns all sequences as the union of all of the sequence collections.
   *
   * @return the set of all sequence objects in this set of collections
   */
  public Set<Sequence> getAllSequences() {
    Set<Sequence> result = new LinkedHashSet<>();
    for (SequenceCollection c : map.values()) {
      result.addAll(c.getAllSequences());
    }
    return result;
  }
}
