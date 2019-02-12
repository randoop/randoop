package randoop.sequence;

import randoop.types.Type;

/** Represents the result of a statement call in a sequence. */
public class Variable implements Comparable<Variable> {

  // The index of the statement that creates this value.
  public final int index;

  // The sequence that creates this value.
  public final Sequence sequence;

  public Variable(Sequence owner, int i) {
    if (owner == null) throw new IllegalArgumentException("missing owner");
    if (i < 0) {
      throw new IllegalArgumentException("negative index: " + i);
    }
    this.sequence = owner;
    this.index = i;
  }

  /** Do not use! Only for use by DummyVariable. */
  protected Variable() {
    index = 0;
    sequence = null;
  }

  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Variable)) {
      return false;
    }
    if (o == this) {
      return true;
    }
    Variable other = (Variable) o;
    // Two values are equal only if they are owned by the
    // same sequence, where "same" means the same reference.
    // return this.sequence == other.sequence && this.index == other.index;
    return this.index == other.index;
  }

  @Override
  public int hashCode() {
    return this.index * this.sequence.hashCode();
  }

  /**
   * Returns the type of this variable.
   *
   * @return the type of this variable
   */
  public Type getType() {
    return this.sequence.getStatement(index).getOutputType();
  }

  /**
   * The statement that produced this variable.
   *
   * @return the statement to which this variable belongs
   */
  public Statement getDeclaringStatement() {
    return this.sequence.getStatement(index);
  }

  /**
   * The index of the statement that creates this value.
   *
   * @return the statement index where this variable is created
   */
  public int getDeclIndex() {
    return this.index;
  }

  /*
  public Variable copyWithIndexUpdated(Map<Integer, Integer> map) {
    return new Variable(sequence, map.get(index));
  }

  public static List<Integer> statementIndexList(List<Variable> values) {
    List<Integer> result = new ArrayList<>(values.size());
    for (Variable value : values) {
      result.add(value.getDeclIndex());
    }
    return result;
  }
  */

  /**
   * Returns the name of this variable.
   *
   * @return the name of this variable as a string
   */
  public String getName() {
    return getName(classToVariableName(getType()), index);
  }

  /**
   * The name of this variable using the given type name and index.
   *
   * @param className the classname to use
   * @param i the index to use in the name
   * @return the variable name that appends the index to the classname
   */
  private String getName(String className, int i) {
    String basename = classNameToVariableName(className);
    return basename + Integer.toString(i);
  }

  @SuppressWarnings("ReferenceEquality")
  @Override
  public int compareTo(Variable o) {
    if (o == null) throw new IllegalArgumentException();
    if (o.sequence != this.sequence) throw new IllegalArgumentException();
    return (Integer.valueOf(this.index).compareTo(o.index));
  }

  /**
   * Build a variable name from the name of the given type.
   *
   * @param type the type
   * @return the variable name as a string
   */
  static String classToVariableName(Type type) {
    return VariableRenamer.getVariableName(type);
  }

  /**
   * Convert the classname to a variable name, preserving camel case if used.
   *
   * @param className the class name
   * @return the variable form of the classname
   */
  private static String classNameToVariableName(String className) {
    assert !className.contains(".");
    assert !className.contains("[");
    assert !className.equals("");
    return Character.toLowerCase(className.charAt(0)) + className.substring(1);
  }

  /**
   * True if this variable's value should be inlined as a literal at call sites, rather than
   * referencing the variable.
   *
   * @return whether this variable's value should be inlined as a literal at call sites
   */
  public boolean shouldInlineLiterals() {
    return sequence.shouldInlineLiterals();
  }
}
