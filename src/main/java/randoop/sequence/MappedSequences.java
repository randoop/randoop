package randoop.sequence;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import randoop.main.RandoopBug;
import randoop.types.Type;
import randoop.util.ListOfLists;
import randoop.util.SimpleList;

/**
 * A multimap from keys of type K to sequences. Such a map can be useful to specify sequences that
 * should only be used in specific contexts, for example sequences that should only be used as
 * components when testing a specific class.
 */
public class MappedSequences<K> {

  private Map<K, SequenceCollection> map;

  /** Map that stores the frequency information for each sequence in each package. */
  private Map<K, Map<Sequence, Integer>> sequenceFrequencyMap;

  public MappedSequences() {
    this.map = new LinkedHashMap<>();
    this.sequenceFrequencyMap = new HashMap<>();
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
   * Adds the frequency information for a sequence to the global frequency map associated with the
   * given key.
   *
   * @param key the key value
   * @param seq the sequence
   * @param freq the frequency of the sequence
   */
  public void addSequenceFrequency(K key, Sequence seq, int freq) {
    isPrimitive(key, seq);
    Map<Sequence, Integer> freqMap =
        sequenceFrequencyMap.computeIfAbsent(key, __ -> new HashMap<>());
    freqMap.put(seq, freq);
  }

  // TODO: DELETE THIS. ONLY USED FOR TESTING
  public Map<K, Map<Sequence, Integer>> getSequenceFrequencyMap() {
    return sequenceFrequencyMap;
  }

  /**
   * Returns the sequence frequency map associated with the given key.
   *
   * @param key the given key
   * @return the sequence frequency map
   */
  public Map<Sequence, Integer> getSequenceFrequency(K key) {
    return sequenceFrequencyMap.get(key);
  }

  // Validate the sequence to be a primitive sequence. If not, throw an IllegalArgumentException.
  protected void isPrimitive(K key, Sequence seq) {
    if (seq == null) throw new IllegalArgumentException("seq is null");
    if (key == null) throw new IllegalArgumentException("key is null");
    if (!seq.isNonreceiver()) {
      throw new IllegalArgumentException("seq is not a primitive sequence");
    }
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

  // TODO: Deprecated. Delete this after testing
  public int getSequencesFrequency(K key, Sequence seq) {
    if (key == null) {
      throw new IllegalArgumentException("key is null");
    }
    Integer freq = sequenceFrequencyMap.get(key).get(seq);
    if (freq == null) {
      throw new RandoopBug(String.format("Sequence not found in frequency map: %s", seq));
    }
    return freq;
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
