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
  private static Set<Class<?>> nonUserSpecifiedClasses = new LinkedHashSet<>();

  /** True if an exact type match is required. */
  private static boolean EXACT_TYPE_MATCH;

  /** If true, only return sequences that are appropriate to use as a method call receiver. */
  private static boolean ONLY_RECEIVERS;

  // TODO: The original paper uses a "secondary object pool (SequenceCollection in Randoop)"
  // to store the results of the demand-driven input creation. This theorectically reduces
  // the search space for the missing types. Consider implementing this feature and test whether
  // it improves the performance.

  /**
   * Performs a demand-driven approach for constructing input objects of a specified type, when the
   * sequence collection contains no objects of that type.
   *
   * <p>This method internally identifies a set of methods/constructors that return objects of the
   * required type. For each of these methods: it generates a method sequence for the method by
   * recursively searching for necessary inputs from the provided sequence collection; executes it;
   * and if successful, stores the sequence in the sequence collection for future use.
   *
   * <p>Finally, it returns the newly-created sequences.
   *
   * <p>Invariant: This method is only called where the component manager lacks an object that is of
   * a type compatible with the one required by the forward generator. See {@link
   * randoop.generation.ForwardGenerator#selectInputs}.
   *
   * @param sequenceCollection the sequence collection from which to draw input sequences
   * @param t the type of objects to create
   * @param exactTypeMatch the flag to indicate whether an exact type match is required
   * @param onlyReceivers if true, only return sequences that are appropriate to use as a method
   *     call receiver
   * @return method sequences that produce objects of the required type
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

    // For each producer method, create a sequence that produces an object of the demanded type
    // if possible, or produce a sequence that leads to the eventual creation of the demanded type.
    for (TypedOperation producerMethod : producerMethods) {
      Sequence newSequence = generateSequenceForCall(sequenceCollection, producerMethod);
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
      logNonUserSpecifiedClasses();
    }

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
    // Methods that return objects of the specified type.
    Set<TypedOperation> producerMethods = new LinkedHashSet<>();

    for (String className : SPECIFIED_CLASSES) {
      try {
        Class<?> cls = Class.forName(className);
        Type specifiedType = new NonParameterizedType(cls);
        producerMethods.addAll(iterativeProducerMethodSearch(t, specifiedType));
      } catch (ClassNotFoundException e) {
        throw new RandoopBug("Class not found: " + className);
      }
    }

    // Recursively search for methods that construct objects of the specified type.
    producerMethods.addAll(iterativeProducerMethodSearch(t, t));

    return producerMethods;
  }

  /**
   * Helper method for getProducerMethods. This method recursively searches for methods that
   * construct objects of the specified type.
   *
   * @param t the return type of the resulting methods
   * @param initType the initial type to start the search
   * @return a set of TypedOperations that construct objects of the specified type t
   */
  private static Set<TypedOperation> iterativeProducerMethodSearch(Type t, Type initType) {
    Set<Type> processed = new HashSet<>();
    boolean initialCall = true; // Flag to indicate the first recursive call.
    List<TypedOperation> producerMethodsList = new ArrayList<>();
    Set<Type> producerParameterTypes = new HashSet<>();
    Queue<Type> workList = new ArrayDeque<>();
    workList.add(initType);
    while (!workList.isEmpty()) {
      Type currentType = workList.poll();

      // Log the nonUserSpecified classes that are used in demand-driven input creation.
      if (!SPECIFIED_CLASSES.contains(currentType.getRuntimeClass().getName())) {
        nonUserSpecifiedClasses.add(currentType.getRuntimeClass());
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

        // The first call checks for methods that return the specified type. Subsequent calls
        // check for methods that return the current type.
        Type returnType = initialCall ? t : currentType;
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
      initialCall = false;
    }

    // TODO: Reverse the producerMethodsList may improve the quality of the generated tests.
    // Producer methods are added to the list in the order they are needed. However, objects are
    // often built up from the simplest types. Reversing the producerMethodsList may help generate
    // basic types first, leading to the generation of more complex types within fewer tests.
    // This needs to be looked into further.
    Collections.reverse(producerMethodsList);

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
   * This method creates a sequence that ends with a call to the given TypedOperation.
   *
   * @param sequenceCollection the SequenceCollection from which to draw input sequences
   * @param typedOperation the operation for which input sequences are to be generated
   * @return a sequence that ends with a call to the provided TypedOperation, or null if no such
   *     sequence can be found
   */
  private static @Nullable Sequence generateSequenceForCall(
      SequenceCollection sequenceCollection, TypedOperation typedOperation) {
    TypeTuple inputTypes = typedOperation.getInputTypes();
    List<Sequence> inputSequences = new ArrayList<>();

    // Represents the position of a statement in a sequence.
    int index = 0;

    // Create a input type to index mapping.
    // This allows us to find the exact statements in a sequence that generate objects
    // of the required type.
    Map<Type, List<Integer>> typeToIndex = new HashMap<>();

    for (int i = 0; i < inputTypes.size(); i++) {
      // Get a set of sequences, each of which generates an object of the required type.
      // TODO: Using getSequencesForType there would cause demand-driven to generate
      // non-generic List when generic List is required. Investigate this.
      SimpleList<Sequence> sequencesOfType =
          getSequencesForTypeConsideringBoxing(sequenceCollection, inputTypes.get(i));

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
   * are compatible with the target type.
   *
   * @param typeToIndex a map of types to indices
   * @param t the target type
   * @return a list of indices that are compatible with the target type
   */
  private static List<Integer> findCompatibleIndices(Map<Type, List<Integer>> typeToIndex, Type t) {
    List<Integer> compatibleIndices = new ArrayList<>();
    for (Map.Entry<Type, List<Integer>> entry : typeToIndex.entrySet()) {
      if (EquivalenceChecker.areEquivalentTypesConsideringBoxing(
          entry.getKey().getRuntimeClass(), t.getRuntimeClass())) {
        compatibleIndices.addAll(entry.getValue());
      }
    }
    return compatibleIndices;
  }

  /**
   * Executes a set of sequences and updates the sequence collection with each successful execution.
   * It iterates through each sequence, executes it, and if the execution is normal and yields a
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
   * Extracts sequences from the sequence collection that can generate an object of the specified
   * type.
   *
   * @param sequenceCollection the SequenceCollection from which to extract sequences
   * @param t the type of object that the sequences should be able to generate
   * @return a ListOfLists containing sequences that can generate an object of the specified type
   */
  public static SimpleList<Sequence> getCandidateMethodSequences(
      SequenceCollection sequenceCollection, Type t) {
    return sequenceCollection.getSequencesForType(t, EXACT_TYPE_MATCH, ONLY_RECEIVERS);
  }

  /**
   * Get a subset of the sequence collection that contains sequences that return specific type of
   * objects. This method considers boxing equivalence when comparing boxed and unboxed types.
   *
   * @param t the type of objects to be included in the subset
   * @return a list of sequences that contains only the objects of the specified type and their
   *     sequences
   */
  private static SimpleList<Sequence> getSequencesForTypeConsideringBoxing(
      SequenceCollection sequenceCollection, Type t) {
    Set<Sequence> subPoolOfType = new HashSet<>();
    Set<Sequence> sequences = sequenceCollection.getAllSequences();
    for (Sequence seq : sequences) {
      if (EquivalenceChecker.areEquivalentTypesConsideringBoxing(
          seq.getLastVariable().getType().getRuntimeClass(), t.getRuntimeClass())) {
        subPoolOfType.add(seq);
      }
    }
    SimpleList<Sequence> subPool = new SimpleArrayList<>(subPoolOfType);
    return subPool;
  }

  /**
   * Get a set of classes that are utilized by the demand-driven input creation process but were not
   * explicitly specified by the user. As of the current manual, Randoop only invokes methods or
   * constructors that are specified by the user. Demand-driven input creation, however, ignores
   * this restriction and uses all classes that are necessary to generate inputs for the specified
   * classes. This method returns a set of nonUserSpecified classes that demand-driven input
   * automatically used.
   *
   * @return a set of nonUserSpecified classes that are automatically included in the demand-driven
   *     input creation process
   */
  public static Set<Class<?>> getNonUserSpecifiedClasses() {
    return nonUserSpecifiedClasses;
  }

  /**
   * Returns true if the set of nonUserSpecified classes is empty.
   *
   * @return true if the set of nonUserSpecified classes is empty, false otherwise.
   */
  public static boolean isNonUserSpecifiedClassEmpty() {
    return nonUserSpecifiedClasses.isEmpty();
  }

  /**
   * Get a set of classes that are utilized by the demand-driven input creation process but were not
   * explicitly specified by the user. This method filters out classes that are part of the Java
   * standard library.
   *
   * @return A set of nonUserSpecified, non-Java classes that are automatically included in the
   *     demand-driven input creation process.
   */
  public static Set<Class<?>> getNonJavaClasses() {
    Set<Class<?>> nonJavaClasses = new LinkedHashSet<>();
    for (Class<?> cls : nonUserSpecifiedClasses) {
      if (!startsWithJava(cls.getName()) && !cls.isPrimitive()) {
        nonJavaClasses.add(cls);
      }
    }
    return nonJavaClasses;
  }

  /**
   * Determines whether a class name starts with "java.".
   *
   * @param className the name of the class
   * @return true if the class name starts with "java.", false otherwise.
   */
  public static boolean startsWithJava(String className) {
    // TODO: `matches()` must be `find()`, once the regex has been made into a Pattern field.
    return className.startsWith("java.") || className.matches("^\\[+.java\\.");
  }

  /**
   * Logs the nonUserSpecified classes that are used in demand-driven input creation to the
   * demand-driven logging file.
   */
  public static void logNonUserSpecifiedClasses() {
    // Write to GenInputsAbstract.demand_driven_logging
    try (PrintWriter writer =
        new PrintWriter(new FileWriter(GenInputsAbstract.demand_driven_logging, UTF_8))) {
      writer.println("NonUserSpecified classes used in demand-driven input creation:");
      for (Class<?> cls : nonUserSpecifiedClasses) {
        writer.println(cls.getName());
      }
    } catch (Exception e) {
      throw new RandoopBug("Error writing to demand-driven logging file: " + e);
    }
  }
}
