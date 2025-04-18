package randoop.generation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
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
import randoop.sequence.Sequence.RelativeNegativeIndex;
import randoop.sequence.Statement;
import randoop.types.NonParameterizedType;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;

/**
 * Implements the "GRT Impurity" component, as described in "GRT: Program-Analysis-Guided Random
 * Testing" by Ma et. al (ASE 2015): <a
 * href="https://people.kth.se/~artho/papers/lei-ase2015.pdf">...</a>.
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
   * The index where a primitive 0 is inserted into the fuzzing sequence. This 0 is required during
   * fuzzing to supply the second argument for Array.getChar(Object, int) since only the first
   * argument (resulting from Character.toChars) is generated by default. Expected sequence order:
   * 0–2: [omitted] 3: result of Character.toChars (using statement at index 2) 4: primitive 0 (this
   * value) 5: result of Array.getChar (using statements at indices 3 and 4)
   */
  private static int SECOND_ARGUMENT_INDEX = 4;

  /**
   * An enum representing the fuzzing operations for Strings. Each set of fuzzing operations has a
   * corresponding method to be invoked on a StringBuilder object. Each run of GRT Fuzzing will
   * randomly select one of these set of operations to perform on the input String.
   *
   * <p>Assumptions:
   *
   * <ul>
   *   <li>Each `getStringBuilderTransform()` list contains methods to be invoked sequentially on a
   *       StringBuilder object.
   *   <li>Among these methods, only one method accepts arguments, which are exactly the arguments
   *       generated by `getInputs()`.
   *   <li>The remaining methods in the `getStringBuilderTransform()` list take no arguments (other
   *       than the receiver, which is the StringBuilder instance).
   * </ul>
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
      List<Executable> getStringBuilderTransform() throws NoSuchMethodException {
        return Arrays.asList(
            StringBuilder.class.getMethod("insert", int.class, char.class),
            StringBuilder.class.getMethod("toString"));
      }
    },

    /** Remove a character at a random index in the string. */
    REMOVE {
      @Override
      Sequence getInputs(int stringLength) {
        int randomIndex = Randomness.nextRandomInt(stringLength);
        return Sequence.createSequenceForPrimitive(randomIndex);
      }

      @Override
      List<Executable> getStringBuilderTransform() throws NoSuchMethodException {
        return Arrays.asList(
            StringBuilder.class.getMethod("deleteCharAt", int.class),
            StringBuilder.class.getMethod("toString"));
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
      List<Executable> getStringBuilderTransform() throws NoSuchMethodException {
        return Arrays.asList(
            StringBuilder.class.getMethod("replace", int.class, int.class, String.class),
            StringBuilder.class.getMethod("toString"));
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
      List<Executable> getStringBuilderTransform() throws NoSuchMethodException {
        return Arrays.asList(StringBuilder.class.getMethod("substring", int.class, int.class));
      }
    };

    /** Get the randomly generated inputs for the operation (method that accepts arguments). */
    abstract Sequence getInputs(int stringLength);

    /** Get the methods to be invoked on a StringBuilder object that correspond to the operation. */
    abstract List<Executable> getStringBuilderTransform() throws NoSuchMethodException;
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
   * @param sequence the (non-null, non-empty) sequence to fuzz
   * @return a sequence consisting of {@code sequence} with additional fuzzing statements appended
   *     at the end. The original sequence is returned if no valid fuzzing operation is found.
   */
  public static Sequence fuzz(Sequence sequence) {
    // The number of fuzzing statements added to the sequence.
    Type outputType = sequence.getLastVariable().getType();

    Class<?> outputClass = outputType.getRuntimeClass();

    // Do not fuzz void and boolean.
    if (outputClass.equals(void.class)
        || outputClass.equals(boolean.class)
        || outputClass.equals(Boolean.class)) {
      return sequence;
    }

    Sequence output;
    // Fuzz the sequence based on the output class.
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
    } catch (Exception e) { // All exceptions are unexpected
      throw new RandoopBug("GRT Fuzzing failed: " + e.getMessage(), e);
    }

    return output;
  }

  /**
   * Fuzz a sequence producing a primitive/boxed number.
   *
   * @param sequence the sequence to fuzz
   * @param outputClass the class of the output
   * @return a sequence with the fuzzing statements appended at the end
   * @throws NoSuchMethodException if a method required for fuzzing is not found
   */
  private static Sequence fuzzNumberSequence(Sequence sequence, Class<?> outputClass)
      throws NoSuchMethodException {
    // Create a Gaussian sample statement for the given class.
    Statement gaussianStatement = createGaussianStatement(outputClass);

    // Create a list of statements for the fuzzing operation.
    // [Optimization]
    // Include the last statement of the original sequence (the number to be fuzzed) in addition to
    // the Gaussian sample statement to ensure that the fuzzing operation will have all required
    // inputs. This approach allows direct concatenation to the original sequence without repeatedly
    // extending it with fuzzing operations (inefficient) as sequence can validly be created from
    // the list of statements without missing any input.
    List<Statement> fuzzingOperationStatements =
        new ArrayList<>(
            Arrays.asList(sequence.getStatement(sequence.size() - 1), gaussianStatement));

    // Get the fuzzing operation methods for the given class and convert them to statements.
    List<Executable> fuzzingOperationMethods = getNumberFuzzingMethods(outputClass);
    fuzzingOperationStatements.addAll(fuzzingOperationsToStatements(fuzzingOperationMethods));

    // If the output class is char or Character, add a primitive 0 statement
    // needed for the second argument of java.lang.reflect.Array.getChar(Object, int) to the list.
    if (outputClass.equals(char.class) || outputClass.equals(Character.class)) {
      fuzzingOperationStatements.add(
          SECOND_ARGUMENT_INDEX, Sequence.createSequenceForPrimitive(0).getStatement(0));
    }

    return Sequence.concatenate(sequence, createSequenceFromStatements(fuzzingOperationStatements));
  }

  /**
   * Fuzz a sequence producing a String.
   *
   * @param sequence the sequence to fuzz
   * @return a sequence with the fuzzing statement appended at the end
   * @throws NoSuchMethodException if a method required for fuzzing is not found
   */
  private static Sequence fuzzStringSequence(Sequence sequence) throws NoSuchMethodException {
    // Randomly select a set of fuzzing operations for String.
    StringFuzzingOperation operation =
        StringFuzzingOperation.values()[
            Randomness.nextRandomInt(StringFuzzingOperation.values().length)];

    // Create the inputs for the methods used in the fuzzing operation.
    List<Statement> stringFuzzingInputStatements =
        createStringFuzzingInputStatements(sequence, operation);

    // If the sequence cannot be fuzzed, return the original sequence.
    if (stringFuzzingInputStatements == null) {
      return sequence;
    }

    // Get the fuzzing operation methods for the given operation and convert them to statements.
    List<Executable> fuzzingOperationMethods = operation.getStringBuilderTransform();

    List<Statement> fuzzingOperationStatements = new ArrayList<>(stringFuzzingInputStatements);
    fuzzingOperationStatements.addAll(fuzzingOperationsToStatements(fuzzingOperationMethods));

    return Sequence.concatenate(sequence, createSequenceFromStatements(fuzzingOperationStatements));
  }

  /**
   * Create a sequence from a list of statements. It is assumed that the list of statements {@code
   * statements} is valid and can be used to create a sequence (i.e., input statements are present
   * for all operations).
   *
   * @param statements the list of statements to create the sequence from
   * @return a sequence created from the given list of statements
   */
  private static Sequence createSequenceFromStatements(List<Statement> statements) {
    return new Sequence(new SimpleArrayList<>(statements));
  }

  /**
   * Converts a list of fuzzing operation executables to a list of statements.
   *
   * <p>In most cases, the input for any resulting statement is the last {@code
   * fuzzingOperation.getInputTypes().size()} statements relative to the statement itself.
   *
   * <p>For example, consider the following list of statements:
   *
   * <pre>
   * [a, b, c, d]
   * </pre>
   *
   * Suppose statement <code>d</code> represents an operation that is a non-static method and has 2
   * parameters, then the last 1 (receiver) + 2 (parameters) = 3 statements are mapped to the input
   * of <code>d</code> through {@link randoop.sequence.Sequence.RelativeNegativeIndex} with indices
   * -1 (output of c), -2 (output of b), and -3 (output of a). The last statement would look like:
   * <code>
   * d = a.m(b, c)</code> for some method <code>m</code> that <code>d</code> represents.
   *
   * <p>This assumption holds for all list of statements created this way, except for the char and
   * Character fuzzing operations. For these operations, the list of statements is modified to
   * include an additional statement needed after call to this method. See {@link
   * #fuzzNumberSequence(Sequence, Class)} for more details.
   *
   * @param fuzzingOperations the list of fuzzing operation executables to convert to statements
   * @return a list of statements representing the fuzzing operation executables
   */
  private static List<Statement> fuzzingOperationsToStatements(List<Executable> fuzzingOperations) {
    List<Statement> statements = new ArrayList<>();
    for (Executable executable : fuzzingOperations) {
      TypedOperation typedOperation = createTypedOperation(executable);
      List<RelativeNegativeIndex> indices =
          createRelativeNegativeIndices(
              getInputTypes(executable, new NonParameterizedType(executable.getDeclaringClass()))
                  .size());
      statements.add(new Statement(typedOperation, indices));
    }
    return statements;
  }

  /**
   * Create a TypedOperation from the given executable.
   *
   * @param executable the executable to create the TypedOperation from
   * @return a TypedOperation created from the given executable
   */
  private static TypedOperation createTypedOperation(Executable executable) {
    CallableOperation callableOperation = createCallableOperation(executable);
    NonParameterizedType declaringType = new NonParameterizedType(executable.getDeclaringClass());
    List<Type> inputTypeList = getInputTypes(executable, declaringType);
    Type outputType = getOutputType(executable);
    TypeTuple inputType = new TypeTuple(inputTypeList);
    return new TypedClassOperation(callableOperation, declaringType, inputType, outputType);
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
   * Returns the output type of the given executable.
   *
   * @param executable the executable to get the output type of
   * @return the output type of the given executable
   */
  private static Type getOutputType(Executable executable) {
    return Type.forType(executable.getAnnotatedReturnType().getType());
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
   * Create a list of RelativeNegativeIndex objects for the given size. This method creates a list
   * of indices from -size to -1 following the assumption made in {@link
   * #fuzzingOperationsToStatements(List)}.
   *
   * @param size the size of the list of RelativeNegativeIndex objects to create
   * @return a list of RelativeNegativeIndex objects from -size to -1
   */
  private static List<RelativeNegativeIndex> createRelativeNegativeIndices(int size) {
    List<RelativeNegativeIndex> indices = new ArrayList<>();
    for (int i = -size; i < 0; i++) {
      indices.add(new RelativeNegativeIndex(i));
    }
    return indices;
  }

  /**
   * Calculate the indices of the input statements in the sequence that will be used for the fuzzing
   * operation.
   *
   * <p>It is assumed that the input statements for the fuzzing operation are the last {@code
   * TypedOperation.getInputTypes().size()} statements in the sequence. The first statement has an
   * index of {@code sequence.size() - TypedOperation.getInputTypes().size()}, the second statement
   * has an index of {@code sequence.size() - TypedOperation.getInputTypes().size() + 1}, and so on.
   *
   * @param sequence the sequence containing the input statements
   * @param operation the fuzzing operation
   * @return a list of indices representing the input statements for the fuzzing operation
   */
  private static List<Integer> calculateInputIndices(Sequence sequence, TypedOperation operation) {
    int sequenceSize = sequence.size();
    int numOfParams = operation.getInputTypes().size();
    List<Integer> inputIndices = new ArrayList<>();
    int firstIndex = sequenceSize - numOfParams;
    for (int i = 0; i < numOfParams; i++) {
      inputIndices.add(firstIndex + i);
    }
    return inputIndices;
  }

  /**
   * Create a statement representing a Gaussian sample value of the given class.
   *
   * @param cls the class of the Gaussian number to be generated
   * @return a statement for a Gaussian sample value of the given class
   */
  private static Statement createGaussianStatement(Class<?> cls) {
    Object gaussianSample = generateGaussianSample(cls);
    return Sequence.createSequenceForPrimitive(gaussianSample).getStatement(0);
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
  // TODO: Use "+" for fuzzing.
  private static List<Executable> getNumberFuzzingMethods(Class<?> cls)
      throws NoSuchMethodException {
    if (cls == int.class || cls == Integer.class) {
      return Collections.singletonList(Integer.class.getMethod("sum", int.class, int.class));
    } else if (cls == long.class || cls == Long.class) {
      return Collections.singletonList(Long.class.getMethod("sum", long.class, long.class));
    } else if (cls == float.class || cls == Float.class) {
      return Collections.singletonList(Float.class.getMethod("sum", float.class, float.class));
    } else if (cls == double.class || cls == Double.class) {
      return Collections.singletonList(Double.class.getMethod("sum", double.class, double.class));
    } else if (cls == byte.class || cls == Byte.class) {
      // Byte doesn't have a sum method, so we use Integer.sum and get the byte value
      return Arrays.asList(
          Integer.class.getMethod("sum", int.class, int.class),
          Integer.class.getMethod("valueOf", int.class),
          Integer.class.getMethod("byteValue"));
    } else if (cls == short.class || cls == Short.class) {
      // Short doesn't have a sum method, so we use Integer.sum and get the short value
      return Arrays.asList(
          Integer.class.getMethod("sum", int.class, int.class),
          Integer.class.getMethod("valueOf", int.class),
          Integer.class.getMethod("shortValue"));
    } else if (cls == char.class || cls == Character.class) {
      // Character doesn't have a sum method, so we use Integer.sum and call toChars
      return Arrays.asList(
          Integer.class.getMethod("sum", int.class, int.class),
          Character.class.getMethod("toChars", int.class),
          java.lang.reflect.Array.class.getMethod("getChar", Object.class, int.class));
    } else {
      throw new IllegalArgumentException("Unexpected primitive type: " + cls.getName());
    }
  }

  /**
   * Create the input statements for the given String fuzzing operation. This method assumes that
   * the sequence to be fuzzed produces a String value.
   *
   * @param sequence the sequence to be fuzzed
   * @param operation the String fuzzing operation to perform
   * @return a list of statements representing the inputs for the fuzzing operation, or null if the
   *     operation cannot be performed on an empty string
   * @throws NoSuchMethodException if a required method for the fuzzing operation is not found
   */
  private static List<Statement> createStringFuzzingInputStatements(
      Sequence sequence, StringFuzzingOperation operation) throws NoSuchMethodException {

    String string = getStringValue(sequence);
    int stringLength = string.length();

    // Return null if the string is empty and the operation is not INSERT
    if (stringLength == 0 && operation != StringFuzzingOperation.INSERT) {
      return null; // Cannot remove/replace/substring an empty string
    }

    List<Statement> fuzzingStatements = createStringBuilderStatements(string);
    Sequence fuzzingInputsSequence = getStringFuzzingMethodInputs(operation, stringLength);
    fuzzingStatements.addAll(fuzzingInputsSequence.statements.toJDKList());

    return fuzzingStatements;
  }

  /**
   * Create a list of statements that construct a StringBuilder object with the given string value.
   *
   * @return a list of statements that construct a StringBuilder object with the given string value
   */
  private static List<Statement> createStringBuilderStatements(String string)
      throws NoSuchMethodException {
    // Create a primitive String sequence
    Sequence stringSequence = Sequence.createSequenceForPrimitive(string);

    // Create the StringBuilder constructor statement
    Constructor<?> stringBuilderConstructor = StringBuilder.class.getConstructor(String.class);
    TypedOperation stringBuilderOperation = createTypedOperation(stringBuilderConstructor);
    List<Integer> inputIndex = calculateInputIndices(stringSequence, stringBuilderOperation);
    Sequence stringBuilderSequence =
        Sequence.createSequence(
            stringBuilderOperation, Collections.singletonList(stringSequence), inputIndex);
    return stringBuilderSequence.statements.toJDKList();
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
   * Get the sequence that creates the inputs (excludes the receiver) for the given fuzzing
   * operation method (INSERT, REMOVE, REPLACE, or SUBSTRING).
   *
   * @param operation the String fuzzing operation to perform
   * @param stringLength the length of the string to be fuzzed, for generating valid random indices
   * @return a list of sequences that represent the inputs for the fuzzing operation
   * @throws IllegalArgumentException if an invalid enum value is passed
   */
  private static Sequence getStringFuzzingMethodInputs(
      StringFuzzingOperation operation, int stringLength) {
    return operation.getInputs(stringLength);
  }
}
