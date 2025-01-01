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
import randoop.main.RandoopUsageError;
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
 * <p>The demand-driven approach implements the "Detective" component described by the ASE 2015
 * paper <a href="https://people.kth.se/~artho/papers/lei-ase2015.pdf">"GRT: Program-Analysis-Guided
 * Random Testing" by Ma et al.</a> .
 */
public class DemandDrivenInputCreator {
  /**
   * The principal set of sequences used to create other, larger sequences by the generator. New
   * sequences are added on demand for creating object of missing types. Shared with {@link
   * ComponentManager#gralComponents}.
   */
  private final SequenceCollection sequenceCollection;

  /**
   * If true, {@link #createInputForType(Type)} returns only sequences that declare values of the
   * exact type that was requested.
   */
  private boolean exactTypeMatch;

  /**
   * If true, {@link #createInputForType(Type)} returns only sequences that are appropriate to use
   * as a method call receiver, i.e., Type.isNonreceiverType() returns false for the type of the
   * variable created by the sequence.
   */
  private boolean onlyReceivers;

  // TODO: The original paper uses a "secondary object pool (SequenceCollection in Randoop)"
  // to store the results of the demand-driven input creation. This theoretically reduces
  // the search space for the missing types. Consider implementing this feature and test whether
  // it improves the performance.

  /** Constructs a new {@code DemandDrivenInputCreation} object. */
  public DemandDrivenInputCreator(
      SequenceCollection sequenceCollection, boolean exactTypeMatch, boolean onlyReceivers) {
    this.sequenceCollection = sequenceCollection;
    this.exactTypeMatch = exactTypeMatch;
    this.onlyReceivers = onlyReceivers;
  }

  /**
   * Performs a demand-driven approach for constructing input objects of a target type, when the
   * sequence collection contains no objects of that type.
   *
   * <p>This method processes all available constructors and methods to identify possible ways to
   * create objects of the {@code targetType}. For each method or constructor, it attempts to
   * generate a sequence by searching for necessary inputs from the provided sequence collection,
   * executing the sequence, and, if successful, storing it in the sequence collection for future
   * use.
   *
   * <p>At the end of the process, it filters and returns the sequences that produce objects of the
   * {@code targetType}, if any are found.
   *
   * <p>Here is the demand-driven algorithm in more detail:
   *
   * <ol>
   *   <li>Initialize a worklist with the {@code targetType} and user-specified classes.
   *   <li>Process types in the worklist:
   *       <ul>
   *         <li>Remove the next type. Skip this type if already processed or if it is a
   *             non-receiver type.
   *         <li>Identify constructors and methods that can produce objects of the current type or
   *             the target type.
   *         <li>Add input parameter types of these producer methods to the worklist.
   *       </ul>
   *   <li>For each producer method, try to find sequences for its inputs in the sequence
   *       collection.
   *   <li>If inputs are found, create and execute a sequence. Store successful sequences.
   *   <li>Return sequences that produce the {@code targetType}.
   * </ol>
   *
   * <p>Note that a single call to this method may not be sufficient to construct the target type,
   * even when possible sequences exist. The method may need to be called multiple times to
   * successfully construct the object. If no sequences are found in a single run but the sequence
   * can possibly be constructed, the call to this method often constructs intermediate sequences
   * and stores them in the sequence collection that can help future runs of demand-driven input
   * creation to succeed.
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
  public SimpleList<Sequence> createInputForType(Type targetType) {
    // Constructors/methods that return the demanded type.
    Set<TypedOperation> producerMethods = getProducers(targetType);

    // Check if there are no producer methods
    if (producerMethods.isEmpty()) {
      // Warn the user
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
    // to demand-driven `createInputForType`.
    // Intermediate objects are added to the sequence collection and may be used in future tests.
    for (TypedOperation producerMethod : producerMethods) {
      Sequence newSequence = getInputAndGenSeq(producerMethod);
      if (newSequence != null) {
        // If the sequence is successfully executed, add it to the sequenceCollection.
        executeAndAddToPool(Collections.singleton(newSequence));
      }
    }

    // Note: At the beginning of the `createInputForType` call, getSequencesForType here would
    // return an empty list. However, it is not guaranteed that the method will return a non-empty
    // list at this point.
    // It may take multiple calls to `createInputForType` during the forward generation process
    // to fully construct the specified target type to be used.
    SimpleList<Sequence> result =
        sequenceCollection.getSequencesForType(targetType, exactTypeMatch, onlyReceivers, false);

    return result;
  }

  /**
   * Returns methods that return objects of the target type.
   *
   * <p>Note that the order of the {@code TypedOperation} instances in the resulting set does not
   * necessarily reflect the order in which methods need to be called to construct types needed by
   * the producers.
   *
   * @param targetType the return type of the resulting methods
   * @return a set of {@code TypedOperations} (constructors and methods) that return objects of the
   *     target type {@code targetType}. May return an empty set.
   */
  public Set<TypedOperation> getProducers(Type targetType) {
    Set<TypedOperation> producerMethods = new LinkedHashSet<>();

    // Include user-specified types (types specified by the user via command-line options)
    Set<Type> userSpecifiedTypes = new LinkedHashSet<>();

    // TODO: Considering all user-specified types may do unnecessary work.
    // Not all types are needed to construct the target type. It may be possible to optimize this.
    for (String className : UnspecifiedClassTracker.getSpecifiedClasses()) {
      try {
        Class<?> cls = Class.forName(className);
        userSpecifiedTypes.add(new NonParameterizedType(cls));
      } catch (ClassNotFoundException e) {
        throw new RandoopUsageError("Class not found: " + className);
      }
    }
    userSpecifiedTypes.add(targetType);

    // Search for constructors/methods that can produce the target type.
    producerMethods.addAll(getProducers(targetType, userSpecifiedTypes));

    return producerMethods;
  }

  /**
   * Returns constructors and methods that return objects of the target type.
   *
   * <p>Starting from {@code startingTypes}, examine all visible constructors and methods that
   * return a type compatible with the target type {@code targetType}. It recursively processes the
   * inputs needed to execute these constructors and methods.
   *
   * @param targetType the return type of the resulting methods
   * @param startingTypes the types to start the search from
   * @return a set of {@code TypedOperations} (constructors and methods) that return the target type
   *     {@code targetType}
   */
  private static Set<TypedOperation> getProducers(Type targetType, Set<Type> startingTypes) {
    Set<TypedOperation> result = new LinkedHashSet<>();
    Set<Type> processed = new HashSet<>();
    Queue<Type> workList = new ArrayDeque<>(startingTypes);

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
      for (Method method : currentClass.getMethods()) {
        Type returnType = Type.forClass(method.getReturnType());

        // A method is considered only if it returns a type that is:
        // 1. Assignable to the target type `targetType`, OR
        // 2. Returns the current class and is static
        boolean isStaticAndReturnsCurrentClass =
            returnType.equals(new NonParameterizedType(currentClass))
                && Modifier.isStatic(method.getModifiers());

        if (targetType.isAssignableFrom(returnType) || isStaticAndReturnsCurrentClass) {
          constructorsAndMethods.add(method);
        }
      }

      // Process each constructor/method
      for (Executable executable : constructorsAndMethods) {
        Type returnType;
        if (executable instanceof Constructor) {
          returnType = new NonParameterizedType(currentClass);
        } else if (executable instanceof Method) {
          Method method = (Method) executable;
          returnType = Type.forClass(method.getReturnType());

          // A method is considered only if it returns a type that is:
          // 1. Assignable to the target type `targetType`, OR
          // 2. Returns the current class and is static
          boolean isStaticAndReturnsCurrentClass =
              returnType.equals(new NonParameterizedType(currentClass))
                  && Modifier.isStatic(method.getModifiers());

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
          if (!paramType.isPrimitive() && !processed.contains(paramType)) {
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
    // Used to keep track of the index of the statement that generates an object of the required
    // type.
    int index = 0;

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
        typeToIndex.computeIfAbsent(type, k -> new ArrayList<>()).add(index++);
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
        sequenceCollection.add(genSeq);
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
