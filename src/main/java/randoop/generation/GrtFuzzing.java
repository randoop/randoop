package randoop.generation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.operation.CallableOperation;
import randoop.operation.ConstructorCall;
import randoop.operation.MethodCall;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.types.NonParameterizedType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.Randomness;

/**
 * Implements the "GRT Impurity" component, as described in "GRT: Program-Analysis-Guided Random
 * Testing" by Ma et. al (ASE 2015): https://people.kth.se/~artho/papers/lei-ase2015.pdf.
 *
 * <p>The GRT Impurity component is a fuzzing mechanism that alters the states of (or creates new)
 * input objects for methods under test to generate more object states and potentially trigger more
 * branches, improving coverage for the program under test. Because it is a fuzzing mechanism, our
 * implementation is called "GrtFuzzing".
 *
 * <p>[TODO: As described in the GRT paper, "GRT Impurity" also generates more effective tests with
 * shorter length by reducing the number of redundant sequences that do not side-effect the state of
 * an object. That is not implemented here.]
 *
 * <p>For details about fuzzing, see {@link #fuzz}.
 */
@SuppressWarnings("NotJavadoc") // perhaps https://github.com/google/error-prone/issues/3904
public class GrtFuzzing {

  /**
   * An enum representing the fuzzing operations for Strings. Each operation has a corresponding
   * method to be invoked on a StringBuilder object.
   */
  private enum StringFuzzingOperation {
    /** Insert a random character at a random index in the string. */
    INSERT {
      @Override
      Sequence getInputs(int stringLength) {
        int randomIndex = Randomness.nextRandomInt(stringLength + 1);
        char randomChar = (char) (Randomness.nextRandomInt(95) + 32); // ASCII 32-126
        Sequence randomIndexSequence = Sequence.createSequenceForPrimitive(randomIndex);
        Sequence randomCharSequence = Sequence.createSequenceForPrimitive(randomChar);
        return Sequence.concatenate(randomIndexSequence, randomCharSequence);
      }

      @Override
      List<Executable> getMethods() throws NoSuchMethodException {
        List<Executable> methodList = new ArrayList<>();
        methodList.add(StringBuilder.class.getMethod("insert", int.class, char.class));
        methodList.add(StringBuilder.class.getMethod("toString"));
        return methodList;
      }
    },

    /** Remove a character at a random index in the string. */
    REMOVE {
      @Override
      Sequence getInputs(int stringLength) {
        int randomIndex = Randomness.nextRandomInt(stringLength);
        Sequence randomIndexSequence = Sequence.createSequenceForPrimitive(randomIndex);
        return randomIndexSequence;
      }

      @Override
      List<Executable> getMethods() throws NoSuchMethodException {
        List<Executable> methodList = new ArrayList<>();
        methodList.add(StringBuilder.class.getMethod("deleteCharAt", int.class));
        methodList.add(StringBuilder.class.getMethod("toString"));
        return methodList;
      }
    },

    /** Replace a substring with a random character at a random index in the string. */
    REPLACE {
      @Override
      Sequence getInputs(int stringLength) {
        int randomIndex1 = Randomness.nextRandomInt(stringLength);
        int randomIndex2 = Randomness.nextRandomInt(stringLength);
        int startIndex = Math.min(randomIndex1, randomIndex2);
        int endIndex = Math.max(randomIndex1, randomIndex2);
        // ASCII 32-126
        String randomChar = String.valueOf((char) (Randomness.nextRandomInt(95) + 32));
        Sequence startIndexSequence = Sequence.createSequenceForPrimitive(startIndex);
        Sequence endIndexSequence = Sequence.createSequenceForPrimitive(endIndex);
        Sequence randomCharSequence = Sequence.createSequenceForPrimitive(randomChar);
        return Sequence.concatenate(startIndexSequence, endIndexSequence, randomCharSequence);
      }

      @Override
      List<Executable> getMethods() throws NoSuchMethodException {
        List<Executable> methodList = new ArrayList<>();
        methodList.add(
            StringBuilder.class.getMethod("replace", int.class, int.class, String.class));
        methodList.add(StringBuilder.class.getMethod("toString"));
        return methodList;
      }
    },

    /** Get a substring from a random index to another random index in the string. */
    SUBSTRING {
      @Override
      Sequence getInputs(int stringLength) {
        int randomIndex1 = Randomness.nextRandomInt(stringLength);
        int randomIndex2 = Randomness.nextRandomInt(stringLength);
        int startIndex = Math.min(randomIndex1, randomIndex2);
        int endIndex = Math.max(randomIndex1, randomIndex2);
        Sequence startIndexSequence = Sequence.createSequenceForPrimitive(startIndex);
        Sequence endIndexSequence = Sequence.createSequenceForPrimitive(endIndex);
        return Sequence.concatenate(startIndexSequence, endIndexSequence);
      }

      @Override
      List<Executable> getMethods() throws NoSuchMethodException {
        List<Executable> methodList = new ArrayList<>();
        methodList.add(StringBuilder.class.getMethod("substring", int.class, int.class));
        return methodList;
      }
    };

    /** Get the inputs for the fuzzing operation as a sequence. */
    abstract Sequence getInputs(int stringLength);

    /** Get the methods for the fuzzing operation. */
    abstract List<Executable> getMethods() throws NoSuchMethodException;
  }

  /** The standard deviation of the Gaussian distribution used to generate fuzzed numbers. */
  private static final double GAUSSIAN_STD = GenInputsAbstract.grt_fuzzing_stddev;

  /** Do not instantiate. */
  private GrtFuzzing() {
    throw new Error("Do not instantiate");
  }

  /**
   * Generate an extended fuzzed sequence for the given sequence.
   *
   * <p>This is the main entry point for GRT Fuzzing. This method appends additional fuzzing
   * statements to the input sequence to fuzz the input object. This will generate more object
   * states and potentially improve coverage for the unit test generated by Randoop by triggering
   * more branches.
   *
   * <p>Different types have different fuzzing strategies:
   *
   * <ul>
   *   <li><b>Numbers (byte, short, char (treated as numbers), int, long, float, double, and their
   *       wrapper classes):</b> Fuzzed by adding a 0-centered Gaussian distribution. The fuzzed
   *       value is original_value + N(0, GAUSSIAN_STD).
   *   <li><b>Strings:</b> A random fuzzing operation is selected from the {@link
   *       StringFuzzingOperation} enum.
   *   <li><b>Other Objects:</b> [TODO: Further implementation required.] Methods are analyzed for
   *       side-effects to enhance test effectiveness by focusing on these interactions.
   * </ul>
   *
   * @param sequence the (non-null) sequence to fuzz
   * @return a sequence consisting of {@code sequence} with additional fuzzing statements appended
   *     at the end. The original sequence is returned if no valid fuzzing operation is found.
   */
  public static Sequence fuzz(Sequence sequence) {
    // The number of fuzzing statements added to the sequence.
    Type outputType = sequence.getLastVariable().getType();

    Class<?> outputClass = outputType.getRuntimeClass();

    // Do not fuzz void, char, boolean, or byte.
    if (outputClass.equals(void.class)
        || outputClass.equals(boolean.class)
        || outputClass.equals(Boolean.class)) {
      return sequence;
    }

    Sequence output;
    // Append input statements for fuzzing operations to the sequence.
    try {
      if (outputClass.isPrimitive()) { // fuzzing primitive numbers
        output = fuzzNumberSequence(sequence, outputClass);
      } else if (outputClass == String.class) { // fuzzing String
        output = fuzzStringSequence(sequence);
      } else {
        // TODO: Fuzz other objects based on purity analysis.
        //  Return the original sequence for now.
        return sequence;
      }
    } catch (Exception e) { // All other exceptions are unexpected
      throw new RandoopBug("GRT Fuzzing failed: " + e.getMessage(), e);
    }

    return output;
  }

  /**
   * Fuzz a sequence producing a primitive/boxed number.
   *
   * @param sequence the sequence to fuzz
   * @param outputClass the class of the output
   * @return a sequence with the fuzzing statement appended at the end
   */
  private static Sequence fuzzNumberSequence(Sequence sequence, Class<?> outputClass)
      throws NoSuchMethodException {
    Sequence output = appendGaussianSampleSequence(sequence, outputClass);
    List<Executable> fuzzingOperations = getNumberFuzzingMethods(outputClass);
    if (!(outputClass.equals(char.class) || outputClass.equals(Character.class))) {
      return appendListOfFuzzingOperations(output, fuzzingOperations);
    }

    return appendCharFuzzingOperation(fuzzingOperations, output);
  }

  /**
   * Append a fuzzing operations for char to the given sequence.
   *
   * @param fuzzingOperations the fuzzing operations
   * @param sequence the sequence to fuzz
   * @return a sequence with the fuzzing statement appended at the end
   */
  private static Sequence appendCharFuzzingOperation(
      List<Executable> fuzzingOperations, Sequence sequence) {
    sequence =
        appendFuzzingOperation(
            sequence, fuzzingOperations.get(0), getOutputType(fuzzingOperations.get(0)));
    sequence =
        appendFuzzingOperation(
            sequence, fuzzingOperations.get(1), getOutputType(fuzzingOperations.get(1)));
    sequence = Sequence.concatenate(sequence, Sequence.createSequenceForPrimitive(0));
    sequence =
        appendFuzzingOperation(
            sequence, fuzzingOperations.get(2), getOutputType(fuzzingOperations.get(2)));
    return sequence;
  }

  /**
   * Fuzz a sequence producing a String.
   *
   * @param sequence the sequence to fuzz
   * @return a sequence with the fuzzing statement appended at the end
   */
  private static Sequence fuzzStringSequence(Sequence sequence) throws NoSuchMethodException {
    int preFuzzSequenceLength = sequence.size();
    // Randomly select a fuzzing operation for String.
    StringFuzzingOperation operation =
        StringFuzzingOperation.values()[
            Randomness.nextRandomInt(StringFuzzingOperation.values().length)];
    Sequence output = appendStringFuzzingInputs(sequence, operation);
    if (preFuzzSequenceLength == output.size()) { // sequence not fuzzed, return original sequence
      return sequence;
    }
    List<Executable> fuzzingOperations = getStringFuzzingMethod(operation);
    return appendListOfFuzzingOperations(output, fuzzingOperations);
  }

  /**
   * Append a list of fuzzing operations to the given sequence.
   *
   * @param sequence the sequence to append the fuzzing operations to
   * @param fuzzingOperations the fuzzing operations
   * @return a sequence with the fuzzing statements appended at the end
   */
  public static Sequence appendListOfFuzzingOperations(
      Sequence sequence, List<Executable> fuzzingOperations) {
    // Append fuzzing operation statements to the sequence.
    for (Executable executable : fuzzingOperations) {
      sequence = appendFuzzingOperation(sequence, executable, getOutputType(executable));
    }

    return sequence;
  }

  /**
   * Create a new sequence with a fuzzing operation statement appended to the given sequence.
   *
   * @param sequence the sequence to append the fuzzing operations to
   * @param fuzzingOperation the method to be invoked to fuzz the object
   * @param outputType the output type of the fuzzing operation
   * @return a sequence with the fuzzing statement appended at the end
   */
  private static Sequence appendFuzzingOperation(
      Sequence sequence, Executable fuzzingOperation, Type outputType) {
    CallableOperation callableOperation = createCallableOperation(fuzzingOperation);
    NonParameterizedType declaringType =
        new NonParameterizedType(fuzzingOperation.getDeclaringClass());
    List<Type> inputTypeList = getInputTypes(fuzzingOperation, declaringType);
    TypeTuple inputType = new TypeTuple(inputTypeList);
    TypedOperation typedOperation =
        new TypedClassOperation(callableOperation, declaringType, inputType, outputType);
    List<Integer> inputIndex = calculateInputIndices(sequence.size(), inputTypeList.size());
    List<Sequence> sequenceList = Collections.singletonList(sequence);
    return Sequence.createSequence(typedOperation, sequenceList, inputIndex);
  }

  /**
   * Create a method call or constructor call to the given executable.
   *
   * @param executable the executable to invoke
   * @return an invocation of the given executable
   */
  private static CallableOperation createCallableOperation(Executable executable) {
    if (executable instanceof Method) {
      return new MethodCall((Method) executable);
    } else {
      return new ConstructorCall((Constructor<?>) executable);
    }
  }

  /**
   * Returns the output type of the given executable.
   *
   * @param executable the executable to get the output type of
   * @return the output type of the given executable
   */
  private static Type getOutputType(Executable executable) {
    return Type.forType(executable.getAnnotatedReturnType().getType());
  }

  /**
   * Get the list of input types for the given executable.
   *
   * <p>Note: This method doesn't handle cases where the executable is a constructor of a non-static
   * inner class. This is because all executable objects passed to this method are known fuzzing
   * operations, none of which are in an inner class.
   *
   * @param executable the executable to get the input types of
   * @param declaringType the type that declares the given executable
   * @return the list of input types for the given executable
   */
  private static List<Type> getInputTypes(
      Executable executable, NonParameterizedType declaringType) {
    List<Type> inputTypeList = new ArrayList<>();
    if (!Modifier.isStatic(executable.getModifiers()) && executable instanceof Method) {
      inputTypeList.add(declaringType);
    }
    for (Class<?> cls : executable.getParameterTypes()) {
      inputTypeList.add(Type.forClass(cls));
    }
    return inputTypeList;
  }

  /**
   * Calculate the indices of the input statements for the fuzzing operation. This method is called
   * in {@link #appendFuzzingOperation}, where a fuzzing operation is appended to a sequence. The
   * last {@code numOfParams} statements of the sequence will be used as the input statements for
   * the fuzzing operation. This method returns the last {@code numOfParams} indices in ascending
   * order.
   *
   * @param sequenceSize the size of the sequence
   * @param numOfParams the number of parameters for the fuzzing operation
   * @return a list of indices representing the input statements for the fuzzing operation
   */
  private static List<Integer> calculateInputIndices(int sequenceSize, int numOfParams) {
    List<Integer> inputIndices = new ArrayList<>();
    int firstIndex = sequenceSize - numOfParams;
    for (int i = 0; i < numOfParams; i++) {
      inputIndices.add(firstIndex + i);
    }
    return inputIndices;
  }

  /**
   * Create a statement representing a Gaussian sampling result and append it to the given sequence.
   *
   * @param sequence the sequence to append the Gaussian sampling result to
   * @param cls the class of the Gaussian number to be generated and appended
   * @return a sequence with the Gaussian sampling result appended at the end
   */
  private static Sequence appendGaussianSampleSequence(Sequence sequence, Class<?> cls) {
    Object gaussianSample = generateGaussianSample(cls);
    Sequence gaussianSampleSequence = Sequence.createSequenceForPrimitive(gaussianSample);
    return Sequence.concatenate(sequence, gaussianSampleSequence);
  }

  /**
   * Generate a random Gaussian sample value N(0, GAUSSIAN_STD) of the given class.
   *
   * @param cls the class of the number to be generated
   * @return a Gaussian sample value with 0 mean and a predefined standard deviation
   */
  private static Object generateGaussianSample(Class<?> cls) {
    double randomGaussian = GAUSSIAN_STD * Randomness.nextRandomGaussian(1);
    if (cls == byte.class || cls == Byte.class) {
      return (byte) Math.round(randomGaussian);
    } else if (cls == short.class || cls == Short.class) {
      return (short) Math.round(randomGaussian);
    } else if (cls == char.class || cls == Character.class) {
      return (short) Math.round(randomGaussian);
    } else if (cls == int.class || cls == Integer.class) {
      return (int) Math.round(randomGaussian);
    } else if (cls == long.class || cls == Long.class) {
      return Math.round(randomGaussian);
    } else if (cls == float.class || cls == Float.class) {
      return (float) randomGaussian;
    } else if (cls == double.class || cls == Double.class) {
      return randomGaussian;
    } else {
      throw new RuntimeException("Unexpected primitive type: " + cls.getName());
    }
  }

  /**
   * Get the methods for fuzzing primitive numbers of the given class.
   *
   * @param cls a numeric class
   * @return a list of methods that will, together, be used to fuzz a number of the given class
   */
  private static List<Executable> getNumberFuzzingMethods(Class<?> cls)
      throws NoSuchMethodException {
    List<Executable> methodList = new ArrayList<>();

    if (cls == byte.class || cls == Byte.class) {
      // Byte doesn't have a sum method, so we use Integer.sum and get the byte value
      methodList.add(Integer.class.getMethod("sum", int.class, int.class));
      methodList.add(Integer.class.getMethod("valueOf", int.class));
      methodList.add(Integer.class.getMethod("byteValue"));
    } else if (cls == short.class || cls == Short.class) {
      // Short doesn't have a sum method, so we use Integer.sum and get the short value
      methodList.add(Integer.class.getMethod("sum", int.class, int.class));
      methodList.add(Integer.class.getMethod("valueOf", int.class));
      methodList.add(Integer.class.getMethod("shortValue"));
    } else if (cls == char.class || cls == Character.class) {
      // Character doesn't have a sum method, so we use Integer.sum and call toChars
      methodList.add(Integer.class.getMethod("sum", int.class, int.class));
      methodList.add(Character.class.getMethod("toChars", int.class));
      methodList.add(java.lang.reflect.Array.class.getMethod("getChar", Object.class, int.class));
    } else if (cls == int.class || cls == Integer.class) {
      methodList.add(Integer.class.getMethod("sum", int.class, int.class));
    } else if (cls == long.class || cls == Long.class) {
      methodList.add(Long.class.getMethod("sum", long.class, long.class));
    } else if (cls == float.class || cls == Float.class) {
      methodList.add(Float.class.getMethod("sum", float.class, float.class));
    } else if (cls == double.class || cls == Double.class) {
      methodList.add(Double.class.getMethod("sum", double.class, double.class));
    } else {
      throw new IllegalArgumentException("Unexpected primitive type: " + cls.getName());
    }

    return methodList;
  }

  /**
   * Create and append all statements needed for the String fuzzing operation to the given sequence.
   *
   * @param sequence the (non-empty) sequence to append the String fuzzing operation inputs to
   * @param operation the String fuzzing operation to perform
   * @return a sequence with the String fuzzing operation inputs appended at the end
   */
  private static Sequence appendStringFuzzingInputs(
      Sequence sequence, StringFuzzingOperation operation) throws NoSuchMethodException {
    int stringLength = getStringValue(sequence).length();

    if (stringLength == 0 && operation != StringFuzzingOperation.INSERT) {
      return sequence; // Cannot remove/replace/substring an empty string
    }

    sequence = appendStringBuilder(sequence);

    Sequence fuzzingInputsSequence = getStringFuzzingInputs(operation, stringLength);

    return Sequence.concatenate(sequence, fuzzingInputsSequence);
  }

  /**
   * Append a StringBuilder constructor statement to the given sequence. The string value used by
   * the StringBuilder constructor is from the last statement in the sequence.
   *
   * @param sequence the sequence to append the StringBuilder constructor to
   * @return a sequence with the StringBuilder constructor appended at the end
   */
  private static Sequence appendStringBuilder(Sequence sequence) throws NoSuchMethodException {
    Constructor<?> stringBuilderConstructor = StringBuilder.class.getConstructor(String.class);
    return appendFuzzingOperation(
        sequence, stringBuilderConstructor, getOutputType(stringBuilderConstructor));
  }

  /**
   * Get the String value from the given sequence.
   *
   * @param sequence a sequence whose last statement produces a String value
   * @return the String value from the given sequence
   * @throws IllegalArgumentException if the String value cannot be obtained
   */
  private static String getStringValue(Sequence sequence) {
    Object value = sequence.getStatement(sequence.size() - 1).getValue();
    if (value instanceof String) {
      return (String) value;
    } else {
      throw new IllegalArgumentException(
          "Invalid sequence, last statement does not have a String value");
    }
  }

  /**
   * Get a list of sequences that represent the inputs for the fuzzing operation for String.
   *
   * @param operation the String fuzzing operation to perform
   * @param stringLength the length of the string to be fuzzed, for generating valid random indices
   * @return a list of sequences that represent the inputs for the fuzzing operation
   * @throws IllegalArgumentException if an invalid enum value is passed
   */
  public static Sequence getStringFuzzingInputs(
      StringFuzzingOperation operation, int stringLength) {
    return operation.getInputs(stringLength);
  }

  /**
   * Get a list of methods for fuzzing the input String based on the given operation.
   *
   * @param operation the string fuzzing operation to perform
   * @return a list of methods that will be used to fuzz the input String
   */
  public static List<Executable> getStringFuzzingMethod(StringFuzzingOperation operation)
      throws NoSuchMethodException {
    return operation.getMethods();
  }
}
