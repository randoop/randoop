package randoop;

/**
 * A variable that is part of a mutable sequence.
 */
public class MVariable {

  // Do not override equals/hashcode! Two MVariables
  // should be equal iff they are ==.
  
  public final MSequence owner;
  private final String name;

  public MVariable(MSequence owner, String name) {
    if (owner == null) throw new IllegalArgumentException();
    if (name == null) throw new IllegalArgumentException();

    this.owner = owner;
    this.name = name;
  }

  public MStatement getCreatingStatementWithInputs() {
    return owner.getDeclaringStatement(this);
  }

  public Class<?> getType() {
    return owner.getDeclaringStatement(this).statementKind.getOutputType();
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
