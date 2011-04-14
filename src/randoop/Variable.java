package randoop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Represents the result of a statement call in a sequence. */
public class Variable implements Comparable<Variable>, Serializable {

  private static final long serialVersionUID = 4465111607016458010L;

  // The index of the statement that creates this value.
  public final int index;

  // The sequence that creates this value.
  public final Sequence sequence;

  public Variable(Sequence owner, int i) {
    if (owner == null) 
      throw new IllegalArgumentException("missing owner");
    if (i < 0)
      throw new IllegalArgumentException("negative index:" + i);
    this.sequence = owner;
    this.index = i;
  }

  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Variable))
      return false;
    if (o == this)
      return true;
    Variable other = (Variable)o;
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
    return this.sequence.getStatementKind(index).getOutputType();
  }
  
  /** The statement that produced this variable. */
  public StatementKind getDeclaringStatement() {
    return this.sequence.getStatementKind(index);
  }

  /** The index of the statement that creates this value. */
  public int getDeclIndex() {
    return this.index;
  }

  public Variable copyWithIndexUpdated(Map<Integer, Integer> map) {
    return new Variable(sequence, map.get(index));
  }

  public static List<Integer> statementIndexList(List<Variable> values) {
    List<Integer> result= new ArrayList<Integer>(values.size());
    for (Variable value : values) {
      result.add(value.getDeclIndex());
    }
    return result;
  }

  private static String getName(int i) {
    return "var" + Integer.toString(i);
  }

  public String getName() {
    return Variable.getName(index);
  }

  public int compareTo(Variable o) {
    if (o==null) throw new IllegalArgumentException();
    if (o.sequence != this.sequence) throw new IllegalArgumentException();
    return (new Integer(this.index).compareTo(new Integer(o.index)));
  }
}
