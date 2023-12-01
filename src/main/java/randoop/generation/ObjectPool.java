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
 * Represents a mapping between objects and their associated sequences, functioning as a central
 * repository in the GRT Detective input generation process. This class acts as a specialized map,
 * where each key is an object and the corresponding value is a list of sequences. There are two
 * instances of using this class in the GRT Detective: the main object pool and the secondary object
 * pool. In the main object pool, each key represents an object that was retrieved from the Randoop
 * forward generation's component manager (previously generated sequences by randoop) In the
 * secondary object pool, each key represents a generated object by the GRT Detective (newly
 * generated sequences by GRT). For both instances, the value is a list of sequences that are
 * associated with the key object. Execution of any element in the list of sequences will generate
 * the corresponding key object.
 */
public class ObjectPool extends LinkedHashMap<Object, SimpleList<Sequence>> {

  private static final long serialVersionUID = 1L;

  /**
   * Creates an empty object pool.
   */
  public ObjectPool() {
    super();
  }

  /**
   * Creates an object pool with a given sequence collection.
   *
   * @param sequenceCollection the sequence collection
   */
  public ObjectPool(Set<Sequence> sequenceSet) {
    super();
    addExecutedSequencesToPool(sequenceSet);
  }

  /**
   * Executes a given set of sequences, extracts the last outcome's runtime value if it is a
   * NormalExecution, and adds or updates the value-sequence pair in the object pool if the
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
   * Add a new sequence to an object's associated sequences or create a new entry if the object is
   * not in the pool.
   *
   * @param obj the object
   * @param seq the sequence to be added
   */
  @SuppressWarnings("unchecked")
  public void addOrUpdate(Object obj, Sequence seq) {
    SimpleList<Sequence> existingSequences = this.get(obj);
    if (existingSequences != null) {
      SimpleArrayList<Sequence> newList = new SimpleArrayList<>();
      newList.add(seq);
      this.put(obj, new ListOfLists<>(existingSequences, newList));
    } else {
      SimpleArrayList<Sequence> newList = new SimpleArrayList<>();
      newList.add(seq);
      this.put(obj, newList);
    }
  }

  /**
   * Get a subset of the object pool that contains objects of a specific type and their sequences.
   *
   * @param t the type of objects to be included in the subset
   * @return a new ObjectPool that contains only the objects of the specified type and their
   * sequences
   */
  public ObjectPool getSubPoolOfType(Type t) {
    ObjectPool subPoolOfType = new ObjectPool();
    for (Object obj : this.keySet()) {
      if (equivalentTypes(obj.getClass(), t.getRuntimeClass())) {
        subPoolOfType.put(obj, this.get(obj));
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
    for (Object obj : this.keySet()) {
      if (equivalentTypes(obj.getClass(), t.getRuntimeClass())) {
        sequencesOfType = new ListOfLists<>(sequencesOfType, this.get(obj));
      }
    }
    return sequencesOfType;
  }

  /**
   * Get a string representation of the object pool.
   *
   * @return a string representation of the pool where each line contains an object and its
   * associated sequences
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Object, SimpleList<Sequence>> entry : this.entrySet()) {
      sb.append(entry.getKey().toString())
              .append(" : ")
              .append(entry.getValue().toString())
              .append(System.lineSeparator());
    }
    return sb.toString();
  }
}
