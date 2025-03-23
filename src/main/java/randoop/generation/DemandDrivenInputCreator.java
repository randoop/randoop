package randoop.generation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.DummyVisitor;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.operation.CallableOperation;
import randoop.operation.ConstructorCall;
import randoop.operation.MethodCall;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceCollection;
import randoop.test.DummyCheckGenerator;
import randoop.types.ArrayType;
import randoop.types.NonParameterizedType;
import randoop.types.Type;
import randoop.types.TypeTuple;
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
  /**
   * The principal set of sequences used to create other, larger sequences by the generator. New
   * sequences are added on demand for creating objects of missing types. Shared with {@link
   * ComponentManager#gralComponents}. This is Randoop's full sequence collection.
   */
  private final SequenceCollection sequenceCollection;

  /**
   * A secondary sequence collection used to store sequences generated during the demand-driven
   * input creation process. These sequences are added to the main sequence collection if they
   * successfully create objects of the target type.
   *
   * <p>This is an optimization to reduce the search space for the missing types in the main
   * sequence collection.
   */
  private final SequenceCollection secondarySequenceCollection;

  /**
   * If true, {@link #createSequencesForType(Type)} returns only sequences that declare values of
   * the exact type that was requested. If false, it also returns sequences that declare values of
   * subtypes of the requested type.
   */
  private boolean exactTypeMatch;

  /**
   * If true, {@link #createSequencesForType(Type)} returns only sequences that are appropriate to
   * use as a method call receiver, i.e., Type.isNonreceiverType() returns false for the type of the
   * variable created by the sequence. If false, it returns sequences regardless of whether they can
   * be used as receivers.
   */
  private boolean onlyReceivers;

  /**
   * Constructs a new {@code DemandDrivenInputCreator} object.
   *
   * @param sequenceCollection the sequence collection used for generating input sequences. This
   *     should be the component sequence collection ({@link ComponentManager#gralComponents}),
   *     i.e., Randoop's full sequence collection.
   * @param exactTypeMatch if true, {@link #createSequencesForType(Type)} returns only sequences
   *     that declare values of the exact requested type. If false, it also returns sequences that
   *     declare values * of subtypes of the requested type.
   * @param onlyReceivers if true, {@link #createSequencesForType(Type)} returns only sequences
   *     suitable as method call receivers. If false, it returns sequences regardless of whether
   *     they can be used as receivers.
   */
  public DemandDrivenInputCreator(
      SequenceCollection sequenceCollection, boolean exactTypeMatch, boolean onlyReceivers) {
    this.sequenceCollection = sequenceCollection;
    this.secondarySequenceCollection = new SequenceCollection();
    this.exactTypeMatch = exactTypeMatch;
    this.onlyReceivers = onlyReceivers;
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
   * collection, the method assembles the corresponding execution sequence, executes it, and, if
   * successful, stores the resulting object for future use. If unsuccessful, demand-driven input
   * creation gives up for this type within this test generation step, does not add the sequence to
   * the main sequence collection, and returns an empty list.
   *
   * <p>Note: If no sequence for the target type is found in one call to this method, intermediate
   * sequences may still be constructed and stored to improve the chance of finding a sequence in
   * future test generation steps for the same target type through {@link #createSequencesForType}.
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
   *         <li>Add input parameter types of these producer methods to the worklist.
   *       </ul>
   *   <li>Let resultSequences := empty list of sequences
   *   <li>For each producer method, try to find sequences for its inputs in the sequence
   *       collection.
   *       <ul>
   *         <li>If inputs are found, create and execute a sequence. Store successful sequences in
   *             resultSequences.
   *       </ul>
   *   <li>Return sequences in resultSequences that produce the {@code targetType}.
   * </ol>
   *
   * <p>Invariant: This method is only called when the component sequence collection ({@link
   * ComponentManager#gralComponents}) lacks a sequence that creates an object of a type compatible
   * with the one required by the forward generator. See {@link
   * randoop.generation.ForwardGenerator#selectInputs(TypedOperation)}.
   *
   * @param targetType the type of objects to create
   * @return method sequences that produce objects of the target type if any are found, or an empty
   *     list otherwise
   */
  public SimpleList<Sequence> createSequencesForType(Type targetType) {
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

    // For each producer method, create a sequence if possible.
    // Note: The order of methods in `producerMethods` does not guarantee that all necessary
    // methods will be called in the correct order to fully construct the specified type in one call
    // to demand-driven `createSequencesForType`.
    // Intermediate objects are added to the sequence collection of this DemandDrivenInputCreator
    // and may be used in future tests.
    for (TypedOperation producerMethod : producerMethods) {
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
    sequenceCollection.addAll(secondarySequenceCollection);
    return result;
  }

  /**
   * Returns constructors and methods that return objects of the target type.
   *
   * <p>Starting from {@code startingTypes}, examine all visible constructors and methods that
   * return a type compatible with the target type {@code targetType}. It recursively processes the
   * inputs needed to execute these constructors and methods.
   *
   * <p>Note that the order of the {@code TypedOperation} instances in the resulting set does not
   * necessarily reflect the order in which methods need to be called to construct types needed by
   * the producers.
   *
   * @param targetType the return type of the resulting methods
   * @return a set of {@code TypedOperations} (constructors and methods) that return the target type
   *     {@code targetType}
   */
  private static Set<TypedOperation> getProducers(Type targetType) {
    Set<TypedOperation> result = new LinkedHashSet<>();
    Set<Type> processed = new HashSet<>();
    Queue<Type> workList = new ArrayDeque<>();
    workList.add(targetType);

    while (!workList.isEmpty()) {
      Type currentType = workList.remove();

      // Skip if already processed or if it's a non-receiver type
      if (processed.contains(currentType) || currentType.isNonreceiverType()) {
        continue;
      }
      processed.add(currentType);

      // For logging purposes
      checkAndAddUnspecifiedType(currentType);

      Class<?> currentClass = currentType.getRuntimeClass();

      // Get all constructors and methods of the current class
      List<Executable> constructorsAndMethods = new ArrayList<>();
      Collections.addAll(constructorsAndMethods, currentClass.getConstructors());
      Collections.addAll(constructorsAndMethods, currentClass.getMethods());

      // Process each constructor/method
      for (Executable executable : constructorsAndMethods) {
        Type returnType;
        if (executable instanceof Constructor) {
          returnType = currentType;
        } else if (executable instanceof Method) {
          Method method = (Method) executable;
          returnType = Type.forClass(method.getReturnType());

          // A method is considered only if it returns a type that is:
          // 1. Assignable to the target type `targetType`, OR
          // 2. Returns the current class and is static
          boolean isStaticAndReturnsCurrentClass =
              returnType.equals(currentType) && Modifier.isStatic(method.getModifiers());

          if (!(targetType.isAssignableFrom(returnType) || isStaticAndReturnsCurrentClass)) {
            continue;
          }
        } else {
          continue; // Skip other types of executables
        }

        // Obtain the input types of the constructor/method
        TypeTuple inputTypes;
        if (executable instanceof Constructor) {
          inputTypes = TypedOperation.forConstructor((Constructor<?>) executable).getInputTypes();
        } else {
          inputTypes = TypedOperation.forMethod((Method) executable).getInputTypes();
        }

        CallableOperation callableOperation =
            (executable instanceof Constructor)
                ? new ConstructorCall((Constructor<?>) executable)
                : new MethodCall((Method) executable);
        NonParameterizedType declaringType = new NonParameterizedType(currentClass);
        TypedOperation typedClassOperation =
            new TypedClassOperation(callableOperation, declaringType, inputTypes, returnType);

        // Add the method call to the result.
        result.add(typedClassOperation);

        // Add parameter types to the workList for further processing
        for (Type paramType : inputTypes) {
          if (!paramType.isNonreceiverType() && !processed.contains(paramType)) {
            workList.add(paramType);
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
   * Executes a set of sequences and add the successfully executed sequences to the sequence
   * collection allowing them to be used in future tests. A successful execution is a normal
   * execution and yields a non-null value.
   *
   * @param sequenceSet a set of sequences to be executed
   */
  private void executeAndAddToPool(Set<Sequence> sequenceSet) {
    for (Sequence genSeq : sequenceSet) {
      ExecutableSequence eseq = new ExecutableSequence(genSeq);
      eseq.execute(new DummyVisitor(), new DummyCheckGenerator());

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
