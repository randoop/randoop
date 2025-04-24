package randoop.generation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.operation.PlusOperation;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
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

  /** The starting ASCII value for printable characters. */
  private static final int PRINTABLE_ASCII_START = 32;

  /** The end of the printable ASCII character range. */
  private static final int PRINTABLE_ASCII_RANGE = 95;

  /** Prevent instantiation. */
  private GrtFuzzing() {
    throw new AssertionError("No instances");
  }

  /**
   * A cache mapping each size of {@link RelativeNegativeIndex} list to its corresponding
   * unmodifiable list.
   *
   * <p>Used to avoid repeated creation of identical lists of indices for the same size.
   */
  private static final Map<Integer, List<RelativeNegativeIndex>> INDEX_CACHE = new HashMap<>();

  /**
   * A shared instance of {@link PlusOperation} used for all numeric addition statements.
   *
   * <p>Since {@code PlusOperation} is stateless and identical for all uses, a single instance can
   * be reused across all primitive types to avoid unnecessary allocation.
   */
  private static final PlusOperation PLUS_OP = new PlusOperation();

  /**
   * A cache mapping each primitive numeric type to its corresponding {@code Statement} that
   * performs a {@link PlusOperation}.
   *
   * <p>Used to avoid repeated creation of identical addition operations (e.g., {@code int + int
   * -&gt; int}, {@code float + float -&gt; float}, etc.) across fuzzing invocations.
   */
  private static final Map<Class<?>, Statement> PLUS_STMTS = new HashMap<>();

  static {
    for (Class<?> cls :
        Arrays.asList(
            byte.class,
            short.class,
            char.class,
            int.class,
            long.class,
            float.class,
            double.class)) {
      PLUS_STMTS.put(cls, createPlusStatement(cls));
    }
  }

  /**
   * The constructor operation for {@link StringBuilder} used to create a new instance.
   *
   * <p>This is a shared instance, as the constructor is stateless and identical for all uses.
   */
  private static final TypedClassOperation SB_CTOR_OP;

  static {
    try {
      Constructor<?> ctor = StringBuilder.class.getConstructor(String.class);
      SB_CTOR_OP = TypedOperation.forConstructor(ctor);
    } catch (NoSuchMethodException e) {
      throw new AssertionError("StringBuilder constructor missing: " + e);
    }
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
   * Fuzz a numeric sequence by appending: 1) a literal of the last output, 2) a Gaussian‐sample
   * constant, 3) and the shared plus operation that adds them.
   *
   * @param sequence the executed sequence whose last value we’ll duplicate
   * @param cls the numeric class of the sequence’s output
   * @return a new sequence with the three fuzzing statements appended
   * @throws Exception if any sequence‐creation step fails
   */
  private static Sequence fuzzNumber(Sequence sequence, Class<?> cls) throws Exception {
    // Workaround: Emit lastStmt’s value as a literal to avoid extra concatenations to the sequence.
    Statement lastStmt = sequence.getStatement(sequence.size() - 1);

    // 1) Emit the last output as a literal to avoid reusing a potentially impure or
    // non-deterministic operation.
    Statement valueStmt = Sequence.createSequenceForPrimitive(lastStmt.getValue()).getStatement(0);

    // 2) Gaussian constant
    Statement gaussianStmt =
        Sequence.createSequenceForPrimitive(sampleGaussian(cls)).getStatement(0);

    // 3) Shared plus‐operation statement expecting the two preceding values
    Statement plusStmt = PLUS_STMTS.get(cls);

    // 4) Concatenate onto the original sequence
    return Sequence.concatenate(
        sequence,
        new Sequence(new SimpleArrayList<>(Arrays.asList(valueStmt, gaussianStmt, plusStmt))));
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
   * Creates a {@link Statement} representing {@code x + y} for the given primitive type.
   *
   * <p>Both input operands and the result type are set to {@code cls}, and the statement is
   * constructed with argument indices {@code -2} and {@code -1}, assuming the two operands
   * immediately precede the statement in a sequence.
   *
   * @param cls the primitive numeric type (e.g., {@code int.class})
   * @return a {@code Statement} performing the addition
   */
  private static Statement createPlusStatement(Class<?> cls) {
    TypedTermOperation plusOp =
        new TypedTermOperation(
            PLUS_OP,
            new TypeTuple(Arrays.asList(PrimitiveType.forClass(cls), PrimitiveType.forClass(cls))),
            PrimitiveType.forClass(cls));
    // The two operands are the last two values in the sequence. Indices are -2 and -1.
    List<RelativeNegativeIndex> indices = getRelativeNegativeIndices(2);
    return new Statement(plusOp, indices);
  }

  /**
   * Generate a random Gaussian sample of the given numeric type. The value is sampled from a normal
   * distribution with mean 0 and standard deviation {@link #GAUSSIAN_STD}. The value is rounded to
   * the nearest integer for integer types.
   *
   * @param cls the numeric class
   * @return a boxed primitive value sampled from N(0, GAUSSIAN_STD)
   */
  private static Object sampleGaussian(Class<?> cls) {
    double g = Randomness.nextRandomGaussian(0, GAUSSIAN_STD);
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
    List<Integer> idxs = Collections.singletonList(0);
    Sequence sbSeq = Sequence.createSequence(SB_CTOR_OP, Collections.singletonList(strSeq), idxs);
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
      TypedClassOperation typedOp;
      if (executable instanceof Method) {
        typedOp = TypedOperation.forMethod((Method) executable);
      } else {
        typedOp = TypedOperation.forConstructor((Constructor<?>) executable);
      }
      int numInputs =
          getInputTypes(executable, new NonParameterizedType(executable.getDeclaringClass()))
              .size();
      List<RelativeNegativeIndex> indices = getRelativeNegativeIndices(numInputs);
      statements.add(new Statement(typedOp, indices));
    }
    return statements;
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
   * Get a sequence of RelativeNegativeIndex from -size to -1. If the size is already cached, return
   * the cached version. Otherwise, create a new one and cache it.
   *
   * @param size number of indices
   * @return the list of indices
   */
  private static List<RelativeNegativeIndex> getRelativeNegativeIndices(int size) {
    return INDEX_CACHE.computeIfAbsent(
        size,
        s -> {
          List<RelativeNegativeIndex> list = new ArrayList<>(s);
          for (int i = -s; i < 0; i++) list.add(new RelativeNegativeIndex(i));
          return Collections.unmodifiableList(list);
        });
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
   *   <li>Each {@code getTransformMethods()} list contains methods to be invoked sequentially on a
   *       StringBuilder object.
   *   <li>Among these methods, exactly one method accepts arguments, which are the arguments
   *       generated by {@code getInputs()}.
   *   <li>The remaining methods (if any) in the {@code getTransformMethods()} list take no
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
          this.transformMethods = Arrays.asList(m);
        } else {
          // method + toString()
          Method toStringM = StringBuilder.class.getMethod("toString");
          this.transformMethods = Arrays.asList(m, toStringM);
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
