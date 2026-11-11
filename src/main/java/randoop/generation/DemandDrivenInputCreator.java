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
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.plumelib.util.CollectionsPlume;
import org.plumelib.util.SIList;
import randoop.DummyVisitor;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.condition.RandoopSpecificationError;
import randoop.main.RandoopBug;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.AccessibilityPredicate;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.TypeInstantiator;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceCollection;
import randoop.test.DummyCheckGenerator;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.DemandDrivenLog;
import randoop.util.Log;
import randoop.util.Randomness;

/**
 * Constructs inputs for types that some method in the SUT needs but is not returned by any SUT
 * method.
 *
 * <p>Randoop normally works bottom-up: it abandons a method call if inputs aren't available. This
 * top-down demand-driven approach gives different treatment to an input whose type is a
 * non-SUT-returned class.
 *
 * <p>Consider a class T that is not returned by any method or constructor declared in the SUT. When
 * an input of type T is needed, the demand-driven algorithm calls every accessible constructor or
 * static method in T that can produce T once (recursively building its inputs if needed, possibly
 * including values of non-SUT-parameter, non-SUT-returned classes). Those results form the
 * candidate set from which Randoop can choose.
 *
 * <p>This creator relies on {@link ComponentManager}, which holds the main {@link
 * SequenceCollection}, to fetch and store existing sequences. Whenever demand-driven needs to build
 * inputs, it queries the ComponentManager's sequence collection for reusable values and adds any
 * newly constructed sequences back into it.
 *
 * <p>TODO: Later, look for methods in every known class that produce T, not just in T itself. This
 * would be an extension of the GRT algorithm.
 *
 * <p>The main entry point is {@link #createSequencesForType}.
 *
 * <p>Definitions:
 *
 * <dl>
 *   <dt>SUT class
 *   <dd>A SUT class is a class that the user specified on the command line to be tested. SUT stands
 *       for "software under test".
 *   <dt>SUT-returned class
 *   <dd>a class that is the return type for some accessible method or constructor in the SUT.
 *   <dt>SUT-parameter class
 *   <dd>a class that is a formal parameter for some accessible method or constructor in the SUT.
 *   <dt>SUT-parameter-only class
 *   <dd>a SUT-parameter class that is not a SUT-returned class
 *   <dt>SUT-parameter-only-closure class
 *   <dd>either:
 *       <ul>
 *         <li>a SUT-parameter-only class, or
 *         <li>a closure-only class: is a formal parameter for some method in a
 *             SUT-parameter-only-closure class. Being a closure-only class does not imply the class
 *             is SUT-returned or SUT-parameter; it may be either, both, or neither. (TODO: after
 *             optimizing to skip exploring methods whose parameter values are already available
 *             from the main sequence collection, restrict this to non-SUT-returned parameters)
 *       </ul>
 *   <dt>non-instantiable class
 *   <dd>a class C that contains no method whose return type is C. (TODO: Later, permit
 *       instantiation via any method in a known class that produces C, not just methods in C
 *       itself. This would be an extension of the GRT algorithm.)
 * </dl>
 *
 * None of these subsumes the others. For example, there may be SUT classes that are not
 * SUT-returned, and there may be SUT-returned classes that are not SUT classes.
 *
 * <p>This class implements the "Detective" component from the ASE 2015 paper <a
 * href="https://people.kth.se/~artho/papers/lei-ase2015.pdf">"GRT: Program-Analysis-Guided Random
 * Testing" by Ma et al.</a>
 */
public class DemandDrivenInputCreator {
  /**
   * Randoop's main sequence collection or pool. It contains objects of SUT-returned classes. It
   * also contains objects of some SUT-parameter-only classes: those promoted after demand-driven
   * creation when their value will be used as an argument to a SUT operation (so normal generation
   * can reuse them). It never contains sequences whose result type is a closure-only class.
   *
   * <p>This is the same object as {@code gralComponents} in {@link ComponentManager}.
   */
  private final SequenceCollection sequenceCollection;

  /**
   * Secondary collection for sequences whose result type lies in the SUT-parameter-only-closure.
   *
   * <p>Whenever demand-driven input creation produces a SUT-parameter-only value that will be used
   * as an argument, that sequence is also copied into the main {@link #sequenceCollection} so it
   * can be reused by subsequent calls. The two collections intentionally overlap on such promoted
   * SUT-parameter-only sequences; closure-only sequences remain only here.
   *
   * <p>This separation is a performance optimization to prevent the main sequence collection from
   * growing too large with sequences that are not needed for SUT-returned classes.
   */
  private final SequenceCollection secondarySequenceCollection = new SequenceCollection();

  /**
   * All reference types visited during demand-driven producer discovery (per instance, cumulative).
   *
   * <p>A type is considered "visited" if its constructors or static methods were inspected as
   * potential producers, or if it appeared as a parameter type of such a producer. Being visited
   * does not imply the class is SUT-returned or SUT-parameter; it may be either, both, or neither.
   * (TODO: Currently, visited classes are equivalent to SUT-parameter-only-closure classes, but
   * this will no longer hold after optimizing to skip exploring methods whose parameter values are
   * already available from the main sequence collection.) Non-receiver types (primitive/void) are
   * excluded. This set is therefore a superset of all non-SUT classes referenced during
   * demand-driven input creation.
   *
   * <p><strong>Purpose:</strong> Log the <em>non-SUT</em> subset of visited types to inform users
   * about external dependencies introduced by demand-driven input creation, which is an exception
   * to Randoop's usual contract of using only SUT classes.
   */
  private final Set<Type> visitedTypes = new HashSet<>();

  /**
   * A set of SUT types that were found to be non-instantiable, at any time during this run. This is
   * cumulative diagnostic information for the user; it is retained when sequences are cleared.
   *
   * @see #uninstantiableTypesSinceClear
   */
  private final Set<Type> uninstantiableTypes = new LinkedHashSet<>();

  /**
   * A set of SUT types that were found to be non-instantiable since the sequence collections were
   * last cleared. This is used to avoid generating sequences through {@link
   * DemandDrivenInputCreator} for such types.
   *
   * <p>Whether a type is instantiable can depend on the contents of the main sequence collection:
   * instantiating a generic producer uses the types in that collection. Therefore, this set is
   * reset by {@link #clearSecondarySequences}, so that producers are reconsidered after a clear.
   */
  private final Set<Type> uninstantiableTypesSinceClear = new HashSet<>();

  /**
   * Maps each type whose candidate producers (see {@link #getCandidateProducers}) were all unusable
   * at the type's most recent request to the value of {@link
   * SequenceCollection#getTypesModificationCount} at that request. For example, a generic producer
   * is not usable while the type instantiator cannot instantiate it from the types of the sequences
   * in {@link #sequenceCollection}. Unlike a type in {@link #uninstantiableTypes}, such a type is
   * retried, but only after those types change. A type is removed from this map when one of its
   * producers becomes usable.
   *
   * <p>Not retrying while the types are unchanged is a heuristic: the type instantiator makes
   * random choices, so a retry with the same types could succeed where the previous attempt failed.
   */
  private final Map<Type, Integer> typesWithoutUsableProducers = new HashMap<>();

  /**
   * A cache for {@link #getCandidateProducers}. The candidate producers of a type do not change
   * during a run.
   */
  private final Map<Type, List<TypedOperation>> candidateProducersCache = new HashMap<>();

  /**
   * A predicate that determines what constructors/methods are accessible (e.g., {@code public}) to
   * be used during demand-driven input creation.
   */
  private final AccessibilityPredicate accessibility;

  /**
   * Constructs a new {@code DemandDrivenInputCreator} object.
   *
   * @param sequenceCollection the sequence collection used for generating input sequences. This
   *     should be the component sequence collection ({@code gralComponents} from {@link
   *     ComponentManager}), i.e., Randoop's full sequence collection.
   * @param accessibility decides which constructors/methods are callable from the generated test
   *     code
   */
  public DemandDrivenInputCreator(
      SequenceCollection sequenceCollection, AccessibilityPredicate accessibility) {
    this.sequenceCollection = sequenceCollection;
    this.accessibility = accessibility;
  }

  /**
   * Removes all sequences from the secondary sequence collection, so that sequences created before
   * the main sequence collection was cleared are not reintroduced into it. Also forgets which types
   * were found to be uninstantiable for the purpose of skipping them, because instantiability can
   * depend on the contents of the main sequence collection (see {@link
   * #uninstantiableTypesSinceClear}). Retains the diagnostic information: the visited types (which
   * are reported for the whole run) and the cumulative set of uninstantiable types. Also retains
   * the types without usable producers; clearing the main sequence collection changes its types, so
   * the next request for such a type retries it.
   */
  public void clearSecondarySequences() {
    secondarySequenceCollection.clear();
    uninstantiableTypesSinceClear.clear();
  }

  /**
   * Returns the number of sequences in the secondary sequence collection.
   *
   * @return the number of sequences in the secondary sequence collection
   */
  public int numSecondarySequences() {
    return secondarySequenceCollection.size();
  }

  /**
   * Returns the set of visited types.
   *
   * @return the set of visited types
   */
  public Set<Type> getVisitedTypes() {
    return visitedTypes;
  }

  /**
   * Creates sequences to construct objects of the target type.
   *
   * <ol>
   *   <li>Finds constructors/methods in the target type that return the target type.
   *   <li>Calls each such producer method once.
   *       <ul>
   *         <li>Recursively creates all necessary inputs.
   *       </ul>
   *   <li>Returns sequences that produce objects of the target type.
   * </ol>
   *
   * This method has the following side effects:
   *
   * <ul>
   *   <li>Adds the returned sequences to the main sequence collection. Adds the returned sequences
   *       to the secondary sequence collection. This is the only way to add sequences to the
   *       secondary sequence collection.
   *   <li>Logs warnings and adds a target type to the {@code uninstantiableTypes} and {@code
   *       uninstantiableTypesSinceClear} sets if the target type has no candidate producers. If the
   *       target type has candidate producers but none of them is usable now (e.g., a generic
   *       producer that cannot be instantiated from the types of the current sequences), records
   *       the target type in {@code typesWithoutUsableProducers} instead, so that a later call
   *       retries it once those types change.
   *   <li>Adds newly visited types to the {@code visitedTypes} set.
   * </ul>
   *
   * <p>For the detailed algorithm description, see the GRT paper.
   *
   * @param targetType the type of object to create. This type is SUT-parameter-only. This type
   *     might or might not be uninstantiable, but it has not yet been recorded as such.
   * @param exactTypeMatch if true, returns only sequences producing the exact requested type; if
   *     false, includes sequences producing subtypes of the requested type
   * @param onlyReceivers if true, returns only sequences usable as method call receivers; if false,
   *     returns all sequences regardless of receiver usability
   * @return a possibly empty list of sequences that produce objects of the target type. They are
   *     also added to the secondary sequence.
   */
  @RequiresNonNull("this.secondarySequenceCollection.sequenceMap")
  public SIList<Sequence> createSequencesForType(
      Type targetType, boolean exactTypeMatch, boolean onlyReceivers) {
    int typesModificationCount = sequenceCollection.getTypesModificationCount();
    Integer failedAtCount = typesWithoutUsableProducers.get(targetType);
    if (failedAtCount != null && failedAtCount == typesModificationCount) {
      // The type instantiator has the same types as at the last attempt, which found no usable
      // producer.
      return SIList.empty();
    }

    List<TypedOperation> producerMethods = getProducers(targetType);

    if (producerMethods.isEmpty()) {
      if (getCandidateProducers(targetType).isEmpty()) {
        Log.logPrintf(
            "Warning: No producer methods found for type %s. Cannot create values of this type.%n",
            targetType);
        // Track the types with no producers.
        uninstantiableTypes.add(targetType);
        uninstantiableTypesSinceClear.add(targetType);
      } else {
        // A later call might succeed, once the type instantiator knows more types.  Log only the
        // first of consecutive failures.
        if (failedAtCount == null) {
          Log.logPrintf("No usable producer methods found yet for type %s.%n", targetType);
        }
        typesWithoutUsableProducers.put(targetType, typesModificationCount);
      }
      return SIList.empty();
    }
    typesWithoutUsableProducers.remove(targetType);

    // For each producer method, create a sequence if possible.
    for (TypedOperation producerMethod : producerMethods) {
      Sequence newSequence = getInputsAndGenSeq(producerMethod);
      if (newSequence != null) {
        // If the sequence is successfully executed, add it to the secondary sequenceCollection.
        executeAndAddToSecondaryPool(newSequence);
      }
    }

    // Note: At the beginning of this method, this call to `getSequencesForType()` would
    // return an empty list. It may or may not return a non-empty list at this point.
    SIList<Sequence> result =
        secondarySequenceCollection.getSequencesForType(
            targetType, exactTypeMatch, onlyReceivers, false);

    sequenceCollection.addAll(result);
    return result;
  }

  /**
   * Returns constructors and methods within the target type that return objects of the target type.
   * May also return producers for their parameter types, recursively.
   *
   * @param targetType the return type of the operations to find. This type is a SUT-parameter-only
   *     type.
   * @return a list of {@code TypedOperation} instances, including both:
   *     <ul>
   *       <li>producers whose output is assignable to {@code targetType}, and
   *       <li>all new producers discovered recursively while inspecting and constructing the
   *           necessary parameter types.
   *     </ul>
   */
  private List<TypedOperation> getProducers(Type targetType) {
    List<TypedOperation> result = new ArrayList<>();
    // The worklist is used as a stack, not a queue.
    Deque<Type> worklist = new ArrayDeque<>();
    Set<Type> processed = new HashSet<>();
    worklist.add(targetType);

    while (!worklist.isEmpty()) {
      Type currentType = worklist.remove();

      if (currentType.isNonreceiverType()) {
        continue;
      }
      if (!processed.add(currentType)) {
        // `currentType` was already a member of `processed`.
        continue;
      }

      // Add to `result` all constructors and methods that return an instance of the current type
      // and are accessible to Randoop.

      for (TypedOperation op : getCandidateProducers(currentType)) {
        TypedOperation producer = instantiateToProduce((TypedClassOperation) op, currentType);
        if (producer == null) {
          continue;
        }

        // Add this operation as a producer of the type.
        result.add(producer);

        // Add each of its parameter types for further processing.
        for (Type paramType : producer.getInputTypes()) {
          worklist.addFirst(paramType);
        }
      }
    }

    // Reverse so that producers for parameter types (dependencies) appear
    // before the producers that consume them (ensuring prerequisites come first).
    Collections.reverse(result);

    // Demand-driven input creation may call operations declared in non-SUT classes (guaranteed to
    // be on the classpath when running Randoop), which violates Randoop's invariant that only SUT
    // operations are used in test generation. Here, we log the classes (types) declaring each such
    // operation to notify users about dependencies on non-SUT classes.
    visitedTypes.addAll(processed);

    return result;
  }

  /**
   * Returns the candidate producers of the given type: the accessible constructors and static
   * methods that the type declares and that are generic or whose output type is assignable to the
   * type. A candidate producer is not necessarily usable: a generic one is usable only if the type
   * instantiator can instantiate it so that its output type is assignable to the type (see {@link
   * #instantiateToProduce}).
   *
   * @param type a type
   * @return the candidate producers of {@code type}
   */
  private List<TypedOperation> getCandidateProducers(Type type) {
    List<TypedOperation> result = candidateProducersCache.get(type);
    if (result == null) {
      List<TypedOperation> operations;
      try {
        operations =
            OperationExtractor.operations(
                type.getRuntimeClass(), new DefaultReflectionPredicate(), accessibility);
      } catch (RandoopSpecificationError e) {
        // No specifications are passed to OperationExtractor.operations, so it cannot fail on one.
        throw new RandoopBug("specification error for type " + type, e);
      }
      result = CollectionsPlume.filter(operations, (TypedOperation op) -> isProducer(op, type));
      candidateProducersCache.put(type, result);
    }
    return result;
  }

  /**
   * True iff `op` might produce an instance of `currentType` without needing a receiver: it is a
   * constructor or static method, and it is generic or its output type is assignable to {@code
   * currentType}.
   *
   * @param op the operation to check
   * @param currentType the type to produce
   * @return true if {@code op} is a candidate producer of {@code currentType}
   */
  private boolean isProducer(TypedOperation op, Type currentType) {
    // Only allow constructors and static methods (no receiver needed).
    if (!op.isConstructorCall() && !op.isStatic()) {
      return false;
    }
    // A generic operation might be instantiable so that its output is assignable to the goal type.
    return op.isGeneric() || currentType.isAssignableFrom(op.getOutputType());
  }

  /**
   * Returns the given operation, instantiated if it is generic, if its output type is assignable to
   * the given type.
   *
   * @param op a constructor or static method
   * @param type the type for {@code op} to produce
   * @return {@code op}, instantiated if it is generic, whose output type is assignable to {@code
   *     type}; or null if there is no such instantiation or none was found
   * @see TypeInstantiator#instantiate(TypedClassOperation, Type)
   */
  private @Nullable TypedOperation instantiateToProduce(TypedClassOperation op, Type type) {
    if (op.isGeneric()) {
      return sequenceCollection.getTypeInstantiator().instantiate(op, type);
    }
    return type.isAssignableFrom(op.getOutputType()) ? op : null;
  }

  /**
   * Creates a sequence that executes the given operation by finding sequences for its inputs.
   *
   * <p>Searches for appropriate input sequences in both the main and secondary sequence
   * collections. For each input type, randomly selects a compatible sequence from those available.
   *
   * @param typedOperation the producer for which to generate inputs and create a sequence
   * @return a sequence for the given operation, or {@code null} if some input cannot be found
   */
  private @Nullable Sequence getInputsAndGenSeq(TypedOperation typedOperation) {
    TypeTuple inputTypes = typedOperation.getInputTypes();
    List<Sequence> inputSequences = new ArrayList<>();

    for (Type inputType : inputTypes) {
      Sequence chosen = getSequenceForType(inputType);
      if (chosen == null) {
        return null;
      }
      inputSequences.add(chosen);
    }

    // The indices of the statements in the final, combined sequence that will be used as inputs to
    // the typedOperation.
    List<Integer> inputIndices = new ArrayList<>();

    // Compute the indices of the input values within the final concatenated sequence.
    // Each input sequence contributes one value: its last statement produces an input
    // for the target operation. We record the absolute index of that statement by
    // tracking the cumulative offset of all preceding sequences.
    // TODO: Permit using earlier statements in each input sequence, not just the last one.
    int stmtOffset = 0;
    for (Sequence seq : inputSequences) {
      int size = seq.size();
      int stmtInSeq = size - 1;
      inputIndices.add(stmtOffset + stmtInSeq);
      stmtOffset += size;
    }

    // Create a sequence that calls `typedOperation` on the given inputs.
    return Sequence.createSequence(typedOperation, inputSequences, inputIndices);
  }

  /**
   * Returns one sequence that can serve as an input for {@code inputType}, or {@code null} if none.
   * Searches the main collection first, then the secondary. Requires exact type for primitives,
   * otherwise allows assignable types. Only considers the sequence's last statement output.
   *
   * @param inputType the type of input needed
   * @return a sequence producing a value of the required type, or null if none found
   */
  private @Nullable Sequence getSequenceForType(Type inputType) {
    boolean exactForPrimitives = inputType.isPrimitive();

    // Try main collection
    SIList<Sequence> candidates =
        sequenceCollection.getSequencesForType(inputType, exactForPrimitives, false, false);

    // Fallback to secondary if needed
    if (candidates.isEmpty()) {
      candidates =
          secondarySequenceCollection.getSequencesForType(
              inputType, exactForPrimitives, false, false);
      if (candidates.isEmpty()) {
        return null; // none anywhere
      }
    }

    // Filter by assignability of produced type (last statement).
    List<Sequence> compatible =
        CollectionsPlume.filter(
            candidates,
            (Sequence s) -> inputType.isAssignableFrom(s.getLastStatement().getOutputType()));
    if (compatible.isEmpty()) {
      return null;
    }

    // TODO: Uniform random selection now; swap for Randoop selection strategy later.
    return Randomness.randomMember(compatible);
  }

  /**
   * Executes a sequence and adds any non-null normal execution result to the secondary sequence
   * collection.
   *
   * @param sequence the sequence to execute
   */
  @RequiresNonNull("this.secondarySequenceCollection.sequenceMap")
  private void executeAndAddToSecondaryPool(Sequence sequence) {
    ExecutableSequence executableSequence = new ExecutableSequence(sequence);
    try {
      executableSequence.execute(new DummyVisitor(), new DummyCheckGenerator());
    } catch (Throwable e) {
      DemandDrivenLog.logPrintf("Error executing the following sequence: %s%n", sequence);
      DemandDrivenLog.logStackTrace(e);
      return; // Skip this sequence if execution fails.
    }
    ExecutionOutcome outcome = executableSequence.getResult(executableSequence.sequence.size() - 1);
    if (outcome instanceof NormalExecution) {
      Object generatedObjectValue = ((NormalExecution) outcome).getRuntimeValue();
      if (generatedObjectValue != null) {
        secondarySequenceCollection.add(sequence);
      }
    }
  }

  /**
   * Returns the set of uninstantiable types. These are types that cannot be instantiated due to the
   * absence of producer methods, and no calls to {@link #createSequencesForType} could ever create
   * sequences of these types. Future calls to {@link #createSequencesForType} will not generate
   * sequences for these types.
   *
   * <p>This method exists only so that {@code GenTests} can print them for the user.
   *
   * @return an unmodifiable set of uninstantiable types
   */
  public Set<Type> getUninstantiableTypes() {
    return Collections.unmodifiableSet(uninstantiableTypes);
  }

  /**
   * Checks if the given type is uninstantiable, meaning it has no accessible producer methods that
   * create instances of it. Only considers determinations made since the last call to {@link
   * #clearSecondarySequences}.
   *
   * @param type the type to check
   * @return true if the type is uninstantiable, false otherwise
   */
  public boolean isUninstantiableType(Type type) {
    return uninstantiableTypesSinceClear.contains(type);
  }
}
