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
import randoop.sequence.Sequence;
import randoop.types.NonParameterizedType;
import randoop.operation.MethodCall;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.util.Randomness;
import randoop.util.ListOfLists;

public class Impurity {
    // private static final Map<Class<?>, Method> fuzzStrategies = new HashMap<>();

    // Sequence might actually be Object


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
     *

     */

    /*
    static {
        try {
            // Map each wrapper type to its specific fuzzing method.
            fuzzStrategies.put(int.class, Impurity.class.getDeclaredMethod("fuzzInteger", int.class));
            fuzzStrategies.put(Integer.class, Impurity.class.getDeclaredMethod("fuzzInteger", Integer.class));
            fuzzStrategies.put(long.class, Impurity.class.getDeclaredMethod("fuzzLong", long.class));
            fuzzStrategies.put(Long.class, Impurity.class.getDeclaredMethod("fuzzLong", Long.class));
            fuzzStrategies.put(short.class, Impurity.class.getDeclaredMethod("fuzzShort", short.class));
            fuzzStrategies.put(Short.class, Impurity.class.getDeclaredMethod("fuzzShort", Short.class));
            fuzzStrategies.put(float.class, Impurity.class.getDeclaredMethod("fuzzFloat", float.class));
            fuzzStrategies.put(Float.class, Impurity.class.getDeclaredMethod("fuzzFloat", Float.class));
            fuzzStrategies.put(byte.class, Impurity.class.getDeclaredMethod("fuzzByte", byte.class));
            fuzzStrategies.put(Byte.class, Impurity.class.getDeclaredMethod("fuzzByte", Byte.class));
            fuzzStrategies.put(double.class, Impurity.class.getDeclaredMethod("fuzzDouble", double.class));
            fuzzStrategies.put(Double.class, Impurity.class.getDeclaredMethod("fuzzDouble", Double.class));
            fuzzStrategies.put(char.class, Impurity.class.getDeclaredMethod("fuzzCharacter", char.class));
            fuzzStrategies.put(Character.class, Impurity.class.getDeclaredMethod("fuzzCharacter", Character.class));
            fuzzStrategies.put(boolean.class, Impurity.class.getDeclaredMethod("fuzzBoolean", boolean.class));
            fuzzStrategies.put(Boolean.class, Impurity.class.getDeclaredMethod("fuzzBoolean", Boolean.class));
            fuzzStrategies.put(String.class, Impurity.class.getDeclaredMethod("fuzzString", String.class));
        } catch (Exception e) {
            // System.out.println("The exception is: " + e);
            throw new RuntimeException("Initialization failed due to missing method", e);
        }
    }

     */

    // Private constructor to prevent instantiation
    private Impurity() {}

    public static Sequence fuzz(Sequence chosenSeq) {
        Type outputType = chosenSeq.getLastVariable().getType();
        // If output type is void, then we don't need to fuzz anything.
        if (outputType.isVoid()) {
            return chosenSeq;
        }
        // Class<?> outputClass = outputType.getRuntimeClass();
        /*
        Executable fuzzStrategy;
        try {
            fuzzStrategy = fuzzStrategies.getOrDefault(outputClass,
                    Impurity.class.getDeclaredMethod("fuzzObject", Object.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Initialization failed due to missing method", e);
        }

         */
        /*
        CallableOperation fuzzCallableOperation = new MethodCall((Method) fuzzStrategy);
        NonParameterizedType declaringType = new NonParameterizedType(Impurity.class);

        List<Type> inputTypeList = new ArrayList<>();
        inputTypeList.add(outputType);
        TypeTuple inputType = new TypeTuple(inputTypeList);
        TypedOperation fuzzTypedOperation = new TypedClassOperation(fuzzCallableOperation,
                declaringType, inputType, outputType);
        */
        List<Sequence> chosenSeqList = Collections.singletonList(chosenSeq);

        // List<Integer> inputIndex = Collections.singletonList(chosenSeq.size() - 1);

        // System.out.println("Typed Operation: " + fuzzTypedOperation);
        // System.out.println("Fuzzing sequence: " + chosenSeq.toString());
        // System.out.println("Input index: " + inputIndex.toString());
        double gaussian = Randomness.nextRandomGaussian(0, 1);

        Sequence gaussianSequence = Sequence.createSequenceForPrimitive(gaussian);
        List<Sequence> gaussianSequenceList = Collections.singletonList(gaussianSequence);
        List<Sequence> temp = new ArrayList<>();
        temp.addAll(chosenSeqList);
        temp.addAll(gaussianSequenceList);
        chosenSeq = Sequence.concatenate(temp);
        chosenSeqList = Collections.singletonList(chosenSeq);

        CallableOperation fuzzCallableOperation;
        try {
            fuzzCallableOperation = new MethodCall(Double.class.getMethod("sum", double.class, double.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Initialization failed due to missing method", e);
        }

        NonParameterizedType declaringType = new NonParameterizedType(Double.class);

        List<Type> inputTypeList = new ArrayList<>();
        inputTypeList.add(outputType);
        inputTypeList.add(outputType);
        TypeTuple inputType = new TypeTuple(inputTypeList);
        TypedOperation fuzzTypedOperation = new TypedClassOperation(fuzzCallableOperation,
                declaringType, inputType, outputType);

        List<Integer> inputIndex = new ArrayList<>();
        inputIndex.add(chosenSeq.size() - 2);
        inputIndex.add(chosenSeq.size() - 1);
        Sequence output = Sequence.createSequence(fuzzTypedOperation, chosenSeqList, inputIndex);

        // System.out.println("Fuzzed sequence: " + output.toString());

        return output;
    }

    public static int fuzzInteger(int i) {
        //System.out.println("Fuzzing primitive int: " + i);
        return i;  // Implement fuzzing logic here
    }

    // Input Sequence might actually just be Object
    public static Integer fuzzInteger(Integer i) {
        //System.out.println("Fuzzing Integer: " + i);
        return i;
    }

    public static long fuzzLong(long l) {
        //System.out.println("Fuzzing primitive long: " + l);
        return l;  // Implement fuzzing logic here
    }

    public static Long fuzzLong(Long l) {
        //System.out.println("Fuzzing Long: " + l);
        return l;
    }

    public static short fuzzShort(short s) {
        //System.out.println("Fuzzing primitive short: " + s);
        return s;
    }

    public static Short fuzzShort(Short s) {
        //System.out.println("Fuzzing Short: " + s);
        return s;
    }

    public static float fuzzFloat(float f) {
        //System.out.println("Fuzzing primitive float: " + f);
        return f;
    }

    public static Float fuzzFloat(Float f) {
        //System.out.println("Fuzzing Float: " + f);
        return f;
    }

    public static byte fuzzByte(byte b) {
        //System.out.println("Fuzzing primitive byte: " + b);
        return b;
    }

    public static Byte fuzzByte(Byte b) {
        //System.out.println("Fuzzing Byte: " + b);
        return b;
    }

    public static double fuzzDouble(double d) {
        // System.out.println("Fuzzing primitive double: " + d);
        return d;
    }

    public static Double fuzzDouble(Double d) {
        // System.out.println("Fuzzing Double: " + d);
        return d;
    }

    public static char fuzzCharacter(char c) {
        // System.out.println("Fuzzing primitive char: " + c);
        return c;
    }

    public static Character fuzzCharacter(Character c) {
        // System.out.println("Fuzzing Character: " + c);
        return c;
    }

    public static boolean fuzzBoolean(boolean b) {
        // System.out.println("Fuzzing primitive boolean: " + b);
        return b;
    }

    public static Boolean fuzzBoolean(Boolean b) {
        // System.out.println("Fuzzing Boolean: " + b);
        return b;
    }

    public static String fuzzString(String str) {
        // System.out.println("Fuzzing string: " + str);
        return str;
    }

    public static Object fuzzObject(Object obj) {
        // System.out.println("Fuzzing object: " + obj);
        return obj;
    }
}
