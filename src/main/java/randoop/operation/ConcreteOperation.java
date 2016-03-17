package randoop.operation;

import java.io.PrintStream;
import java.util.List;

import randoop.ExecutionOutcome;
import randoop.sequence.Variable;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeTuple;

/**
 * {@code ConcreteOperation} is an abstract implementation of {@link Operation} that represents
 * the operations that can occur within a test.
 * A concrete operation is either defined in a non-generic context, or is instantiated from a
 * {@link GenericOperation}.
 */
public abstract class ConcreteOperation extends AbstractOperation {

  /** The declaring type for this operation */
  private final ConcreteType declaringType;

  /** The type tuple of concrete input types. */
  private final ConcreteTypeTuple inputTypes;

  /** The concrete output type. */
  private final ConcreteType outputType;

  /**
   * Creates a {@code ConcreteOperation} with the given input and output types.
   *
   * @param inputTypes  the tuple of concrete input types
   * @param outputType  the concrete output type
     */
  public ConcreteOperation(ConcreteType declaringType, ConcreteTypeTuple inputTypes, ConcreteType outputType) {
    this.declaringType = declaringType;
    this.inputTypes = inputTypes;
    this.outputType = outputType;
  }

  /**
 ConcreteType  * Returns the tuple of input types for this operation.
   * If a method call or field access, the first input corresponds to the
   * receiver, which must be an object of the declaring class.
   *
   * @return tuple of concrete input types
   */
  public ConcreteTypeTuple getInputTypes() {
    return inputTypes;
  }

  /**
   * Returns the type returned by the operation.
   *
   * @return {@link ConcreteType} type returned by this operation
   */
  public ConcreteType getOutputType() {
    return outputType;
  }

  /**
   * Returns the class in which the operation is defined, or, if the operation
   * represents a value, the type of the value.
   *
   * @return class to which the operation belongs.
   */
  public ConcreteType getDeclaringType() {
    return declaringType;
  }

  /**
   * Performs this operation using the array of input values. Returns
   * the results of execution as an ResultOrException object and can
   * output results to specified PrintStream.
   * @param input array containing appropriate inputs to operation
   * @param out stream to output results of execution;
   *            can be null if you don't want to print.
   * @return results of executing this statement
   */
  public abstract ExecutionOutcome execute(Object[] input, PrintStream out);
  
  /**
   * Produces a Java source code representation of this statement and append it
   * to the given StringBuilder.
   * 
   * @param inputVars  the list of variables that are inputs to operation.
   * @param b  the {@link StringBuilder} to which code is added.
   */
  public abstract void appendCode(List<Variable> inputVars, StringBuilder b);
  
  /**
   * Returns a string representation of this Operation, which can be read by 
   * static parse method for class. 
   * For a class C implementing the Operation interface, this method should 
   * return a String s such that parsing the string 
   * returns an object equivalent to this object, i.e. C.parse(this.s).equals(this).
   * 
   * @return string descriptor of {@link Operation} object.
   */
  public abstract String toParseableString();
 
}
