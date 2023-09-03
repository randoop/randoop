package randoop.generation;

import java.lang.reflect.*;
import java.util.*;
import randoop.DummyVisitor;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.operation.*;
import randoop.sequence.*;
import randoop.test.DummyCheckGenerator;
import randoop.types.NonParameterizedType;
import randoop.types.PrimitiveType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.ListOfLists;
import randoop.util.Randomness;
import randoop.util.SimpleList;

/**
 * Implements the Detective component, as described by the paper "GRT: Program-Analysis-Guided
 * Random Testing" by Ma et. al (appears in ASE 2015):
 * https://people.kth.se/~artho/papers/lei-ase2015.pdf .
 *
 * <p>A demand-driven approach to construct input objects that are missing for all methods under
 * tests (MUTs). Sometimes not all MUTs can be tested, which could be due to reasons such as
 * required object of type is defined in a third-party library, or the object is created by a method
 * that is not accessible.
 */
public class Detective {

  /**
   * Performs a demand-driven approach for constructing input objects of a specified type that are
   * not directly available.
   *
   * <p>This method identifies a set of dependent methods that return or construct objects of the
   * required type. It then generates method sequences for each of these dependent methods by
   * obtaining necessary inputs from the provided object pools. The resultant method sequence is
   * then executed, and if successful, the resultant object is stored in the secondary object pool.
   *
   * <p>Finally, it extracts the method sequences that produce objects of the required type from the
   * secondary object pool, which can then be utilized for future use.
   *
   * @param mainObjPool The main object pool used for obtaining necessary inputs for the dependent
   *     methods.
   * @param secondObjPool The secondary object pool used for storing the resultant objects of the
   *     method sequences.
   * @param t The class type for which the input objects need to be constructed.
   * @return A SimpleList of method sequences that produce objects of the required type.
   */
  // TODO: I should consider using getDeclaredMethods() instead of getMethods() to extract all
  // methods
  //  rather than only the public ones
  public static SimpleList<Sequence> demandDrivenInputCreation(
      ObjectPool mainObjPool, ObjectPool secondObjPool, Type t) {
    // Extract all constructors/methods that constructs/returns the demanded type by
    // searching through all dependent methods of the main object pool
    Set<TypedOperation> dependentMethodSet = extractDependentMethods(t);

    // For each dependent method, create a sequence that produces an object of the demanded type
    // if possible, or produce a sequence that leads to the eventual creation of the demanded type
    for (TypedOperation dependentMethod : dependentMethodSet) {
      Sequence newSequence = getInputAndGenSeq(mainObjPool, secondObjPool, dependentMethod);
      if (newSequence != null) {
        // Execute the sequence and store the resultant object in the secondary object pool
        // if the sequence is successful
        processSuccessfulSequence(secondObjPool, newSequence);
      }
    }

    // Extract all method sequences that produce objects of the demanded type from the secondary
    // object pool
    // and return them
    return extractCandidateMethodSequences(secondObjPool, t);
  }

  /**
   * Identifies a set of methods that construct objects of a specified type. This method is used in
   * the process of demand-driven input creation to find the necessary methods to create objects of
   * the required type.
   *
   * <p>The method checks for all visible methods in the specified class type, including
   * constructors and methods that return the required type. It also recursively searches for inputs
   * needed to execute a method that returns the sought-after type. The recursive search terminates
   * if the current type is a primitive type or if it has already been processed.
   *
   * @param t The class type for which the dependent methods need to be identified.
   * @return A set of dependent methods that construct objects of the required type.
   */
  public static Set<TypedOperation> extractDependentMethods(Type t) {
    Set<Type> processedSet = new LinkedHashSet<>();
    Queue<Type> workList = new ArrayDeque<>();
    Set<TypedOperation> dependentMethodSet = new LinkedHashSet<>();
    Set<Type> dependentTypeSet = new LinkedHashSet<>();
    workList.add(t);

    // Recursively search for methods that construct objects of the required type
    while (!workList.isEmpty()) {
      Type currentType = workList.poll();

      // Skip if the current type is a primitive type or if it has already been processed
      if (processedSet.contains(currentType) || currentType.isNonreceiverType()) {
        workList.poll();
      } else {
        Class<?> currentTypeClass = currentType.getRuntimeClass();
        List<Executable> executableList =
            new ArrayList<>(List.of(currentTypeClass.getConstructors()));
        executableList.addAll(List.of(currentTypeClass.getMethods()));
        for (Executable executable : executableList) {
          if (executable instanceof Constructor
              || (executable instanceof Method
                  && ((Method) executable).getReturnType().equals(currentTypeClass))) {
            // Obtain the input types and output type of the executable
            Class<?>[] reflectionInputTypes = executable.getParameterTypes();
            Class<?> reflectionOutputType =
                executable instanceof Constructor
                    ? ((Constructor<?>) executable).getDeclaringClass()
                    : ((Method) executable).getReturnType();

            // Create TypeTuple for input types
            List<Type> inputTypeList = classArrayToTypeList(reflectionInputTypes);
            TypeTuple inputTypes = new TypeTuple(inputTypeList);

            // Create Type for output type
            Type outputType = classToType(reflectionOutputType);

            // Determine whether the method call is a constructor call or a method call
            // and create the corresponding subclass of CallableOperation
            CallableOperation callableOperation =
                executable instanceof Constructor
                    ? new ConstructorCall((Constructor<?>) executable)
                    : new MethodCall((Method) executable);

            NonParameterizedType declaringType =
                new NonParameterizedType(currentType.getRuntimeClass());
            TypedOperation typedClassOperation =
                new TypedClassOperation(callableOperation, declaringType, inputTypes, outputType);

            // Add the method call to the dependentMethodSet
            dependentMethodSet.add(typedClassOperation);
            dependentTypeSet.addAll(inputTypeList);
          }
          processedSet.add(currentType);
          workList.addAll(dependentTypeSet);
        }
      }
    }
    return dependentMethodSet;
  }

  // Helper methods for demand-driven input creation
  // Turns reflection class array into TypeTuple
  private static List<Type> classArrayToTypeList(Class<?>[] classes) {
    List<Type> inputTypeList = new ArrayList<>();
    for (Class<?> inputType : classes) {
      if (inputType.isPrimitive()) {
        PrimitiveType primitiveType = PrimitiveType.forClass(inputType);
        inputTypeList.add(primitiveType);
      } else {
        NonParameterizedType nonParameterizedType = new NonParameterizedType(inputType);
        inputTypeList.add(nonParameterizedType);
      }
    }
    return inputTypeList;
  }

  // Helper methods for demand-driven input creation
  // Turns reflection class into Type
  private static Type classToType(Class<?> reflectionClass) {
    if (reflectionClass.isPrimitive()) {
      return PrimitiveType.forClass(reflectionClass);
    } else {
      return new NonParameterizedType(reflectionClass);
    }
  }

  /**
   * Given a TypedOperation, this method finds a sequence of method calls that can generate an
   * instance of each input type required by the TypedOperation. It then merges these sequences into
   * a single sequence that culminates in a call to the TypedOperation.
   *
   * @param mainObjPool The main object pool from which to draw input sequences.
   * @param secondObjPool The secondary object pool from which to draw input sequences if the main
   *     object pool does not contain a suitable sequence.
   * @param typedOperation The operation for which input sequences are to be generated.
   * @return A sequence that ends with a call to the provided TypedOperation and contains calls to
   *     generate each required input, or null if no such sequence can be found.
   */
  private static Sequence getInputAndGenSeq(
      ObjectPool mainObjPool, ObjectPool secondObjPool, TypedOperation typedOperation) {
    TypeTuple inputTypes = typedOperation.getInputTypes();
    List<Sequence> inputSequences = new ArrayList<>();

    for (int i = 0; i < inputTypes.size(); i++) {
      // Obtain a sequence that generates an object of the required type from the main object pool
      ObjectPool objSeqPair = mainObjPool.getObjSeqPair(inputTypes.get(i));
      // If no such sequence exists, obtain a sequence from the secondary object pool
      if (objSeqPair.isEmpty()) {
        objSeqPair = secondObjPool.getObjSeqPair(inputTypes.get(i));
        if (objSeqPair.isEmpty()) {
          // If no such sequence exists, return null
          return null;
        }
      }

      // Randomly select an object and sequence from the object-sequence pair
      Object obj = Randomness.randomMember(objSeqPair.getObjects());
      Sequence seq = Randomness.randomMember(objSeqPair.get(obj));

      inputSequences.add(seq);
    }

    // Merge the input sequences into a single sequence
    List<Integer> inputIndices = new ArrayList<>();
    for (int i = 0; i < inputSequences.size(); i++) {
      inputIndices.add(i);
    }

    return Sequence.createSequence(typedOperation, inputSequences, inputIndices);
  }

  /**
   * Executes a given sequence, extracts the last outcome's runtime value if it is a
   * NormalExecution, and adds or updates the value-sequence pair in the provided object pool if the
   * runtime value is not null.
   *
   * @param objectPool The ObjectPool in which the value-sequence pair is to be added or updated.
   * @param sequence The sequence to be executed and possibly added to the object pool.
   */
  public static void processSuccessfulSequence(ObjectPool objectPool, Sequence sequence) {
    // Guaranteed to have only one sequence per execution
    Set<Sequence> setSequence = new HashSet<>();
    setSequence.add(sequence);
    addExecutedSequencesToPool(objectPool, setSequence);
  }

  /**
   * Executes a given set of sequences, extracts the last outcome's runtime value if it is a
   * NormalExecution, and adds or updates the value-sequence pair in the provided object pool if the
   * runtime value is not null.
   *
   * @param objectPool The ObjectPool in which the value-sequence pair is to be added or updated.
   * @param sequenceSet The set of sequences to be executed and possibly added to the object pool.
   */
  private static void addExecutedSequencesToPool(ObjectPool objectPool, Set<Sequence> sequenceSet) {
    for (Sequence genSeq : sequenceSet) {
      ExecutableSequence eseq = new ExecutableSequence(genSeq);
      eseq.execute(new DummyVisitor(), new DummyCheckGenerator());

      Object generatedObjectValue = null;
      ExecutionOutcome lastOutcome = eseq.getResult(eseq.sequence.size() - 1);
      if (lastOutcome instanceof NormalExecution) {
        generatedObjectValue = ((NormalExecution) lastOutcome).getRuntimeValue();
      }

      if (generatedObjectValue != null) {
        objectPool.addOrUpdate(generatedObjectValue, genSeq);
      }
    }
  }

  /**
   * Filters the sequences in the provided object pool based on the specified type and returns a
   * list of lists of sequences, each of which can generate an object of the specified type.
   *
   * @param objectPool The ObjectPool from which sequences are to be extracted.
   * @param t The type based on which sequences are to be filtered.
   * @return A ListOfLists containing sequences that can generate an object of the specified type.
   */
  public static ListOfLists<Sequence> extractCandidateMethodSequences(
      ObjectPool objectPool, Type t) {
    return objectPool.filterByType(t);
  }
}
