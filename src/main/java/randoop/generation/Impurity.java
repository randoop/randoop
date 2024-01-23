package randoop.generation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import randoop.operation.CallableOperation;
import randoop.operation.ConstructorCall;
import randoop.sequence.Statement;
import randoop.sequence.Sequence;
import randoop.types.NonParameterizedType;
import randoop.operation.MethodCall;
import randoop.types.PrimitiveType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.util.Randomness;
import randoop.util.ListOfLists;

/**
 * Implements the Impurity component, as outlined in "GRT: Program-Analysis-Guided Random Testing" by Ma et. al (ASE 2015):
 * https://people.kth.se/~artho/papers/lei-ase2015.pdf.
 *
 * <p> The Impurity component is a fuzzing mechanism that alters the states of input objects to generate a wider variety
 * of object states and hence potentially trigger more branches in the program under test.
 *
 * <p> This component fuzzes inputs differently based on their type:
 * <ul>
 *     <li> Primitive Numbers: Fuzzed using a Gaussian distribution, taking the original value as the mean (μ) and
 *     a predefined constant as the standard deviation (σ). This approach probabilistically generates new values,
 *     with a higher likelihood of values closer to the mean, while still allowing for the generation of values
 *     further from μ.</li>
 *     <li> String: Fuzzed through random string operations, including insertion, removal, replacement of characters,
 *     or taking a substring of the given string.</li>
 *     <li> Other Objects: Fuzzed by employing methods that produce side effects (impure methods) to alter the
 *     state of the object. Static purity analysis is used to identify impure methods suitable for fuzzing the state
 *     of an object of a given type. These methods are then invoked on the object to achieve the desired fuzzing effect.</li>
 * </ul>
 * This approach aims to increase the likelihood of satisfying more branch conditions in the program under test by
 * diversifying the state space of the objects involved.
 */
public class Impurity {

    // The standard deviation of the Gaussian distribution used to generate fuzzed numbers.
    private static final double GAUSSIAN_STD = 30;

    private Impurity() {}


    /**
     * Fuzzes the given sequence using the Impurity component.
     * @param sequence the sequence representing the input to be fuzzed
     * @return a sequence with additional fuzzing statements added to the end, and the
     *         number of fuzzing statements added
     */
    public static ImpurityAndNumStatements fuzz(Sequence sequence) {
        // Count the number of fuzzing statements added to the sequence for Randoop
        // to correctly function.
        FuzzStatementOffset fuzzStatementOffset = new FuzzStatementOffset();

        Type outputType = sequence.getLastVariable().getType();
        boolean isShort = false;  // Short fuzzing needs special handling.
        if (outputType.runtimeClassIs(short.class)) {
            // Convert short to int so we can use the sum method to fuzz the number.
            outputType = PrimitiveType.forClass(int.class);
            isShort = true;
        }

        // Do not fuzz void, char, boolean, or byte.
        if (outputType.isVoid()
            || outputType.runtimeClassIs(char.class)
            || outputType.runtimeClassIs(boolean.class)
            || outputType.runtimeClassIs(byte.class)) {
            return new ImpurityAndNumStatements(sequence, 0);
        }

        // There are 4 fuzzing strategies for String. This index keeps track of which one is used.
        int stringFuzzingStrategyIndex = 0;

        Class<?> outputClass = outputType.getRuntimeClass();
        if (outputClass.isPrimitive()) {  // fuzzing primitive numbers
            sequence = getFuzzedSequenceForPrimNumber(sequence, outputClass);
        } else if (outputClass == String.class) {  // fuzzing String
            stringFuzzingStrategyIndex = Randomness.nextRandomInt(4);
            try {
                sequence = getFuzzedSequenceForString(sequence, stringFuzzingStrategyIndex, fuzzStatementOffset);
            } catch (IndexOutOfBoundsException e) {
                // This happens when the input String is empty. This is normal and does not indicate error.
                // We will ignore this fuzzing operation (without reporting error for now).
                return new ImpurityAndNumStatements(sequence, 0);
            }
        } else if (outputClass == null) {
            throw new RuntimeException("Output class is null");
        }
        List<Method> methodList;
        try {
            if (outputClass.isPrimitive()) {
                methodList = getNumberFuzzingMethod(outputClass);
            } else {
                methodList = getStringFuzzingMethod(stringFuzzingStrategyIndex);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Initialization failed due to missing method", e);
        }

        Sequence output = sequence;
        for (Method method : methodList) {
            output = createSequence(output, method, fuzzStatementOffset);
        }

        /*
            * Special handling for short fuzzing as Short does not have a sum method.
            * We need to convert the int value back to short.
            * e.g. `int int3 = java.lang.Integer.sum(int1, int2);`
            *      then `short short5 = int4.shortValue();`
            * This is a temporary work around to bypass the issue when fuzzing short.
         */
        if (isShort) {
            // First, wrap the int value to a Integer object so we can use the shortValue method
            // to get the short value.
            Method wrapPrimitiveInt;
            try {
                wrapPrimitiveInt = Integer.class.getMethod("valueOf", int.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Initialization failed due to missing method", e);
            }
            output = createSequence(output, wrapPrimitiveInt, fuzzStatementOffset);

            Method shortValue;
            try {
                shortValue = Integer.class.getMethod("shortValue");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Initialization failed due to missing method", e);
            }
            output = createSequence(output, shortValue, fuzzStatementOffset);
        }

        return new ImpurityAndNumStatements(output, fuzzStatementOffset.getOffset());
    }

    // Get a new sequence given a sequence and an executable to be invoked on it.
    private static Sequence createSequence(Sequence sequence, Executable executable,
                                           FuzzStatementOffset fuzzStatementOffset) {
        CallableOperation callableOperation;
        Class<?> outputClass;
        if (executable instanceof Method) {
            callableOperation = new MethodCall((Method) executable);
            outputClass = ((Method) executable).getReturnType();
        } else {
            callableOperation = new ConstructorCall((Constructor<?>) executable);
            outputClass = ((Constructor<?>) executable).getDeclaringClass();
        }

        NonParameterizedType declaringType = new NonParameterizedType(executable.getDeclaringClass());

        List<Type> inputTypeList = new ArrayList<>();
        if (!Modifier.isStatic(executable.getModifiers()) && executable instanceof Method) {
            inputTypeList.add(declaringType);
        }

        for (Class<?> clazz : executable.getParameterTypes()) {
            inputTypeList.add(clazz.isPrimitive() ? PrimitiveType.forClass(clazz) : new NonParameterizedType(clazz));
        }
        TypeTuple inputType = new TypeTuple(inputTypeList);

        Type outputType;
        if (outputClass.isPrimitive()) {
            outputType = PrimitiveType.forClass(outputClass);
        } else {
            outputType = new NonParameterizedType(outputClass);
        }
        TypedOperation typedOperation = new TypedClassOperation(callableOperation,
                declaringType, inputType, outputType);
        List<Integer> inputIndex = new ArrayList<>();
        for (int i = 0; i < inputTypeList.size(); i++) {
            inputIndex.add(sequence.size() - inputTypeList.size() + i);
        }
        fuzzStatementOffset.increment(inputTypeList.size());

        List<Sequence> sequenceList = Collections.singletonList(sequence);

        return Sequence.createSequence(typedOperation, sequenceList, inputIndex);
    }


    // Get a fuzzed sequence given a sequence and the output class.
    private static Sequence getFuzzedSequenceForPrimNumber(Sequence sequence, Class<?> outputClass) {
        List<Sequence> sequenceList = Collections.singletonList(sequence);

        double randomGaussian = GAUSSIAN_STD * Randomness.nextRandomGaussian(1);
        Object fuzzedValue;
        if (outputClass == int.class) {
            fuzzedValue = (int) Math.round(randomGaussian);
        } else if (outputClass == short.class) {
            // This is a temporary work around to bypass the issue when fuzzing short.
            // Short does not have a sum method, and this introduce challenges to the implementation
            // of short fuzzing as using sum method is the only obvious way to fuzz numbers.
            fuzzedValue = (int) Math.round(randomGaussian);
        } else if (outputClass == long.class) {
            fuzzedValue = Math.round(randomGaussian);
        } else if (outputClass == byte.class) {
            fuzzedValue = (byte) Math.round(randomGaussian);
        } else if (outputClass == float.class) {
            fuzzedValue = (float) randomGaussian;
        } else if (outputClass == double.class) {
            fuzzedValue = randomGaussian;
        } else {
            throw new RuntimeException("Unexpected primitive type: " + outputClass.getName());
        }

        Sequence fuzzingSequence = Sequence.createSequenceForPrimitive(fuzzedValue);
        List<Sequence> fuzzingSequenceList = Collections.singletonList(fuzzingSequence);
        List<Sequence> temp = new ArrayList<>(sequenceList);
        temp.addAll(fuzzingSequenceList);
        sequence = Sequence.concatenate(temp);
        return sequence;
    }

    // Get a fuzzed sequence given a sequence and the fuzzing operation index.
    private static Sequence getFuzzedSequenceForString(Sequence sequence, int fuzzingOperationIndex,
                                                       FuzzStatementOffset fuzzStatementOffset)
            throws IllegalArgumentException, IndexOutOfBoundsException {
        // Create a Stringbuilder object
        Constructor<?> stringBuilderConstructor;
        try {
            stringBuilderConstructor = StringBuilder.class.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Initialization failed due to missing method", e);
        }
        sequence = createSequence(sequence, stringBuilderConstructor, fuzzStatementOffset);

        List<Sequence> sequenceList = Collections.singletonList(sequence);
        List<Sequence> fuzzingSequenceList = new ArrayList<>();

        Object stringValue;
        try {
            stringValue = sequence.getStatement(sequence.size() - 2).getValue();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("This is normal and does not indicate error. " +
                    "It happens when the input String " +
                    "is not obtained from the collection of known String. ");
        }
        int stringLength = stringValue.toString().length();
        if (fuzzingOperationIndex == 0) {
            // Inserting a character
            int randomIndex = (stringLength == 0 ? 0 : Randomness.nextRandomInt(stringLength));
            Sequence randomIndexSequence = Sequence.createSequenceForPrimitive(randomIndex);
            char randomChar = (char) (Randomness.nextRandomInt(95) + 32);  // ASCII 32-126
            Sequence randomCharSequence = Sequence.createSequenceForPrimitive(randomChar);

            fuzzingSequenceList.add(randomIndexSequence);
            fuzzingSequenceList.add(randomCharSequence);
        } else if (fuzzingOperationIndex == 1) {
            // Removing a character
            if (stringLength == 0) {
                throw new IndexOutOfBoundsException("String length is 0. Will ignore this fuzzing operation.");
            }
            int randomIndex = (stringLength == 0 ? 0 : Randomness.nextRandomInt(stringLength));
            Sequence randomIndexSequence = Sequence.createSequenceForPrimitive(randomIndex);

            fuzzingSequenceList.add(randomIndexSequence);
        } else if (fuzzingOperationIndex == 2) {
            // Replacing a character
            if (stringLength == 0) {
                throw new IndexOutOfBoundsException("String length is 0. Will ignore this fuzzing operation.");
            }
            int randomIndex1 = (stringLength == 0 ? 0 : Randomness.nextRandomInt(stringLength));
            int randomIndex2 = (stringLength == 0 ? 0 : Randomness.nextRandomInt(stringLength));
            int startIndex = Math.min(randomIndex1, randomIndex2);
            int endIndex = Math.max(randomIndex1, randomIndex2);
            Sequence startIndexSequence = Sequence.createSequenceForPrimitive(startIndex);
            Sequence endIndexSequence = Sequence.createSequenceForPrimitive(endIndex);
            String randomChar = String.valueOf((char) (Randomness.nextRandomInt(95) + 32));  // ASCII 32-126
            Sequence randomCharSequence = Sequence.createSequenceForPrimitive(randomChar);

            fuzzingSequenceList.add(startIndexSequence);
            fuzzingSequenceList.add(endIndexSequence);
            fuzzingSequenceList.add(randomCharSequence);
        } else if (fuzzingOperationIndex == 3) {
            // Selecting a substring
            if (stringLength == 0) {
                throw new IndexOutOfBoundsException("String length is 0. Will ignore this fuzzing operation.");
            }
            int randomIndex1 = (stringLength == 0 ? 0 : Randomness.nextRandomInt(stringLength));
            int randomIndex2 = (stringLength == 0 ? 0 : Randomness.nextRandomInt(stringLength));
            int startIndex = Math.min(randomIndex1, randomIndex2);
            int endIndex = Math.max(randomIndex1, randomIndex2);
            Sequence startIndexSequence = Sequence.createSequenceForPrimitive(startIndex);
            Sequence endIndexSequence = Sequence.createSequenceForPrimitive(endIndex);

            fuzzingSequenceList.add(startIndexSequence);
            fuzzingSequenceList.add(endIndexSequence);
        } else {
            // This should never happen
            throw new IllegalArgumentException("Invalid fuzzing operation index: " + fuzzingOperationIndex);
        }

        List<Sequence> temp = new ArrayList<>(sequenceList);
        temp.addAll(fuzzingSequenceList);
        sequence = Sequence.concatenate(temp);
        return sequence;
    }

    // Get the method (in a list) that can be used to fuzz numbers.
    private static List<Method> getNumberFuzzingMethod(Class<?> outputClass) throws NoSuchMethodException {

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
            throw new NoSuchMethodException("Object fuzzing is not supported yet");
        }

        if (methodList.isEmpty()) {
            throw new NoSuchMethodException("No suitable method found for class " + outputClass.getName());
        }

        return methodList;
    }

    // Get a list of methods that can be used to fuzz String based on the given fuzzing strategy index.
    private static List<Method> getStringFuzzingMethod(int stringFuzzingStrategyIndex) throws NoSuchMethodException {
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
            throw new NoSuchMethodException("No suitable method found for class String");
        }

        return methodList;
    }


    // A helper class to keep track of the number of fuzzing statements added to the sequence.
    // Used to correctly count variable indices in the tests generated by Randoop.
    private static class FuzzStatementOffset {
        private int offset;

        private FuzzStatementOffset() {
            this.offset = 0;
        }

        private int getOffset() {
            return this.offset;
        }

        private void increment(int numStatements) {
            this.offset += numStatements;
        }
    }
}
