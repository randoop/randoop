package randoop.generation;

import static randoop.util.EquivalenceChecker.equivalentTypes;

import java.util.*;

import randoop.DummyVisitor;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceCollection;
import randoop.test.DummyCheckGenerator;
import randoop.types.Type;
import randoop.util.ListOfLists;
import randoop.util.SimpleList;
import randoop.util.SimpleArrayList;

/**
 * Represents a mapping between objects and their associated sequences, functioning as a central
 * repository in the GRT Detective input generation process (see
 * {@link randoop.generation.DemandDrivenInputCreation}).
 * This class acts as a specialized map, where each key is an object and the corresponding value
 * is a list of sequences. There are two instances of using this class in the GRT Detective: the
 * main object pool and the secondary object pool. In the main object pool, each key represents
 * an object that was retrieved from the Randoop forward generation's component manager
 * (previously generated sequences by randoop) In the secondary object pool, each key represents
 * a generated object by the GRT Detective (newly generated sequences by GRT). For both instances,
 * the value is a list of sequences that are associated with the key object. Execution of any element
 * in the list of sequences will generate the corresponding key object.
 */
public class ObjectPoolExperimental {

    private static final long serialVersionUID = 2L;

    private SequenceCollection gralComponents;
    private final boolean exactMatch;
    private final boolean onlyReceivers;

    /**
     * Creates an object pool with a given sequence collection.
     *
     * @param sequenceCollection the sequence collection
     */
    public ObjectPoolExperimental(SequenceCollection sequenceCollection, boolean exactMatch, boolean onlyReceivers) {
        this.gralComponents = sequenceCollection;
        this.exactMatch = exactMatch;
        this.onlyReceivers = onlyReceivers;
    }

    /*
     * Executes a given set of sequences, extracts the last outcome's runtime value if it is a
     * NormalExecution, and adds or updates the value-sequence pair in the object pool if the runtime
     * value is not null.
     *
     * @param sequenceSet the set of sequences to be executed and possibly added to the object pool
     */
    /*
    private void addExecutedSequencesToPool(Set<Sequence> sequenceSet) {
        for (Sequence sequence : sequenceSet) {
            ExecutableSequence eseq = new ExecutableSequence(sequence);
            eseq.execute(new DummyVisitor(), new DummyCheckGenerator());

            Object generatedObjectValue = null;
            ExecutionOutcome outcome = eseq.getResult(eseq.sequence.size() - 1);
            if (outcome instanceof NormalExecution) {
                generatedObjectValue = ((NormalExecution) outcome).getRuntimeValue();
            }

            if (generatedObjectValue != null) {
                this.put(generatedObjectValue, sequence);
            }
        }
    }

     */

    public void add(Sequence seq) {
        this.gralComponents.add(seq);
    }

    public void addAll(Collection<Sequence> col) {
        this.gralComponents.addAll(col);
    }



    /**
     * Get a subset of the object pool that contains objects of a specific type and their sequences.
     *
     * @param t the type of objects to be included in the subset
     * @return a new ObjectPool that contains only the objects of the specified type and their
     *     sequences
     */
    public SimpleList<Sequence> getSubPoolOfType(Type t) {

        Set<Sequence> subPoolOfType = new HashSet<>();
        Set<Sequence> sequences = this.gralComponents.getAllSequences();
        for (Sequence seq : sequences) {
            if (equivalentTypes(seq.getLastVariable().getType().getRuntimeClass(), t.getRuntimeClass())) {
                subPoolOfType.add(seq);
            }
        }
        SimpleList<Sequence> subPool = new SimpleArrayList<>(subPoolOfType);
        return subPool;
    }

    /**
     * Get a list of sequences that create objects of a specific type.
     *
     * @param t the type of objects that the sequences create
     * @return a list of sequences that create objects of the specified type
     */
    @SuppressWarnings("unchecked")
    public SimpleList<Sequence> getSequencesOfType(Type t) {
        /*
        ListOfLists<Sequence> sequencesOfType = new ListOfLists<>();
        for (Object obj : this.keySet()) {
            if (equivalentTypes(obj.getClass(), t.getRuntimeClass())) {
                sequencesOfType =
                        new ListOfLists<>(
                                sequencesOfType,
                                new SimpleArrayList<Sequence>(Collections.singleton(this.get(obj))));
            }
        }
        return sequencesOfType;

         */
        return this.gralComponents.getSequencesForType(t, this.exactMatch, this.onlyReceivers, false);
    }

    /**
     * Get a string representation of the object pool.
     *
     * @return a string representation of the pool where each line contains an object and its
     *     associated sequences
     */
    @Override
    public String toString() {
        /*
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Object, Sequence> entry : this.entrySet()) {
            sb.append(entry.getKey().toString())
                    .append(" : ")
                    .append(entry.getValue().toString())
                    .append(System.lineSeparator());
        }
        return sb.toString();

         */
        return this.gralComponents.getAllSequences().toString();
    }
}
