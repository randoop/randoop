package randoop.sequence;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import randoop.types.Type;
import randoop.util.list.SimpleList;

/**
 * A multimap from keys of type K to sequences. Such a map can be useful to specify sequences that
 * should only be used in specific contexts, for example sequences that should only be used as
 * components when testing a specific class.
 */
public class MappedSequences<K> {

  private Map<K, SequenceCollection> map;

  public MappedSequences() {
    this.map = new LinkedHashMap<>();
  }

  /**
   * Adds a sequence to the set of sequences associated with the given key.
   *
   * @param key the key value
   * @param seq the sequence
   */
  public void addSequence(K key, Sequence seq) {
    if (seq == null) throw new IllegalArgumentException("seq is null");
    if (key == null) throw new IllegalArgumentException("key is null");
    SequenceCollection c = map.computeIfAbsent(key, __ -> new SequenceCollection());
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
  public SimpleList<Sequence> getSequences(K key, Type desiredType) {
    if (key == null) {
      throw new IllegalArgumentException("key is null");
    }
    SequenceCollection sc = map.get(key);
    if (sc == null) {
      return SimpleList.empty();
    }
    return sc.getSequencesForType(desiredType, true, false);
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
