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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.plumelib.util.CollectionsPlume;
import randoop.DummyVisitor;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.main.GenInputsAbstract;
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
import randoop.types.NonParameterizedType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.EquivalenceChecker;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;

/**
 * A demand-driven approach to construct inputs. Randoop works by selecting a method, then trying to
 * find inputs to that method. Ordinarily, Randoop works bottom-up: if Randoop cannot find inputs
 * for the selected method, it gives up and selects a different method. This demand-driven approach
 * works top-down: if Randoop cannot find inputs for the selected method, then it looks for methods
 * that create values of the necessary type, and recursively tries to call them.
 *
 * <p>The demand-driven approach implements the "Detective" component described by the paper "GRT:
 * Program-Analysis-Guided Random Testing" by Ma et. al (appears in ASE 2015):
 * https://people.kth.se/~artho/papers/lei-ase2015.pdf .
 */
public class DemandDrivenInputCreation {

  // The set of classes (names) that are specified by the user for Randoop to consider.
  // These are classes supplied in the command line arguments (e.g. --classlist).
  private static Set<@ClassGetName String> SPECIFIED_CLASSES =
      GenInputsAbstract.getClassnamesFromArgs(AccessibilityPredicate.IS_ANY);

  // The set of classes that demand-driven uses to generate inputs but are not specified by the user.
  private static Set<Class<?>> unspecifiedClasses = new LinkedHashSet<>();

  // Options for getting sequences from the SequenceCollection.
  private static boolean EXACT_MATCH = true;
  private static boolean ONLY_RECEIVERS = true;

  // TODO: The original paper uses a "secondary object pool" to store the results of the
  // demand-driven input creation. This theorectically reduces the search space for the
  // missing types. Consider implementing this feature and test whether it improves the
  // performance.

  /**
   * Performs a demand-driven approach for constructing input objects of a specified type, when the
   * sequence collection contains no objects of that type.
   *
   * <p>This method identifies a set of methods/constructors that return objects of the required
   * type. For each of these methods: it generates a method sequence for the method by recursively
   * searching for necessary inputs from the provided sequence collection; executes it; and if
   * successful, stores the sequence in the sequence collection.
   *
   * <p>Finally, it returns the newly-created sequences.
   *
   * <p>Invariant: This method is only called where the component manager lacks an object that is of
   * a type compatible with the one required by the forward generator. See {@link
   * randoop.generation.ForwardGenerator#selectInputs}.
   *
   * @param sequenceCollection the sequence collection from which to draw input sequences
   * @param t the type of objects to create
   * @return method sequences that produce objects of the required type
   */
  public static SimpleList<Sequence> createInputForType(
      SequenceCollection sequenceCollection, Type t, boolean exactMatch, boolean onlyReceivers) {
    EXACT_MATCH = exactMatch;
    ONLY_RECEIVERS = onlyReceivers;

    // All constructors/methods found that return the demanded type.
    Set<TypedOperation> producerMethods = getProducerMethods(t);

    // For each producer method, create a sequence that produces an object of the demanded type
    // if possible, or produce a sequence that leads to the eventual creation of the demanded type.
    for (TypedOperation producerMethod : producerMethods) {
      Sequence newSequence = getInputAndGenSeq(sequenceCollection, producerMethod);
      if (newSequence != null) {
        // Execute the sequence and store the resultant sequence in the sequenceCollection
        // if the execution is successful.
        executeAndAddToPool(sequenceCollection, Collections.singleton(newSequence));
      }
    }

    // Get all method sequences that produce objects of the demanded type from the
    // sequenceCollection.
    SimpleList<Sequence> result = getCandidateMethodSequences(sequenceCollection, t);

    if (GenInputsAbstract.demand_driven_logging != null) {
      logUnspecifiedClasses();
    }

    System.out.println("Result: " + result);

    return result;
  }

  /**
   * Returns a set of methods with a given return type.
   *
   * <p>The method checks for all visible methods and constructors in the specified type that return
   * the same type. It also recursively searches for inputs needed to execute a method that returns
   * the type. The recursive search terminates if the current type is a primitive type or if it has
   * already been processed.
   *
   * @param t the return type of the resulting methods
   * @return a set of TypedOperations that construct objects of the specified type t
   */
  public static Set<TypedOperation> getProducerMethods(Type t) {
    // The set of producer methods that construct objects of the specified type.
    Set<TypedOperation> producerMethods = new LinkedHashSet<>();

    for (String className : SPECIFIED_CLASSES) {
      try {
        Class<?> cls = Class.forName(className);
        Type specifiedType = new NonParameterizedType(cls);
        producerMethods.addAll(iterativeProducerMethodSearch(t, specifiedType, processed));
      } catch (ClassNotFoundException e) {
        // Ignore the class if it cannot be found.
        // TODO: Log the error message.
      }
    }

    System.out.println("");
    System.out.println("--------------------------------------------------");
    System.out.println("t: " + t);
    System.out.println("SPECIFIED_CLASSES: " + SPECIFIED_CLASSES);
    System.out.println("Producer methods before t search: " + producerMethods);

    // Recursively search for methods that construct objects of the specified type.
    producerMethods.addAll(iterativeProducerMethodSearch(t, t, processed));

    System.out.println("Producer methods after t search: " + producerMethods);
    System.out.println("--------------------------------------------------");

    return producerMethods;
  }

  /**
   * Helper method for getProducerMethods. This method recursively searches for methods that
   * construct objects of the specified type.
   * @param t the return type of the resulting methods
   * @param processed a set of types that have already been processed
   * @return a set of TypedOperations that construct objects of the specified type t
   */
  private static Set<TypedOperation> iterativeProducerMethodSearch(Type t, Type initType) {
    Set<Type> processed = new HashSet<>();
    boolean initialRun = true; // The first recursive call checks for t but with initType.
    List<TypedOperation> producerMethodsList = new ArrayList<>();
    // Set<TypedOperation> producerMethods = new LinkedHashSet<>();
    Set<Type> producerParameterTypes = new HashSet<>();
    Queue<Type> workList = new ArrayDeque<>();
    workList.add(initType);
    while (!workList.isEmpty()) {
      Type currentType = workList.poll();

      // Log the unspecified classes that are used in demand-driven input creation.
      if (!SPECIFIED_CLASSES.contains(currentType.getRuntimeClass().getName())) {
        unspecifiedClasses.add(currentType.getRuntimeClass());
      }

      // Only consider the type if it is not a primitive type or if it hasn't already been
      // processed.
      if (!processed.contains(currentType) && !currentType.isNonreceiverType()) {
        Class<?> currentClass = currentType.getRuntimeClass();
        List<Executable> executableList = new ArrayList<>();

        // Adding constructors if the current type is what we are looking for.
        if (t.equals(currentType)) {
          for (Constructor<?> constructor : currentClass.getConstructors()) {
            executableList.add(constructor);
          }
        }

        // Adding methods that return the current type.
        for (Method method : currentClass.getMethods()) {
          executableList.add(method);
        }
        Type returnType = initialRun ? t : currentType;
        for (Executable executable : executableList) {
          if (executable instanceof Constructor
              || (executable instanceof Method
                  && ((Method) executable).getReturnType().equals(returnType.getRuntimeClass()))) {

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
          // Recursively search for methods that construct objects of the specified type.
          workList.addAll(producerParameterTypes);
        }
      }
      initialRun = false;
    }

    Collections.reverse(producerMethodsList);
    // producerMethods.addAll(producerMethodsList);
    Set<TypedOperation> producerMethods = new LinkedHashSet<>(producerMethodsList);

    return producerMethods;
  }

  /**
   * Given an array of classes, this method converts them into a list of Types.
   *
   * @param classes an array of reflection classes
   * @return a list of Types
   */
  private static List<Type> classArrayToTypeList(Class<?>[] classes) {
    return CollectionsPlume.mapList(Type::forClass, classes);
  }

  /**
   * Given a TypedOperation, this method finds a sequence of method calls that can generate an
   * instance of each input type required by the TypedOperation. It then merges these sequences into
   * a single sequence.
   *
   * @param sequenceCollection the SequenceCollection from which to draw input sequences
   * @param typedOperation the operation for which input sequences are to be generated
   * @return a sequence that ends with a call to the provided TypedOperation, or null if no such
   *     sequence can be found
   */
  private static @Nullable Sequence getInputAndGenSeq(
      SequenceCollection sequenceCollection, TypedOperation typedOperation) {
    TypeTuple inputTypes = typedOperation.getInputTypes();
    List<Sequence> inputSequences = new ArrayList<>();

    // Represents the position of a statement in a sequence.
    int index = 0;

    // Create a input type to index mapping.
    // This allows us to find the exact statements in the sequence that generate objects
    // of the required type.
    Map<Type, List<Integer>> typeToIndex = new HashMap<>();

    for (int i = 0; i < inputTypes.size(); i++) {
      // Get a set of sequence that generates an object of the required type from the
      // sequenceCollection.
      // TODO: Investigate if getSubPoolOfType can be replaced with getSequencesForType.
      //  Improve the name of the method if it is to be used.
      SimpleList<Sequence> sequencesOfType =
          getSubPoolOfType(sequenceCollection, inputTypes.get(i));

      // Is there any reason other than primitive-box type equivalence to not use the following
      // line?
      // SimpleList<Sequence> sequencesOfType = sequenceCollection.getSequencesForType(
      //  inputTypes.get(i), EXACT_MATCH, ONLY_RECEIVERS);

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

    List<Integer> inputIndices = new ArrayList<>();

    // For each input type of the operation, find the index of the statement in the sequence
    // that generates an object of the required type.
    Map<Type, Integer> typeIndexCount = new HashMap<>();
    for (Type inputType : inputTypes) {
      List<Integer> indices = findCompatibleIndices(typeToIndex, inputType);
      if (indices.isEmpty()) {
        return null; // No compatible type found, cannot proceed
      }

      Integer count = typeIndexCount.getOrDefault(inputType, 0);
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
   * Get a subset of the sequence collection that contains sequences that returns specific type of
   * objects.
   *
   * @param t the type of objects to be included in the subset
   * @return a list of sequences that contains only the objects of the specified type and their
   *     sequences
   */
  private static SimpleList<Sequence> getSubPoolOfType(
      SequenceCollection sequenceCollection, Type t) {
    Set<Sequence> subPoolOfType = new HashSet<>();
    Set<Sequence> sequences = sequenceCollection.getAllSequences();
    for (Sequence seq : sequences) {
      if (EquivalenceChecker.equivalentTypes(
          seq.getLastVariable().getType().getRuntimeClass(), t.getRuntimeClass())) {
        subPoolOfType.add(seq);
      }
    }
    SimpleList<Sequence> subPool = new SimpleArrayList<>(subPoolOfType);
    return subPool;
  }

  /**
   * Given a map of types to indices and a target type, this method returns a list of indices that
   * are compatible with the target type.
   *
   * @param typeToIndex a map of types to indices
   * @param t the target type
   * @return a list of indices that are compatible with the target type
   */
  private static List<Integer> findCompatibleIndices(Map<Type, List<Integer>> typeToIndex, Type t) {
    List<Integer> compatibleIndices = new ArrayList<>();
    for (Map.Entry<Type, List<Integer>> entry : typeToIndex.entrySet()) {
      if (EquivalenceChecker.equivalentTypes(
          entry.getKey().getRuntimeClass(), t.getRuntimeClass())) {
        compatibleIndices.addAll(entry.getValue());
      }
    }
    return compatibleIndices;
  }

  /**
   * Executes a set of sequences and updates the object pool with each successful execution. It
   * iterates through each sequence, executes it, and if the execution is normal and yields a
   * non-null value, the value along with its generating sequence is added or updated in the object
   * pool.
   *
   * @param sequenceCollection the SequenceCollection to be updated with successful execution
   *     outcomes
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
   * Extracts sequences from the object pool that can generate an object of the specified type.
   *
   * @param sequenceCollection the SequenceCollection from which to extract sequences
   * @param t the type of object that the sequences should be able to generate
   * @return a ListOfLists containing sequences that can generate an object of the specified type
   */
  public static SimpleList<Sequence> getCandidateMethodSequences(
      SequenceCollection sequenceCollection, Type t) {
    return sequenceCollection.getSequencesForType(t, EXACT_MATCH, ONLY_RECEIVERS);
  }

  /**
   * Get a set of classes that are utilized by the demand-driven input creation process but
   * were not explicitly specified by the user.
   * As of the current manual, Randoop only invokes methods or constructors that are specified by the
   * user. Demand-driven input creation, however, ignores this restriction and uses all classes
   * that are necessary to generate inputs for the specified classes. This methods returns a set of
   * unspecified classes to help inform the user of the classes that are automatically included in
   * the testing process.
   *
   * @return A set of unspecified classes that are automatically included in the demand-driven
   * input creation process.
   */
  public static Set<Class<?>> getUnspecifiedClasses() {
    return unspecifiedClasses;
  }

  /**
   * Determines whether the set of unspecified classes is empty.
   * @return true if the set of unspecified classes is empty, false otherwise.
   */
  public static boolean isUnspecifiedClassEmpty() {
    return unspecifiedClasses.isEmpty();
  }

  /**
   * Get a set of classes that are not part of the Java standard library.
   * @return A set of classes that are not part of the Java standard library.
   */
  public static Set<Class<?>> getNonJavaClasses() {
    Set<Class<?>> nonJavaClasses = new LinkedHashSet<>();
    for (Class<?> cls : unspecifiedClasses) {
      // if (!cls.getName().startsWith("java.") && !cls.isPrimitive()) {
      if (!startsWithJava(cls.getName()) && !cls.isPrimitive()) {
        nonJavaClasses.add(cls);
      }
    }
    return nonJavaClasses;
  }

  public static boolean startsWithJava(String className) {
    return className.startsWith("java.") || className.matches("^\\[+.java\\..*");
  }

  /**
   * Logs the unspecified classes that are used in demand-driven input creation
   * to the demand-driven logging file.
   */
  public static void logUnspecifiedClasses() {
    // Write to GenInputsAbstract.demand_driven_logging
    try (PrintWriter writer = new PrintWriter(new FileWriter(GenInputsAbstract.demand_driven_logging, UTF_8))) {
      writer.println("Unspecified classes used in demand-driven input creation:");
      for (Class<?> cls : unspecifiedClasses) {
        writer.println(cls.getName());
      }
    } catch (Exception e) {
      // TODO: Log the error message.
    }
  }
}
