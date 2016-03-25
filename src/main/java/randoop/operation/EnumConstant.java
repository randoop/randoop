package randoop.operation;

import java.io.PrintStream;
import java.util.List;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.Variable;
import randoop.types.ConcreteType;
import randoop.types.GeneralType;
import randoop.types.GeneralTypeTuple;

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
   * @see EnumConstant#parse(String)
   */
  @Override
  public String toParseableString(GeneralType declaringType, GeneralTypeTuple inputTypes, GeneralType outputType) {
    return declaringType.getName() + ":" + value.name();
  }

  /**
   * Parses the description of an enum constant value in a string as returned by
   * {@link EnumConstant#toParseableString()}.
   *
   * Valid strings may be of the form EnumType:EnumValue, or
   * OuterClass$InnerEnum:EnumValue for an enum that is an inner type of a class.
   *
   * @param desc string representing type-value pair for an enum constant
   * @return an EnumConstant representing the enum constant value in desc
   * @throws OperationParseException
   *           if desc does not match expected form.
   */
  /*
    public static EnumConstant parse(String desc) throws OperationParseException {
      if (desc == null) {
        throw new IllegalArgumentException("s cannot be null");
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

      Class<?> type;
      try {
        type = TypeNames.getTypeForName(typeName);
      } catch (ClassNotFoundException e) {
        String msg = errorPrefix + " The type given \"" + typeName + "\" was not recognized.";
        throw new OperationParseException(msg);
      }
      if (!type.isEnum()) {
        String msg = errorPrefix + " The type given \"" + typeName + "\" is not an enum.";
        throw new OperationParseException(msg);
      }

      value = valueOf(type,valueName);
      if (value == null) {
        String msg =
            errorPrefix
                + " The value given \""
                + valueName
                + "\" is not a constant of the enum "
                + typeName
                + ".";
        throw new OperationParseException(msg);
      }

      return new EnumConstant(value);
    }
  */
  /**
   * valueOf searches the enum constant list of a class for a constant with the given name.
   * Note: cannot make this work using valueOf method of Enum due to typing.
   *
   * @param type class that is already known to be an enum.
   * @param valueName name for value that may be a constant of the enum.
   * @return reference to actual constant value, or null if none exists in type.
   */
  private static Enum<?> valueOf(Class<?> type, String valueName) {
    for (Object obj : type.getEnumConstants()) {
      Enum<?> e = (Enum<?>) obj;
      if (e.name().equals(valueName)) {
        return e;
      }
    }
    return null;
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
