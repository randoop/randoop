package randoop.operation;

import java.io.PrintStream;
import java.util.List;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.reflection.ClassVisitor;
import randoop.reflection.OperationParseVisitor;
import randoop.sequence.Variable;
import randoop.types.GeneralType;
import randoop.types.GeneralTypeTuple;
import randoop.types.TypeNames;

/**
 * EnumConstant is an {@link Operation} representing a constant value from an
 * enum.
 * <p>
 * Using the formal notation in {@link Operation}, a constant named BLUE from
 * the enum Colors is an operation BLUE : [] &rarr; Colors.
 * <p>
 * Execution simply returns the constant value.
 */
public class EnumConstant extends CallableOperation {

  public static final String ID = "enum";

  private Enum<?> value;

  public EnumConstant(Enum<?> value) {
    if (value == null) {
      throw new IllegalArgumentException("enum constant cannot be null");
    }

    this.value = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof EnumConstant) {
      EnumConstant e = (EnumConstant) obj;
      return equals(e);
    }
    return false;
  }

  public boolean equals(EnumConstant e) {
    return (this.value.equals(e.value));
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value.toString();
  }

  /**
   * {@inheritDoc}
   * @return a {@link NormalExecution} object holding the value of the enum constant.
   */
  @Override
  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {
    assert statementInput.length == 0;
    return new NormalExecution(this.value, 0);
  }

  /**
   * {@inheritDoc} Adds qualified name of enum constant.
   */
  @Override
  public void appendCode(GeneralType declaringType, GeneralTypeTuple inputTypes, GeneralType outputType, List<Variable> inputVars, StringBuilder b) {
    b.append(declaringType.getName()).append(".").append(this.value.name());
  }

  /**
   * {@inheritDoc} Issues a string representation of an enum constant as a
   * type-value pair. The parse function should return an equivalent object.
   *
   * @see EnumConstant#parse(String, OperationParseVisitor)
   */
  @Override
  public String toParseableString(GeneralType declaringType, GeneralTypeTuple inputTypes, GeneralType outputType) {
    return declaringType.getName() + ":" + value.name();
  }

  /**
   * Parses the description of an enum constant value in a string as returned by
   * {@link EnumConstant#toParseableString(GeneralType,GeneralTypeTuple,GeneralType)}.
   *
   * Valid strings may be of the form EnumType:EnumValue, or
   * OuterClass$InnerEnum:EnumValue for an enum that is an inner type of a class.
   *
   * @param desc string representing type-value pair for an enum constant
   * @param visitor
   * @return an EnumConstant representing the enum constant value in desc
   * @throws OperationParseException
   *           if desc does not match expected form.
   */
    public static void parse(String desc, OperationParseVisitor visitor) throws OperationParseException {
      if (desc == null) {
        throw new IllegalArgumentException("desc cannot be null");
      }
      int colonIdx = desc.indexOf(':');
      if (colonIdx < 0) {
        String msg =
            "An enum constant description must be of the form \""
                + "<type>:<value>"
                + " but description is \""
                + desc
                + "\".";
        throw new OperationParseException(msg);
      }

      String typeName = desc.substring(0, colonIdx).trim();
      String valueName = desc.substring(colonIdx+1).trim();

      Enum<?> value = null;

      String errorPrefix = "Error when parsing type-value pair " + desc +
          " for an enum description of the form <type>:<value>.";

      if (typeName.isEmpty()) {
        String msg = errorPrefix + " No type given.";
        throw new OperationParseException(msg);
      }

      if (valueName.isEmpty()) {
        String msg = errorPrefix + " No value given.";
        throw new OperationParseException(msg);
      }

      String whitespacePattern = ".*\\s+.*";
      if (typeName.matches(whitespacePattern)) {
        String msg = errorPrefix + " The type has unexpected whitespace characters.";
        throw new OperationParseException(msg);
      }
      if (valueName.matches(whitespacePattern)) {
        String msg = errorPrefix + " The value has unexpected whitespace characters.";
        throw new OperationParseException(msg);
      }

     visitor.visitEnum(typeName, valueName);

    }


  /**
   * value
   * @return object for value of enum constant.
   */
  public Enum<?> value() {
    return this.value;
  }

  /**
   * {@inheritDoc}
   *
   * @return value of enum constant.
   */
  @Override
  public Object getValue() {
    return value();
  }

}
