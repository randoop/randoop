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
import randoop.test.DummyCheckGenerator;
import randoop.types.NonParameterizedType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.EquivalenceChecker;
import randoop.util.ListOfLists;
import randoop.util.Randomness;
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
public class DemandDrivenInputCreationExperimental {

    private static Set<@ClassGetName String> CONSIDERED_CLASSES =
            GenInputsAbstract.getClassnamesFromArgs(AccessibilityPredicate.IS_ANY);

    // TODO: Test performance (speed) with and without the secondary object pool.

    /**
     * Performs a demand-driven approach for constructing input objects of a specified type, when the
     * object pool (and secondary object pool) contains no objects of that type.
     *
     * <p>This method identifies a set of methods/constructors that return objects of the required
     * type. For each of these methods: it generates a method sequence for the method by recursively
     * searching for necessary inputs from the provided object pools; executes it; and if successful,
     * stores the resultant object in the secondary object pool.
     *
     * <p>Finally, it returns the newly-created sequences (that produce objects of the required type)
     * from the secondary object pool. (We might want to merge the two pools eventually; using
     * different pools reproduces GRT's Detective exactly.)
     *
     * <p>Invariant: This method is only called where the component manager lacks an object that is of
     * a type compatible with the one required by the forward generator. See {@link
     * randoop.generation.ForwardGenerator#selectInputs}.
     *
     * @param objPool the object pool from which to draw input sequences
     * @param t the type of objects to create
     * @return method sequences that produce objects of the required type
     */
    public static SimpleList<Sequence> createInputForType(ObjectPoolExperimental objPool, Type t) {
        // All constructors/methods that return the demanded type.
        Set<TypedOperation> producerMethods = getProducerMethods(t);

        // Add to the secondary pool.
        // For each producer method, create a sequence that produces an object of the demanded type
        // if possible, or produce a sequence that leads to the eventual creation of the demanded type.
        for (TypedOperation producerMethod : producerMethods) {
            Sequence newSequence = getInputAndGenSeq(objPool, producerMethod);
            if (newSequence != null) {
                // Execute the sequence and store the resultant object in the secondary object pool
                // if the sequence is successful.
                // executeAndAddToPool(secondaryObjPool, Collections.singleton(newSequence));
                executeAndAddToPool(objPool, Collections.singleton(newSequence));
            }
        }

        // Extract all method sequences that produce objects of the demanded type from the secondary
        // object pool and return them.
        return extractCandidateMethodSequences(objPool, t);
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
        // System.out.println(GenInputsAbstract.getClassnamesFromArgs(AccessibilityPredicate.IS_ANY));
        Set<Type> processed = new HashSet<>();
        Queue<Type> workList = new ArrayDeque<>();

        // The set of producer methods that construct objects of the specified type.
        Set<TypedOperation> producerMethods = new LinkedHashSet<>();

        // The set of types that are needed to pass to producer methods.
        Set<Type> producerParameterTypes = new HashSet<>();
        workList.add(t);

        // Recursively search for methods that construct objects of the specified type.
        while (!workList.isEmpty()) {
            Type currentType = workList.poll();

            if (!CONSIDERED_CLASSES.contains(currentType.getRuntimeClass().getName())) {
                // TODO: Warning mechanism for non-specified classes.
            }
            // Only consider the type if it is not a primitive type or if it hasn't already been
            // processed.
            if (!processed.contains(currentType) && !currentType.isNonreceiverType()) {
                Class<?> currentTypeClass = currentType.getRuntimeClass();
                List<Executable> executableList = new ArrayList<>();

                // Adding constructors.
                for (Constructor<?> constructor : currentTypeClass.getConstructors()) {
                    executableList.add(constructor);
                }
                // Adding methods.
                for (Method method : currentTypeClass.getMethods()) {
                    executableList.add(method);
                }

                for (Executable executable : executableList) {
                    if (executable instanceof Constructor
                            || (executable instanceof Method
                            && ((Method) executable).getReturnType().equals(currentTypeClass))) {
                        // Obtain the input types and output type of the executable.
                        List<Type> inputTypeList = classArrayToTypeList(executable.getParameterTypes());
                        // If the executable is a non-static method, add the receiver type to
                        // the front of the input type list.
                        if (executable instanceof Method && !Modifier.isStatic(executable.getModifiers())) {
                            inputTypeList.add(0, new NonParameterizedType(currentTypeClass));
                        }
                        TypeTuple inputTypes = new TypeTuple(inputTypeList);

                        CallableOperation callableOperation =
                                executable instanceof Constructor
                                        ? new ConstructorCall((Constructor<?>) executable)
                                        : new MethodCall((Method) executable);

                        NonParameterizedType declaringType = new NonParameterizedType(currentTypeClass);
                        TypedOperation typedClassOperation =
                                new TypedClassOperation(callableOperation, declaringType, inputTypes, currentType);

                        // Add the method call to the producerMethods.
                        producerMethods.add(typedClassOperation);
                        producerParameterTypes.addAll(inputTypeList);
                    }
                    processed.add(currentType);
                    // Recursively search for methods that construct objects of the specified type.
                    workList.addAll(producerParameterTypes);
                }
            }
        }
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
     * @param objPool the object pool from which to draw input sequences
     * @param typedOperation the operation for which input sequences are to be generated
     * @return a sequence that ends with a call to the provided TypedOperation, or null if no such
     *     sequence can be found
     */
    private static @Nullable Sequence getInputAndGenSeq(ObjectPoolExperimental objPool, TypedOperation typedOperation) {

        System.out.println("An instance of getInputAndGenSeq is called: " + typedOperation + " --------------------------");

        TypeTuple inputTypes = typedOperation.getInputTypes();

        System.out.println("The input types involved in this instance: " + inputTypes + " -------------------------------");

        List<Sequence> inputSequences = new ArrayList<>();
        List<Integer> inputIndices = new ArrayList<>();

        // Represents the position of a statement in a sequence.
        int index = 0;

        // Create a input type to index mapping.
        // This allows us to find the exact statements in the sequence that generate objects
        // of the required type.
        Map<Type, List<Integer>> typeToIndex = new HashMap<>();

        for (int i = 0; i < inputTypes.size(); i++) {
            /*
            // Obtain a sequence that generates an object of the required type from the main object pool.
            ObjectPool typeFilteredPool = mainObjPool.getSubPoolOfType(inputTypes.get(i));
            // If no such sequence exists, obtain a sequence from the secondary object pool.
            if (typeFilteredPool.isEmpty()) {
                typeFilteredPool = secondaryObjPool.getSubPoolOfType(inputTypes.get(i));
                if (typeFilteredPool.isEmpty()) {
                    // If no such sequence exists, return null.
                    return null;
                }
            }

             */
            // System.out.println("Extracting sequences of type: " + inputTypes.get(i));
            // System.out.println("Object pool: " + objPool.toString());
            SimpleList<Sequence> typeFilteredPool = objPool.getSequencesOfType(inputTypes.get(i));
            if (typeFilteredPool.isEmpty()) {
                System.out.println("No sequences found for type: " + inputTypes.get(i));
                return null;
            }

            // System.out.println("Typed operation: " + typedOperation);
            // System.out.println("Type filtered pool: " + typeFilteredPool);
            // System.out.println("TypeToIndex: " + typeToIndex);

            // Randomly select an object and get its sequence from the typeFilteredPool.
            Sequence seq = Randomness.randomMember(typeFilteredPool);
            // Sequence seq = entry.getValue();

            inputSequences.add(seq);

            // For each statement in the sequence, add the index of the statement to the typeToIndex map.
            for (int j = 0; j < seq.size(); j++) {
                Type type = seq.getVariable(j).getType();
                if (!typeToIndex.containsKey(type)) {
                // if (!containsType(typeToIndex, type)) {
                    typeToIndex.put(type, new ArrayList<>());
                }
                typeToIndex.get(type).add(index);
                index++;
            }
        }

        Set<Integer> inputIndicesSet = new LinkedHashSet<>();

        // For each input type of the operation, find the index of the statement in the sequence
        // that generates an object of the required type.
        System.out.println("-------- Enter typeIndexCount creation block --------");
        Map<Type, Integer> typeIndexCount = new HashMap<>();
        for (Type inputType : inputTypes) {
            System.out.println("------ typeIndexCount iteration ------");
            System.out.println("For input type: " + inputType);
            // if (typeToIndex.containsKey(inputType)) {
            System.out.println("Type to index map: " + typeToIndex);
            if (containsType(typeToIndex, inputType)) {
                // if (!typeIndexCount.containsKey(inputType)) {
                System.out.println("Type index count: " + typeIndexCount);
                if (!containsType(typeIndexCount, inputType)) {
                    typeIndexCount.put(inputType, 0);
                    System.out.println("Type index count added for type: " + inputType + " with count: " + typeIndexCount.get(inputType));
                }

                System.out.println("-------- Enter inputIndices creation block --------");
                // TODO: Fix the issue where typeToIndex keeps the given type but inputType has the
                //      runtime type (maybe the otherway around). -- done

                // TODO: Fix the issue where the runtime object implements multiple interfaces.
                // - LocalDate implements Temproal & TemporalAdjusters, but typeIndexCount treat
                //      them as different types while the runtime object is LocalDate.
                List<Integer> typeIndices = getTypeIndices(typeToIndex, inputType);
                // System.out.println("Sequence: " + inputSequences);
                System.out.println("Type to index map: " + typeToIndex);
                System.out.println("Type indices: " + typeIndices + " for type: " + inputType);
                System.out.println("Type index count: " + typeIndexCount);
                System.out.println("indicesAdded: " + typeIndices.get(getTypeIndexCount(typeIndexCount, inputType)));

                // Temporarily switch to list from set to fix input size - variable size difference.
                // inputIndicesSet.add(typeIndices.get(getTypeIndexCount(typeIndexCount, inputType)));
                // TODO: Extremely long tests output for some reason with current list temp fix. Needs to be fixed.
                inputIndices.add(typeIndices.get(getTypeIndexCount(typeIndexCount, inputType)));

                // typeIndexCount.put(inputType, typeIndexCount.get(inputType) + 1);
                typeIndexCount.put(inputType, getTypeIndexCount(typeIndexCount, inputType) + 1);
                System.out.println("Type index count updated: " + typeIndexCount);
                System.out.println("-------- End inputIndices creation block --------");
            }
            System.out.println("InputIndicesSet: " + inputIndicesSet);
            System.out.println("------ End typeIndexCount iteration ------");
        }

        // Add the indices to the inputIndices list.
        // Temporary fix to convert set to list.
        // inputIndices.addAll(inputIndicesSet);

        System.out.println("Input indices: " + inputIndices);

        return Sequence.createSequence(typedOperation, inputSequences, inputIndices);
    }

    private static boolean containsType(Map<Type, ?> map, Type type) {
        System.out.println("---- containsType instance for type: " + type + " ----");
        for (Type key : map.keySet()) {
            System.out.println("Key: " + key + " Type: " + type);
            if (EquivalenceChecker.assignCompatible(key.getRuntimeClass(), type.getRuntimeClass())) {
                System.out.println("Type found in map: " + key);
                System.out.println("---- End containsType instance ----");
                return true;
            }
        }
        if (map.keySet().isEmpty()) {
            System.out.println("Map is empty");
        } else {
            System.out.println("Type not found in map");
        }
        System.out.println("---- End containsType instance ----");
        return false;
    }

    private static List<Integer> getTypeIndices(Map<Type, List<Integer>> typeToIndex, Type type) {
        List<Integer> indices = new ArrayList<>();
        for (Type key : typeToIndex.keySet()) {
            if (EquivalenceChecker.assignCompatible(key.getRuntimeClass(), type.getRuntimeClass())) {
                indices.addAll(typeToIndex.get(key));
            }
        }
        return indices;
    }

    private static int getTypeIndexCount(Map<Type, Integer> typeIndexCount, Type type) {
        for (Type key : typeIndexCount.keySet()) {
            if (EquivalenceChecker.assignCompatible(key.getRuntimeClass(), type.getRuntimeClass())) {
                return typeIndexCount.get(key);
            }
        }
        return 0;
    }

    /**
     * Executes a set of sequences and updates the object pool with each successful execution. It
     * iterates through each sequence, executes it, and if the execution is normal and yields a
     * non-null value, the value along with its generating sequence is added or updated in the object
     * pool.
     *
     * @param objectPool the ObjectPool to be updated with successful execution outcomes
     * @param sequenceSet a set of sequences to be executed
     */
    private static void executeAndAddToPool(ObjectPoolExperimental objectPool, Set<Sequence> sequenceSet) {
        for (Sequence genSeq : sequenceSet) {
            ExecutableSequence eseq = new ExecutableSequence(genSeq);
            eseq.execute(new DummyVisitor(), new DummyCheckGenerator());

            Object generatedObjectValue = null;
            ExecutionOutcome outcome = eseq.getResult(eseq.sequence.size() - 1);
            if (outcome instanceof NormalExecution) {
                generatedObjectValue = ((NormalExecution) outcome).getRuntimeValue();
            }

            if (generatedObjectValue != null) {
                // Sequences generated and put into objectPool for an object earlier can be overwritten by
                // new sequences, but this is not a problem because:
                //      - If the object's class is visible to Randoop, then Randoop will be able to generate
                //        tests for the class without the help of Detective.
                //      - If the object's class is not visible to Randoop, we are not testing that class.
                //        Thus, having multiple sequences generating the same object that is not visible to
                //        Randoop does not contribute to the coverage of the tests generated by Randoop.
                // objectPool.put(generatedObjectValue, genSeq);
                objectPool.add(genSeq);
                System.out.println("Object added to object pool: " + generatedObjectValue);
            }
        }
    }

    /**
     * Extracts sequences from the object pool that can generate an object of the specified type.
     *
     * @param objectPool the ObjectPool from which sequences are to be extracted
     * @param t the type of object that the sequences should be able to generate
     * @return a ListOfLists containing sequences that can generate an object of the specified type
     */
    public static SimpleList<Sequence> extractCandidateMethodSequences(
            ObjectPoolExperimental objectPool, Type t) {
        return objectPool.getSequencesOfType(t);
    }
}
