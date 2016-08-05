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
    if (i < 0) throw new IllegalArgumentException("negative index:" + i);
    this.sequence = owner;
    this.index = i;
  }

  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Variable)) return false;
    if (o == this) return true;
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
    return getName(index);
  }

  /**
   * Returns the name of this variable assuming the given type.
   *
   * @param type  the type of this variable
   * @return  the name of this variable as a string
   */
  public String getName(Type type) {
    return getName(type, index);
  }

  /**
   * Returns the name of this variable with the given index.
   *
   * @param i  the index for the variable name
   * @return  a string using the index to form the name
   */
  public String getName(int i) {
    return getName(getType(), i);
  }

  /**
   * For use by clients when the statement has not yet been appended, so
   * getType() would fail.
   *
   * @param type  the type to use when building variable name
   * @param i  the index for the variable
   * @return gets variable names based on the type of the variable
   */
  public String getName(Type type, int i) {
    return getName(classToVariableName(type), index);
  }

  /**
   * For use by clients when the statement has not yet been appended, so
   * getType() would fail.
   *
   * @param className  the class name to us to construct the variable name
   * @return the name of this variable
   */
  public String getName(String className) {
    return getName(className, index);
  }

  /**
   * The name of this variable using the given type name and index.
   *
   * @param className  the classname to use
   * @param i  the index to use in the name
   * @return  the variable name that appends the index to the classname
   */
  public String getName(String className, int i) {
    String basename = classNameToVariableName(className);
    return basename + Integer.toString(i);
  }

  @Override
  public int compareTo(Variable o) {
    if (o == null) throw new IllegalArgumentException();
    if (o.sequence != this.sequence) throw new IllegalArgumentException();
    return (new Integer(this.index).compareTo(o.index));
  }

  /**
   * Build a variable name from the name of the given type.
   *
   * @param type  the type
   * @return the variable name as a string
   */
  public static String classToVariableName(Type type) {
    return VariableRenamer.getVariableName(type);
  }

  /**
   * Convert the classname to a variable name, preserving camel case if used.
   *
   * @param className  the class name
   * @return the variable form of the classname
   */
  public static String classNameToVariableName(String className) {
    assert !className.contains(".");
    assert !className.contains("[");
    assert !className.equals("");
    return Character.toLowerCase(className.charAt(0)) + className.substring(1);
  }
}
