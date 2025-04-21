package randoop.generation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
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
import randoop.util.EquivalenceChecker;
import randoop.util.Log;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;

/**
 * A demand-driven approach to construct inputs. Randoop works by selecting a method and then trying
 * to find inputs for that method. Ordinarily, Randoop works bottom-up: if Randoop cannot find
 * inputs for the selected method, it gives up and selects a different method. This demand-driven
 * approach works top-down: if Randoop cannot find inputs for the selected method, then it looks for
 * methods that create values of the necessary type, and iteratively tries to call them.
 *
 * <p>The main entry point is {@link #createSequencesForType}.
 *
 * <p>The demand-driven approach implements the "Detective" component described by the ASE 2015
 * paper <a href="https://people.kth.se/~artho/papers/lei-ase2015.pdf">"GRT: Program-Analysis-Guided
 * Random Testing" by Ma et al.</a>. This is one interpretation of the paper, whose description is
 * ambiguous.
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
   * A set of types that have been processed during the demand-driven input creation process. This
   * set is used to avoid re-processing types that have already been processed. This field is static
   * to avoid revisiting already‐checked types across invocations.
   */
  private static final Set<Type> processed = new HashSet<>();

  /**
   * Constructs a new {@code DemandDrivenInputCreator} object.
   *
   * @param sequenceCollection the sequence collection used for generating input sequences. This
   *     should be the component sequence collection ({@link ComponentManager#gralComponents}),
   *     i.e., Randoop's full sequence collection
   * @param objectProducersMap a map of types to lists of operations that produce objects of those
   *     types
   */
  public DemandDrivenInputCreator(
      SequenceCollection sequenceCollection, Map<Type, List<TypedOperation>> objectProducersMap) {
    this.sequenceCollection = sequenceCollection;
    this.secondarySequenceCollection = new SequenceCollection(new ArrayList<Sequence>(0));
    this.objectProducersMap = objectProducersMap;
  }

  /**
   * Performs a demand-driven approach for constructing input objects of a target type, when the
   * sequence collection of this DemandDrivenInputCreator contains no objects of that type.
   *
   * <p>It starts by identifying all constructors and methods that produce an instance of the target
   * type from the provided {@code targetType}. For each candidate, the algorithm extracts the
   * required parameters and adds types to a worklist for further processing. Processing for a given
   * type stops when it is either a non-receiver type or has already been processed.
   *
   * <p>Once all the necessary parameters for a candidate are available in the provided sequence
   * collection, the method assembles the corresponding execution sequence, executes it, stores the
   * resulting object for future use, and returns the sequence. If no sequences that can produce the
   * target type are found, the method returns an empty list.
   *
   * <p>Here is the demand-driven algorithm in more detail:
   *
   * <ol>
   *   <li>Let producerMethods := empty list
   *   <li>Initialize a worklist with the {@code targetType}.
   *   <li>For each type T in the worklist, until it is empty:
   *       <ul>
   *         <li>Remove T from the worklist.
   *         <li>Continue the loop (skip type T) if T was already processed or if T is a
   *             non-receiver type.
   *         <li>Identify constructors and methods of T that can produce objects of type T or of
   *             type {@code targetType}. Add them to producerMethods.
   *         <li>Add input parameter types of these producer methods to the front of the worklist.
   *       </ul>
   *   <li>For each producer method, try to find sequences for its inputs in the sequence
   *       collection.
   *       <ul>
   *         <li>If inputs are found, create and execute a sequence. Store successful sequences in a
   *             secondary sequence collection.
   *       </ul>
   *   <li>Let result := sequences in the secondary sequence collection that produce objects of the
   *       target type.
   *   <li>Add result to the main sequence collection.
   *   <li>Return result.
   * </ol>
   *
   * <p><b>Invariant:</b> This method is invoked only when the component sequence collection ({@link
   * ComponentManager#gralComponents}) does not contain a sequence that produces an object of a type
   * compatible with the one required by the forward generator, and when the required type cannot be
   * instantiated using any methods from the class under test.
   *
   * <p>Side-effects:
   *
   * <ul>
   *   <li>Sequence that can successfully create an object of the target type are added to the main
   *       sequence collection {@link#sequenceCollection}.
   *   <li>If no producer methods are found for the target type, the method logs a warning and adds
   *       the target type to the {@link UninstantiableTypeTracker}.
   *   <li>Types not specified by the user but used in the generation process are added to the
   *       {@link UnspecifiedClassTracker}.
   * </ul>
   *
   * @param targetType the type of objects to create
   * @param exactTypeMatch if true, only sequences that declare values of the exact requested type
   *     are returned; if false, sequences that declare values of subtypes of the requested type are
   *     also returned
   * @param onlyReceivers if true, only sequences that are appropriate to use as a method call
   *     receiver are returned; if false, sequences regardless of whether they can be used as
   *     receivers are returned
   * @return method sequences that produce objects of the target type if any are found, or an empty
   *     list otherwise
   */
  public SimpleList<Sequence> createSequencesForType(
      Type targetType, boolean exactTypeMatch, boolean onlyReceivers) {
    // Do not process generic types. Randoop cannot generate inputs for
    // generic types.
    if (targetType.isGeneric()) {
      return new SimpleArrayList<>();
    }

    // Constructors/methods that return the demanded type.
    Set<TypedOperation> producerMethods = getProducers(targetType);

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
    return result;
  }

  /**
   * Returns constructors and methods that return objects of the target type.
   *
   * <p>Identifies constructors and methods that produce an instance of {@code targetType} (or a
   * compatible type), starting from {@code targetType} itself. When a matching constructor or
   * method is found, its parameter types are added to a worklist so that the operations needed to
   * construct these inputs can be discovered. The process stops when a type is non-receiver or has
   * already been processed, and the collected operations are returned as a set.
   *
   * @param targetType the return type of the resulting methods
   * @return a set of {@code TypedOperations} (constructors and methods) that return the target type
   *     {@code targetType}
   */
  private Set<TypedOperation> getProducers(Type targetType) {
    Set<TypedOperation> result = new LinkedHashSet<>();
    Deque<Type> workList = new ArrayDeque<>();
    workList.add(targetType);

    while (!workList.isEmpty()) {
      Type currentType = workList.remove();

      // Skip if already processed or if it is a non-receiver type.
      if (processed.contains(currentType) || currentType.isNonreceiverType()) {
        continue;
      }
      processed.add(currentType);

      // For logging or bookkeeping purposes.
      checkAndAddUnspecifiedType(currentType);

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

          // Final qualification
          boolean qualifies = assignable && !needReceiver;
          if (!qualifies) {
            continue;
          }

          // Add this operation as a producer of the type.
          result.add(op);

          // Add each of its parameter types for further processing.
          for (Type paramType : op.getInputTypes()) {
            if (!paramType.isNonreceiverType() && !processed.contains(paramType)) {
              workList.add(paramType);
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Get sequences that generate inputs for the given operation, then create a sequence for the
   * operation. Returns null if the inputs are not found.
   *
   * @param typedOperation the operation for which input sequences are to be generated
   * @return a sequence for the given {@code TypedOperation}, or {@code null} if the inputs are not
   *     found
   */
  private @Nullable Sequence getInputAndGenSeq(TypedOperation typedOperation) {
    TypeTuple inputTypes = typedOperation.getInputTypes();
    List<Sequence> inputSequences = new ArrayList<>();

    // Represents the position of a statement within a sequence.
    // Tracks the index of statement that generates an object of the required type.
    int stmtIndex = 0;

    // Create an input type to index mapping.
    // This allows us to find the exact statements in a sequence that generate objects
    // of the type required by the typedOperation.
    Map<Type, List<Integer>> typeToIndex = new HashMap<>();

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

      // For each statement in the sequence, add the index of the statement to the typeToIndex map.
      for (int j = 0; j < seq.size(); j++) {
        Type type = seq.getVariable(j).getType();
        typeToIndex.computeIfAbsent(type, k -> new ArrayList<>()).add(stmtIndex++);
      }
    }

    // The indices of the statements in the sequence that will be used as inputs to the
    // typedOperation.
    List<Integer> inputIndices = new ArrayList<>();

    // For each input type of the operation, find the indices of the statements in the sequence
    // that generates an object of the required type.
    Map<Type, Integer> typeIndexCount = new HashMap<>();
    for (Type inputType : inputTypes) {
      List<Integer> indices = findCompatibleIndices(typeToIndex, inputType);
      if (indices.isEmpty()) {
        return null; // No compatible type found, cannot proceed
      }

      int count = typeIndexCount.getOrDefault(inputType, 0);
      if (count < indices.size()) {
        inputIndices.add(indices.get(count));
        typeIndexCount.put(inputType, count + 1);
      } else {
        return null; // Not enough sequences to satisfy the input needs
      }
    }

    return Sequence.createSequence(typedOperation, inputSequences, inputIndices);
  }

  /**
   * Given a map of types to indices and a target type, this method returns a list of indices that
   * are compatible with the target type. This method considers boxing equivalence when comparing
   * boxed and unboxed types, but does not consider subtyping.
   *
   * @param typeToIndex a map of types to indices
   * @param targetType the target type
   * @return a list of indices that are compatible with the target type
   */
  private List<Integer> findCompatibleIndices(
      Map<Type, List<Integer>> typeToIndex, Type targetType) {
    List<Integer> compatibleIndices = new ArrayList<>();
    for (Map.Entry<Type, List<Integer>> entry : typeToIndex.entrySet()) {
      if (EquivalenceChecker.areEquivalentTypesConsideringBoxing(entry.getKey(), targetType)) {
        compatibleIndices.addAll(entry.getValue());
      }
    }
    return compatibleIndices;
  }

  /**
   * Executes each sequence in {@code sequenceSet}. If a sequence ends in a {@link NormalExecution}
   * whose runtime value is non-null, the sequence is copied into {@link
   * #secondarySequenceCollection}.
   *
   * <p>Building up the secondary pool like this lets later steps compose these helper sequences
   * until one finally produces an object of the original {@code targetType}.
   *
   * @param sequenceSet sequences to execute
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
   * Checks if the type was specified by the user. If not, adds the class as an unspecified class.
   *
   * @param type the type to check
   */
  private static void checkAndAddUnspecifiedType(Type type) {
    String className;
    if (type.isArray()) {
      className = ((ArrayType) type).getElementType().getRuntimeClass().getName();
    } else {
      className = type.getRuntimeClass().getName();
    }

    // Add the class to the unspecified classes if it is not user-specified.
    if (!UnspecifiedClassTracker.getSpecifiedClasses().contains(className)) {
      UnspecifiedClassTracker.addClass(type.getRuntimeClass());
    }
  }
}
