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
import randoop.operation.PlusOperation;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedTermOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Sequence.RelativeNegativeIndex;
import randoop.sequence.Statement;
import randoop.types.NonParameterizedType;
import randoop.types.PrimitiveType;
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
 * <p>[TODO: As described in the GRT paper, "GRT Impurity" also fuzzes non-primitive types by
 * calling side-effect (impure) methods on the objects. We have not implemented this part yet. The
 * current implementation only fuzzes primitive number and String.]
 *
 * <p>For details about fuzzing, see {@link #fuzz}.
 */
@SuppressWarnings("NotJavadoc") // perhaps https://github.com/google/error-prone/issues/3904
public final class GrtFuzzing {

  /** Standard deviation for Gaussian fuzzing of numeric types. */
  private static final double GAUSSIAN_STD = GenInputsAbstract.grt_fuzzing_stddev;

  /** The start of the printable ASCII character range. */
  private static final int PRINTABLE_ASCII_START = 32;

  /** The end of the printable ASCII character range. */
  private static final int PRINTABLE_ASCII_RANGE = 95;

  /** Prevent instantiation. */
  private GrtFuzzing() {
    throw new AssertionError("No instances");
  }

  /**
   * Generate an extended fuzzed sequence for the given sequence.
   *
   * <p>For primitive number outputs, appends a Gaussian sample and a plus operation. For String
   * outputs, applies one of several StringBuilder transformations. Returns the original sequence if
   * the output is void, boolean, or no valid fuzz is found.
   *
   * @param sequence the (non-null, non-empty) sequence to fuzz
   * @return a new sequence with fuzzing statements appended or the original sequence
   */
  public static Sequence fuzz(Sequence sequence) {
    Type outType = sequence.getLastVariable().getType();
    Class<?> cls = outType.getRuntimeClass();
    if (cls == void.class || cls == boolean.class || cls == Boolean.class) {
      return sequence;
    }
    try {
      if (cls.isPrimitive()) {
        return fuzzNumber(sequence, cls);
      } else if (cls == String.class) {
        return fuzzString(sequence);
      }
    } catch (Exception e) {
      throw new RandoopBug("GRT Fuzzing failed: " + e.getMessage(), e);
    }
    return sequence;
  }

  /**
   * Fuzz a sequence producing a primitive or boxed numeric value. Appends: last statement, a
   * Gaussian sample, and a plus operation on them.
   *
   * @param sequence the sequence to fuzz
   * @param cls the numeric class type of the sequence's output
   * @return a new sequence with fuzzing statements appended
   * @throws Exception if reflection or sequence creation fails
   */
  private static Sequence fuzzNumber(Sequence sequence, Class<?> cls) throws Exception {
    Statement lastStmt = sequence.getStatement(sequence.size() - 1);
    Statement gaussianStmt =
        Sequence.createSequenceForPrimitive(sampleGaussian(cls)).getStatement(0);
    Statement plusStmt = createPlusStatement(cls);
    List<Statement> stmts = new ArrayList<>();
    // Workaround: duplicate lastStmt to avoid costly repeated concatenations.
    // Randoop subsequences can only reference values they themselves create.
    // Re‑emitting lastStmt lets us compute “orig + gaussian” at indices (‑2, ‑1)
    // in one Sequence.concatenate call rather than many.
    stmts.add(lastStmt);
    stmts.add(gaussianStmt);
    stmts.add(plusStmt);
    return Sequence.concatenate(sequence, new Sequence(new SimpleArrayList<>(stmts)));
  }

  /**
   * Fuzz a sequence producing a String. Randomly selects an INSERT, REMOVE, REPLACE, or SUBSTRING
   * operation. Returns the original sequence if the string is empty and no INSERT.
   *
   * @param sequence the sequence to fuzz
   * @return a new sequence with fuzzing statements appended
   * @throws Exception if reflection or sequence creation fails
   */
  private static Sequence fuzzString(Sequence sequence) throws Exception {
    StringFuzzingOperation op = StringFuzzingOperation.random();
    String str = getStringValue(sequence);
    if (str.isEmpty() && op != StringFuzzingOperation.INSERT) {
      return sequence;
    }
    List<Statement> stmts = new ArrayList<>(createStringBuilderStatements(str));
    Sequence inputs = op.getInputs(str.length());
    stmts.addAll(inputs.statements.toJDKList());
    stmts.addAll(convertFuzzingExecutablesToStatements(op.getTransformMethods()));
    return Sequence.concatenate(sequence, new Sequence(new SimpleArrayList<>(stmts)));
  }

  /**
   * Create a plus operation statement for the given numeric class.
   *
   * @param cls the numeric class
   * @return a Statement applying the plus operation to the two preceding values
   * @throws NoSuchMethodException never (PlusOperation is no-reflection)
   */
  private static Statement createPlusStatement(Class<?> cls) throws NoSuchMethodException {
    TypedTermOperation plusOp =
        new TypedTermOperation(
            new PlusOperation(),
            new TypeTuple(Arrays.asList(PrimitiveType.forClass(cls), PrimitiveType.forClass(cls))),
            PrimitiveType.forClass(cls));
    List<RelativeNegativeIndex> indices = new ArrayList<>();
    indices.add(new RelativeNegativeIndex(-2));
    indices.add(new RelativeNegativeIndex(-1));
    return new Statement(plusOp, indices);
  }

  /**
   * Generate a random Gaussian sample of the given numeric type.
   *
   * @param cls the numeric class
   * @return a boxed primitive value sampled from N(0, GAUSSIAN_STD)
   */
  private static Object sampleGaussian(Class<?> cls) {
    double g = GAUSSIAN_STD * Randomness.nextRandomGaussian(1);
    if (cls == byte.class || cls == Byte.class) return (byte) Math.round(g);
    if (cls == short.class || cls == Short.class) return (short) Math.round(g);
    if (cls == char.class || cls == Character.class) return (char) Math.round(g);
    if (cls == int.class || cls == Integer.class) return (int) Math.round(g);
    if (cls == long.class || cls == Long.class) return Math.round(g);
    if (cls == float.class || cls == Float.class) return (float) g;
    return g;
  }

  /**
   * Construct a StringBuilder initialized with the given string.
   *
   * @param s the initial string
   * @return the list of statements constructing StringBuilder(s)
   * @throws NoSuchMethodException if the StringBuilder constructor is missing
   */
  private static List<Statement> createStringBuilderStatements(String s)
      throws NoSuchMethodException {
    Sequence strSeq = Sequence.createSequenceForPrimitive(s);
    Constructor<?> ctor = StringBuilder.class.getConstructor(String.class);
    TypedClassOperation op =
        new TypedClassOperation(
            new ConstructorCall(ctor),
            new NonParameterizedType(StringBuilder.class),
            new TypeTuple(Collections.<Type>singletonList(Type.forClass(String.class))),
            Type.forClass(StringBuilder.class));
    List<Integer> idxs = Collections.singletonList(0);
    Sequence sbSeq = Sequence.createSequence(op, Collections.singletonList(strSeq), idxs);
    return sbSeq.statements.toJDKList();
  }

  /**
   * Converts a list of Executable operations into Randoop statements, using relative indices.
   *
   * @param fuzzingOperations the operations to convert
   * @return the resulting list of statements
   */
  private static List<Statement> convertFuzzingExecutablesToStatements(
      List<Executable> fuzzingOperations) {
    List<Statement> statements = new ArrayList<>();
    for (Executable executable : fuzzingOperations) {
      TypedClassOperation typedOp = createTypedOperation(executable);
      int numInputs =
          getInputTypes(executable, new NonParameterizedType(executable.getDeclaringClass()))
              .size();
      List<RelativeNegativeIndex> indices = createRelativeNegativeIndices(numInputs);
      statements.add(new Statement(typedOp, indices));
    }
    return statements;
  }

  /**
   * Create a TypedClassOperation for the given Executable (method or constructor).
   *
   * @param executable the reflective executable
   * @return a TypedClassOperation wrapping it
   */
  private static TypedClassOperation createTypedOperation(Executable executable) {
    CallableOperation callable =
        (executable instanceof Method)
            ? new MethodCall((Method) executable)
            : new ConstructorCall((Constructor<?>) executable);
    NonParameterizedType decl = new NonParameterizedType(executable.getDeclaringClass());
    List<Type> inputs = getInputTypes(executable, decl);
    Type output =
        executable instanceof Constructor
            ? decl
            : Type.forType(((Method) executable).getGenericReturnType());
    return new TypedClassOperation(callable, decl, new TypeTuple(inputs), output);
  }

  /**
   * Get parameter types (including receiver for instance methods) for an Executable.
   *
   * @param executable the reflective executable
   * @param declaringType the Randoop type of its declaring class
   * @return a list of Randoop Types for inputs
   */
  private static List<Type> getInputTypes(
      Executable executable, NonParameterizedType declaringType) {
    List<Type> types = new ArrayList<>();
    if (!Modifier.isStatic(executable.getModifiers()) && executable instanceof Method) {
      types.add(declaringType);
    }
    for (Class<?> p : executable.getParameterTypes()) {
      types.add(Type.forClass(p));
    }
    return types;
  }

  /**
   * Create a sequence of RelativeNegativeIndex from -size to -1.
   *
   * @param size number of indices
   * @return the list of indices
   */
  private static List<RelativeNegativeIndex> createRelativeNegativeIndices(int size) {
    List<RelativeNegativeIndex> indices = new ArrayList<>();
    for (int i = -size; i < 0; i++) {
      indices.add(new RelativeNegativeIndex(i));
    }
    return indices;
  }

  /**
   * Retrieve the String value from the last statement in the sequence.
   *
   * <p>Note: This method assumes that the last statement is a String-producing statement. If the
   * last statement does not produce a String, an IllegalArgumentException is thrown.
   *
   * @param seq the sequence
   * @return the produced String
   * @throws IllegalArgumentException if the last value is not a String
   */
  private static String getStringValue(Sequence seq) {
    Object v = getLastStatement(seq).getValue();
    if (!(v instanceof String)) {
      throw new IllegalArgumentException("Last statement did not produce a String");
    }
    return (String) v;
  }

  /**
   * Retrieve the last Statement of a sequence. The statement contains the value to be fuzzed.
   *
   * <p>We assume the sequence is not empty.
   *
   * @param sequence the sequence
   * @return the last statement
   */
  private static Statement getLastStatement(Sequence sequence) {
    return sequence.getStatement(sequence.size() - 1);
  }

  /**
   * An enum representing the fuzzing operations for Strings. Each set of fuzzing operations has a
   * corresponding method to be invoked on a StringBuilder object. Each run of GRT Fuzzing will
   * randomly select one of these set of operations to perform on the input String.
   *
   * <p>Assumptions:
   *
   * <ul>
   *   <li>Each {@code getStringBuilderTransform()} list contains methods to be invoked sequentially
   *       on a StringBuilder object.
   *   <li>Among these methods, only one method accepts arguments, which are exactly the arguments
   *       generated by {@code getInputs()}.
   *   <li>The remaining methods (if any) in the {@code getStringBuilderTransform()} list take no
   *       arguments (other than the receiver, which is the StringBuilder instance).
   * </ul>
   */
  private enum StringFuzzingOperation {
    /** Insert a random character at a random index in the string. */
    INSERT("insert", int.class, char.class),

    /** Remove a character at a random index in the string. */
    REMOVE("deleteCharAt", int.class),

    /** Replace a random substring with a random character. */
    REPLACE("replace", int.class, int.class, String.class),

    /** Create a substring with random start and end indices of the string. */
    SUBSTRING("substring", int.class, int.class);

    /** The list of methods to be invoked on a StringBuilder object. */
    private final List<Executable> transformMethods;

    /** The list of all StringFuzzingOperation values. */
    private static final StringFuzzingOperation[] VALUES = values();

    /**
     * Constructor for StringFuzzingOperation.
     *
     * @param methodName the name of the method to be invoked on StringBuilder
     * @param paramTypes the parameter types of the method
     */
    StringFuzzingOperation(String methodName, Class<?>... paramTypes) {
      try {
        Method m = StringBuilder.class.getMethod(methodName, paramTypes);
        if (methodName.equals("substring")) {
          // only substring()
          this.transformMethods = List.of(m);
        } else {
          // method + toString()
          Method toStringM = StringBuilder.class.getMethod("toString");
          this.transformMethods = List.of(m, toStringM);
        }
      } catch (NoSuchMethodException e) {
        throw new AssertionError("StringBuilder method missing: " + e);
      }
    }

    /**
     * Randomly select one of the StringFuzzingOperation values.
     *
     * @return a random StringFuzzingOperation
     */
    static StringFuzzingOperation random() {
      return VALUES[Randomness.nextRandomInt(VALUES.length)];
    }

    /** Generate the argument sequence for this fuzz operation on a string of given length. */
    Sequence getInputs(int length) {
      if (this == INSERT) {
        int idx = Randomness.nextRandomInt(length + 1);
        char c = (char) (Randomness.nextRandomInt(PRINTABLE_ASCII_RANGE) + PRINTABLE_ASCII_START);
        return Sequence.concatenate(
            Sequence.createSequenceForPrimitive(idx), Sequence.createSequenceForPrimitive(c));
      } else if (this == REMOVE) {
        return Sequence.createSequenceForPrimitive(Randomness.nextRandomInt(length));
      } else if (this == REPLACE) {
        int i1 = Randomness.nextRandomInt(length);
        int i2 = Randomness.nextRandomInt(length);
        int start = Math.min(i1, i2);
        int end = Math.max(i1, i2);
        String r =
            String.valueOf(
                (char) (Randomness.nextRandomInt(PRINTABLE_ASCII_RANGE) + PRINTABLE_ASCII_START));
        return Sequence.concatenate(
            Sequence.createSequenceForPrimitive(start),
            Sequence.createSequenceForPrimitive(end),
            Sequence.createSequenceForPrimitive(r));
      } else { // SUBSTRING
        int i1 = Randomness.nextRandomInt(length);
        int i2 = Randomness.nextRandomInt(length);
        int start = Math.min(i1, i2);
        int end = Math.max(i1, i2);
        return Sequence.concatenate(
            Sequence.createSequenceForPrimitive(start), Sequence.createSequenceForPrimitive(end));
      }
    }

    /** Return the list of StringBuilder methods to invoke for this operation. */
    List<Executable> getTransformMethods() {
      return transformMethods;
    }
  }
}
