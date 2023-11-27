package randoop.generation;

import static randoop.util.EquivalenceChecker.equivalentTypes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import randoop.DummyVisitor;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.DummyCheckGenerator;
import randoop.types.Type;
import randoop.util.ListOfLists;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;

/**
 * Represents a mapping between objects and their associated sequences, functioning as
 * a central repository in the GRT Detective input generation process. This class acts as a
 * specialized map, where each key is an object and the corresponding value is a
 * list of sequences.
 * There are two instances of using this class in the GRT Detective: the main object pool
 * and the secondary object pool.
 * In the main object pool, each key represents an object that was retrieved from the
 * Randoop forward generation's component manager (previously generated sequences by randoop)
 * In the secondary object pool, each key represents a generated object by the GRT Detective
 * (newly generated sequences by GRT).
 * For both instances, the value is a list of sequences that are associated with the key object.
 * Execution of any element in the list of sequences will generate the corresponding key object.
 */

public class ObjectPool {
  /** The underlying data structure storing the objects and their associated sequences */
  private final LinkedHashMap<Object, SimpleList<Sequence>> objPool;

  /** Creates a new, empty ObjectPool. */
  public ObjectPool() {
    this.objPool = new LinkedHashMap<>();
  }

  /**
   * Creates an object pool with the given sequence collection.
   *
   * @param sequenceSet the sequence collection
   */
  public ObjectPool(Set<Sequence> sequenceSet) {
    this.objPool = new LinkedHashMap<>();
    addExecutedSequencesToPool(sequenceSet);
  }

  /**
   * Executes a given set of sequences, extracts the last outcome's runtime value if it is a
   * NormalExecution, and adds or updates the value-sequence pair in the provided object pool if the
   * runtime value is not null.
   *
   * @param sequenceSet the set of sequences to be executed and possibly added to the object pool
   */
  private void addExecutedSequencesToPool(Set<Sequence> sequenceSet) {
    for (Sequence sequence : sequenceSet) {
      ExecutableSequence eseq = new ExecutableSequence(sequence);
      eseq.execute(new DummyVisitor(), new DummyCheckGenerator());

      Object generatedObjectValue = null;
      ExecutionOutcome lastOutcome = eseq.getResult(eseq.sequence.size() - 1);
      if (lastOutcome instanceof NormalExecution) {
        generatedObjectValue = ((NormalExecution) lastOutcome).getRuntimeValue();
      }

      if (generatedObjectValue != null) {
        this.addOrUpdate(generatedObjectValue, sequence);
      }
    }
  }

  /**
   * Check if the object pool is empty.
   *
   * @return true if the pool is empty, false otherwise
   */
  public boolean isEmpty() {
    return this.objPool.isEmpty();
  }

  /**
   * Get the size of the object pool.
   *
   * @return the number of objects in the pool
   */
  public int size() {
    return this.objPool.size();
  }

  /**
   * Add a new object and its associated sequences to the pool.
   *
   * @param obj the object to be added
   * @param sequences the sequences associated with the object
   */
  public void put(Object obj, SimpleList<Sequence> sequences) {
    this.objPool.put(obj, sequences);
  }

  /**
   * Get the sequences associated with a specific object.
   *
   * @param obj the object
   * @return the sequences associated with the object
   */
  public SimpleList<Sequence> get(Object obj) {
    return this.objPool.get(obj);
  }

  /**
   * Get a list of all objects in the pool.
   *
   * @return a list of all objects
   */
  public List<Object> getObjects() {
    // return List.copyOf(this.objPool.keySet());
    return new ArrayList<>(this.objPool.keySet());
  }

  /**
   * Add a new sequence to an object's associated sequences or create a new entry if the object is
   * not in the pool.
   *
   * @param obj the object
   * @param seq the sequence to be added
   */
  @SuppressWarnings("unchecked")
  /*
  // Java 9 version
  public void addOrUpdate(Object obj, Sequence seq) {
    if (this.objPool.containsKey(obj)) {
      SimpleList<Sequence> existingSequences = this.objPool.get(obj);
      this.objPool.put(
          obj, new ListOfLists<>(existingSequences, new SimpleArrayList<>(List.of(seq))));
    } else {
      this.objPool.put(obj, new SimpleArrayList<>(List.of(seq)));
    }
  }
   */
  public void addOrUpdate(Object obj, Sequence seq) {
    if (this.objPool.containsKey(obj)) {
      SimpleList<Sequence> existingSequences = this.objPool.get(obj);

      // Create a new SimpleArrayList and add 'seq' to it.
      SimpleArrayList<Sequence> newList = new SimpleArrayList<>();
      newList.add(seq);

      // Use ListOfLists with existingSequences and newList.
      this.objPool.put(obj, new ListOfLists<>(existingSequences, newList));
    } else {
      // Create a new SimpleArrayList and add 'seq' to it for the else case.
      SimpleArrayList<Sequence> newList = new SimpleArrayList<>();
      newList.add(seq);

      this.objPool.put(obj, newList);
    }
  }

  /**
   * Get a subset of the object pool that contains objects of a specific type and their sequences.
   *
   * @param t the type of objects to be included in the subset
   * @return a new ObjectPool that contains only the objects of the specified type and their
   *    sequences
   */
  public ObjectPool getSubPoolOfType(Type t) {
    ObjectPool subPoolOfType = new ObjectPool();
    for (Object obj : this.objPool.keySet()) {
      if (equivalentTypes(obj.getClass(), t.getRuntimeClass())) {
        subPoolOfType.put(obj, this.objPool.get(obj));
      }
    }
    return subPoolOfType;
  }

  /**
   * Get a list of sequences that create objects of a specific type.
   *
   * @param t the type of objects that the sequences create
   * @return a list of sequences that create objects of the specified type
   */
  @SuppressWarnings("unchecked")
  public ListOfLists<Sequence> getSequencesOfType(Type t) {
    ListOfLists<Sequence> sequencesOfType = new ListOfLists<>();
    for (Object obj : this.objPool.keySet()) {
      if (equivalentTypes(obj.getClass(), t.getRuntimeClass())) {
        sequencesOfType = new ListOfLists<>(sequencesOfType, this.objPool.get(obj));
      }
    }
    return sequencesOfType;
  }

  /**
   * Get a string representation of the object pool.
   *
   * @return a string representation of the pool where each line contains an object and its
   *     associated sequences
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Object, SimpleList<Sequence>> entry : this.objPool.entrySet()) {
      sb.append(entry.getKey().toString())
          .append(" : ")
          .append(entry.getValue().toString())
          .append(System.lineSeparator());
    }
    return sb.toString();
  }
}
