package randoop.generation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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
 * Implements the Impurity component, as described in "GRT: Program-Analysis-Guided Random Testing"
 * by Ma et. al (ASE 2015): https://people.kth.se/~artho/papers/lei-ase2015.pdf.
 *
 * <p>The Impurity component is a fuzzing mechanism that alters the states of input objects or
 * creates a new input object (if the input object is immutable) for methods under test to generate
 * a wider variety of object states and hence potentially trigger more branches and improve coverage
 * for the program under test. [TODO: It also generates more effective test with shorter length by
 * reducing the number of redundant sequences that does not side-effect the state of an object.]
 *
 * <p>This component fuzzes inputs differently based on their type:
 *
 * <ul>
 *   <li>Primitive Numbers: Fuzzed using a Gaussian distribution, taking the original value as the
 *       mean and a predefined constant as the standard deviation. This approach probabilistically
 *       generates new values around the mean.
 *   <li>String: Fuzzed through uniformly selecting a random string operations, including insertion,
 *       removal, replacement of characters, or taking a substring of the given string.
 *   <li>Other Objects (TODO): Perform purity analysis to determine methods that have side-effects
 *       to the state of the object, and mark them as impure. Then, Randoop will construct more
 *       effective test cases by calling these impure methods without redundant calls to pure
 *       methods.
 * </ul>
 */
public class GrtImpurity {

  /** Ways to fuzz a string. */
  private enum StringFuzzingOperation {
    /**
     * Insert a random character at a random index in the string. The index is randomly selected
     * from 0 to the length of the string.
     */
    INSERT,

    /**
     * Remove a character at a random index in the string. The index is randomly selected from 0 to
     * the length of the string.
     */
    REMOVE,

    /**
     * Replace a substring of the string with a random character. The start and end index of the
     * substring are randomly selected from 0 to the length of the string.
     */
    REPLACE,

    /**
     * Take a substring of the string. The start and end index of the substring are randomly
     * selected from 0 to the length of the string.
     */
    SUBSTRING
  }

  /** The standard deviation of the Gaussian distribution used to generate fuzzed numbers. */
  private static final double GAUSSIAN_STD = GenInputsAbstract.impurity_stddev;

  /** Do not instantiate. */
  private GrtImpurity() {
    throw new Error("Do not instantiate");
  }

  /**
   * Fuzzes the given sequence using the GRT Impurity component.
   *
   * @param sequence the sequence to construct the inputs of test cases
   * @return a new sequence with additional fuzzing statements appended at the end, and a count of
   *     the number of fuzzing statements added to the sequence. If no fuzzing statements are added,
   *     the original sequence is returned and the count is 0.
   */
  public static GrtImpurityAndNumStatements fuzz(Sequence sequence) {
    // A counter to keep track of the number of fuzzing statements added to the sequence
    FuzzStatementOffset fuzzStatementOffset = new FuzzStatementOffset();

    Type outputType = sequence.getLastVariable().getType();

    // Do not fuzz void, char, boolean, or byte.
    if (outputType.isVoid()
        || outputType.runtimeClassIs(char.class)
        || outputType.runtimeClassIs(Character.class)
        || outputType.runtimeClassIs(boolean.class)
        || outputType.runtimeClassIs(Boolean.class)
        || outputType.runtimeClassIs(byte.class)
        || outputType.runtimeClassIs(Byte.class)) {
      return new GrtImpurityAndNumStatements(sequence, 0);
    }

    Class<?> outputClass = outputType.getRuntimeClass();
    List<Method> fuzzingMethods = new ArrayList<>();
    try {
      if (outputClass.isPrimitive()) { // fuzzing primitive numbers
        sequence = getGaussianDeltaSequence(sequence, outputClass);
        fuzzingMethods = getNumberSumMethods(outputClass);
      } else if (outputClass == String.class) { // fuzzing String
        // Randomly select a fuzzing operation for String.
        StringFuzzingOperation operation =
            StringFuzzingOperation.values()[
                Randomness.nextRandomInt(StringFuzzingOperation.values().length)];
        try {
          sequence = appendStringFuzzingInputs(sequence, operation, fuzzStatementOffset);
        } catch (IndexOutOfBoundsException e) {
          // This happens when the input String is empty but a fuzzing operation requires
          // a non-empty string.
          // In this case, we will ignore this fuzzing operation.
          return new GrtImpurityAndNumStatements(sequence, 0);
        }

        // TODO: Create a class to track a method and desired output type. Use this to extend the
        //  sequence. This will allow us to use an explicit cast to replace the Integer.valueOf()
        //  call for more readable generated test with short fuzzing.
        fuzzingMethods = getStringFuzzingMethod(operation);
      } else if (outputClass == null) {
        throw new RuntimeException("Output class is null");
      }
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Initialization failed due to missing method", e);
    }

    Sequence output = sequence;

    Iterator<Method> iterator = fuzzingMethods.iterator();
    while (iterator.hasNext()) {
      Method method = iterator.next();
      if (!iterator.hasNext()) {
        break;
      }
      output = extendWithOperation(output, method, fuzzStatementOffset);
    }

    output =
        extendWithOperation(
            output,
            fuzzingMethods.get(fuzzingMethods.size() - 1),
            outputType,
            fuzzStatementOffset,
            outputType.runtimeClassIs(short.class) || outputType.runtimeClassIs(Short.class));

    return new GrtImpurityAndNumStatements(output, fuzzStatementOffset.getOffset());
  }

  /**
   * Create a sequence for fuzzing an object of a given type using the given method. This overload
   * assumes that the output type of the method is the same as the given type.
   *
   * @param sequence the sequence to append the fuzzing sequence to
   * @param fuzzingOperation the method to be invoked to fuzz the object
   * @param fuzzStatementOffset the offset counter for the number of fuzzing statements added
   * @return a sequence with the fuzzing statement appended at the end
   */
  private static Sequence extendWithOperation(
      Sequence sequence, Executable fuzzingOperation, FuzzStatementOffset fuzzStatementOffset) {
    Type outputType = getOutputType(fuzzingOperation);
    return extendWithOperation(sequence, fuzzingOperation, outputType, fuzzStatementOffset, false);
  }

  /**
   * Extend a sequence with an operation. This overload allows the output type to be specified to
   * handle cases where the output type of the fuzzing operation is different from the output type
   * of the sequence. (e.g. short fuzzing, where the output type of the fuzzing operation is int but
   * requires an explicit cast to short)
   *
   * @param sequence the sequence to append the fuzzing sequence to
   * @param fuzzingOperation the method to be invoked to fuzz the object
   * @param outputType the type of the object to be fuzzed
   * @param fuzzStatementOffset the offset counter for the number of fuzzing statements added
   * @param explicitCast whether to perform an explicit cast for the right-hand side of the fuzzing
   *     statement
   * @return a sequence with the fuzzing statement appended at the end
   */
  private static Sequence extendWithOperation(
      Sequence sequence,
      Executable fuzzingOperation,
      Type outputType,
      FuzzStatementOffset fuzzStatementOffset,
      boolean explicitCast) {
    CallableOperation callableOperation = createCallableOperation(fuzzingOperation, explicitCast);
    NonParameterizedType declaringType =
        new NonParameterizedType(fuzzingOperation.getDeclaringClass());
    List<Type> inputTypeList = getInputTypeList(fuzzingOperation, declaringType);
    TypeTuple inputType = new TypeTuple(inputTypeList);
    TypedOperation typedOperation =
        new TypedClassOperation(callableOperation, declaringType, inputType, outputType);
    List<Integer> inputIndex = calculateInputIndex(sequence, inputTypeList);
    fuzzStatementOffset.increment(inputTypeList.size());
    List<Sequence> sequenceList = Collections.singletonList(sequence);
    return Sequence.createSequence(typedOperation, sequenceList, inputIndex);
  }

  /**
   * Create a callable operation for fuzzing an object of a given type using the given method.
   *
   * @param executable the method to be invoked to fuzz the object
   * @param explicitCast whether to perform an explicit cast for the right-hand side of the fuzzing
   *     statement
   * @return a callable operation for fuzzing an object of a given type using the given method
   */
  private static CallableOperation createCallableOperation(
      Executable executable, boolean explicitCast) {
    if (executable instanceof Method) {
      return new MethodCall((Method) executable, explicitCast);
    } else {
      return new ConstructorCall((Constructor<?>) executable);
    }
  }

  /**
   * Returns the output type of the given method.
   *
   * @param executable the method to determine the output type of
   * @return the output type of the given method
   */
  private static Type getOutputType(Executable executable) {
    Class<?> outputClass;
    if (executable instanceof Method) {
      outputClass = ((Method) executable).getReturnType();
    } else {
      outputClass = ((Constructor<?>) executable).getDeclaringClass();
    }
    return Type.forClass(outputClass);
  }

  /**
   * Get the list of input types for the given method.
   *
   * @param executable the method to get the input types of
   * @param declaringType the type that declares the given method
   * @return the list of input types for the given method
   */
  private static List<Type> getInputTypeList(
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
   * Calculate the index of statements in the sequence that correspond to the input types of the
   * given executable. Precondition: The input statements are the last n statements in the sequence,
   * where n is the number of input types.
   *
   * @param sequence the sequence to calculate the input index of
   * @param inputTypeList the list of input types
   * @return the index of statements in the sequence that correspond to the input types of the given
   *     executable
   */
  private static List<Integer> calculateInputIndex(Sequence sequence, List<Type> inputTypeList) {
    List<Integer> inputIndex = new ArrayList<>();
    for (int i = 0; i < inputTypeList.size(); i++) {
      inputIndex.add(sequence.size() - inputTypeList.size() + i);
    }
    return inputIndex;
  }

  /**
   * Get a new sequence with a Gaussian delta appended to the given sequence. This generates and
   * appends a delta = N(0, GAUSSIAN_STD) of the given class to the sequence. This is part of the
   * fuzzing process for primitive numbers, where the original value is used as the mean (mu) and
   * fuzzed with a Gaussian distribution through mu + delta.
   *
   * @param sequence the sequence to append the Gaussian delta to
   * @param cls the class of the Gaussian number to be generated and appended
   * @return a sequence with the Gaussian delta appended at the end
   */
  private static Sequence getGaussianDeltaSequence(Sequence sequence, Class<?> cls) {
    Object gaussianDelta = getGaussianDelta(cls);
    Sequence deltaSequence = Sequence.createSequenceForPrimitive(gaussianDelta);
    List<Sequence> temp = new ArrayList<>(Collections.singletonList(sequence));
    temp.add(deltaSequence); // Add fuzzing sequence to the list
    return Sequence.concatenate(temp); // Assuming concatenate combines all sequences in the list
  }

  /**
   * Get a Gaussian delta value (N(0, GAUSSIAN_STD)) with the given class.
   *
   * @param cls the class of the Gaussian number to be generated
   * @return a Gaussian delta value with 0 mean and a predefined standard deviation with the given
   *     class
   */
  private static Object getGaussianDelta(Class<?> cls) {
    double randomGaussian = GAUSSIAN_STD * Randomness.nextRandomGaussian(1);
    if (cls == int.class || cls == Integer.class) {
      return (int) Math.round(randomGaussian);
    } else if (cls == short.class || cls == Short.class) {
      return (short) Math.round(randomGaussian);
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
   * Get the corresponding sum method for fuzzing primitive numbers for the given class.
   *
   * @param cls the class of the primitive number to be fuzzed
   * @return a method that can be used to fuzz primitive numbers
   * @throws NoSuchMethodException if no suitable method is found for the given class
   */
  private static List<Method> getNumberSumMethods(Class<?> cls) throws NoSuchMethodException {
    List<Method> methodList = new ArrayList<>();

    if (cls == int.class || cls == Integer.class) {
      methodList.add(Integer.class.getMethod("sum", int.class, int.class));
    } else if (cls == double.class || cls == Double.class) {
      methodList.add(Double.class.getMethod("sum", double.class, double.class));
    } else if (cls == float.class || cls == Float.class) {
      methodList.add(Float.class.getMethod("sum", float.class, float.class));
    } else if (cls == long.class || cls == Long.class) {
      methodList.add(Long.class.getMethod("sum", long.class, long.class));
    } else if (cls == short.class || cls == Short.class) {
      methodList.add(Integer.class.getMethod("sum", int.class, int.class));
      methodList.add(Integer.class.getMethod("valueOf", int.class));
      methodList.add(Integer.class.getMethod("shortValue"));
    } else {
      throw new RandoopBug(
          "Unexpected primitive type: "
              + cls.getName()
              + ", and code "
              + "should not reach this point.");
    }

    return methodList;
  }

  /**
   * Create and append all necessary statements for the String fuzzing operation to the given
   * sequence. Precondition: Length of the input String to be fuzzed is not 0.
   *
   * @param sequence the sequence to append the String fuzzing operation inputs to
   * @param operation the String fuzzing operation to perform
   * @param fuzzStatementOffset the offset counter for the number of fuzzing statements added
   * @return a sequence with the String fuzzing operation inputs appended at the end
   * @throws IllegalArgumentException if the fuzzing operation is invalid
   * @throws IndexOutOfBoundsException if the input String is empty
   */
  private static Sequence appendStringFuzzingInputs(
      Sequence sequence, StringFuzzingOperation operation, FuzzStatementOffset fuzzStatementOffset)
      throws IllegalArgumentException, IndexOutOfBoundsException {
    sequence = appendStringBuilder(sequence, fuzzStatementOffset);

    Object stringValue = getStringValue(sequence);
    int stringLength = stringValue.toString().length();

    if (stringLength == 0) {
      throw new IndexOutOfBoundsException(
          "String length is 0. Will ignore this fuzzing" + " operation.");
    }

    List<Sequence> fuzzingSequenceList = getStringFuzzingInputs(operation, stringLength);

    List<Sequence> temp = new ArrayList<>(Collections.singletonList(sequence));
    temp.addAll(fuzzingSequenceList); // Assuming these are sequences that need to be concatenated
    return Sequence.concatenate(temp); // Concatenate all sequences together
  }

  /**
   * Append a StringBuilder constructor to the given sequence. The value used by the StringBuilder
   * constructor is from the last statement in the sequence. It is assumed that the last statement
   * in the sequence is a String value.
   *
   * @param sequence the sequence to append the StringBuilder constructor to
   * @param fuzzStatementOffset the offset counter for the number of fuzzing statements added
   * @return a sequence with the StringBuilder constructor appended at the end
   * @throws IllegalArgumentException if the StringBuilder constructor cannot be found
   */
  private static Sequence appendStringBuilder(
      Sequence sequence, FuzzStatementOffset fuzzStatementOffset) {
    try {
      Constructor<?> stringBuilderConstructor = StringBuilder.class.getConstructor(String.class);
      return extendWithOperation(sequence, stringBuilderConstructor, fuzzStatementOffset);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Initialization failed due to missing method", e);
    }
  }

  /**
   * Get the String value from the given sequence. Precondition: The String value is the 2nd last
   * statement in the sequence. This is because a StringBuilder constructor is appended to the
   * sequence before calling this method.
   *
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
      throw new IllegalArgumentException(
          "Error obtaining String value. Will ignore this fuzzing operation.", e);
    }
  }

  /**
   * Perform a fuzzing operation on a String.
   *
   * @param operation the String fuzzing operation to perform
   * @param stringLength the length of the string to be fuzzed
   * @return a list of sequences that represent the inputs for the fuzzing operation
   */
  private static List<Sequence> getStringFuzzingInputs(
      StringFuzzingOperation operation, int stringLength) {
    switch (operation) {
      case INSERT:
        return getInsertInputs(stringLength);
      case REMOVE:
        return getRemoveInputs(stringLength);
      case REPLACE:
        return getReplaceInputs(stringLength);
      case SUBSTRING:
        return getSubstringInputs(stringLength);
      default:
        throw new IllegalArgumentException("Invalid fuzziing operation: " + operation);
    }
  }

  /**
   * Generate a set of random inputs as sequences for the insertion operation.
   *
   * @param stringLength the length of the string to be fuzzed
   * @return a sequence (singleton) that represent the input for the insertion operation
   */
  private static List<Sequence> getInsertInputs(int stringLength) {
    int randomIndex = Randomness.nextRandomInt(stringLength + 1); // Include stringLength as
    // possible index for insertion at the end.
    char randomChar = (char) (Randomness.nextRandomInt(95) + 32); // ASCII 32-126
    Sequence randomIndexSequence = Sequence.createSequenceForPrimitive(randomIndex);
    Sequence randomCharSequence = Sequence.createSequenceForPrimitive(randomChar);
    // return Collections.singletonList(Sequence.concatenate(Arrays.asList(randomIndexSequence,
    // randomCharSequence)));
    return Arrays.asList(randomIndexSequence, randomCharSequence);
  }

  /**
   * Generate a random index as sequence for the removal operation.
   *
   * @param stringLength the length of the string to be fuzzed
   * @return a sequence (singleton) that represent the input for the removal operation
   */
  private static List<Sequence> getRemoveInputs(int stringLength) {
    int randomIndex = Randomness.nextRandomInt(stringLength);
    Sequence randomIndexSequence = Sequence.createSequenceForPrimitive(randomIndex);
    return Collections.singletonList(randomIndexSequence);
  }

  /**
   * Generate a set of random inputs as sequences for the replacement operation.
   *
   * @param stringLength the length of the string to be fuzzed
   * @return a list of sequences that represent the input for the replacement operation
   */
  private static List<Sequence> getReplaceInputs(int stringLength) {
    int randomIndex1 = Randomness.nextRandomInt(stringLength);
    int randomIndex2 = Randomness.nextRandomInt(stringLength);
    int startIndex = Math.min(randomIndex1, randomIndex2);
    int endIndex = Math.max(randomIndex1, randomIndex2);
    String randomChar = String.valueOf((char) (Randomness.nextRandomInt(95) + 32)); // ASCII 32-126
    Sequence startIndexSequence = Sequence.createSequenceForPrimitive(startIndex);
    Sequence endIndexSequence = Sequence.createSequenceForPrimitive(endIndex);
    Sequence randomCharSequence = Sequence.createSequenceForPrimitive(randomChar);
    return Arrays.asList(startIndexSequence, endIndexSequence, randomCharSequence);
  }

  /**
   * Generate a set of random inputs as sequences for the substring operation.
   *
   * @param stringLength the length of the string to be fuzzed
   * @return a list of sequences that represent the input for the substring operation
   */
  private static List<Sequence> getSubstringInputs(int stringLength) {
    int randomIndex1 = Randomness.nextRandomInt(stringLength);
    int randomIndex2 = Randomness.nextRandomInt(stringLength);
    int startIndex = Math.min(randomIndex1, randomIndex2);
    int endIndex = Math.max(randomIndex1, randomIndex2);
    Sequence startIndexSequence = Sequence.createSequenceForPrimitive(startIndex);
    Sequence endIndexSequence = Sequence.createSequenceForPrimitive(endIndex);
    return Arrays.asList(startIndexSequence, endIndexSequence);
  }

  /**
   * Get a list of methods that represent the fuzzing operations for String.
   *
   * @param operation the string fuzzing operation to perform
   * @return a list of methods that will be used to fuzz the input String
   * @throws NoSuchMethodException if no suitable method is found for class String
   */
  private static List<Method> getStringFuzzingMethod(StringFuzzingOperation operation)
      throws NoSuchMethodException {
    List<Method> methodList = new ArrayList<>();

    switch (operation) {
      case INSERT:
        methodList.add(StringBuilder.class.getMethod("insert", int.class, char.class));
        methodList.add(StringBuilder.class.getMethod("toString"));
        break;
      case REMOVE:
        methodList.add(StringBuilder.class.getMethod("deleteCharAt", int.class));
        methodList.add(StringBuilder.class.getMethod("toString"));
        break;
      case REPLACE:
        methodList.add(
            StringBuilder.class.getMethod("replace", int.class, int.class, String.class));
        methodList.add(StringBuilder.class.getMethod("toString"));
        break;
      case SUBSTRING:
        methodList.add(StringBuilder.class.getMethod("substring", int.class, int.class));
        break;
      default:
        throw new NoSuchMethodException("Object fuzzing is not supported yet");
    }

    if (methodList.isEmpty()) {
      // Should be unreachable
      throw new NoSuchMethodException("No suitable method found for class String");
    }

    return methodList;
  }

  /**
   * A helper class to store the extended sequence and the number of fuzzing statements added to the
   * sequence. The number of fuzzing statements added to the sequence is needed for the forward
   * generation's input selection process to select the correct fuzzed inputs.
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
