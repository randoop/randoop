package randoop.generation;

import java.lang.Number;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Function;
import randoop.operation.CallableOperation;
// import randoop.operation.UncheckedCast;
import randoop.sequence.Sequence;
import randoop.types.NonParameterizedType;
import randoop.operation.MethodCall;
import randoop.types.PrimitiveType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
// import randoop.operation.TypedTermOperation;
import randoop.util.Randomness;
import randoop.util.ListOfLists;

public class Impurity {
    /*
     * TODO:
     * - Improve code readability, there are way too many methods to handle each type. (TODO)
     * - The way randoop works seem to be that when a primitive is initialized, it's value is
     *   used for the final assertion in the test. This means that if we fuzz a primitive, we
     *   will have a different value for the final assertion. Re-read the paper to see if this
     *   is a problem. (SOLVED)
     *   - This isn't an issue because our focus is to let the methods use the fuzzed value to
     *     trigger coverage increase. The final assertion is just a sanity check.
     * - Refactor and implement the fuzzing functionality using Java default methods (maybe). (SOLVED)
     * - Consider how to include the java Random object in the test suite. (SOLVED)
     *   - This seems to be the way I should aim for. I need to figure out how to include the fuzzing
     *     logic in the test suite.
     *   - The import statements of a unit test suite is written in randoop.output.JUnitCreator.java
     *   - Nevermind, I can handle the randomness here so the test suite use deterministic values.
     * - Primitives for fuzzing might be extracted from runtime rather than from the sequence. (TODO)
     * - Deal with statements that outputs void: now we aren't getting any lines that fuzz objects. (SOLVED)
     *   Sequences outputted by fuzz and selectInputs show that the object fuzzing line are
     *   actually there, but we aren't seeing it in tests. (SOLVED)
     *   - The issue isn't just about void. The answer is found in AbstractGenerator.java and GenTests.java handle()
     *     method. Line 479 generated a isOutputTest predicate. It uses several predicates to determine if a
     *     sequence is an output test:
     *     - ExcludeTestPredicate (Success)
     *     - ValueSizePredicate (Success)
     *     - RegressionTestPredicate (Success)
     *     - CompilableTestPredicate (Fail)
     *   - Why did CompilableTestPredicate fail? What we know for now is that person fuzzing and non-assigning
     *     statements are not compilable for some reason.
     *   - Getting more information on why the code isn't compilable doesn't seem trivial. It may be that
     *     Impurity doesn't defaultly understand an object outside of its scope, like `person` in this case.
     *   - Thus, we should try to use Java default methods as a place holder for fuzzer.
     *   - For now, we are not going to fuzz non-primitive types.
     * - Remove all code related to Detective when pushing. (TODO)
     * - We aren't fuzzing the inputs used for method calls. (SOLVED)
     *   - We might need to include extra lines to fuzz the inputs.
     *   - Or we need to fuzz every value upon creation.
     *   - However, the final check is using the fuzzed value. So we only worry about the inputs.
     *   - The component manager doesn't have the fuzzed objects. We might need to include them explicitly.
     *   - I think I just found a trick: for primitives in Sequence, set
     *     private transient boolean shouldInlineLiterals from `true` to `false`. (It works, kinda)
     *   - We still need to figure out how to always let the fuzzed inputs be used.
     *     - Actually, most fuzzed input are selected if properly added 1 to variable index. However,
     *       one relatively rare and hard to solve issue comes up: candidate output type != input type.
     *       - When selecting candidates, we obviously want the last statement to output a
     *         variable of the input type that we need. However, randoop doesn't always do that:
     *         - Consider the following code:
     *          java.lang.String str0 = "hi!";
                java.lang.String str1 = randoop.generation.Impurity.fuzzString(str0);
                char char2 = '#';
                char char3 = randoop.generation.Impurity.fuzzCharacter(char2);
                Person person4 = new Person(str1, (double)char3);
                java.lang.String str5 = "";
                java.lang.String str6 = randoop.generation.Impurity.fuzzString(str5);
                person4.setName(str6);
                double double8 = 100.0d;
                double double9 = randoop.generation.Impurity.fuzzDouble(double8);
                person4.setMoney(double9);
                double double11 = person4.getMoney();
    *          - The last statement isn't the Person that we need, although it involves person. It is
    *            still a possible candidate for some reason.
    *          - Check ComponentManager.getSequencesForType() for more information.

    * - I need to set this to false when Impurity is on. (SOLVED)
    * - Think about how to use TypedOperation.createPrimitiveInitialization(Type type, Object value) to
    *   improve readability. (TODO)

     */

    // Private constructor to prevent instantiation
    private Impurity() {}

    public static ImpurityAndSuccessFlag fuzz(Sequence chosenSeq) {
        int numStatements = 0;

        Type outputType = chosenSeq.getLastVariable().getType();
        boolean shortType = false;
        if (outputType.runtimeClassIs(short.class)) {
            outputType = PrimitiveType.forClass(int.class);
            shortType = true;
        }
        // If output type is void, then we don't need to fuzz anything.
        // System.out.println("Output type is: " + outputType);

        // TODO: String fuzzing is not supported yet
        if (outputType.isVoid()
            || outputType.runtimeClassIs(char.class)
            || outputType.runtimeClassIs(boolean.class)
            || outputType.runtimeClassIs(String.class)){
            return new ImpurityAndSuccessFlag(false, chosenSeq, 0);
        }

        /*
        CompilableTestPredicate => false for

        sequence =
        java.lang.String str0 = ""; // [NormalExecution  [class java.lang.String]]
        short short1 = (short)10; // [NormalExecution 10 [class java.lang.Short]]
        short short2 = (short)0; // [NormalExecution 0 [class java.lang.Short]]
        short short3 = java.lang.Integer.sum((short)10, (short)0); // [NormalExecution 10 [class java.lang.Integer]]
        short short4 = ((int)short3).shortValue(); // [NormalExecution 10 [class java.lang.Short]]
        Person person5 = new Person("", (double)short4); // [NormalExecution Person@450458d7 [class Person]]
        <check: randoop.PrimValue, value=10 [short3]>
        <check: randoop.PrimValue, value=10 [short4]>
         */

        // TODO: Use valueOf() and intValue() to fuzz short

        else if (outputType.runtimeClassIs(short.class)) {
            // Wrap the short value in a Short object to set up for intValue() call
            CallableOperation shortWrapper;
            System.out.println("Haha man, what can I say? Mamba out.");
            try {
                shortWrapper = new MethodCall(Short.class.getMethod("valueOf", short.class));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Initialization failed due to missing method", e);
            }
            NonParameterizedType shortWrapperDeclaration = new NonParameterizedType(Short.class);
            List<Type> shortWrapperInputTypeList = new ArrayList<>();
            shortWrapperInputTypeList.add(outputType);
            TypeTuple shortWrapperInputType = new TypeTuple(shortWrapperInputTypeList);
            Type shortWrapperOutputType = PrimitiveType.forClass(short.class).toBoxedPrimitive();

            TypedOperation intTypedOperation = new TypedClassOperation(shortWrapper,
                    shortWrapperDeclaration, shortWrapperInputType, shortWrapperOutputType);

            List<Integer> shortWrapperInputIndex = new ArrayList<>();
            shortWrapperInputIndex.add(chosenSeq.size() - 1);
            numStatements += 1;

            List<Sequence> chosenSeqShortWrapperList = Collections.singletonList(chosenSeq);
            chosenSeq = Sequence.createSequence(intTypedOperation, chosenSeqShortWrapperList, shortWrapperInputIndex);

            // Call intValue() to get the int value of the short
            CallableOperation intValue;
            try {
                intValue = new MethodCall(Short.class.getMethod("intValue"));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Initialization failed due to missing method", e);
            }
            NonParameterizedType shortToIntDeclaration = new NonParameterizedType(Integer.class);
            List<Type> shortToIntInputTypeList = new ArrayList<>();
            shortToIntInputTypeList.add(shortWrapperOutputType);
            TypeTuple shortToIntInputType = new TypeTuple(shortToIntInputTypeList);
            Type shortToIntOutputType = PrimitiveType.forClass(int.class);

            TypedOperation shortToIntTypedOperation = new TypedClassOperation(intValue,
                    shortToIntDeclaration, shortToIntInputType, shortToIntOutputType);

            List<Integer> shortToIntInputIndex = new ArrayList<>();
            shortToIntInputIndex.add(chosenSeq.size() - 1);
            numStatements += 1;

            List<Sequence> chosenSeqShortToIntList = Collections.singletonList(chosenSeq);
            chosenSeq = Sequence.createSequence(shortToIntTypedOperation, chosenSeqShortToIntList, shortToIntInputIndex);


            // Initialize an int value to be used for sum() that creates an int value for
            // trivial fuzzing
            /*
            Sequence intSequence = Sequence.createSequenceForPrimitive(0);
            List<Sequence> intInitSequenceList = Collections.singletonList(intSequence);
            List<Sequence> chosenSeqList = Collections.singletonList(chosenSeq);
            List<Sequence> temp = new ArrayList<>(chosenSeqList);
            temp.addAll(intInitSequenceList);
            chosenSeq = Sequence.concatenate(temp);

            // Call sum() to get the sum of the two int values
            CallableOperation intShortValue;
            System.out.println("Haha man, what can I say? Mamba out.");
            try {
                intShortValue = new MethodCall(Integer.class.getMethod("sum", int.class, int.class));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Initialization failed due to missing method", e);
            }

            NonParameterizedType intDeclaration = new NonParameterizedType(Integer.class);
            List<Type> intInputTypeList = new ArrayList<>();
            intInputTypeList.add(outputType);
            intInputTypeList.add(outputType);
            TypeTuple intInputType = new TypeTuple(intInputTypeList);
            Type intOutputType = PrimitiveType.forClass(int.class);

            TypedOperation intTypedOperation = new TypedClassOperation(intShortValue,
                    intDeclaration, intInputType, intOutputType);

            List<Integer> inputIndex = new ArrayList<>();

            inputIndex.add(chosenSeq.size() - 2);
            inputIndex.add(chosenSeq.size() - 1);
            numStatements += 2;

            List<Sequence> chosenSeqIntList = Collections.singletonList(chosenSeq);
            chosenSeq = Sequence.createSequence(intTypedOperation, chosenSeqIntList, inputIndex);

             */

        }


        Class<?> outputClass = outputType.getRuntimeClass();
        Sequence fuzzedChosenSeq = getFuzzedSequence(chosenSeq, outputClass);

        CallableOperation fuzzCallableOperation;
        try {
            fuzzCallableOperation = getCallableOperation(outputClass);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Initialization failed due to missing method", e);
        }

        Class<?> declaringClass = getDeclaringClass(outputClass);
        NonParameterizedType declaringType = new NonParameterizedType(declaringClass);

        List<Type> inputTypeList = new ArrayList<>();
        if (outputClass == short.class) {
            inputTypeList.add(PrimitiveType.forClass(int.class));
            inputTypeList.add(PrimitiveType.forClass(int.class));
        } else {
            inputTypeList.add(outputType);
            inputTypeList.add(outputType);
        }
        // inputTypeList.add(outputType);
        // inputTypeList.add(outputType);
        TypeTuple inputType = new TypeTuple(inputTypeList);
        TypedOperation fuzzTypedOperation = new TypedClassOperation(fuzzCallableOperation,
                declaringType, inputType, outputType);

        List<Integer> inputIndex = new ArrayList<>();

        inputIndex.add(fuzzedChosenSeq.size() - 2);
        inputIndex.add(fuzzedChosenSeq.size() - 1);
        numStatements += 2;

        // System.out.println("fuzzTypedOperation is: " + fuzzTypedOperation);
        // System.out.println("chosenSeq is: " + chosenSeq.toCodeString());
        // System.out.println("Input index is: " + inputIndex);

        List<Sequence> fuzzedChosenSeqList = Collections.singletonList(fuzzedChosenSeq);

        // System.out.println("fuzzedTypeOperation is: " + fuzzTypedOperation);
        // System.out.println("fuzzedChosenSeqList is: " + fuzzedChosenSeqList);
        Sequence output = Sequence.createSequence(fuzzTypedOperation, fuzzedChosenSeqList, inputIndex);

        // TODO: Cast int back to short currently seems to have issues - results in unnecessary casts
        //       that are not compilable.
        // if (outputClass == short.class) {
        if (shortType) {
            CallableOperation callableShortValue;
            try {
                callableShortValue = new MethodCall(Integer.class.getMethod("shortValue"));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Initialization failed due to missing method", e);
            }
            NonParameterizedType declaringInteger = new NonParameterizedType(Integer.class);
            List<Type> intTypeSingleton = new ArrayList<>();
            intTypeSingleton.add(PrimitiveType.forClass(int.class));
            TypeTuple intTypeTuple = new TypeTuple(intTypeSingleton);
            Type outputShortType = PrimitiveType.forClass(short.class);

            TypedOperation typedShortValueOperation = new TypedClassOperation(callableShortValue,
                    declaringInteger, intTypeTuple, outputShortType);
            List<Sequence> outputSingleton = Collections.singletonList(output);

            List<Integer> shortCastIndex = new ArrayList<>();

            shortCastIndex.add(output.size() - 1);
            numStatements += 1;

            output = Sequence.createSequence(typedShortValueOperation, outputSingleton, shortCastIndex);
            System.out.println("output is: " + output.toCodeString());
        }

        /*
        UncheckedCast uncheckedCast;
        if (outputClass == short.class) {
            uncheckedCast = new UncheckedCast(PrimitiveType.forClass(int.class));
            List<Type> intTypeSingleton = new ArrayList<>();
            intTypeSingleton.add(PrimitiveType.forClass(short.class));
            TypeTuple intTypeTuple = new TypeTuple(intTypeSingleton);
            Type outputIntType = PrimitiveType.forClass(int.class);

            TypedOperation shortCast = new TypedTermOperation(uncheckedCast,
                    intTypeTuple, outputIntType);
            List<Sequence> outputSingleton = Collections.singletonList(output);

            List<Integer> shortCastIndex = new ArrayList<>();
            shortCastIndex.add(output.size() - 1);

            output = Sequence.createSequence(shortCast, outputSingleton, shortCastIndex);
        }

         */

        return new ImpurityAndSuccessFlag(true, output, numStatements);
    }

    private static Sequence getFuzzedSequence(Sequence chosenSeq, Class<?> outputClass) {
        List<Sequence> chosenSeqList = Collections.singletonList(chosenSeq);

        if (outputClass.isPrimitive()) {
            Object gaussian;
            if (outputClass == int.class) {
                gaussian = (int) Math.round(Randomness.nextRandomGaussian(0, 1));
            } else if (outputClass == short.class) {
                // This is a temporary work around to bypass the complexity of fuzzing short.
                // Short does not have a sum method, and this introduce challenges to the implementation
                // of fuzzing as we can't directly supply a after-fuzzed short given that
                // the input short might be a variable without a value known at compile time.
                gaussian = (int) Math.round(Randomness.nextRandomGaussian(0, 1));
            } else if (outputClass == long.class) {
                gaussian = Math.round(Randomness.nextRandomGaussian(0, 1));
            } else if (outputClass == float.class) {
                gaussian = (float) Randomness.nextRandomGaussian(0, 1);
            } else if (outputClass == double.class) {
                gaussian = Randomness.nextRandomGaussian(0, 1);
            } else if (outputClass == byte.class) {
                gaussian = (byte) Math.round(Randomness.nextRandomGaussian(0, 1));
            } else {
                throw new RuntimeException("Unexpected primitive type: " + outputClass.getName());
            }

            Sequence gaussianSequence = Sequence.createSequenceForPrimitive(gaussian);
            List<Sequence> gaussianSequenceList = Collections.singletonList(gaussianSequence);
            List<Sequence> temp = new ArrayList<>(chosenSeqList);
            temp.addAll(gaussianSequenceList);
            chosenSeq = Sequence.concatenate(temp);
            return chosenSeq;
        }

        throw new RuntimeException("getFuzzedSequence() is not implemented for non-primitive types");

    }

    private static CallableOperation getCallableOperation(Class<?> outputClass) throws NoSuchMethodException {
        // System.out.println("Output class is: " + outputClass);

        Method method = null;

        // Map each wrapper to its primitive type and a common method
        if (outputClass == int.class) {
            method = Integer.class.getMethod("sum", int.class, int.class);
        } else if (outputClass == double.class) {
            method = Double.class.getMethod("sum", double.class, double.class);
        } else if (outputClass == float.class) {
            method = Float.class.getMethod("sum", float.class, float.class);
        } else if (outputClass == long.class) {
            method = Long.class.getMethod("sum", long.class, long.class);
        } else if (outputClass == short.class) {
            method = Integer.class.getMethod("sum", int.class, int.class);
        } else if (outputClass == byte.class) {
            throw new NoSuchMethodException("Byte fuzzing is not supported yet");
        } else if (outputClass == char.class) {
            throw new NoSuchMethodException("Character fuzzing is not supported yet");
        } else if (outputClass == boolean.class) {
            throw new NoSuchMethodException("Boolean fuzzing is not supported yet");
        } else if (outputClass == String.class) {
            throw new NoSuchMethodException("String fuzzing is not supported yet");
        } else {
            throw new NoSuchMethodException("Object fuzzing is not supported yet");
        }

        if (method == null) {
            throw new NoSuchMethodException("No suitable method found for class " + outputClass.getName());
        }

        return new MethodCall(method);
    }

    private static Class<?> getDeclaringClass(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            // return wrapper clazz
            if (clazz == int.class) {
                return Integer.class;
            } else if (clazz == double.class) {
                return Double.class;
            } else if (clazz == float.class) {
                return Float.class;
            } else if (clazz == long.class) {
                return Long.class;
            } else if (clazz == short.class) {
                return Integer.class;
            } else if (clazz == byte.class) {
                return Byte.class;
            } else if (clazz == char.class) {
                return Character.class;
            } else if (clazz == boolean.class) {
                return Boolean.class;
            } else {
                throw new RuntimeException("Unexpected primitive type: " + clazz.getName());
            }
        } else {
            return clazz;
        }
    }
}
