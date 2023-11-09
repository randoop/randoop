package randoop.generation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.DummyVisitor;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceCollection;
import randoop.test.DummyCheckGenerator;
import randoop.types.Type;
import randoop.util.ListOfLists;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;
import static randoop.util.EquivalenceChecker.equivalentTypes;

/**
 * A class representing a pool of objects, each associated with a list of sequences. Used in
 * Detective.
 */
public class ObjectPool {
  // The underlying data structure storing the objects and their associated sequences
  private final LinkedHashMap<Object, SimpleList<Sequence>> objPool;

  /** Default constructor that initializes the object pool. */
  public ObjectPool() {
    this.objPool = new LinkedHashMap<>();
  }

  /**
   * Constructor that initializes the object pool with a given sequence collection.
   *
   * @param sequenceSet The sequence collection.
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
   * @param sequenceSet The set of sequences to be executed and possibly added to the object pool.
   */
  private void addExecutedSequencesToPool(Set<Sequence> sequenceSet) {
    for (Sequence genSeq : sequenceSet) {
      ExecutableSequence eseq = new ExecutableSequence(genSeq);
      eseq.execute(new DummyVisitor(), new DummyCheckGenerator());

      Object generatedObjectValue = null;
      ExecutionOutcome lastOutcome = eseq.getResult(eseq.sequence.size() - 1);
      if (lastOutcome instanceof NormalExecution) {
        generatedObjectValue = ((NormalExecution) lastOutcome).getRuntimeValue();
      }

      if (generatedObjectValue != null) {
        this.addOrUpdate(generatedObjectValue, genSeq);
      }
    }
  }

  /**
   * Check if the object pool is empty.
   *
   * @return True if the pool is empty, false otherwise.
   */
  public boolean isEmpty() {
    return this.objPool.isEmpty();
  }

  /**
   * Get the size of the object pool.
   *
   * @return The number of objects in the pool.
   */
  public int size() {
    return this.objPool.size();
  }

  /**
   * Add a new object and its associated sequences to the pool.
   *
   * @param obj The object to be added.
   * @param sequences The sequences associated with the object.
   */
  public void put(Object obj, SimpleList<Sequence> sequences) {
    this.objPool.put(obj, sequences);
  }

  /**
   * Get the sequences associated with a specific object.
   *
   * @param obj The object.
   * @return The sequences associated with the object.
   */
  public SimpleList<Sequence> get(Object obj) {
    return this.objPool.get(obj);
  }

  /**
   * Get a list of all objects in the pool.
   *
   * @return A list of all objects.
   */
  public List<Object> getObjects() {
    return List.copyOf(this.objPool.keySet());
  }

  /**
   * Add a new sequence to an object's associated sequences or create a new entry if the object is
   * not in the pool.
   *
   * @param obj The object.
   * @param seq The sequence to be added.
   */
  @SuppressWarnings("unchecked")
  public void addOrUpdate(Object obj, Sequence seq) {
    if (this.objPool.containsKey(obj)) {
      SimpleList<Sequence> existingSequences = this.objPool.get(obj);
      this.objPool.put(
          obj, new ListOfLists<>(existingSequences, new SimpleArrayList<>(List.of(seq))));
    } else {
      this.objPool.put(obj, new SimpleArrayList<>(List.of(seq)));
    }
  }

  /**
   * Filter the sequences in the pool by their type.
   *
   * @param t The type to filter by.
   * @return A list of lists of sequences that match the type.
   */
  public ObjectPool getObjSeqPair(Type t) {
    ObjectPool objSeqPair = new ObjectPool();
    for (Object obj : this.objPool.keySet()) {
      if (equivalentTypes(obj.getClass(), t.getRuntimeClass())) {
        objSeqPair.put(obj, this.objPool.get(obj));
      }
    }
    return objSeqPair;
  }

  /**
   * Get a subset of the object pool that contains objects of a specific type and their sequences.
   *
   * @param t The type to filter by.
   * @return A new ObjectPool that contains only the objects of the specified type and their
   *     sequences.
   */
  @SuppressWarnings("unchecked")
  public ListOfLists<Sequence> filterByType(Type t) {
    ListOfLists<Sequence> filteredSequences = new ListOfLists<>();
    for (Object obj : this.objPool.keySet()) {
      if (equivalentTypes(obj.getClass(), t.getRuntimeClass())) {
        filteredSequences = new ListOfLists<>(filteredSequences, this.objPool.get(obj));
      }
    }
    return filteredSequences;
  }

  /**
   * Get a string representation of the object pool.
   *
   * @return A string representation of the pool where each line contains an object and its
   *     associated sequences.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Object, SimpleList<Sequence>> entry : this.objPool.entrySet()) {
      sb.append(entry.getKey().toString())
          .append(" : ")
          .append(entry.getValue().toString())
          .append("\n");
    }
    return sb.toString();
  }
}