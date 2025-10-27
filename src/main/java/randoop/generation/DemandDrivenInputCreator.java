package randoop.generation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.plumelib.util.SIList;
import randoop.DummyVisitor;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
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
 * <p>Consider a class T which is not returned by any method or constructor declared in the SUT.
 * When an input of type T is needed, the demand-driven algorithm calls every accessible constructor
 * or static method in T that can produce T once (recursively building its inputs if needed,
 * possibly including values of non-SUT-parameter, non-SUT-returned classes). Those results form the
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
 *             from the main sequence collection, restrict this to non–SUT-returned parameters)
 *       </ul>
 *   <dt>non-instantiable class
 *   <dd>a class C that contains no method whose return type is C. (TODO: Later, permit
 *       instantiation via any method in a known class that produces C, not just methods in C
 *       itself. This would be an extension of the GRT algorithm.)
 * </dl>
 *
 * None of these subsumes the others: there may be SUT classes that are not SUT-returned, and there
 * may be SUT-returned classes that are not SUT classes.
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
   * Given a TypedOperation whose output or parameter types are unbound type variables (e.g., {@code
   * List<T>} or {@code T}), produces a concrete TypedClassOperation by choosing concrete type
   * arguments (e.g., {@code T->String}).
   */
  private final TypeInstantiator typeInstantiator;

  /**
   * A set of SUT types that are non-instantiable. This is used to avoid generating sequences
   * through {@link DemandDrivenInputCreator} for such types.
   */
  private final Set<Type> uninstantiableTypes = new LinkedHashSet<>();

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
   * @param typeInstantiator a type instantiator that creates concrete instances of
   *     TypedClassOperation
   * @param accessibility decides which constructors/methods are callable from the generated test
   *     code
   */
  public DemandDrivenInputCreator(
      SequenceCollection sequenceCollection,
      TypeInstantiator typeInstantiator,
      AccessibilityPredicate accessibility) {
    this.sequenceCollection = sequenceCollection;
    this.typeInstantiator = typeInstantiator;
    this.accessibility = accessibility;
  }

  /**
   * Getter for the set of visited types.
   *
   * @return the set of visited types
   */
  public Set<Type> getVisitedTypes() {
    return visitedTypes;
  }

  /**
   * Returns the set of uninstantiable types. These are types that cannot be instantiated due to the
   * absence of accessible producer methods in the type itself.
   *
   * @return a set of uninstantiable types.
   */
  public Set<Type> getUninstantiableTypesSet() {
    return uninstantiableTypes;
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
   *   <li>Logs warnings and adds a target type to uninstantiableTypes set if no producers found.
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
   * @return a possibly empty list of sequences that produce objects of the target type
   */
  @RequiresNonNull("this.secondarySequenceCollection.sequenceMap")
  public SIList<Sequence> createSequencesForType(
      Type targetType, boolean exactTypeMatch, boolean onlyReceivers) {
    List<TypedOperation> producerMethods = getProducers(targetType);

    if (producerMethods.isEmpty()) {
      Log.logPrintf(
          "Warning: No producer methods found for type %s. Cannot create values of this type.%n",
          targetType);
      // Track the types with no producers.
      uninstantiableTypes.add(targetType);
      return SIList.empty();
    }

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
   *       <li>all producers discovered recursively while inspecting and constructing the necessary
   *           parameter types.
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

      // Get all constructors and methods that is accessible to Randoop and return an instance
      // of the current type.
      List<TypedOperation> operations =
          OperationExtractor.operations(
              currentType.getRuntimeClass(), new DefaultReflectionPredicate(), accessibility);

      // Iterate over the operations and check if they can produce the target type.
      for (TypedOperation op : operations) {
        Type opOutputType = op.getOutputType();

        // Only consider operations that produce instances of the type we're currently resolving
        if (!isProducer(op, currentType)) {
          continue;
        }

        if (opOutputType.isGeneric()) {
          TypedClassOperation instantiated = typeInstantiator.instantiate((TypedClassOperation) op);
          if (instantiated == null) {
            continue; // Skip if instantiation fails
          }
          op = instantiated;
        }

        // Add this operation as a producer of the type.
        result.add(op);

        // Add each of its parameter types for further processing.
        for (Type paramType : op.getInputTypes()) {
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
   * True iff `op` can produce an instance of `currentType` without needing a receiver we cannot
   * guarantee.
   *
   * @param op the operation to check
   * @param currentType the type we want to produce
   * @return true if `op` can produce an instance of `currentType`
   */
  private boolean isProducer(TypedOperation op, Type currentType) {
    // Output must be assignable to the type we are resolving.
    if (!currentType.isAssignableFrom(op.getOutputType())) {
      return false;
    }
    // We only allow constructors and static methods (no receiver needed).
    return op.isConstructorCall() || op.isStatic();
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
      Sequence chosen = pickCompatibleInputSequence(inputType);
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
      int stmtInSeq = seq.size() - 1;
      inputIndices.add(stmtOffset + stmtInSeq);
      stmtOffset += seq.size();
    }

    // Create a sequence that calls `typedOperation` on the given inputs.
    return Sequence.createSequence(typedOperation, inputSequences, inputIndices);
  }

  /**
   * Returns one sequence that can serve as an input for {@code inputType}, or {@code null} if none.
   * Searches the main collection first, then the secondary; requires exact type for primitives,
   * otherwise allows assignable types; and only considers the sequence's last statement output.
   *
   * @param inputType the type of input needed
   * @return a sequence producing a value of the required type, or null if none found
   */
  private @Nullable Sequence pickCompatibleInputSequence(Type inputType) {
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

    // Filter by assignability of produced type (last statement)
    List<Sequence> compatible = new ArrayList<>();
    for (Sequence s : candidates) {
      Type produced = s.getStatement(s.size() - 1).getOutputType();
      if (inputType.isAssignableFrom(produced)) {
        compatible.add(s);
      }
    }
    if (compatible.isEmpty()) {
      return null;
    }

    // TODO: Uniform random selection now; swap for Randoop selection strategy later)
    return Randomness.randomMember(compatible);
  }

  /**
   * Executes a sequence and adds non-null normal execution results to the secondary sequence
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
   * create instances of it.
   *
   * @param type the type to check
   * @return true if the type is uninstantiable, false otherwise
   */
  public boolean isUninstantiableType(Type type) {
    return uninstantiableTypes.contains(type);
  }
}
