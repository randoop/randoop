/**
 * 
 */
package randoop;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;
import java.util.Collections;

import randoop.util.Reflection;

/**
 * EnumConstant represents a constant value from an enum.
 * 
 * @author bjkeller
 *
 */
public class EnumConstant extends AbstractOperation implements Operation, Serializable {
  
  private static final long serialVersionUID = 849994347169442078L;
  
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
      EnumConstant e = (EnumConstant)obj;
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
    return toParseableString();
  }

  /* (non-Javadoc)
   * @see randoop.StatementKind#getInputTypes()
   */
  @Override
  public List<Class<?>> getInputTypes() {
    return Collections.emptyList();
  }

  public Class<?> type() { return value.getDeclaringClass(); }
  
  /**
   * getOutputType returns the type of the enum constant.
   * 
   * @see randoop.Operation#getOutputType()
   */
  @Override
  public Class<?> getOutputType() {
    return type();
  }

  /* (non-Javadoc)
   * @see randoop.StatementKind#execute(java.lang.Object[], java.io.PrintStream)
   */
  @Override
  public ExecutionOutcome execute(Object[] statementInput, PrintStream out) {
    assert statementInput.length == 0;
    return new NormalExecution(this.value,0);
  }

  /* (non-Javadoc)
   * @see randoop.StatementKind#appendCode(randoop.Variable, java.util.List, java.lang.StringBuilder)
   */
  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    b.append(Reflection.getCompilableName(type()) + "." + this.value.name());
  }

  /**
   * toParseableString issues a string representation of an enum constant as a
   * type-value pair. The parse function should return an equivalent object.
   * 
   * @see EnumConstant#parse(String)
   * @see randoop.Operation#toParseableString()
   */
  @Override
  public String toParseableString() {
    return type().getName() + ":" + value.name();
  }

  /**
   * parse recognizes the description of an enum constant value in a string.
   * 
   * Valid strings may be of the form EnumType:EnumValue, or
   * OuterClass$InnerEnum:EnumValue for an enum that is an inner type of a class.
   * 
   * @param s string representing type-value pair for an enum constant
   * @return an EnumConstant representing the enum constant value in {@link s}
   * @throws OperationParseException
   */
  public static EnumConstant parse(String s) throws OperationParseException {
    if (s == null) {
      throw new IllegalArgumentException("s cannot be null");
    }
    int colonIdx = s.indexOf(':');
    if (colonIdx < 0) {
      String msg = "An enum constant description must be of the form \"" +
          "<type>:<value>" + " but description is \"" + s + "\".";
      throw new OperationParseException(msg);
    }
    
    String typeName = s.substring(0, colonIdx).trim();
    String valueName = s.substring(colonIdx+1).trim();
    
    Enum<?> value = null;
    
    String errorPrefix = "Error when parsing type-value pair " + s + 
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
    
    Class<?> type = Reflection.classForName(typeName,true);
    if (type == null) {
      String msg = errorPrefix + " The type given \"" + typeName + "\" was not recognized.";
      throw new OperationParseException(msg);
    }
    if (!type.isEnum()) {
      String msg = errorPrefix + " The type given \"" + typeName + "\" is not an enum.";
      throw new OperationParseException(msg);
    }
    
    value = valueOf(type,valueName);
    if (value == null) {
      String msg = errorPrefix + " The value given \"" + valueName + "\" is not a constant of the enum " +
          typeName + ".";
      throw new OperationParseException(msg);
    }
    
    return new EnumConstant(value);
  }

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
      Enum<?> e = (Enum<?>)obj;
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
  
  @Override
  public Object getValue() { return value(); }

  @Override
  public Class<?> getDeclaringClass() {
    return value.getDeclaringClass();
  }
}
