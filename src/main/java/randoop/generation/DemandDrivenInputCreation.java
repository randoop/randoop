package randoop.generation;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.FileWriter;
import java.io.PrintWriter;
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
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.plumelib.util.CollectionsPlume;
import randoop.DummyVisitor;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.operation.CallableOperation;
import randoop.operation.ConstructorCall;
import randoop.operation.MethodCall;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.reflection.AccessibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceCollection;
import randoop.test.DummyCheckGenerator;
import randoop.types.ArrayType;
import randoop.types.NonParameterizedType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.EquivalenceChecker;
import randoop.util.Randomness;
import randoop.util.SimpleList;

/**
 * A demand-driven approach to construct inputs. Randoop works by selecting a method and then trying
 * to find inputs for that method. Ordinarily, Randoop works bottom-up: if Randoop cannot find
 * inputs for the selected method, it gives up and selects a different method. This demand-driven
 * approach works top-down: if Randoop cannot find inputs for the selected method, then it looks for
 * methods that create values of the necessary type, and iteratively tries to call them.
 *
 * <p>A simple example that roughly demonstrates how the demand-driven approach works:
 *
 * <ol>
 *   <li>Let {@code A} be the missing type.
 *   <li>Identify constructors and methods that create that create {@code A} (producer methods).
 *   <li>For each producer method (e.g. {@code A.foo(B, C)}):
 *       <ul>
 *         <li>Mark {@code B} and {@code C} as missing types.
 *         <li>Recursively apply steps 1-3 for each missing type if:
 *             <ul>
 *               <li>The type is not primitive.
 *               <li>The type has not been processed.
 *             </ul>
 *       </ul>
 *   <li>Iterate through all producer methods, creating and executing sequences.
 *   <li>Store successful sequences in the sequence collection.
 *   <li>Return sequences that produce objects of type {@code A}.
 * </ol>
 *
 * <p>The demand-driven approach implements the "Detective" component described by the paper "GRT:
 * Program-Analysis-Guided Random Testing" by Ma et al. (appears in ASE 2015): <a
 * href="https://people.kth.se/~artho/papers/lei-ase2015.pdf">...</a> .
 */
public class DemandDrivenInputCreation {

  /**
   * The set of classes that are specified by the user for Randoop to consider. These are classes
   * supplied in the command line arguments (e.g. --classlist).
   */
  private static Set<@ClassGetName String> SPECIFIED_CLASSES =
      GenInputsAbstract.getClassnamesFromArgs(AccessibilityPredicate.IS_ANY);

  /**
   * The set of classes that demand-driven uses to generate inputs but are not specified by the
   * user.
   */
  private static Set<Class<?>> unspecifiedClasses = new LinkedHashSet<>();

  /** A pattern to match class names that are java classes. */
  private static final Pattern JAVA_PATTERN = Pattern.compile("^\\[+.java\\..*");

  /**
   * If true, {@link #createInputForType(SequenceCollection, Type, boolean, boolean)} returns only
   * sequences that declare values of the exact type that was requested.
   */
  private static boolean EXACT_TYPE_MATCH;

  /**
   * If true, {@link #createInputForType(SequenceCollection, Type, boolean, boolean)} only return
   * sequences that are appropriate to use as a method call receiver.
   */
  private static boolean ONLY_RECEIVERS;

  // TODO: The original paper uses a "secondary object pool (SequenceCollection in Randoop)"
  // to store the results of the demand-driven input creation. This theoretically reduces
  // the search space for the missing types. Consider implementing this feature and test whether
  // it improves the performance.

  /**
   * Performs a demand-driven approach for constructing input objects of a specified type, when the
   * sequence collection contains no objects of that type.
   *
   * <p>This method internally identifies a set of methods/constructors that return object that is
   * compatible with (i.e., assignable to the variable of) the specified type. For each of these
   * methods: it generates a method sequence for the method by searching for necessary inputs from
   * the provided sequence collection, executing it, and, if successful, storing the sequence in the
   * sequence collection for future use.
   *
   * <p>Finally, it returns a list of sequences that produce objects of the specified type, if any
   * are found.
   *
   * <p>Note that multiple iterations of this method may be necessary to successfully construct the
   * object. Even if no sequences are found in a single run, the method often constructs
   * intermediate sequences and store them in the sequence collection that can help future runs of
   * demand-driven input creation to succeed.
   *
   * <p>Invariant: This method is only called when the component sequence collection ({@link
   * ComponentManager#gralComponents}) is lacking a sequence that creates an object of a type
   * compatible with the one required by the forward generator. See {@link
   * randoop.generation.ForwardGenerator#selectInputs(TypedOperation)}
   *
   * @param sequenceCollection the component sequence collection
   * @param t the type of objects to create
   * @param exactTypeMatch if true, only returns sequences that declare values of the exact type
   * @param onlyReceivers if true, only returns sequences that are appropriate to use as a method
   *     call receiver
   * @return method sequences that produce objects of the specified type if any are found, or an
   *     empty list otherwise
   */
  public static SimpleList<Sequence> createInputForType(
      SequenceCollection sequenceCollection,
      Type t,
      boolean exactTypeMatch,
      boolean onlyReceivers) {

    EXACT_TYPE_MATCH = exactTypeMatch;
    ONLY_RECEIVERS = onlyReceivers;

    // All constructors/methods found that return the demanded type.
    Set<TypedOperation> producerMethods = getProducerMethods(t);

    // For each producer method, create a sequence if possible.
    // Note: The order of methods in `producerMethods` does not guarantee that all necessary
    // methods will be called in the correct order to fully construct the specified type in one call
    // to demand-driven `createInputForType`.
    // Intermediate objects are added to the sequence collection and may be used in future tests.
    for (TypedOperation producerMethod : producerMethods) {
      Sequence newSequence = createSequenceForOperation(sequenceCollection, producerMethod);
      if (newSequence != null) {
        // If the sequence is successfully executed, add it to the sequenceCollection.
        executeAndAddToPool(sequenceCollection, Collections.singleton(newSequence));
      }
    }

    // Note: At the beginning of the `createInputForType` call, getSequencesForType here would
    // return an empty list. However, it is not guaranteed that the method will return a non-empty
    // list at this point.
    // Multiple iterations of `createInputForType` may be needed to successfully construct the
    // object.
    SimpleList<Sequence> result =
        sequenceCollection.getSequencesForType(t, EXACT_TYPE_MATCH, ONLY_RECEIVERS);

    if (GenInputsAbstract.demand_driven_logging != null) {
      writeUnspecifiedClassesToLog();
    }

    return result;
  }

  /**
   * Returns a set of methods that can be used to construct objects of the specified type.
   *
   * <p>Note that the order of the {@code TypedOperation} instances in the resulting set does not
   * necessarily reflect the order in which methods need to be called to construct the specified
   * type.
   *
   * <p>Despite being called "getProducerMethods", the resulting set of {@code TypedOperations} can
   * contain both constructors and methods.
   *
   * @param t the return type of the resulting methods
   * @return a set of {@code TypedOperations} that construct objects of the specified type {@code
   *     t}, or an empty set if no such methods are found
   */
  public static Set<TypedOperation> getProducerMethods(Type t) {
    Set<TypedOperation> producerMethods = new LinkedHashSet<>();

    // Search for methods that return the specified type in the specified classes.
    for (String className : SPECIFIED_CLASSES) {
      try {
        Class<?> cls = Class.forName(className);
        Type specifiedType = new NonParameterizedType(cls);
        producerMethods.addAll(producerMethodSearch(t, specifiedType));
      } catch (ClassNotFoundException e) {
        throw new RandoopBug("Class not found: " + className);
      }
    }

    // Search starting from the specified type.
    producerMethods.addAll(producerMethodSearch(t, t));

    return producerMethods;
  }

  /**
   * Performs a search for constructors/methods that can produce objects of the specified type.
   *
   * <p>Starting from {@code startingType}, examine all visible constructors/methods in it that
   * return a type compatible with the specified type {@code t}. It then searches for the inputs
   * needed to execute these constructors and methods. For each input type, the method initiates a
   * new search within the input class for constructors/methods that can produce that input type.
   * The search terminates if the current type is a primitive type or if it has already been
   * processed.
   *
   * <p>Despite being called "producerMethodSearch", the resulting set of {@code TypedOperations}
   * may contain both constructors and methods.
   *
   * @param t the return type of the resulting methods
   * @param startingType the type from which to start the search
   * @return a set of {@code TypedOperations} that construct objects of the specified type {@code t}
   */
  private static Set<TypedOperation> producerMethodSearch(Type t, Type startingType) {
    Set<Type> processed = new HashSet<>();
    boolean isSearchingForTargetType = true;
    List<TypedOperation> producerMethodsList = new ArrayList<>();
    Set<Type> producerParameterTypes = new HashSet<>();
    Queue<Type> workList = new ArrayDeque<>();
    workList.add(startingType);

    // Search for constructors/methods that can produce the specified type.
    while (!workList.isEmpty()) {
      // Set the front of the workList as the current type.
      Type currentType = workList.poll();

      // Log the unspecified classes that are used in demand-driven input creation.
      logUnspecifiedClasses(currentType);

      // Only consider the type if it is not a primitive type and if it hasn't already been
      // processed.
      if (!processed.contains(currentType) && !currentType.isNonreceiverType()) {
        Class<?> currentClass = currentType.getRuntimeClass();
        List<Executable> executableList = new ArrayList<>();

        // Adding constructors if the current type is what we are looking for.
        if (t.isAssignableFrom(currentType) && !Modifier.isAbstract(currentClass.getModifiers())) {
          Collections.addAll(executableList, currentClass.getConstructors());
        }

        // Adding methods that return the current type.
        Collections.addAll(executableList, currentClass.getMethods());

        // The first call checks for methods that return the specified type. Subsequent calls
        // check for methods that return the current type.
        Type returnType = isSearchingForTargetType ? t : currentType;
        for (Executable executable : executableList) {
          if (executable instanceof Constructor
              || (executable instanceof Method
                  && returnType.isAssignableFrom(
                      Type.forClass(((Method) executable).getReturnType())))) {

            // Obtain the input types and output type of the executable.
            List<Type> inputTypeList = classArrayToTypeList(executable.getParameterTypes());
            // If the executable is a non-static method, add the receiver type to
            // the front of the input type list.
            if (executable instanceof Method && !Modifier.isStatic(executable.getModifiers())) {
              inputTypeList.add(0, new NonParameterizedType(currentClass));
            }
            TypeTuple inputTypes = new TypeTuple(inputTypeList);
            CallableOperation callableOperation =
                executable instanceof Constructor
                    ? new ConstructorCall((Constructor<?>) executable)
                    : new MethodCall((Method) executable);
            NonParameterizedType declaringType = new NonParameterizedType(currentClass);
            TypedOperation typedClassOperation =
                new TypedClassOperation(callableOperation, declaringType, inputTypes, returnType);

            // Add the method call to the producerMethods.
            producerMethodsList.add(typedClassOperation);
            producerParameterTypes.addAll(inputTypeList);
          }
          processed.add(currentType);
          // Add the parameter types of the current method to the workList.
          // This allows the search for methods that can produce these parameter types,
          // thereby creating the sequences of methods needed to generate the input types
          // for methods that lead to the generation of the specified type.
          workList.addAll(producerParameterTypes);
        }
      }
      isSearchingForTargetType = false;
    }

    // TODO: Reverse the producerMethodsList may improve the quality of the generated tests.
    // Producer methods are added to the list in the order they are needed. However, objects are
    // often built up from the simplest types. Reversing the producerMethodsList may help generate
    // basic types first, leading to the generation of more complex types within fewer tests.
    // This needs to be looked into further.
    Collections.reverse(producerMethodsList);

    return new LinkedHashSet<>(producerMethodsList);
  }

  /**
   * Given an array of classes, this method converts them into a list of {@code Types}.
   *
   * @param classes an array of reflection classes
   * @return a list of Types
   */
  private static List<Type> classArrayToTypeList(Class<?>[] classes) {
    return CollectionsPlume.mapList(Type::forClass, classes);
  }

  /**
   * This method creates a new sequence for the given {@code TypedOperation}. The method iteratively
   * searches for the necessary inputs from the provided sequence collection. If the inputs are
   * found, the method creates a new sequence and returns it. If the inputs are not found, the
   * method returns {@code null}.
   *
   * @param sequenceCollection the {@code SequenceCollection} to look for inputs
   * @param typedOperation the operation for which input sequences are to be generated
   * @return a sequence for the given {@code TypedOperation}, or {@code null} if the inputs are not
   *     found
   */
  private static @Nullable Sequence createSequenceForOperation(
      SequenceCollection sequenceCollection, TypedOperation typedOperation) {
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
          sequenceCollection.getSequencesForType(inputTypes.get(i), inputType.isPrimitive(), false);

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
   * @param t the target type
   * @return a list of indices that are compatible with the target type
   */
  private static List<Integer> findCompatibleIndices(Map<Type, List<Integer>> typeToIndex, Type t) {
    List<Integer> compatibleIndices = new ArrayList<>();
    for (Map.Entry<Type, List<Integer>> entry : typeToIndex.entrySet()) {
      if (EquivalenceChecker.areEquivalentTypesConsideringBoxing(entry.getKey(), t)) {
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
   * @param sequenceCollection the {@code SequenceCollection} to add the sequences to
   * @param sequenceSet a set of sequences to be executed
   */
  private static void executeAndAddToPool(
      SequenceCollection sequenceCollection, Set<Sequence> sequenceSet) {
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
   * Get a set of classes that are utilized by the demand-driven input creation process but were not
   * explicitly specified by the user.
   *
   * <p>As of the current manual, Randoop only invokes methods or constructors that are specified by
   * the user. Demand-driven approach, however, ignores this restriction and uses all classes that
   * are necessary to generate inputs for the specified classes. This method returns a set of
   * classes that demand-driven approach automatically used.
   *
   * @return a set of unspecified classes that are automatically included in the demand-driven input
   *     creation process
   */
  public static Set<Class<?>> getUnspecifiedClasses() {
    return unspecifiedClasses;
  }

  /**
   * Checks if the type is specified by the user for Randoop to consider. If not, logs the class as
   * an unspecified class.
   *
   * @param type the type of the object to check for specification
   */
  private static void logUnspecifiedClasses(Type type) {
    String currentClassName = type.getRuntimeClass().getName();
    if (type.isArray()) {
      currentClassName = ((ArrayType) type).getElementType().getRuntimeClass().getName();
    }

    // Add the class to the unspecified classes if it is not specified by the user.
    if (!SPECIFIED_CLASSES.contains(currentClassName)) {
      unspecifiedClasses.add(type.getRuntimeClass());
    }
  }

  /**
   * Get a set of classes that are utilized by the demand-driven input creation process but were not
   * explicitly specified by the user. This method additionally filters out classes that are part of
   * the Java standard library.
   *
   * @return A set of unspecified, non-Java classes that are automatically included in the
   *     demand-driven input creation process.
   */
  public static Set<Class<?>> getNonJavaClasses() {
    Set<Class<?>> nonJavaClasses = new LinkedHashSet<>();
    for (Class<?> cls : unspecifiedClasses) {
      if (!startsWithJava(cls.getName()) && !cls.isPrimitive()) {
        nonJavaClasses.add(cls);
      }
    }
    return nonJavaClasses;
  }

  /**
   * Determines whether a class name starts with "java." or represents a Java array.
   *
   * @param className the name of the class
   * @return true if the class name starts with "java." or represents a Java array, false otherwise
   */
  public static boolean startsWithJava(String className) {
    return className.startsWith("java.") || JAVA_PATTERN.matcher(className).find();
  }

  /**
   * Writes the unspecified classes that are automatically used in demand-driven input creation but
   * were not explicitly specified by the user to the demand-driven logging file.
   */
  public static void writeUnspecifiedClassesToLog() {
    // Write to GenInputsAbstract.demand_driven_logging
    try (PrintWriter writer =
        new PrintWriter(new FileWriter(GenInputsAbstract.demand_driven_logging, UTF_8))) {
      writer.println("Unspecified classes used in demand-driven input creation:");
      for (Class<?> cls : unspecifiedClasses) {
        writer.println(cls.getName());
      }
    } catch (Exception e) {
      throw new RandoopBug("Error writing to demand-driven logging file: " + e);
    }
  }
}
