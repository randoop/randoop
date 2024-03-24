package randoop.generation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import randoop.main.GenInputsAbstract;
import randoop.operation.CallableOperation;
import randoop.operation.ConstructorCall;
import randoop.operation.MethodCall;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Statement;
import randoop.sequence.Sequence;
import randoop.types.NonParameterizedType;
import randoop.types.PrimitiveType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.Randomness;
import randoop.util.ListOfLists;

/**
 * Implements the Impurity component, as outlined in "GRT: Program-Analysis-Guided Random Testing"
 * by Ma et. al (ASE 2015):
 * https://people.kth.se/~artho/papers/lei-ase2015.pdf.
 *
 * <p> The Impurity component is a fuzzing mechanism that alters the states of input objects for
 * methods under test to generate a wider variety of object states and hence potentially trigger
 * more branches and improve coverage for the program under test.
 * [TODO: It also generates more effective test with shorter length by reducing the number of
 * redundant sequences that does not side-effect the state of an object].
 *
 * <p> This component fuzzes inputs differently based on their type:
 * <ul>
 *     <li> Primitive Numbers: Fuzzed using a Gaussian distribution, taking the original value as
 *     the mean and a predefined constant as the standard deviation. This approach
 *     probabilistically generates new values around the mean.</li>
 *     <li> String: Fuzzed through uniformly selecting a random string operations, including
 *     insertion, removal, replacement of characters, or taking a substring of the given string.</li>
 *     <li> Other Objects (TODO): Perform purity analysis to determine methods that have side-effects
 *     to the state of the object, and mark them as impure. Then, Randoop will construct more effective
 *     test cases by calling these impure methods without redundant calls to pure methods.</li>
 * </ul>
 */
public class Impurity {
    /** The standard deviation of the Gaussian distribution used to generate fuzzed numbers. */
    private static final double GAUSSIAN_STD = GenInputsAbstract.impurity_stddev;

    /** Prevents instantiation. */
    private Impurity() {}

    /**
     * Fuzzes the given sequence using the Impurity component.
     * @param sequence the sequence to construct the inputs of test cases
     * @return a sequence with additional fuzzing statements appended at the end, and a
     *        count of the number of fuzzing statements added to the sequence
     */
    public static ImpurityAndNumStatements fuzz(Sequence sequence) {
        // A counter to keep track of the number of fuzzing statements added to the sequence
        FuzzStatementOffset fuzzStatementOffset = new FuzzStatementOffset();

        Type outputType = sequence.getLastVariable().getType();

        // Do not fuzz void, char, boolean, or byte.
        if (outputType.isVoid()
                || outputType.runtimeClassIs(char.class)
                || outputType.runtimeClassIs(boolean.class)
                || outputType.runtimeClassIs(byte.class)) {
            return new ImpurityAndNumStatements(sequence, 0);
        }

        Class<?> outputClass = outputType.getRuntimeClass();
        List<Method> methodList = new ArrayList<>();
        try {
            if (outputClass.isPrimitive()) {  // fuzzing primitive numbers
                sequence = getFuzzedSequenceForPrimNumber(sequence, outputClass);
                methodList = getNumberFuzzingMethod(outputClass);
            } else if (outputClass == String.class) {  // fuzzing String
                // There are 4 fuzzing strategies for String. Uniformly select one.
                int stringFuzzingStrategyIndex = Randomness.nextRandomInt(4);
                try {
                    sequence = getFuzzedSequenceForString(sequence, stringFuzzingStrategyIndex,
                            fuzzStatementOffset);
                } catch (IndexOutOfBoundsException e) {
                    // This happens when the input String is empty but a fuzzing operation requires
                    // a non-empty string.
                    // In this case, we will ignore this fuzzing operation.
                    return new ImpurityAndNumStatements(sequence, 0);
                }
                methodList = getStringFuzzingMethod(stringFuzzingStrategyIndex);
            } else if (outputClass == null) {
                throw new RuntimeException("Output class is null");
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Initialization failed due to missing method", e);
        }

        Sequence output = sequence;
        for (int i = 0; i < methodList.size() - 1; i++) {
            Method method = methodList.get(i);
            output = createSequence(output, method, fuzzStatementOffset);
        }

        output = createSequence(output, methodList.get(methodList.size() - 1), outputType,
                fuzzStatementOffset, outputType.runtimeClassIs(short.class));

        return new ImpurityAndNumStatements(output, fuzzStatementOffset.getOffset());
    }


    /**
     * Create a sequence for fuzzing an object of a given type using the given method.
     * This overload assumes that the output type of the method is the same as the given type.
     * @param sequence the sequence to append the fuzzing sequence to
     * @param executable the method to be invoked to fuzz the object
     * @param fuzzStatementOffset the offset counter for the number of fuzzing statements added
     * @return a sequence with the fuzzing statement appended at the end
     */
    private static Sequence createSequence(Sequence sequence, Executable executable,
                                           FuzzStatementOffset fuzzStatementOffset) {
        Type outputType = determineOutputType(executable);
        return createSequence(sequence, executable, outputType, fuzzStatementOffset, false);
    }

    /**
     * Create a sequence for fuzzing an object of a given type using the given method.
     * This overload allows output type and inclusion of explicit cast to be specified.
     * @param sequence the sequence to append the fuzzing sequence to
     * @param executable the method to be invoked to fuzz the object
     * @param outputType the type of the object to be fuzzed
     * @param fuzzStatementOffset the offset counter for the number of fuzzing statements added
     * @param explicitCast whether to perform an explicit cast for the right-hand side of the
     *                     fuzzing statement
     * @return a sequence with the fuzzing statement appended at the end
     * @throws IllegalArgumentException if the method is not a method of the given type
     */
    private static Sequence createSequence(Sequence sequence, Executable executable,
                                           Type outputType, FuzzStatementOffset fuzzStatementOffset,
                                           boolean explicitCast) {
        CallableOperation callableOperation = createCallableOperation(executable, explicitCast);
        NonParameterizedType declaringType = new NonParameterizedType(executable.getDeclaringClass());
        List<Type> inputTypeList = getInputTypeList(executable, declaringType);
        TypeTuple inputType = new TypeTuple(inputTypeList);
        TypedOperation typedOperation = new TypedClassOperation(callableOperation,
                declaringType, inputType, outputType);
        List<Integer> inputIndex = calculateInputIndex(sequence, inputTypeList);
        fuzzStatementOffset.increment(inputTypeList.size());
        List<Sequence> sequenceList = Collections.singletonList(sequence);
        return Sequence.createSequence(typedOperation, sequenceList, inputIndex);
    }

    /**
     * Create a callable operation for fuzzing an object of a given type using the given method.
     * @param executable the method to be invoked to fuzz the object
     * @param explicitCast whether to perform an explicit cast for the right-hand side of the
     *                     fuzzing statement
     * @return a callable operation for fuzzing an object of a given type using the given method
     */
    private static CallableOperation createCallableOperation(Executable executable,
                                                             boolean explicitCast) {
        if (executable instanceof Method) {
            return new MethodCall((Method) executable, explicitCast);
        } else {
            return new ConstructorCall((Constructor<?>) executable);
        }
    }

    /**
     * Determine the output type of the given method.
     * @param executable the method to determine the output type of
     * @return the output type of the given method
     */
    private static Type determineOutputType(Executable executable) {
        Class<?> outputClass;
        if (executable instanceof Method) {
            outputClass = ((Method) executable).getReturnType();
        } else {
            outputClass = ((Constructor<?>) executable).getDeclaringClass();
        }
        return outputClass.isPrimitive() ? PrimitiveType.forClass(outputClass) :
                new NonParameterizedType(outputClass);
    }

    /**
     * Get the list of input types for the given method.
     * @param executable the method to get the input types of
     * @param declaringType the type that declares the given method
     * @return the list of input types for the given method
     */
    private static List<Type> getInputTypeList(Executable executable,
                                               NonParameterizedType declaringType) {
        List<Type> inputTypeList = new ArrayList<>();
        if (!Modifier.isStatic(executable.getModifiers()) && executable instanceof Method) {
            inputTypeList.add(declaringType);
        }
        for (Class<?> clazz : executable.getParameterTypes()) {
            inputTypeList.add(clazz.isPrimitive() ? PrimitiveType.forClass(clazz) :
                    new NonParameterizedType(clazz));
        }
        return inputTypeList;
    }

    /**
     * Calculate the index of the input parameters in the given sequence.
     * @param sequence the sequence to calculate the input index of
     * @param inputTypeList the list of input types
     * @return the index of the input parameters in the given sequence
     */
    private static List<Integer> calculateInputIndex(Sequence sequence, List<Type> inputTypeList) {
        List<Integer> inputIndex = new ArrayList<>();
        for (int i = 0; i < inputTypeList.size(); i++) {
            inputIndex.add(sequence.size() - inputTypeList.size() + i);
        }
        return inputIndex;
    }

    /**
     * Get a sequence with the fuzzing statement appended at the end for fuzzing a primitive number.
     * @param sequence the sequence to append the fuzzing sequence to
     * @param outputClass the class of the primitive number to be fuzzed
     * @return a sequence with the fuzzing statement appended at the end
     */
    private static Sequence getFuzzedSequenceForPrimNumber(Sequence sequence, Class<?> outputClass) {
        Object fuzzedValue = getFuzzedValueForPrim(outputClass);
        Sequence fuzzingSequence = Sequence.createSequenceForPrimitive(fuzzedValue);
        List<Sequence> temp = new ArrayList<>(Collections.singletonList(sequence));
        temp.add(fuzzingSequence); // Add fuzzing sequence to the list
        return Sequence.concatenate(temp); // Assuming concatenate combines all sequences in the list
    }

    /**
     * Get a fuzzed value for a primitive number using a Gaussian distribution.
     * @param outputClass the class of the primitive number to be fuzzed
     * @return a fuzzed value for the primitive number
     */
    private static Object getFuzzedValueForPrim(Class<?> outputClass) {
        double randomGaussian = GAUSSIAN_STD * Randomness.nextRandomGaussian(1);
        if (outputClass == int.class) {
            return (int) Math.round(randomGaussian);
        } else if (outputClass == short.class || outputClass == byte.class) {
            return (short) Math.round(randomGaussian); // Unified handling for short and byte
        } else if (outputClass == long.class) {
            return Math.round(randomGaussian);
        } else if (outputClass == float.class) {
            return (float) randomGaussian;
        } else if (outputClass == double.class) {
            return randomGaussian;
        } else {
            throw new RuntimeException("Unexpected primitive type: " + outputClass.getName());
        }
    }

    /**
     * Get the method (in a list) that can be used to fuzz primitive numbers.
     * @param outputClass the class of the primitive number to be fuzzed
     * @return a list of methods that can be used to fuzz primitive numbers
     * @throws NoSuchMethodException if no suitable method is found for the given class
     */
    private static List<Method> getNumberFuzzingMethod(Class<?> outputClass)
            throws NoSuchMethodException {

        List<Method> methodList = new ArrayList<>();

        // Map each wrapper to its primitive type and a common method
        if (outputClass == int.class) {
            methodList.add(Integer.class.getMethod("sum", int.class, int.class));
        } else if (outputClass == double.class) {
            methodList.add(Double.class.getMethod("sum", double.class, double.class));
        } else if (outputClass == float.class) {
            methodList.add(Float.class.getMethod("sum", float.class, float.class));
        } else if (outputClass == long.class) {
            methodList.add(Long.class.getMethod("sum", long.class, long.class));
        } else if (outputClass == short.class) {
            methodList.add(Integer.class.getMethod("sum", int.class, int.class));
        } else if (outputClass == byte.class) {
            throw new NoSuchMethodException("Byte fuzzing is not supported yet");
        } else {
            throw new NoSuchMethodException("Object fuzzing is not supported");
        }

        if (methodList.isEmpty()) {
            // Should be unreachable
            throw new NoSuchMethodException("Unable to find suitable method for class: "
                    + outputClass.getName() + " in primitive number fuzzing");
        }

        return methodList;
    }

    /**
     * Get a sequence with the fuzzing statement appended at the end for fuzzing a String.
     * @param sequence the sequence to append the fuzzing sequence to
     * @param fuzzingOperationIndex the index of the fuzzing operation to perform
     * @param fuzzStatementOffset the offset counter for the number of fuzzing statements added
     * @return a sequence with the fuzzing statement appended at the end
     * @throws IllegalArgumentException if the fuzzing operation is invalid
     * @throws IndexOutOfBoundsException if the input String is empty
     */
    private static Sequence getFuzzedSequenceForString(Sequence sequence, int fuzzingOperationIndex,
                                                       FuzzStatementOffset fuzzStatementOffset)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        sequence = initializeWithStringBuilder(sequence, fuzzStatementOffset);

        Object stringValue = getStringValue(sequence);
        int stringLength = stringValue.toString().length();

        if (stringLength == 0) {
            throw new IndexOutOfBoundsException("String length is 0. Will ignore this fuzzing" +
                    " operation.");
        }

        List<Sequence> fuzzingSequenceList = performFuzzingOperation(fuzzingOperationIndex,
                stringLength);

        List<Sequence> temp = new ArrayList<>(Collections.singletonList(sequence));
        temp.addAll(fuzzingSequenceList); // Assuming these are sequences that need to be concatenated
        return Sequence.concatenate(temp); // Concatenate all sequences together
    }

    /**
     * Initialize a sequence with a StringBuilder object.
     * @param sequence the sequence to initialize with a StringBuilder object
     * @param fuzzStatementOffset the offset counter for the number of fuzzing statements added
     * @return a sequence with the fuzzing statement appended at the end
     * @throws IllegalArgumentException if the StringBuilder object cannot be initialized
     */
    private static Sequence initializeWithStringBuilder(Sequence sequence,
                                                        FuzzStatementOffset fuzzStatementOffset) {
        try {
            Constructor<?> stringBuilderConstructor = StringBuilder.class.getConstructor(String.class);
            return createSequence(sequence, stringBuilderConstructor, fuzzStatementOffset);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Initialization failed due to missing method", e);
        }
    }

    /**
     * Get the String value from the given sequence.
     * @param sequence the sequence to get the String value from
     * @return the String value from the given sequence
     * @throws IllegalArgumentException if the String value cannot be obtained
     */
    private static Object getStringValue(Sequence sequence) {
        try {
            // Original String value is the 2nd last statement in the sequence
            return sequence.getStatement(sequence.size() - 2).getValue();
        } catch (IllegalArgumentException e) {
            // Randoop could not obtain the String value from its sequence collection.
            // This does not indicate error, ignore this fuzzing operation.
            throw new IllegalArgumentException("Error obtaining String value. Will ignore this" +
                    " fuzzing operation.", e);
        }
    }

    /**
     * Perform a fuzzing operation on a String.
     * @param operationIndex the index of the fuzzing operation to perform
     * @param stringLength the length of the string to be fuzzed
     * @return a list of sequences that represent the fuzzing operation
     */
    private static List<Sequence> performFuzzingOperation(int operationIndex, int stringLength) {
        switch (operationIndex) {
            case 0: return Collections.singletonList(fuzzInsertCharacter(stringLength));
            case 1: return Collections.singletonList(fuzzRemoveCharacter(stringLength));
            case 2: return fuzzReplaceCharacter(stringLength);
            case 3: return fuzzSelectSubstring(stringLength);
            default: throw new IllegalArgumentException("Invalid fuzzing operation index: "
                    + operationIndex);
        }
    }

    /**
     * Fuzzing operation: Insert a random character at a random index in the string.
     * @param stringLength the length of the string to be fuzzed
     * @return a sequence that represents the fuzzing operation
     */
    private static Sequence fuzzInsertCharacter(int stringLength) {
        int randomIndex = Randomness.nextRandomInt(stringLength + 1); // Include stringLength as
        // possible index for insertion at the end.
        char randomChar = (char) (Randomness.nextRandomInt(95) + 32);  // ASCII 32-126
        Sequence randomIndexSequence = Sequence.createSequenceForPrimitive(randomIndex);
        Sequence randomCharSequence = Sequence.createSequenceForPrimitive(randomChar);
        return Sequence.concatenate(Arrays.asList(randomIndexSequence, randomCharSequence));
    }

    /**
     * Fuzzing operation: Remove a character at a random index in the string.
     * @param stringLength the length of the string to be fuzzed
     * @return a sequence that represents the fuzzing operation
     */
    private static Sequence fuzzRemoveCharacter(int stringLength) {
        int randomIndex = Randomness.nextRandomInt(stringLength);
        Sequence randomIndexSequence = Sequence.createSequenceForPrimitive(randomIndex);
        return randomIndexSequence;
    }

    /**
     * Fuzzing operation: Replace a substring in the string with a random character.
     * @param stringLength the length of the string to be fuzzed
     * @return a list of sequences that represent the fuzzing operation
     */
    private static List<Sequence> fuzzReplaceCharacter(int stringLength) {
        int randomIndex1 = Randomness.nextRandomInt(stringLength);
        int randomIndex2 = Randomness.nextRandomInt(stringLength);
        int startIndex = Math.min(randomIndex1, randomIndex2);
        int endIndex = Math.max(randomIndex1, randomIndex2);
        String randomChar = String.valueOf((char) (Randomness.nextRandomInt(95) + 32));  // ASCII 32-126
        Sequence startIndexSequence = Sequence.createSequenceForPrimitive(startIndex);
        Sequence endIndexSequence = Sequence.createSequenceForPrimitive(endIndex);
        Sequence randomCharSequence = Sequence.createSequenceForPrimitive(randomChar);
        return Arrays.asList(startIndexSequence, endIndexSequence, randomCharSequence);
    }

    /**
     * Fuzzing operation: Select a substring from the string.
     * @param stringLength the length of the string to be fuzzed
     * @return a list of sequences that represent the fuzzing operation
     */
    private static List<Sequence> fuzzSelectSubstring(int stringLength) {
        int randomIndex1 = Randomness.nextRandomInt(stringLength);
        int randomIndex2 = Randomness.nextRandomInt(stringLength);
        int startIndex = Math.min(randomIndex1, randomIndex2);
        int endIndex = Math.max(randomIndex1, randomIndex2);
        Sequence startIndexSequence = Sequence.createSequenceForPrimitive(startIndex);
        Sequence endIndexSequence = Sequence.createSequenceForPrimitive(endIndex);
        return Arrays.asList(startIndexSequence, endIndexSequence);
    }

    /**
     * Get the method (in a list) that can be used to fuzz strings.
     * @param stringFuzzingStrategyIndex the index of the fuzzing strategy to use
     * @return a list of methods that can be used to fuzz strings
     * @throws NoSuchMethodException if no suitable method is found for class String
     */
    private static List<Method> getStringFuzzingMethod(int stringFuzzingStrategyIndex)
            throws NoSuchMethodException {
        List<Method> methodList = new ArrayList<>();

        if (stringFuzzingStrategyIndex == 0) {
            methodList.add(StringBuilder.class.getMethod("insert", int.class, char.class));
            methodList.add(StringBuilder.class.getMethod("toString"));
        } else if (stringFuzzingStrategyIndex == 1) {
            methodList.add(StringBuilder.class.getMethod("deleteCharAt", int.class));
            methodList.add(StringBuilder.class.getMethod("toString"));
        } else if (stringFuzzingStrategyIndex == 2) {
            methodList.add(StringBuilder.class.getMethod("replace", int.class, int.class, String.class));
            methodList.add(StringBuilder.class.getMethod("toString"));
        } else if (stringFuzzingStrategyIndex == 3) {
            methodList.add(StringBuilder.class.getMethod("substring", int.class, int.class));
        } else {
            throw new NoSuchMethodException("Object fuzzing is not supported yet");
        }

        if (methodList.isEmpty()) {
            // Should be unreachable
            throw new NoSuchMethodException("No suitable method found for class String");
        }

        return methodList;
    }


    /**
     * A helper class to store the result of the Impurity component.
     */
    private static class FuzzStatementOffset {
        /** The number of fuzzing statements added to the sequence. */
        private int offset;

        /** Prevents instantiation. */
        private FuzzStatementOffset() {
            this.offset = 0;
        }

        /** Get the number of fuzzing statements added to the sequence. */
        private int getOffset() {
            return this.offset;
        }

        /** Increment the number of fuzzing statements added to the sequence. */
        private void increment(int numStatements) {
            this.offset += numStatements;
        }
    }
}