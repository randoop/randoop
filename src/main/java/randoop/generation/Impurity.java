package randoop.generation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import randoop.operation.CallableOperation;
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
    private static final double GAUSSIAN_STD = 10;


    // Private constructor to prevent instantiation
    private Impurity() {}

    public static ImpurityAndNumStatements fuzz(Sequence chosenSeq) {
        NumStatements numStatements = new NumStatements();

        Type outputType = chosenSeq.getLastVariable().getType();
        boolean shortType = false;
        if (outputType.runtimeClassIs(short.class)) {
            outputType = PrimitiveType.forClass(int.class);
            shortType = true;
        }

        // TODO: String fuzzing is not supported yet
        if (outputType.isVoid()
            || outputType.runtimeClassIs(char.class)
            || outputType.runtimeClassIs(boolean.class)
            || outputType.runtimeClassIs(byte.class)
            || outputType.runtimeClassIs(String.class)){
            return new ImpurityAndNumStatements(chosenSeq, 0);
        }

        Class<?> outputClass = outputType.getRuntimeClass();
        chosenSeq = getFuzzedSequence(chosenSeq, outputClass);
        Method method;
        try {
            method = getMethod(outputClass);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Initialization failed due to missing method", e);
        }
        Sequence output = createSequence(chosenSeq, method, numStatements);


        if (shortType) {
            // First, cast the int back to short through getting the wrapper object of the int
            Method intWrapper;
            try {
                intWrapper = Integer.class.getMethod("valueOf", int.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Initialization failed due to missing method", e);
            }
            output = createSequence(output, intWrapper, numStatements);

            // Get the short value of the wrapper object
            Method shortValue;
            try {
                shortValue = Integer.class.getMethod("shortValue");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Initialization failed due to missing method", e);
            }
            output = createSequence(output, shortValue, numStatements);
        }

        return new ImpurityAndNumStatements(output, numStatements.getNumStatements());
    }


    private static Sequence createSequence(Sequence chosenSeq, Method method, NumStatements numStatements) {
        CallableOperation callableOperation = new MethodCall(method);

        NonParameterizedType declaringType = new NonParameterizedType(method.getDeclaringClass());

        List<Type> inputTypeList = new ArrayList<>();
        if (!Modifier.isStatic(method.getModifiers())) {
            inputTypeList.add(declaringType);
        }
        for (Class<?> clazz : method.getParameterTypes()) {
            inputTypeList.add(clazz.isPrimitive() ? PrimitiveType.forClass(clazz) : new NonParameterizedType(clazz));
        }
        TypeTuple inputType = new TypeTuple(inputTypeList);

        Type outputType;
        Class<?> outputClass = method.getReturnType();
        if (outputClass.isPrimitive()) {
            outputType = PrimitiveType.forClass(outputClass);
        } else {
            outputType = new NonParameterizedType(outputClass);
        }

        TypedOperation typedOperation = new TypedClassOperation(callableOperation,
                declaringType, inputType, outputType);

        List<Integer> inputIndex = new ArrayList<>();
        for (int i = 0; i < inputTypeList.size(); i++) {
            inputIndex.add(chosenSeq.size() - inputTypeList.size() + i);
        }
        numStatements.increment(inputTypeList.size());

        List<Sequence> chosenSeqList = Collections.singletonList(chosenSeq);
        return Sequence.createSequence(typedOperation, chosenSeqList, inputIndex);
    }



    private static Sequence getFuzzedSequence(Sequence chosenSeq, Class<?> outputClass) {
        List<Sequence> chosenSeqList = Collections.singletonList(chosenSeq);

        if (outputClass.isPrimitive()) {
            double gaussian = GAUSSIAN_STD * Randomness.nextRandomGaussian(0, 1);
            Object fuzzedValue;
            if (outputClass == int.class) {
                fuzzedValue = (int) Math.round(gaussian);
            } else if (outputClass == short.class) {
                // This is a temporary work around to bypass the complexity of fuzzing short.
                // Short does not have a sum method, and this introduce challenges to the implementation
                // of fuzzing as we can't directly supply a after-fuzzed short given that
                // the input short might be a variable without a value known at compile time.
                fuzzedValue = (int) Math.round(gaussian);
            } else if (outputClass == long.class) {
                fuzzedValue = Math.round(gaussian);
            } else if (outputClass == byte.class) {
                fuzzedValue = (byte) Math.round(gaussian);
            } else if (outputClass == float.class) {
                fuzzedValue = (float) gaussian;
            } else if (outputClass == double.class) {
                fuzzedValue = gaussian;
            } else {
                throw new RuntimeException("Unexpected primitive type: " + outputClass.getName());
            }

            Sequence gaussianSequence = Sequence.createSequenceForPrimitive(fuzzedValue);
            List<Sequence> gaussianSequenceList = Collections.singletonList(gaussianSequence);
            List<Sequence> temp = new ArrayList<>(chosenSeqList);
            temp.addAll(gaussianSequenceList);
            chosenSeq = Sequence.concatenate(temp);
            return chosenSeq;
        }

        throw new RuntimeException("getFuzzedSequence() is not implemented for non-primitive types");

    }

    /*
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

     */


    private static Method getMethod(Class<?> outputClass) throws NoSuchMethodException {
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

        return method;
    }





    /*
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

     */

    private static class NumStatements {
        private int numStatements;

        private NumStatements() {
            this.numStatements = 0;
        }

        private int getNumStatements() {
            return this.numStatements;
        }

        private void increment(int numStatements) {
            this.numStatements += numStatements;
        }
    }
}
