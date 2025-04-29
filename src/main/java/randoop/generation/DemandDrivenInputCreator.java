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
 * <p>While Randoop normally works bottom-up (abandoning method calls when inputs aren't available),
 * this class implements a top-down approach: when inputs of a particular type are needed, it
 * searches for constructors/methods that can produce that type and recursively builds the required
 * input objects.
 *
 * <p>The main entry point is {@link #createSequencesForType}.
 *
 * <p>This class implements the "Detective" component from the ASE 2015 paper <a
 * href="https://people.kth.se/~artho/papers/lei-ase2015.pdf">"GRT: Program-Analysis-Guided Random
 * Testing" by Ma et al.</a>
 */
public class DemandDrivenInputCreator {

  /** A map of types to lists of operations that produce objects of those types. */
  private final Map<Type, List<TypedOperation>> objectProducersMap;

  /**
   * The main sequence collection used by the generator to build larger sequences on demand by
   * creating objects for missing types. This structure exists per the demand-driven approach
   * described in GRT paper and is shared with {@link ComponentManager#gralComponents}. It
   * represents Randoop's full sequence repository.
   */
  private final SequenceCollection sequenceCollection;

  /**
   * A secondary sequence collection used to store sequences generated during the demand-driven
   * input creation process. Sequences that successfully produce targetType are later copied to the
   * main pool; helper sequences that only build intermediate objects stay in the secondary pool.
   *
   * <p>This is an optimization to reduce the search space for the missing types in the main
   * sequence collection.
   */
  private final SequenceCollection secondarySequenceCollection;

  /**
   * Constructs a new {@code DemandDrivenInputCreator} object.
   *
   * @param sequenceCollection the sequence collection used for generating input sequences. This
   *     should be the component sequence collection ({@link ComponentManager#gralComponents}),
   *     i.e., Randoop's full sequence collection. Must not be null.
   * @param objectProducersMap a map of types to lists of operations that produce objects of those
   *     types. Must not be null.
   * @throws NullPointerException if either parameter is null
   */
  public DemandDrivenInputCreator(
      SequenceCollection sequenceCollection, Map<Type, List<TypedOperation>> objectProducersMap) {
    this.sequenceCollection = sequenceCollection;
    this.secondarySequenceCollection = new SequenceCollection(new ArrayList<Sequence>(0));
    this.objectProducersMap = objectProducersMap;
  }

  /**
   * Creates sequences to construct objects of the target type.
   *
   * <p>This method attempts to create objects of the target type when none are available in the
   * main sequence collection and cannot be created using methods of class-under-test.
   *
   * <ol>
   *   <li>Finds constructors/methods that return the target type
   *   <li>Recursively searches for operations to create required input parameters
   *   <li>Assembles and executes sequences that may produce the target type
   *   <li>Returns successful sequences that produce objects of the target type
   * </ol>
   *
   * This method has the following side effects:
   *
   * <ul>
   *   <li>Adds successful sequences to the main sequence collection
   *   <li>Logs warnings and adds target type to UninstantiableTypeTracker if no producers found
   *   <li>Adds discovered types to OutOfScopeClassTracker if they are not in scope (i.e., not
   *       specified by the user in Randoop's command line)
   * </ul>
   *
   * <p>For the detailed algorithm description, see the GRT paper.
   *
   * @param targetType the type of objects to create. Must not be null.
   * @param exactTypeMatch if true, returns only sequences producing the exact requested type; if
   *     false, includes sequences producing subtypes of the requested type.
   * @param onlyReceivers if true, returns only sequences usable as method call receivers; if false,
   *     returns all sequences regardless of receiver usability.
   * @return a list of sequences that produce objects of the target type, or an empty list if none
   *     found
   */
  public SimpleList<Sequence> createSequencesForType(
      Type targetType, boolean exactTypeMatch, boolean onlyReceivers) {

    Set<Type> outOfScopeTypes = new HashSet<>();
    Set<TypedOperation> producerMethods = getProducers(targetType, outOfScopeTypes);

    // Track the out-of-scope types.
    for (Type type : outOfScopeTypes) {
      trackOutOfScopeTypes(type);
    }

    if (producerMethods.isEmpty()) {
      Log.logPrintf(
          "Warning: No producer methods found for type %s. Cannot generate inputs for this type.%n",
          targetType);
      // Track the type with no producers
      UninstantiableTypeTracker.addType(targetType);
      return new SimpleArrayList<>();
    }

    List<TypedOperation> producerMethodsList = new ArrayList<>(producerMethods);

    // GRT paper search for producers recursively, we do it iteratively. This
    // is a depth-first search, so we reverse the order of the list to get the
    // same order as the paper.
    Collections.reverse(producerMethodsList);

    // For each producer method, create a sequence if possible
    for (TypedOperation producerMethod : producerMethodsList) {
      Sequence newSequence = getInputAndGenSeq(producerMethod);
      if (newSequence != null) {
        // If the sequence is successfully executed, add it to the sequenceCollection.
        executeAndAddToPool(Collections.singleton(newSequence));
      }
    }

    // Note: At the beginning of the `createSequencesForType` call, getSequencesForType here would
    // return an empty list. It may or may not return a non-empty
    // list at this point.
    SimpleList<Sequence> result =
        secondarySequenceCollection.getSequencesForType(
            targetType, exactTypeMatch, onlyReceivers, false);
    sequenceCollection.addAll(result.toJDKList());
    secondarySequenceCollection.clear();
    return result;
  }

  /**
   * Finds constructors and methods that return objects of the target type.
   *
   * <p>Searches for operations that produce instances of {@code targetType} (or compatible types).
   * For each discovered operation, adds its parameter types to a worklist for further processing.
   * Stops processing a type when it is non-receiver or already processed.
   *
   * @param targetType the return type of the operations to find. Must not be null.
   * @param outOfScopeTypes output parameter, receives types discovered during search that were out
   *     of scope, i.e., not specified by the user in Randoop's command line.
   * @return a set of {@code TypedOperations} (constructors and methods) that return the target type
   * @throws NullPointerException if targetType is null
   */
  private Set<TypedOperation> getProducers(Type targetType, Set<Type> outOfScopeTypes) {
    Set<TypedOperation> result = new LinkedHashSet<>();
    Deque<Type> workList = new ArrayDeque<>();
    Set<Type> processed = new HashSet<>();
    workList.add(targetType);

    // Iterate over the worklist until it is empty.
    while (!workList.isEmpty()) {
      Type currentType = workList.remove();

      // Skip if already processed or if it is a non-receiver type.
      if (processed.contains(currentType) || currentType.isNonreceiverType()) {
        continue;
      }
      processed.add(currentType);

      // For logging purposes, track the out-of-scope types.
      outOfScopeTypes.add(currentType);

      // Get all constructors and methods of the current class.
      List<TypedOperation> operations = objectProducersMap.get(currentType);
      if (operations != null) {
        // Iterate over the operations and check if they can produce the target type.
        for (TypedOperation op : operations) {
          Type opOutputType = op.getOutputType();

          // Check if the operation can be called with the current type.
          // 1) Check assignability
          boolean assignable = opOutputType.isAssignableFrom(currentType);

          // 2) Check if the operation needs a receiver.
          // We assume a receiver is not available in the sequence. This may not hold when
          // currentType is not assignable to targetType,
          // but the paper makes this simplifying assumption and proceeds regardless.
          boolean needReceiver = !op.isConstructorCall() && !op.isStatic();

          // 3) Check if the operation returns an uninstantiated generic type.
          // Sequences involving uninstantiated generic types (e.g., raw type variables like T or E)
          // without a generic context for type inference or declaration will not compile.
          boolean outputIsGeneric = opOutputType.isGeneric();

          // Final qualification
          boolean qualifies = assignable && !needReceiver && !outputIsGeneric;
          if (!qualifies) {
            continue;
          }

          // Add this operation as a producer of the type.
          result.add(op);

          // Add each of its parameter types for further processing.
          for (Type paramType : op.getInputTypes()) {
            if (!paramType.isNonreceiverType() && !processed.contains(paramType)) {
              workList.addFirst(paramType);
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Creates a sequence that executes the given operation by finding sequences for its inputs.
   *
   * <p>Searches for appropriate input sequences in both the main and secondary sequence
   * collections. For each input type, randomly selects a compatible sequence from those available.
   *
   * @param typedOperation the operation for which to generate inputs and create a sequence. Must
   *     not be null.
   * @return a sequence for the given operation, or {@code null} if any required input cannot be
   *     found
   * @throws NullPointerException if typedOperation is null
   */
  private @Nullable Sequence getInputAndGenSeq(TypedOperation typedOperation) {
    TypeTuple inputTypes = typedOperation.getInputTypes();
    List<Sequence> inputSequences = new ArrayList<>();

    // Map of types to indices
    for (int i = 0; i < inputTypes.size(); i++) {
      // Get a set of sequences, each of which generates an object of the input type of the
      // typedOperation.
      Type inputType = inputTypes.get(i);
      // Return exact type match if the input type is a primitive type, same as how it is done in
      // `ComponentManager.getSequencesForType`. However, allow non-receiver types to be considered
      // at all times.
      SimpleList<Sequence> sequencesOfType =
          sequenceCollection.getSequencesForType(
              inputTypes.get(i), inputType.isPrimitive(), false, false);
      // Search the secondary sequence collection if no sequences are found in the main collection.
      if (sequencesOfType.isEmpty()) {
        sequencesOfType =
            secondarySequenceCollection.getSequencesForType(
                inputTypes.get(i), inputType.isPrimitive(), false, false);
      }

      if (sequencesOfType.isEmpty()) {
        return null;
      }

      // Randomly select a sequence from the sequencesOfType.
      Sequence seq = Randomness.randomMember(sequencesOfType);
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
   * <p>Building up the secondary pool enables later steps to compose these helper sequences until
   * one produces an object of the original target type.
   *
   * @param sequenceSet sequences to execute. Must not be null.
   */
  private void executeAndAddToPool(Set<Sequence> sequenceSet) {
    for (Sequence genSeq : sequenceSet) {
      ExecutableSequence eseq = new ExecutableSequence(genSeq);
      try {
        eseq.execute(new DummyVisitor(), new DummyCheckGenerator());
      } catch (Throwable e) {
        DemandDrivenLog.logPrintf("Error executing the following sequence: %s%n", genSeq);
        DemandDrivenLog.logStackTrace(e);
        continue;
      }
      Object generatedObjectValue = null;
      ExecutionOutcome outcome = eseq.getResult(eseq.sequence.size() - 1);
      if (outcome instanceof NormalExecution) {
        generatedObjectValue = ((NormalExecution) outcome).getRuntimeValue();
      }

      if (generatedObjectValue != null) {
        secondarySequenceCollection.add(genSeq);
      }
    }
  }

  /**
   * Registers a type as "out of scope" if it wasn't part of the original test generation targets.
   *
   * <p>If the given type wasn't explicitly specified by the user for test generation (out of
   * scope), adds it to the out-of-scope classes tracker for reporting.
   *
   * @param type the type to check and potentially register. Must not be null.
   */
  private static void trackOutOfScopeTypes(Type type) {
    String className;
    if (type.isArray()) {
      className = ((ArrayType) type).getElementType().getRuntimeClass().getName();
    } else {
      className = type.getRuntimeClass().getName();
    }

    // Add the class to the out-of-scope classes if it is not specified by the user.
    if (!OutOfScopeClassTracker.getInScopeClasses().contains(className)) {
      OutOfScopeClassTracker.addOutOfScopeClass(type.getRuntimeClass());
    }
  }
}
