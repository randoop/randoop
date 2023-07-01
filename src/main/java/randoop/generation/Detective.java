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
import randoop.util.SimpleArrayList;
import randoop.util.SimpleList;


/**
 * The Detective class provides a demand-driven approach to constructing input objects.
 * It is designed to prepare all input arguments required for the Methods Under Test (MUTs).
 * <p>
 * The ability to create objects that MUTs depend on greatly affects the number of testable MUTs.
 * The Detective class identifies types that cannot be created by running MUTs only and constructs
 * these missing input objects. It does this by maintaining a secondary object pool and performing
 * a static analysis of the method type dependencies at run-time.
 * <p>
 * The class has two main methods: demandDrivenInputCreation and extractDependentMethods.
 */
public class Detective {

    /**
     * Initializes the main object pool with seed sequences and generated sequences. The seed sequences are primitive sequences
     * provided by the ComponentManager, while the generated sequences are all sequences that have been generated so far.
     * <p>
     * Each sequence is executed and the resultant object is stored in the main object pool along with the sequence that produced it.
     * If the execution outcome is a NormalExecution, the runtime value of the outcome is added to the object pool.
     *
     * @param componentManager The ComponentManager that provides the primitive and generated sequences.
     *
     * @return The initialized main object pool containing objects and their corresponding sequences.
     */
    public static ObjectPool mainObjPoolInitialization(ComponentManager componentManager) {
        // Initialize the main object pool with seed sequences
        Set<Sequence> primitiveSequences = componentManager.getAllPrimitiveSequences();
        ObjectPool mainObjPool = new ObjectPool();

        Map<Object, List<Sequence>> primObjPool = new LinkedHashMap<>();
        for (Sequence primSeq : primitiveSequences) {
            ExecutableSequence eseq = new ExecutableSequence(primSeq);
            eseq.execute(new DummyVisitor(), new DummyCheckGenerator());

            Object primObjectValue = null;
            ExecutionOutcome lastOutcome = eseq.getResult(eseq.sequence.size() - 1);
            if (lastOutcome instanceof NormalExecution) {
                primObjectValue = ((NormalExecution) lastOutcome).getRuntimeValue();
            }

            if (primObjectValue != null) {
                mainObjPool.addOrUpdate(primObjectValue, primSeq);
            }
        }

        for (Object primObj : primObjPool.keySet()) {
            mainObjPool.put(primObj, new SimpleArrayList<>(primObjPool.get(primObj)));
        }

        // Initialize the main object pool with generated sequences
        Set<Sequence> generatedSequences = componentManager.getAllGeneratedSequences();
        Map<Object, List<Sequence>> generatedObjPool = new LinkedHashMap<>();
        for (Sequence genSeq : generatedSequences) {
            ExecutableSequence eseq = new ExecutableSequence(genSeq);
            eseq.execute(new DummyVisitor(), new DummyCheckGenerator());

            Object generatedObjectValue = null;
            ExecutionOutcome lastOutcome = eseq.getResult(eseq.sequence.size() - 1);
            if (lastOutcome instanceof NormalExecution) {
                generatedObjectValue = ((NormalExecution) lastOutcome).getRuntimeValue();
            }

            if (generatedObjectValue != null) {
                mainObjPool.addOrUpdate(generatedObjectValue, genSeq);
            }
        }

        for (Object generatedObj : generatedObjPool.keySet()) {
            mainObjPool.put(generatedObj, new SimpleArrayList<>(generatedObjPool.get(generatedObj)));
        }

        return mainObjPool;
    }


    /**
     * Initializes the secondary object pool. This method simply creates a new instance of ObjectPool.
     * The secondary object pool is used for storing resultant objects of method sequences in the demand-driven input creation process.
     *
     * @return A new instance of ObjectPool representing the secondary object pool.
     *
     * @see ObjectPool
     */
    public static ObjectPool secondObjPoolInitialization() {
        return new ObjectPool();
    }


    /**
     * Performs a demand-driven approach for constructing input objects of a specified type that are not directly available.
     * <p>
     * This method identifies a set of dependent methods that return or construct objects of the required type.
     * It then generates method sequences for each of these dependent methods by obtaining necessary inputs from the provided
     * object pools. The resultant method sequence is then executed, and if successful, the resultant object is stored in the
     * secondary object pool.
     * <p>
     * Finally, it extracts the method sequences that produce objects of the required type from the secondary object pool,
     * which can then be utilized for future use.
     *
     * @param mainObjPool The main object pool used for obtaining necessary inputs for the dependent methods.
     * @param secondObjPool The secondary object pool used for storing the resultant objects of the method sequences.
     * @param t The class type for which the input objects need to be constructed.
     *
     * @return A SimpleList of method sequences that produce objects of the required type.
     */
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

        // Extract all method sequences that produce objects of the demanded type from the secondary object pool
        // and return them
        return extractCandidateMethodSequences(secondObjPool, t);
    }

    /**
     * Identifies a set of methods that construct objects of a specified type. This method is used in the process of demand-driven
     * input creation to find the necessary methods to create objects of the required type.
     * <p>
     * The method checks for all visible methods in the specified class type, including constructors and methods that return the
     * required type. It also recursively searches for inputs needed to execute a method that returns the sought-after type. The
     * recursive search terminates if the current type is a primitive type or if it has already been processed.
     *
     * @param t The class type for which the dependent methods need to be identified.
     * @param processedSet A set of types that have already had their methods extracted.
     *
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
                List<Executable> executableList = new ArrayList<>(List.of(currentTypeClass.getConstructors()));
                executableList.addAll(List.of(currentTypeClass.getMethods()));
                for (Executable executable : executableList) {
                    if (executable instanceof Constructor || (executable instanceof Method &&
                            ((Method) executable).getReturnType().equals(currentTypeClass))) {
                        // Obtain the input types and output type of the executable
                        Class<?>[] reflectionInputTypes = executable.getParameterTypes();
                        Class<?> reflectionOutputType = executable instanceof Constructor ?
                                ((Constructor<?>) executable).getDeclaringClass() : ((Method) executable).getReturnType();

                        // Create TypeTuple for input types
                        TypeTuple inputTypes = classArrayToTypeTuple(reflectionInputTypes);

                        // Create Type for output type
                        Type outputType = classToType(reflectionOutputType);

                        // Determine whether the method call is a constructor call or a method call
                        // and create the corresponding subclass of CallableOperation
                        CallableOperation callableOperation = executable instanceof Constructor ?
                                new ConstructorCall((Constructor<?>) executable) : new MethodCall((Method) executable);

                        NonParameterizedType declaringType = new NonParameterizedType(currentType.getRuntimeClass());
                        TypedOperation typedClassOperation = new TypedClassOperation(callableOperation, declaringType, inputTypes, outputType);

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
    private static TypeTuple classArrayToTypeTuple(Class<?>[] classes) {
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
        return new TypeTuple(inputTypeList);
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
     * Given a TypedOperation, this method finds a sequence of method calls that can generate an instance of each input type
     * required by the TypedOperation. It then merges these sequences into a single sequence that culminates in a call to the TypedOperation.
     *
     * @param mainObjPool The main object pool from which to draw input sequences.
     * @param secondObjPool The secondary object pool from which to draw input sequences if the main object pool does not contain a suitable sequence.
     * @param typedOperation The operation for which input sequences are to be generated.
     * @return A sequence that ends with a call to the provided TypedOperation and contains calls to generate each required input, or null if no such sequence can be found.
     */
    private static Sequence getInputAndGenSeq(ObjectPool mainObjPool,
                                              ObjectPool secondObjPool,
                                              TypedOperation typedOperation) {
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
        Sequence mergedSequence = Sequence.createSequence(typedOperation, inputSequences, inputIndices);
        return mergedSequence;
    }


    /**
     * Executes a given sequence, extracts the last outcome's runtime value if it is a NormalExecution,
     * and adds or updates the value-sequence pair in the provided object pool if the runtime value is not null.
     *
     * @param objectPool The ObjectPool in which the value-sequence pair is to be added or updated.
     * @param sequence The sequence to be executed and possibly added to the object pool.
     */
    public static void processSuccessfulSequence(ObjectPool objectPool, Sequence sequence) {
        ExecutableSequence eseq = new ExecutableSequence(sequence);
        eseq.execute(new DummyVisitor(), new DummyCheckGenerator());
        Object objectValue = null;
        ExecutionOutcome lastOutcome = eseq.getResult(eseq.sequence.size() - 1);
        if (lastOutcome instanceof NormalExecution) {
            objectValue = ((NormalExecution) lastOutcome).getRuntimeValue();
        }

        if (objectValue != null) {
            objectPool.addOrUpdate(objectValue, sequence);
        }
    }


    /**
     * Filters the sequences in the provided object pool based on the specified type and returns a list of lists of sequences,
     * each of which can generate an object of the specified type.
     *
     * @param objectPool The ObjectPool from which sequences are to be extracted.
     * @param t The type based on which sequences are to be filtered.
     * @return A ListOfLists containing sequences that can generate an object of the specified type.
     */
    public static ListOfLists<Sequence> extractCandidateMethodSequences(ObjectPool objectPool, Type t) {
        return objectPool.filterByType(t);
    }

}