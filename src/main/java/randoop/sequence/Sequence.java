package randoop.sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import randoop.Globals;
import randoop.main.GenInputsAbstract;
import randoop.operation.OperationParseException;
import randoop.operation.OperationParser;
import randoop.operation.TypedOperation;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;
import randoop.types.PrimitiveTypes;
import randoop.util.ArrayListSimpleList;
import randoop.util.ListOfLists;
import randoop.util.OneMoreElementList;
import randoop.util.Randomness;
import randoop.util.SimpleList;
import randoop.util.WeightedElement;

/**
 * Immutable.
 * <p>
 * A sequence of {@link Statement}s. Each element in the sequence represents a
 * particular {@link Statement}, like a method call
 * <code>Foo f = m(i1...iN)</code> or a declaration <code>int x = 0</code>.
 * <p>
 * This class represents only the structure of a well-formed sequence of
 * statements, and does not contain any information about the runtime behavior
 * of the sequence. The class randoop.ExecutableSequence adds functionality that
 * executes the sequence.
 */
public final class Sequence implements WeightedElement {

  public double lastTimeUsed = java.lang.System.currentTimeMillis();

  /** The list of statements. */
  public final SimpleList<Statement> statements;

  /**
   * The variables that are inputs or output for the last statement of this sequence.
   * These hold the values "produced" by some statement of the sequence.
   * Should be final but cannot because of serialization.
   * This info is used by some generators.
   */
  private transient /* final */ List<Variable> lastStatementVariables;

  /**
   * The types of the inputs and output for the last statement of this sequence.
   * Excludes void in the case the output type of the operation of the last statement is void.
   * Should be final but cannot because of serialization.
   * This info is used by some generators.
   */
  private transient /* final */ List<GeneralType> lastStatementTypes;

  /*
   * Weight is used by heuristic that favors smaller sequences so it makes sense
   * to define weight as the inverse of size.
   */
  @Override
  public double getWeight() {
    return 1 / (double) size();
  }

  /**
   * The statement for the statement at the given index.
   *
   * @param index the index of the statement position in this sequence
   * @return the statement at the given position
   */
  public final Statement getStatement(int index) {
    return getStatementWithInputs(index);
  }

  /**
   * The number of statements in the sequence.
   *
   * @return the number of statements in this sequence
   */
  public final int size() {
    return statements.size();
  }

  /**
   * The value created by the ith statement.
   * @param i  the statement index
   * @return the variable created by the statement at the given index
   */
  public Variable getVariable(int i) {
    checkIndex(i);
    return new Variable(this, i);
  }

  /**
   * The variables involved in the last statement. This includes the output
   * variable.
   *
   * @return the variables used in the last statement of this sequence
   */
  List<Variable> getVariablesOfLastStatement() {
    return this.lastStatementVariables;
  }

  /**
   * The types of all the variables involved in the last statement. This
   * includes the output variable. The types returned are not the types in the
   * signature of the Operation, but the types of the variables.
   *
   * @return the types of the variables in the last statement of this sequence
   */
  List<GeneralType> getTypesForLastStatement() {
    return this.lastStatementTypes;
  }

  /**
   * The value created by the last statement in the sequence.
   *
   * @return the variable assigned to by the last statement of this sequence
   */
  public Variable getLastVariable() {
    return new Variable(this, this.statements.size() - 1);
  }

  /**
   * The statement that created this value.
   *
   * @param value  the variable
   * @return the statement that assigned to this variable
   */
  public Statement getCreatingStatement(Variable value) {
    if (value.sequence != this) throw new IllegalArgumentException("value.owner != this");
    return statements.get((value).index);
  }

  /**
   * The inputs for the ith statement. Includes the receiver.
   *
   * @param statementIndex  the index for the statement
   * @return the list of variables for the statement at the given index
   */
  public List<Variable> getInputs(int statementIndex) {
    List<Variable> inputsAsVariables = new ArrayList<>();
    for (RelativeNegativeIndex relIndex : this.statements.get(statementIndex).inputs)
      inputsAsVariables.add(getVariableForInput(statementIndex, relIndex));
    return inputsAsVariables;
  }

  /**
   * Returns the Java source code representation of this sequence. Similar to
   * {@link ExecutableSequence#toCodeString()} except does not include checks.
   *
   * @return a string containing Java code for this sequence
   */
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < size(); i++) {
      // don't dump primitive initializations, if using literals
      if (getStatement(i).getShortForm() != null) {
        continue;
      }
      appendCode(b, i);
    }
    return b.toString();
  }

  @Override
  public String toString() {
    return toParsableString();
  }

  /**
   * A set of bits, where there is one bit associated with each index. Active
   * flags are used during generation, to determine what values in an existing
   * sequence are useful to be used as inputs when creating a new sequence out
   * of the existing one.
   */
  private BitSet activeFlags;

  public boolean hasActiveFlags() {
    return !activeFlags.isEmpty();
  }

  boolean isActive(int i) {
    return activeFlags.get(i);
  }

  private void setAllActiveFlags() {
    activeFlags.set(0, this.size());
  }

  public void clearAllActiveFlags() {
    activeFlags.clear(0, this.size());
  }

  public void clearActiveFlag(int i) {
    activeFlags.clear(i);
  }

  // Used internally (i.e. in package randoop.sequence) to represent inputs
  // to a statement.
  //
  // IMPLEMENTATION NOTE: Recall that a sequence is a sequence
  // of statements where the inputs to a statement are values created
  // by earlier statements. Instead of using a Variable to represent such
  // inputs, we use a RelativeNegativeIndex, which is just a wrapper
  // for an integer. The integer represents a negative offset from the
  // statement index in which this RelativeNegativeIndex lives, and
  // the offset points to the statement that created the values that is
  // used as an input. In other words, a RelativeNegativeIndex says
  // "I represent the value created by the N-th statement above me".
  //
  // For example, the sequence
  //
  // x = new Foo(); Bar b = x.m();
  //
  // is internally represented as follows:
  //
  // first element: Foo() applied to inputs []
  // second element: m() applied to inputs [-1]
  //
  // Here is a brief history of why we use this particular representation.
  //
  // The very first way we represented inputs to a statement was
  // using a list of StatementWithInput objects, i.e. an input was just
  // a reference to a previous statement that created the input value.
  // For example, a sequence might be represented as follows:
  //
  // StatementWithInputs@123: Foo() applied to inputs []
  // StatementWithInputs@124: m() applied to inputs [StatementWithInputs@123]
  //
  // We discovered that a big slowdown in the input generator was that we were
  // consuming lots of memory when creating sequences: for example, in memory,
  // when extending the sequence x = new Foo(); Bar b = x.m(); we cloned each
  // statement, created a new list, added the cloned statements, and finally
  // appended the new statement to the new list.
  //
  // Instead of cloning, we might imagine just using the original statements
  // in the new sequence. This does not work. For example, let's say that we
  // implement this scheme,
  // so that everywhere we need "new Foo()" we use the same statement (more
  // precisely, the same StatementWithInputs). Then, we cannot express
  // Foo f1 = new Foo(); Foo f2 = new Foo(); f1.equals(f2); because its
  // internal representation must be
  //
  // StatementWithInputs@123: Foo() applied to inputs []
  // StatementWithInputs@123: Foo() applied to inputs []
  // StatementWithInputs@125: Object.equals(Object) applied to inputs
  // [StatementWithInputs@123,
  // StatementWithInputs@123]
  //
  // It's clear that if we want to reuse statements, we cannot directly
  // use the statements as inputs.
  //
  // We can instead use indices: 0 represents the value created by the
  // first statement, 1 the second, etc. Now we can express the above
  // sequence:
  //
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [0, 1]
  //
  // This scheme makes it relatively expensive to concatenate sequences
  // (some generators do lots of concatenation, so concatenation is the
  // hotspot). Because indexing is absolute, some statements
  // will need to have their input indices updated. For example, let's say we
  // wanted to concatenate two copies of the
  // last sequence above. We cannot just concatenate the statements, because
  // then we have
  //
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [0, 1]
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [0, 1]
  //
  // While we really want
  //
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [0, 1]
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [3, 4]
  //
  // This means that we need to (1) adjust indices, which takes time, and
  // (2) create new statements that represent the adjusted indices, which
  // breaks the "reuse statements" idea.
  //
  // Relative indices are the current implementation. Instead of representing
  // inputs as indices that start from the beginning of the sequence, a
  // statement's
  // indices are represented by a relative, negative offsets:
  //
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [-2, -1]
  // Foo() applied to inputs []
  // Foo() applied to inputs []
  // Foo() applied to inputs [-2, -1]
  //
  // Now concatenation is easier: to concatenate two sequences, concatenate
  // their statements. Also, we do not need to create any new
  // statements.
  static final class RelativeNegativeIndex {

    public final int index;

    RelativeNegativeIndex(int index) {
      if (index >= 0)
        throw new IllegalArgumentException("invalid index (expecting non-positive): " + index);
      this.index = index;
    }

    @Override
    public String toString() {
      return Integer.toString(index);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof RelativeNegativeIndex && this.index == ((RelativeNegativeIndex) o).index;
    }

    @Override
    public int hashCode() {
      return this.index;
    }
  }

  /**
   * Returns the relative negative index that would result if we use the given
   * value as an input to the statement at position statementPosition.
   * @param statementPosition  the position of the statement
   * @param v  the variable
   * @return the relative negative index computed from the position and variable
   */
  private static RelativeNegativeIndex getRelativeIndexForVariable(
      int statementPosition, Variable v) {
    if (v.index >= statementPosition) throw new IllegalArgumentException();
    return new RelativeNegativeIndex(-(statementPosition - v.index));
  }

  /**
   * Returns the Variable corresponding to the given input, which is an input to
   * the statement at position statementPosition.
   * @param statementPosition  the statement position
   * @param input  relative index of the input variable
   * @return the variable at the relative index from the given statement position
   */
  private Variable getVariableForInput(int statementPosition, RelativeNegativeIndex input) {
    int absoluteIndex = statementPosition + input.index;
    if (absoluteIndex < 0) {
      throw new IllegalArgumentException(
          "invalid index (expecting non-negative): " + absoluteIndex);
    }
    return new Variable(this, absoluteIndex);
  }

  /**
   * Create a new, empty sequence.
   */
  public Sequence() {
    this(new ArrayListSimpleList<Statement>(), 0, 0);
  }

  /**
   * Returns a sequence that is of the form "Foo f = null;" where Foo is the
   * given class.
   * @param c  the type for initialized variable
   * @return the sequence consisting of the initialization
   */
  public static Sequence zero(GeneralType c) {
    return new Sequence()
        .extend(TypedOperation.createNullOrZeroInitializationForType(c), new ArrayList<Variable>());
  }

  /**
   * Create a sequence with the given statements.
   *
   * @param statements  the statements
   */
  public Sequence(SimpleList<Statement> statements) {
    this(statements, computeHashcode(statements), computeNetSize(statements));
  }

  // The hashcode of a sequence is the sum of each statement's hashcode.
  // This seems good enough, and it makes computing hashCode of a
  // concatenation of sequences faster (it's just the addition of each
  // sequence's'
  // hashCode). Otherwise, hashCode computation used to be a hotspot.
  private static int computeHashcode(SimpleList<Statement> statements) {
    int hashCode = 0;
    for (int i = 0; i < statements.size(); i++) {
      Statement s = statements.get(i);
      hashCode += s.hashCode();
    }
    return hashCode;
  }

  /**
   * Counts the number of statements in a list that are not initializations with
   * a primitive type. For instance <code>int var7 = 0</code>.
   *
   * @param statements
   *          the list of {@link Statement} objects
   * @return count of statements other than primitive initializations
   */
  private static int computeNetSize(SimpleList<Statement> statements) {
    int netSize = 0;
    for (int i = 0; i < statements.size(); i++) {
      if (!(statements.get(i).isPrimitiveInitialization())) {
        netSize++;
      }
    }
    return netSize;
  }

  // Create a sequence that has the given statements and hashCode (hashCode is
  // for optimization).
  //
  // See comment at computeHashCode method for notes on hashCode.
  private Sequence(SimpleList<Statement> statements, int hashCode, int netSize) {
    if (statements == null)
      throw new IllegalArgumentException("`statements' argument cannot be null");
    this.statements = statements;
    this.savedHashCode = hashCode;
    this.savedNetSize = netSize;
    computeLastStatementInfo();
    this.activeFlags = new BitSet(this.size());
    setAllActiveFlags();
    checkRep();
  }

  // Set lastStatementVariables and lastStatementTypes to their appropriate
  // values. See documentation for these fields for more info.
  private void computeLastStatementInfo() {
    this.lastStatementTypes = new ArrayList<>();
    this.lastStatementVariables = new ArrayList<>();

    if (!this.statements.isEmpty()) {
      int lastStatementIndex = this.statements.size() - 1;
      Statement lastStatement = this.statements.get(lastStatementIndex);

      // Process return value
      if (!lastStatement.getOutputType().isVoid()) {
        lastStatementTypes.add(lastStatement.getOutputType());
        lastStatementVariables.add(new Variable(this, lastStatementIndex));
      }

      // Process input arguments.
      if (lastStatement.inputs.size() != lastStatement.getInputTypes().size())
        throw new RuntimeException(
            lastStatement.inputs
                + ", "
                + lastStatement.getInputTypes()
                + ", "
                + lastStatement.toString());

      List<Variable> v = this.getInputs(lastStatementIndex);
      if (v.size() != lastStatement.getInputTypes().size()) throw new RuntimeException();

      for (int i = 0; i < v.size(); i++) {
        Variable actualArgument = v.get(i);
        assert lastStatement.getInputTypes().get(i).isAssignableFrom(actualArgument.getType());
        lastStatementTypes.add(actualArgument.getType());
        lastStatementVariables.add(actualArgument);
      }
    }
  }

  /**
   * Representation invariant check.
   */
  private void checkRep() {

    if (!GenInputsAbstract.debug_checks) return;

    if (statements == null) throw new RuntimeException("statements == null");

    for (int si = 0; si < this.statements.size(); si++) {

      Statement statementWithInputs = this.statements.get(si);

      // No nulls.
      if (statementWithInputs == null)
        throw new IllegalStateException(
            "Null statement in sequence:" + Globals.lineSep + this.toString());
      if (statementWithInputs.inputs == null)
        throw new IllegalArgumentException("parameters cannot be null.");

      // The inputs to the statement are valid: there's the right number
      // of them,
      // and they refer to appropriate input values.
      if (statementWithInputs.getInputTypes().size() != statementWithInputs.inputs.size())
        throw new IllegalArgumentException(
            "statement.getInputConstraints().size()="
                + statementWithInputs.getInputTypes().size()
                + " is different from inputIndices.length="
                + statementWithInputs.inputs.size()
                + ", sequence: "
                + this.toString());
      for (int i = 0; i < statementWithInputs.inputs.size(); i++) {
        int index = statementWithInputs.inputs.get(i).index;
        if (index >= 0) throw new IllegalStateException();
        GeneralType newRefConstraint =
            statements.get(si + statementWithInputs.inputs.get(i).index).getOutputType();
        if (newRefConstraint == null) throw new IllegalStateException();
        if (!(statementWithInputs.getInputTypes().get(i).isAssignableFrom(newRefConstraint)))
          throw new IllegalArgumentException(
              i
                  + "th input constraint "
                  + newRefConstraint
                  + " does not imply "
                  + "statement's "
                  + i
                  + "th input constraint "
                  + statementWithInputs.getInputTypes().get(i)
                  + Globals.lineSep
                  + ".Sequence:"
                  + Globals.lineSep
                  + this.toString());
      }
    }
  }

  /**
   * Two sequences are equal if their statements(+inputs) are element-wise
   * equal.
   */
  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof Sequence)) return false;
    if (o == this) return true;
    Sequence other = (Sequence) o;
    if (this.getStatementsWithInputs().size() != other.getStatementsWithInputs().size())
      return GenInputsAbstract.debug_checks && verifyFalse("size", other);
    for (int i = 0; i < this.statements.size(); i++) {
      Statement thisStatement = this.statements.get(i);
      Statement otherStatement = other.statements.get(i);
      if (GenInputsAbstract.debug_checks) {
        assert this.statements.get(i) == thisStatement;
        assert other.statements.get(i) == otherStatement;
      }
      if (!thisStatement.equals(otherStatement)) {
        return GenInputsAbstract.debug_checks && verifyFalse("statement index " + i, other);
      }
    }
    return true;
  }

  // Debugging helper for equals method.
  private boolean verifyFalse(String message, Sequence other) {
    if (this.toString().equals(other.toString()))
      throw new IllegalStateException(message + " : " + this.toString());
    return false;
  }

  // A saved copy of this sequence's hashcode to avoid recalculation.
  private final int savedHashCode;

  // A saved copy of this sequence's net size to avoid recomputation.
  private final int savedNetSize;

  // See comment at computeHashCode method for notes on hashCode.
  @Override
  public final int hashCode() {
    return savedHashCode;
  }

  /**
   * True iff this sequence contains a statement at the given index.
   *
   * @param index  the index to check for a statement
   * @return true if the index is the position of a statement in this sequence, false otherwise
   */
  private boolean isValidIndex(int index) {
    return index >= 0 && index <= this.size() - 1;
  }

  /**
   * An list of all the statements in this sequence.
   * @return  the list of all statements in this sequence
   */
  private SimpleList<Statement> getStatementsWithInputs() {
    // The list is constructed unmodifiable so we can just return it.
    return this.statements;
  }

  /**
   * The statement(+inputs) at the given index.
   *
   * @param index  the statement position
   * @return the {@link Statement} at the given index
   */
  private Statement getStatementWithInputs(int index) {
    if (!isValidIndex(index))
      throw new IllegalArgumentException("Index " + index + " not valid for sequence " + this);
    return this.getStatementsWithInputs().get(index);
  }

  public Variable randomVariableForTypeLastStatement(GeneralType type) {
    if (type == null) throw new IllegalArgumentException("type cannot be null.");
    List<Variable> possibleIndices = new ArrayList<>(this.lastStatementVariables.size());
    for (Variable i : this.lastStatementVariables) {
      Statement s = statements.get(i.index);
      if (type.isAssignableFrom(s.getOutputType())) {
        possibleIndices.add(i);
      }
    }
    if (possibleIndices.isEmpty()) return null;
    return Randomness.randomMember(possibleIndices);
  }

  void checkIndex(int i) {
    if (i < 0 || i > size() - 1) throw new IllegalArgumentException();
  }

  /**
   * Returns a new sequence that is equivalent to this sequence plus the given
   * operation appended to the end.
   *
   * @param operation  the operation to add
   * @param inputVariables  the input variables
   * @return the sequence formed by appending the given operation to this sequence
   */
  public final Sequence extend(TypedOperation operation, List<Variable> inputVariables) {
    checkInputs(operation, inputVariables);
    List<RelativeNegativeIndex> indexList = new ArrayList<>(1);
    for (Variable v : inputVariables) {
      indexList.add(getRelativeIndexForVariable(size(), v));
    }
    Statement statement = new Statement(operation, indexList);
    int newNetSize = (operation.isNonreceivingValue()) ? this.savedNetSize : this.savedNetSize + 1;
    return new Sequence(
        new OneMoreElementList<>(this.statements, statement),
        this.savedHashCode + statement.hashCode(),
        newNetSize);
  }

  /**
   * Returns a new sequence that is equivalent to this sequence plus the given
   * statement appended at the end.
   * @param operation  the operation to add
   * @param inputs  the input variables for the operation
   * @return the sequence formed by appending the given operation to this sequence
   */
  public final Sequence extend(TypedOperation operation, Variable... inputs) {
    return extend(operation, Arrays.asList(inputs));
  }

  /**
   * extend adds a new statement to this sequence using the operation of the
   * given statement. Intended as the only place that we reach inside a
   * {@link Statement} for its operation.
   *
   * @param statement
   *          is a {@link Statement} object from which the operation is copied.
   * @param inputs
   *          is the list of variables for input.
   * @return sequence constructed from this one plus the operation
   * @see Sequence#extend(TypedOperation, List)
   */
  public Sequence extend(Statement statement, List<Variable> inputs) {
    return extend(statement.getOperation(), inputs);
  }

  // Argument checker for extend method.
  // These checks should be caught by checkRep() too.
  private void checkInputs(TypedOperation operation, List<Variable> inputVariables) {
    if (operation.getInputTypes().size() != inputVariables.size()) {
      String msg =
          "statement.getInputTypes().size():"
              + operation.getInputTypes().size()
              + " inputVariables.size():"
              + inputVariables.size()
              + " statement:"
              + operation;
      throw new IllegalArgumentException(msg);
    }
    for (int i = 0; i < inputVariables.size(); i++) {
      if (inputVariables.get(i).sequence != this) {
        String msg =
            "inputVariables.get("
                + i
                + ").owner != this for"
                + Globals.lineSep
                + "sequence: "
                + toString()
                + Globals.lineSep
                + "statement:"
                + operation
                + Globals.lineSep
                + "inputVariables:"
                + inputVariables;
        throw new IllegalArgumentException(msg);
      }
      GeneralType newRefConstraint = statements.get(inputVariables.get(i).index).getOutputType();
      if (newRefConstraint == null) {
        String msg =
            "newRefConstraint == null for"
                + Globals.lineSep
                + "sequence: "
                + toString()
                + Globals.lineSep
                + "statement:"
                + operation
                + Globals.lineSep
                + "inputVariables:"
                + inputVariables;
        throw new IllegalArgumentException(msg);
      }
      if (!(operation.getInputTypes().get(i).isAssignableFrom(newRefConstraint))) {
        String msg =
            i
                + "th input constraint "
                + newRefConstraint
                + " does not imply "
                + "statement's "
                + i
                + "th input constraint "
                + operation.getInputTypes().get(i)
                + Globals.lineSep
                + ".Sequence:"
                + Globals.lineSep
                + ""
                + this.toString()
                + Globals.lineSep
                + "statement:"
                + operation
                + Globals.lineSep
                + "inputVariables:"
                + inputVariables;
        throw new IllegalArgumentException(msg);
      }
    }
  }

  /**
   * Create a new sequence that is the concatenation of the given sequences.
   *
   * @param sequences
   *          the list of sequences to concatenate
   * @return the concatenation of the sequences in the list
   */
  public static Sequence concatenate(List<Sequence> sequences) {
    List<SimpleList<Statement>> statements1 = new ArrayList<>();
    int newHashCode = 0;
    int newNetSize = 0;
    for (Sequence c : sequences) {
      newHashCode += c.savedHashCode;
      newNetSize += c.savedNetSize;
      statements1.add(c.statements);
    }
    return new Sequence(new ListOfLists<>(statements1), newHashCode, newNetSize);
  }

  /**
   * The inputs for the ith statement, as indices. An index equal to x means
   * that the input is the value created by the x-th statement in the sequence.
   * @param i  the statement index
   * @return the absolute indices for the input variables in the given statement
   */
  public List<Integer> getInputsAsAbsoluteIndices(int i) {
    List<Integer> inputsAsVariables = new ArrayList<>();
    for (RelativeNegativeIndex relIndex : this.statements.get(i).inputs)
      inputsAsVariables.add(getVariableForInput(i, relIndex).index);
    return inputsAsVariables;
  }

  /**
   * Appends the statement at the given index to the {@code StringBuilder}.
   *
   * @param b
   *          the {@link StringBuilder} to which the code is appended.
   * @param index
   *          the position of the statement to print in this {@code Sequence}.
   */
  public void appendCode(StringBuilder b, int index) {
    // Get strings representing the inputs to this statement.
    // Example: { "var2", "(int)3" }
    getStatement(index).appendCode(getVariable(index), getInputs(index), b);
  }

  /**
   * Returns a string representing this sequence. The string can be parsed back
   * into a sequence using the method Sequence.parse(String). In particular, the
   * following invariant holds:
   *
   * <pre>
   * st.equals(parse(st.toParsableCode()))
   * </pre>
   *
   * See the parse(List) for the required format of a String
   * representing a Sequence.
   *
   * @return parsable string description of sequence
   */
  public String toParsableString() {
    return toParsableString(Globals.lineSep);
  }

  /**
   * Like toParsableString, but the client can specify a string that will be
   * used a separator between statements.
   *
   * @param statementSep
   *          the statement separator.
   * @return the string representation of this sequence
   */
  private String toParsableString(String statementSep) {
    assert statementSep != null;
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < size(); i++) {
      Statement sk = getStatement(i);
      b.append(
          sk.toParsableString(Variable.classToVariableName(sk.getOutputType()) + i, getInputs(i)));
      b.append(statementSep);
    }
    return b.toString();
  }

  /**
   * NOTE: the ONLY place this is used is in a test.
   *
   * Parse a sequence encoded as a list of strings, each string corresponding to
   * one statement. This method is similar to parse(String), but expects the
   * individual statements already as separate strings. Each statement is
   * expected to be of the form:
   *
   * <pre>
   *   VAR = OPERATION : VAR ... VAR
   * </pre>
   *
   * where the VAR are strings representing a variable name, and OPERATION is a
   * string representing an Operation. For more on OPERATION, see the
   * documentation for {@link OperationParser#parse(String)}.
   *
   * The first VAR token represents the "output variable" that is the result of
   * the statement call. The VAR tokens appearing after OPERATION represent the
   * "input variables" to the statement call. At the i-th line, the input
   * variable tokens should appear as an output variable for some previous j-th
   * line, j &lt; i.
   *
   * Here is an example of a list of lines representing a sequence.
   *
   * <pre>
   * var0 = cons : java.util.HashMap.&lt;init&gt;() :
   * var1 = prim : double:-1.0 :
   * var2 = prim : java.lang.String:"hi!" :
   * var3 = method : java.util.HashMap.put(java.lang.Object,java.lang.Object) : var0 var1 var2
   * </pre>
   *
   * The above sequence corresponds to the following java code (with package
   * names omitted for brevity):
   *
   * <pre>
   * HashMap var0 = new HashMap();
   * double var1 = -1.0;
   * String var2 = "hi!";
   * Object var3 = var0.put(var1, var2);
   * </pre>
   *
   * When writing/reading sequences out to file: you have two options: serialize
   * the sequences using java's serialization mechanism, or write them out as
   * parsable text. Serialization is faster, and text is human-readable.
   *
   * @param statements  the list of statement strings
   * @return the sequence constructed from the list of strings
   * @throws SequenceParseException if any statement cannot be parsed
   */
  public static Sequence parse(List<String> statements) throws SequenceParseException {

    Map<String, Integer> valueMap = new LinkedHashMap<>();
    Sequence sequence = new Sequence();
    int statementCount = 0;
    try {
      for (String statement : statements) {

        // Remove surrounding whitespace.
        statement = statement.trim();

        // Extract elements:
        // var = <operation> : var ... var
        // | | |
        // | | |
        // newVar stKind inVars
        int equalsInd = statement.indexOf('=');
        int colonInd = statement.lastIndexOf(':');

        if (equalsInd == -1) {
          String msg =
              "A statement must be of the form "
                  + "varname = <operation-name> : varname ... varname"
                  + " but the "
                  + statementCount
                  + "-th (1-based) is missing"
                  + " an \"=\" symbol.";
          throw new SequenceParseException(msg, statements, statementCount);
        }

        if (colonInd == -1) {
          String msg =
              "A statement must be of the form "
                  + "varname = <operation-name> : varname ... varname"
                  + " but the "
                  + statementCount
                  + "-th (1-based) is missing"
                  + " a \":\" symbol.";
          throw new SequenceParseException(msg, statements, statementCount);
        }

        String newVar = statement.substring(0, equalsInd).trim();
        String opStr = statement.substring(equalsInd + 1, colonInd).trim();
        String inVarsStr = statement.substring(colonInd + 1).trim();

        if (valueMap.containsKey(newVar)) {
          String msg =
              "(Statement "
                  + statementCount
                  + ") result variable name "
                  + newVar
                  + " was already declared in a previous statement.";
          throw new SequenceParseException(msg, statements, statementCount);
        }

        System.out.println("operation string: " + opStr);
        // Parse operation.
        TypedOperation operation;
        try {
          operation = OperationParser.parse(opStr);
        } catch (OperationParseException e) {
          throw new SequenceParseException(e.getMessage(), statements, statementCount);
        }
        assert operation != null;

        // Find input variables from their names.
        String[] inVars = new String[0];
        if (!inVarsStr.trim().isEmpty()) {
          // One or more input vars.
          inVars = inVarsStr.split("\\s");
        }

        if (inVars.length != operation.getInputTypes().size()) {
          String msg =
              "Number of input variables given ("
                  + inVarsStr
                  + ") does not match expected (expected "
                  + operation.getInputTypes().size()
                  + ")";
          throw new SequenceParseException(msg, statements, statementCount);
        }

        List<Variable> inputs = new ArrayList<>();
        for (String inVar : inVars) {
          Integer index = valueMap.get(inVar);
          if (index == null) {
            String msg =
                "(Statement "
                    + statementCount
                    + ") input variable name "
                    + newVar
                    + " is not declared by a previous statement.";
            throw new IllegalArgumentException(msg);
          }
          inputs.add(sequence.getVariable(index));
        }

        sequence = sequence.extend(operation, inputs);
        valueMap.put(newVar, sequence.getLastVariable().getDeclIndex());
        statementCount++;
      }
    } catch (RuntimeException e) {
      // Saw some other exception that is not a parse error.
      // Throw an error, giving information on the problem.
      StringBuilder b = new StringBuilder();
      b.append(
              "Error while parsing the following list of strings as a sequence (error was at index ")
          .append(statementCount)
          .append("):\n\n");
      for (String s : statements) {
        b.append(s).append("\n");
      }
      b.append("\n\n");
      b.append("Error: ").append(e.toString()).append("\n");
      b.append("Stack trace:\n");
      for (StackTraceElement s : e.getStackTrace()) {
        b.append(s.toString());
      }
      throw new Error(b.toString());
    }
    return sequence;
  }

  /**
   * Parse a sequence encoded as a strings. Convenience method for
   * parse(List), which parses a sequence of strings, each representing
   * a Statement. See that method for more documentation on the string
   * representation of a sequence.
   * <p>
   * This method breaks up the given string into statements assuming that each
   * statement is separated by a line separator character.
   * <p>
   * The following invariant holds:
   *
   * <pre>
   * st.equals(parse(st.toParsableCode()))
   * </pre>
   *
   * When writing/reading sequences out to file: you have two options: serialize
   * the sequences using java's serialization mechanism, or write them out as
   * parsable text. Serialization is faster, and text is human-readable.
   *
   * @param string  the string descriptor
   * @return the sequence constructed by parsing the input string
   * @throws SequenceParseException
   *           if string is not valid sequence
   */
  public static Sequence parse(String string) throws SequenceParseException {
    return parse(Arrays.asList(string.split(Globals.lineSep)));
  }

  /**
   * A sequence representing a single primitive values, like "Foo var0 = null"
   * or "int var0 = 1".
   *
   * @return true if this sequence is a single primitive initialization statement, false otherwise
   */
  public boolean isPrimitive() {
    return (size() == 1 && getStatement(0).isPrimitiveInitialization());
  }

  /**
   * Test whether any statement of this sequence has an operation whose
   * declaring class matches the given regular expression.
   *
   * @param classNames
   *          the regular expression to test class names
   * @return true if any statement has operation with matching declaring class,
   *         false otherwise
   */
  public boolean hasUseOfMatchingClass(Pattern classNames) {
    for (int i = 0; i < statements.size(); i++) {
      GeneralType declaringType = statements.get(i).getDeclaringClass();
      if (declaringType != null && classNames.matcher(declaringType.getName()).matches()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Using compositional structure of this sequence, return the subsequence of
   * this sequence that contains the statement at the given index.
   *
   * @param index  the statement position in this sequence
   * @return the sequence containing the index position
   */
  Sequence getSubsequence(int index) {
    return new Sequence(statements.getSublist(index));
  }

  /**
   * Creates a sequence corresponding to the given non-null primitive value.
   *
   * @param value  non-null reference to a primitive or String value
   * @return a {@link Sequence} consisting of a statement created with the object.
   */
  public static Sequence createSequenceForPrimitive(Object value) {
    if (value == null) throw new IllegalArgumentException("value is null");
    Class<?> c = value.getClass();
    if (!PrimitiveTypes.isBoxedOrPrimitiveOrStringType(c)) {
      throw new IllegalArgumentException("value is not a (boxed) primitive or String");
    }

    GeneralType type = GeneralType.forClass(c);
    if (type.isBoxedPrimitive()) {
      type = type.toPrimitive();
    }

    if (type.equals(ConcreteTypes.STRING_TYPE) && !PrimitiveTypes.stringLengthOK((String) value)) {
      throw new IllegalArgumentException(
          "value is a string of length > " + GenInputsAbstract.string_maxlen);
    }

    return new Sequence().extend(TypedOperation.createPrimitiveInitialization(type, value));
  }
}
