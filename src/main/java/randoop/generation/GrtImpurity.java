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
import randoop.operation.CallableOperation;
import randoop.operation.ConstructorCall;
import randoop.operation.MethodCall;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.types.NonParameterizedType;
import randoop.types.PrimitiveType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.Randomness;

/**
 * Implements the Impurity component, as outlined in "GRT: Program-Analysis-Guided Random Testing"
 * by Ma et. al (ASE 2015): https://people.kth.se/~artho/papers/lei-ase2015.pdf.
 *
 * <p>The Impurity component is a fuzzing mechanism that alters the states of input objects or
 * creates a new input object (if the input object is immutable) for methods under test to generate
 * a wider variety of object states and hence potentially trigger more branches and improve coverage
 * for the program under test. [TODO: It also generates more effective test with shorter length by
 * reducing the number of redundant sequences that does not side-effect the state of an object].
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
  /** The standard deviation of the Gaussian distribution used to generate fuzzed numbers. */
  private static final double GAUSSIAN_STD = GenInputsAbstract.impurity_stddev;

  /** Prevents instantiation. */
  private GrtImpurity() {}

  /**
   * Fuzzes the given sequence using the GRT Impurity component.
   *
   * @param sequence the sequence to construct the inputs of test cases
   * @return a new sequence with additional fuzzing statements appended at the end, and a count of
   *     the number of fuzzing statements added to the sequence. If no fuzzing statements are added,
   *     the original sequence is returned and the count is 0
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
        sequence = appendGaussianDelta(sequence, outputClass);
        fuzzingMethods = getNumberSumMethods(outputClass);
      } else if (outputClass == String.class) { // fuzzing String
        // There are 4 fuzzing strategies for String. Uniformly select one.
        int stringFuzzingStrategyIndex = Randomness.nextRandomInt(4);
        try {
          sequence =
              getFuzzedSequenceForString(sequence, stringFuzzingStrategyIndex, fuzzStatementOffset);
        } catch (IndexOutOfBoundsException e) {
          // This happens when the input String is empty but a fuzzing operation requires
          // a non-empty string.
          // In this case, we will ignore this fuzzing operation.
          return new GrtImpurityAndNumStatements(sequence, 0);
        }
        fuzzingMethods = getStringFuzzingMethod(stringFuzzingStrategyIndex);
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
            outputType.runtimeClassIs(short.class));

    return new GrtImpurityAndNumStatements(output, fuzzStatementOffset.getOffset());
  }

  /**
   * Extend a sequence with an operation. This overload assumes that the output type of the new
   * sequence is the same as the output type of the fuzzing operation.
   *
   * @param sequence the sequence to append the fuzzing sequence to
   * @param fuzzingOperation the executable (constructor or method) to be invoked as part of the
   *     object fuzzing process
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
   * @param fuzzingOperation the executable (constructor or method) to be invoked as part of the
   *     object fuzzing process
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
   * Create a callable operation given an executable and a flag to perform an explicit cast.
   *
   * @param executable the executable to create a callable operation for
   * @param explicitCast whether to perform an explicit cast for the result of the callable
   *     operation
   * @return a callable operation for the given executable
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
   * Get the output type of the given executable.
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
    return outputClass.isPrimitive()
        ? PrimitiveType.forClass(outputClass)
        : new NonParameterizedType(outputClass);
  }

  /**
   * Get the list of input types for the given executable.
   *
   * @param executable the executable to get the input types of
   * @param declaringType the type that declares the executable
   * @return the list of input types for the given executable
   */
  private static List<Type> getInputTypeList(
      Executable executable, NonParameterizedType declaringType) {
    List<Type> inputTypeList = new ArrayList<>();
    if (!Modifier.isStatic(executable.getModifiers()) && executable instanceof Method) {
      inputTypeList.add(declaringType);
    }
    for (Class<?> cls : executable.getParameterTypes()) {
      inputTypeList.add(
          cls.isPrimitive() ? PrimitiveType.forClass(cls) : new NonParameterizedType(cls));
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
   * Generate and append a Gaussian delta of the given class to a sequence for fuzzing primitive
   * numbers.
   *
   * @param sequence the sequence to append the Gaussian delta to
   * @param cls the class of the Gaussian number to be generated and appended
   * @return a sequence with the Gaussian delta appended at the end
   */
  private static Sequence appendGaussianDelta(Sequence sequence, Class<?> cls) {
    Object fuzzedValue = getGaussianDelta(cls);
    Sequence fuzzingSequence = Sequence.createSequenceForPrimitive(fuzzedValue);
    List<Sequence> temp = new ArrayList<>(Collections.singletonList(sequence));
    temp.add(fuzzingSequence); // Add fuzzing sequence to the list
    return Sequence.concatenate(temp); // Assuming concatenate combines all sequences in the list
  }

  /**
   * Get a Gaussian delta value of the given class for fuzzing primitive numbers.
   *
   * @param cls the class of the Gaussian number to be generated
   * @return a Gaussian delta value with 0 mean and a predefined standard deviation with the given
   *     class
   */
  private static Object getGaussianDelta(Class<?> cls) {
    double randomGaussian = GAUSSIAN_STD * Randomness.nextRandomGaussian(1);
    if (cls == int.class) {
      return (int) Math.round(randomGaussian);
    } else if (cls == short.class || cls == byte.class) {
      return (short) Math.round(randomGaussian); // Unified handling for short and byte
    } else if (cls == long.class) {
      return Math.round(randomGaussian);
    } else if (cls == float.class) {
      return (float) randomGaussian;
    } else if (cls == double.class) {
      return randomGaussian;
    } else {
      throw new RuntimeException("Unexpected primitive type: " + cls.getName());
    }
  }

  /**
   * Get the corresponding sum method (in a list) for fuzzing primitive numbers for the given class.
   *
   * @param cls the class of the primitive number to be fuzzed
   * @return a method (in a list) that can be used to fuzz primitive numbers
   * @throws NoSuchMethodException if no suitable method is found for the given class
   */
  private static List<Method> getNumberSumMethods(Class<?> cls) throws NoSuchMethodException {

    List<Method> methodList = new ArrayList<>();

    // Map each wrapper to its primitive type and a common method
    if (cls == int.class) {
      methodList.add(Integer.class.getMethod("sum", int.class, int.class));
    } else if (cls == double.class) {
      methodList.add(Double.class.getMethod("sum", double.class, double.class));
    } else if (cls == float.class) {
      methodList.add(Float.class.getMethod("sum", float.class, float.class));
    } else if (cls == long.class) {
      methodList.add(Long.class.getMethod("sum", long.class, long.class));
    } else if (cls == short.class) {
      methodList.add(Integer.class.getMethod("sum", int.class, int.class));
    } else if (cls == byte.class) {
      throw new NoSuchMethodException("Byte fuzzing is not supported yet");
    } else {
      throw new NoSuchMethodException("Object fuzzing is not supported");
    }

    if (methodList.isEmpty()) {
      // Should be unreachable
      throw new NoSuchMethodException(
          "Unable to find suitable method for class: "
              + cls.getName()
              + " in primitive number fuzzing");
    }

    return methodList;
  }

  /**
   * Get a sequence with the fuzzing statement appended at the end for fuzzing a String.
   *
   * @param sequence the sequence to append the fuzzing sequence to
   * @param fuzzingOperationIndex the index of the fuzzing operation to perform
   * @param fuzzStatementOffset the offset counter for the number of fuzzing statements added
   * @return a sequence with the fuzzing statement appended at the end
   * @throws IllegalArgumentException if the fuzzing operation is invalid
   * @throws IndexOutOfBoundsException if the input String is empty
   */
  private static Sequence getFuzzedSequenceForString(
      Sequence sequence, int fuzzingOperationIndex, FuzzStatementOffset fuzzStatementOffset)
      throws IllegalArgumentException, IndexOutOfBoundsException {
    sequence = appendStringBuilder(sequence, fuzzStatementOffset);

    Object stringValue = getStringValue(sequence);
    int stringLength = stringValue.toString().length();

    if (stringLength == 0) {
      throw new IndexOutOfBoundsException(
          "String length is 0. Will ignore this fuzzing" + " operation.");
    }

    List<Sequence> fuzzingSequenceList =
        performFuzzingOperation(fuzzingOperationIndex, stringLength);

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
   * Get the String value from the given sequence.
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
          "Error obtaining String value. Will ignore this" + " fuzzing operation.", e);
    }
  }

  /**
   * Return a list of sequences that represent the inputs for the fuzzing operation for String.
   *
   * @param operationIndex the index of the fuzzing operation to generate input sequences for
   * @param stringLength the length of the string to be fuzzed
   * @return a list of sequences that represent the inputs for the fuzzing operation
   */
  private static List<Sequence> performFuzzingOperation(int operationIndex, int stringLength) {
    switch (operationIndex) {
      case 0:
        return Collections.singletonList(fuzzInsertCharacter(stringLength));
      case 1:
        return Collections.singletonList(fuzzRemoveCharacter(stringLength));
      case 2:
        return fuzzReplaceCharacter(stringLength);
      case 3:
        return fuzzSelectSubstring(stringLength);
      default:
        throw new IllegalArgumentException("Invalid fuzzing operation index: " + operationIndex);
    }
  }

  /**
   * Fuzzing operation: Insert a random character at a random index in the string.
   *
   * @param stringLength the length of the string to be fuzzed
   * @return a sequence that represents the fuzzing operation
   */
  private static Sequence fuzzInsertCharacter(int stringLength) {
    int randomIndex = Randomness.nextRandomInt(stringLength + 1); // Include stringLength as
    // possible index for insertion at the end.
    char randomChar = (char) (Randomness.nextRandomInt(95) + 32); // ASCII 32-126
    Sequence randomIndexSequence = Sequence.createSequenceForPrimitive(randomIndex);
    Sequence randomCharSequence = Sequence.createSequenceForPrimitive(randomChar);
    return Sequence.concatenate(Arrays.asList(randomIndexSequence, randomCharSequence));
  }

  /**
   * Fuzzing operation: Remove a character at a random index in the string.
   *
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
   *
   * @param stringLength the length of the string to be fuzzed
   * @return a list of sequences that represent the fuzzing operation
   */
  private static List<Sequence> fuzzReplaceCharacter(int stringLength) {
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
   * Fuzzing operation: Select a substring from the string.
   *
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
   *
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

  /** A helper class to store the result of the GRT Impurity component. */
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
