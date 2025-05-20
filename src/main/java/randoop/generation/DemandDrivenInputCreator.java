package randoop.generation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.DummyVisitor;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceCollection;
import randoop.test.DummyCheckGenerator;
import randoop.types.ArrayType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.DemandDrivenLog;
import randoop.util.Log;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;

/**
 * Provides a demand-driven approach to construct inputs for types that Randoop needs but cannot
 * find in its existing sequence pool.
 *
 * <p>Randoop normally works bottom-up: it abandons a method call if inputs aren't available. This
 * treats inputs of non-SUT-returned classes differently, using a top-down demand-driven approach.
 *
 * <p>When an input of a non-SUT-returned type T is needed, demand-driven creates a set of such
 * values. For each constructor/method in T that produces T, demand-driven calls the producer method
 * once (recursively building its inputs if needed, possibly including values of non-SUT-parameter
 * classes). Those results are the set of values, from which Randoop can choose to call the method.
 *
 * <p>The main entry point is {@link #createSequencesForType}.
 *
 * <p>Definitions:
 *
 * <dl>
 *   <dt>SUT class
 *   <dd>A SUT class, or a "class in the model", is a class that the user specified on the command
 *       line to be tested. SUT stands for "software under test".
 *   <dt>SUT-returned class
 *   <dd>A SUT-returned class is a class that is the return type for some accessible method or
 *       constructor in the SUT.
 *   <dt>SUT-parameter class
 *   <dd>A SUT-parameter class is a class that is a formal parameter for some accessible method or
 *       constructor in the SUT.
 * </dl>
 *
 * Neither of these subsumes the other: there may be SUT classes that are not SUT-returned, and
 * there may be SUT-returned classes that are not SUT classes.
 *
 * <p>This class implements the "Detective" component from the ASE 2015 paper <a
 * href="https://people.kth.se/~artho/papers/lei-ase2015.pdf">"GRT: Program-Analysis-Guided Random
 * Testing" by Ma et al.</a>
 */
public class DemandDrivenInputCreator {

  /** A map from type T to a list of operations in T that return type T. */
  private final Map<Type, List<TypedOperation>> objectProducersMap;

  /**
   * The main sequence collection. It contains objects of SUT-returned classes. It also contains
   * objects of some SUT classes and SUT-parameter classes: those on which demand-driven has been
   * called. It never contains objects of non-SUT-parameter classes.
   *
   * <p>This is the same object as {@code gralComponents} in {@link ComponentManager}. It represents
   * Randoop's full sequence repository.
   */
  private final SequenceCollection sequenceCollection;

  /**
   * A secondary sequence collection. It contains objects of non-SUT-returned classes. Any
   * SUT-parameter values in this collection are also copied to the main sequence collection.
   * Non-SUT, non-SUT-returned, non-SUT-parameter classes only appear in this collection; that is an
   * optimization to avoid making the main sequence collection too large.
   */
  private final SequenceCollection secondarySequenceCollection;

  /**
   * Constructs a new {@code DemandDrivenInputCreator} object.
   *
   * @param sequenceCollection the sequence collection used for generating input sequences. This
   *     should be the component sequence collection ({@code gralComponents} from {@link
   *     ComponentManager}), i.e., Randoop's full sequence collection
   * @param objectProducersMap a map of types to lists of operations that produce objects of those
   *     types
   */
  public DemandDrivenInputCreator(
      SequenceCollection sequenceCollection, Map<Type, List<TypedOperation>> objectProducersMap) {
    this.sequenceCollection = sequenceCollection;
    this.secondarySequenceCollection = new SequenceCollection(new ArrayList<>(0));
    this.objectProducersMap = objectProducersMap;
  }

  /**
   * Creates sequences to construct objects of the target type.
   *
   * <p>This method attempts to create objects of the target type when none are available in the
   * main sequence collection and cannot be created using methods of class-under-test.
   *
   * <ol>
   *   <li>Finds constructors/methods in the target type that return the target type.
   *   <li>Calls each such producer method once.
   *       <ul>
   *         <li>Recursively creates required inputs if needed.
   *       </ul>
   *   <li>Returns sequences that produce objects of the target type.
   * </ol>
   *
   * This method has the following side effects:
   *
   * <ul>
   *   <li>Adds sequences to the main and secondary sequence collection
   *   <li>Logs warnings and adds a target type to UninstantiableTypeTracker if no producers found
   *   <li>Adds discovered types to NonSUTClassTracker if they are not part of the SUT
   * </ul>
   *
   * <p>For the detailed algorithm description, see the GRT paper.
   *
   * @param targetType the type of objects to create. No object of this type currently exists in the
   *     main sequence collection, and it cannot be constructed using only methods and constructors
   *     defined in the SUT.
   * @param exactTypeMatch if true, returns only sequences producing the exact requested type; if
   *     false, includes sequences producing subtypes of the requested type
   * @param onlyReceivers if true, returns only sequences usable as method call receivers; if false,
   *     returns all sequences regardless of receiver usability
   * @return a list of sequences that produce objects of the target type, or an empty list if none
   *     found
   */
  public SimpleList<Sequence> createSequencesForType(
      Type targetType, boolean exactTypeMatch, boolean onlyReceivers) {

    Set<Type> nonSutTypes = new HashSet<>();
    List<TypedOperation> producerMethods = getProducers(targetType, nonSutTypes);

    // Demand-driven violates Randoop's invariant that test generation never uses operations outside
    // the SUT.
    // Record the type here allows us to inform the user about this violation through logging.
    for (Type type : nonSutTypes) {
      recordNonSutTypes(type);
    }

    if (producerMethods.isEmpty()) {
      Log.logPrintf(
          "Warning: No producer methods found for type %s. Cannot generate inputs for this type.%n",
          targetType);
      // Track the type with no producers
      UninstantiableTypeTracker.addType(targetType);
      return new SimpleArrayList<>();
    }

    // For each producer method, create a sequence if possible
    for (TypedOperation producerMethod : producerMethods) {
      Sequence newSequence = getInputAndGenSeq(producerMethod);
      if (newSequence != null) {
        // If the sequence is successfully executed, add it to the sequenceCollection.
        executeAndAddToSecondaryPool(Collections.singleton(newSequence));
      }
    }

    // Note: At the beginning of this method, this call to `getSequencesForType()` would
    // return an empty list. It may or may not return a non-empty list at this point.
    SimpleList<Sequence> result =
        secondarySequenceCollection.getSequencesForType(
            targetType, exactTypeMatch, onlyReceivers, false);
    sequenceCollection.addAll(result.toJDKList());
    secondarySequenceCollection.clear();
    return result;
  }

  /**
   * Finds constructors and methods within the target type that return objects of the target type.
   *
   * <p>Searches for operations that produce instances of {@code targetType} (or compatible types).
   * For each discovered operation, adds its parameter types to a worklist for further processing.
   * Stops processing a type when it is non-receiver or already processed.
   *
   * @param targetType the return type of the operations to find
   * @param nonSutTypes output parameter receives types discovered during search that are not part
   *     of the SUT
   * @return a list of {@code TypedOperations} (constructors and methods) that return the target
   *     type
   * @throws NullPointerException if targetType is null
   */
  private List<TypedOperation> getProducers(Type targetType, Set<Type> nonSutTypes) {
    Set<TypedOperation> resultSet = new LinkedHashSet<>();
    Deque<Type> workList = new ArrayDeque<>();
    Set<Type> processed = new HashSet<>();
    workList.add(targetType);

    while (!workList.isEmpty()) {
      Type currentType = workList.remove();

      // Skip if already processed or if it is a non-receiver type, or it has
      // already been processed.
      if (currentType.isNonreceiverType() || processed.contains(currentType)) {
        continue;
      }
      processed.add(currentType);

      // For logging purposes, track the type if it is not part of the SUT.
      nonSutTypes.add(currentType);

      // Get all constructors and methods of the current class.
      List<TypedOperation> operations = objectProducersMap.get(currentType);
      if (operations != null) {
        // Iterate over the operations and check if they can produce the target type.
        for (TypedOperation op : operations) {
          Type opOutputType = op.getOutputType();

          // Check if the operation can be called with the current type.

          if (!opOutputType.isAssignableFrom(currentType)) {
            // opOutput is not a supertype of currentType
            continue;
          }

          if (!op.isConstructorCall() && !op.isStatic()) {
            // Skip any instance method: it requires a receiver object,
            // and we assume no valid receiver exists in sequenceCollection.
            // This is a conservative assumption. We can only guarantee that receiver does not exist
            // for methods in the targetType, since targetType is not in the sequenceCollection.
            // However, this simplifies the logic and aligns with the GRT paper.
            continue;
          }

          if (opOutputType.isGeneric()) {
            // The operation returns an uninstantiated generic type, ignore it.
            // Sequences involving uninstantiated generic types (e.g., raw type variables like T or
            // E) without a generic context for type inference or declaration will not compile.
            continue;
          }

          // Add this operation as a producer of the type.
          resultSet.add(op);

          // Add each of its parameter types for further processing.
          for (Type paramType : op.getInputTypes()) {
            workList.addFirst(paramType);
          }
        }
      }
    }

    // Reverse the order of the list to get the most specific types first.
    List<TypedOperation> result = new ArrayList<>(resultSet);
    Collections.reverse(result);

    return result;
  }

  /**
   * Creates a sequence that executes the given operation by finding sequences for its inputs.
   *
   * <p>Searches for appropriate input sequences in both the main and secondary sequence
   * collections. For each input type, randomly selects a compatible sequence from those available.
   *
   * @param typedOperation the operation for which to generate inputs and create a sequence
   * @return a sequence for the given operation, or {@code null} if any required input cannot be
   *     found
   * @throws NullPointerException if typedOperation is null
   */
  private @Nullable Sequence getInputAndGenSeq(TypedOperation typedOperation) {
    TypeTuple inputTypes = typedOperation.getInputTypes();
    List<Sequence> inputSequences = new ArrayList<>();

    // Map of types to indices
    for (int i = 0; i < inputTypes.size(); i++) {
      Type inputType = inputTypes.get(i);
      // Get a set of sequences, whose types match with the input type.
      // Return the exact type match if the input type is a primitive type, same as how it is done
      // in
      // `ComponentManager.getSequencesForType`. However, allow non-receiver types to be considered
      // at all times.
      SimpleList<Sequence> candidateSequences =
          sequenceCollection.getSequencesForType(inputType, inputType.isPrimitive(), false, false);
      // Search the secondary sequence collection if no sequences are found in the main collection.
      if (candidateSequences.isEmpty()) {
        candidateSequences =
            secondarySequenceCollection.getSequencesForType(
                inputType, inputType.isPrimitive(), false, false);
      }

      // Filter out the sequences that do not return the required type.
      SimpleArrayList<Sequence> outputMatchingSequences = new SimpleArrayList<>();
      for (Sequence seq : candidateSequences.toJDKList()) {
        Type outputType = seq.getStatement(seq.size() - 1).getOutputType();
        if (outputType.isAssignableFrom(inputType)) {
          outputMatchingSequences.add(seq);
        }
      }

      // If no sequences are found, return null.
      if (outputMatchingSequences.isEmpty()) {
        return null;
      }

      // Randomly select a sequence from the sequencesOfType.
      Sequence seq = Randomness.randomMember((SimpleList<Sequence>) outputMatchingSequences);
      inputSequences.add(seq);
    }

    // The indices of the statements in the sequence that will be used as inputs to the
    // typedOperation.
    List<Integer> inputIndices = new ArrayList<>();

    // For each input sequence, find the index of the statement that generates an object of the
    // required type. This is the last statement in the sequence.
    int stmtOffset = 0;
    for (Sequence seq : inputSequences) {
      int stmtInSeq = seq.size() - 1;
      inputIndices.add(stmtOffset + stmtInSeq);
      stmtOffset += seq.size();
    }

    return Sequence.createSequence(typedOperation, inputSequences, inputIndices);
  }

  /**
   * Executes sequences and adds successful ones to the secondary sequence collection.
   *
   * <p>Evaluates each sequence in the provided set. Adds sequences that terminate normally with a
   * non-null value to the secondary sequence collection.
   *
   * @param sequenceSet sequences to execute
   */
  private void executeAndAddToSecondaryPool(Set<Sequence> sequenceSet) {
    for (Sequence genSeq : sequenceSet) {
      ExecutableSequence eseq = new ExecutableSequence(genSeq);
      try {
        eseq.execute(new DummyVisitor(), new DummyCheckGenerator());
      } catch (Throwable e) {
        DemandDrivenLog.logPrintf("Error executing the following sequence: %s%n", genSeq);
        DemandDrivenLog.logStackTrace(e);
        continue;
      }
      ExecutionOutcome outcome = eseq.getResult(eseq.sequence.size() - 1);
      if (outcome instanceof NormalExecution) {
        Object generatedObjectValue = ((NormalExecution) outcome).getRuntimeValue();
        if (generatedObjectValue != null) {
          secondarySequenceCollection.add(genSeq);
        }
      }
    }
  }

  /**
   * Records the type in the {@link NonSUTClassTracker} if it is not part of the SUT. Since
   * Randoop's invariant of not using operations outside the SUT is violated, we need to track the
   * type and inform the user about this violation through logging.
   *
   * @param type the type to register. The type is not part of the SUT.
   */
  private static void recordNonSutTypes(Type type) {
    String className;
    if (type.isArray()) {
      className = ((ArrayType) type).getElementType().getRuntimeClass().getName();
    } else {
      className = type.getRuntimeClass().getName();
    }

    if (!NonSUTClassTracker.getSutClasses().contains(className)) {
      NonSUTClassTracker.addNonSutClass(type.getRuntimeClass());
    }
  }
}
