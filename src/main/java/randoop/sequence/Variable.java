package randoop.sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

  /** The declared type of the value */
  public Class<?> getType() {
    return this.sequence.getStatement(index).getOutputType();
  }

  /** The statement that produced this variable. */
  public Statement getDeclaringStatement() {
    return this.sequence.getStatement(index);
  }

  /** The index of the statement that creates this value. */
  public int getDeclIndex() {
    return this.index;
  }

  public Variable copyWithIndexUpdated(Map<Integer, Integer> map) {
    return new Variable(sequence, map.get(index));
  }

  public static List<Integer> statementIndexList(List<Variable> values) {
    List<Integer> result = new ArrayList<Integer>(values.size());
    for (Variable value : values) {
      result.add(value.getDeclIndex());
    }
    return result;
  }

  public String getName() {
    return getName(index);
  }

  public String getName(Class<?> clazz) {
    return getName(clazz, index);
  }

  public String getName(int i) {
    return getName(getType(), i);
  }

  /**
   * For use by clients when the statement has not yet been appended, so
   * getType() would fail.
   */
  public String getName(Class<?> clazz, int i) {
    return getName(classToVariableName(clazz), index);
  }

  /**
   * For use by clients when the statement has not yet been appended, so
   * getType() would fail.
   */
  public String getName(String className) {
    return getName(className, index);
  }

  public String getName(String className, int i) {
    String basename = classNameToVariableName(className);
    return basename + Integer.toString(i);
  }

  @Override
  public int compareTo(Variable o) {
    if (o == null) throw new IllegalArgumentException();
    if (o.sequence != this.sequence) throw new IllegalArgumentException();
    return (new Integer(this.index).compareTo(new Integer(o.index)));
  }

  /** Convert to string and downcase the first character. */
  public static String classToVariableName(Class<?> clazz) {
    // assert !clazz.equals(void.class) : "The given variable type can not be
    // void!";
    // return classNameToVariableName(clazz.getSimpleName().replace("[]",
    // "_array"));
    return VariableRenamer.getVariableName(clazz);
  }

  /** Downcase the first character. */
  public static String classNameToVariableName(String className) {
    assert !className.contains(".");
    assert !className.contains("[");
    assert !className.equals("");
    return Character.toLowerCase(className.charAt(0)) + className.substring(1);
  }
}
