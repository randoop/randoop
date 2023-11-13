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

  // TODO: Test performance with and without the secondary object pool.

  /**
   * Performs a demand-driven approach for constructing input objects of a specified type, when the
   * object pool (and secondary object pool) contains no objects of that type.
   *
   * <p>This method identifies a set of methods that return or construct objects of the required
   * type. For each of these methods: it generates a method sequence for the method by obtaining
   * necessary inputs from the provided object pools; executes it; and if successful, stores the
   * resultant object in the secondary object pool.
   *
   * <p>Finally, it returns the newly-created sequences (that produce objects of the required type)
   * from the secondary object pool.
   *
   * @param mainObjPool the main object pool
   * @param secondObjPool the secondary object pool used for storing the resultant objects of the
   *     method sequences
   * @param t the class type for which the input objects need to be constructed
   * @return a SimpleList of method sequences that produce objects of the required type
   */
  public static SimpleList<Sequence> demandDrivenInputCreation(
      ObjectPool mainObjPool, ObjectPool secondObjPool, Type t) {
    // Extract all constructors/methods that constructs/returns the demanded type by
    // searching through all methods of the main object pool
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
    // object pool and return them
    return extractCandidateMethodSequences(secondObjPool, t);
  }

  /**
   * Identifies a set of methods that construct objects of a specified type. This method is used in
   * the process of demand-driven input creation to find the necessary methods to create objects of
   * the required type.
   *
   * <p>The method checks for all visible methods and constructors in the specified type that return
   * the required type. It also recursively searches for inputs needed to execute a method that
   * returns the sought-after type. The recursive search terminates if the current type is a
   * primitive type or if it has already been processed.
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

      // Only consider the type if it is not a primitive type or if it hasn't already been processed
      if (!processedSet.contains(currentType) && !currentType.isNonreceiverType()) {
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

            List<Type> inputTypeList = classArrayToTypeList(reflectionInputTypes);

            // If the executable is a non-static method, add the receiver type to
            // the front of the input type list
            if (executable instanceof Method && !Modifier.isStatic(executable.getModifiers())) {
              inputTypeList.add(0, new NonParameterizedType(currentTypeClass));
            }

            TypeTuple inputTypes = new TypeTuple(inputTypeList);
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
   * a single sequence.
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
    List<Integer> inputIndices = new ArrayList<>();
    Map<Type, List<Integer>> typeToIndex = new HashMap<>();

    int index = 0;
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

      // For each variable in the sequence, assign an index and map its type to this index
      for (int j = 0; j < seq.size(); j++) {
        Type type = seq.getVariable(j).getType();
        if (!typeToIndex.containsKey(type)) {
          typeToIndex.put(type, new ArrayList<>());
        }
        typeToIndex.get(type).add(index);
        index++;
      }
    }

    Set<Integer> inputIndicesSet = new LinkedHashSet<>();

    // For each input type of the operation, add its corresponding indices
    for (Type inputType : inputTypes) {
      if (typeToIndex.containsKey(inputType)) {
        inputIndicesSet.addAll(typeToIndex.get(inputType));
      }
    }

    // Add the indices to the inputIndices list
    inputIndices.addAll(inputIndicesSet);

    return Sequence.createSequence(typedOperation, inputSequences, inputIndices);
  }

  /**
   * Executes a single sequence and updates the object pool with the outcome if it's a successful execution.
   * This method is a convenience wrapper for processing individual sequences.
   *
   * @param objectPool The ObjectPool where the outcome, if successful, is stored.
   * @param sequence The sequence to be executed.
   */
  public static void processSuccessfulSequence(ObjectPool objectPool, Sequence sequence) {
    // Guaranteed to have only one sequence per execution
    Set<Sequence> setSequence = new HashSet<>();
    setSequence.add(sequence);
    addExecutedSequencesToPool(objectPool, setSequence);
  }

  /**
   * Executes a set of sequences and updates the object pool with each successful execution.
   * It iterates through each sequence, executes it, and if the execution is normal and yields a non-null value,
   * the value along with its generating sequence is added or updated in the object pool.
   *
   * @param objectPool The ObjectPool to be updated with successful execution outcomes.
   * @param sequenceSet A set of sequences to be executed.
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
