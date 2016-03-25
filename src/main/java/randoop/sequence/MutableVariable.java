package randoop.sequence;

import randoop.types.ConcreteType;

/**
 * A variable that is part of a mutable sequence.
 *
 * @see MutableSequence
 */
public class MutableVariable {

  // Do not override equals/hashcode! Two MVariables
  // should be equal iff they are ==.

  public final MutableSequence owner;
  private final String name;

  public MutableVariable(MutableSequence owner, String name) {
    if (owner == null) throw new IllegalArgumentException();
    if (name == null) throw new IllegalArgumentException();

    this.owner = owner;
    this.name = name;
  }

  public MutableStatement getCreatingStatementWithInputs() {
    return owner.getDeclaringStatement(this);
  }

  public ConcreteType getType() {
    return owner.getDeclaringStatement(this).operation.getOutputType();
  }

  public int getDeclIndex() {
    return owner.getIndex(this);
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
