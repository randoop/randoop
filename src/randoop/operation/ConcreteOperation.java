package randoop.operation;

import java.io.PrintStream;
import java.util.List;

import randoop.ExecutionOutcome;
import randoop.sequence.Variable;
import randoop.types.ConcreteType;
import randoop.types.ConcreteTypeTuple;

public abstract class ConcreteOperation extends AbstractOperation {

  private ConcreteTypeTuple inputTypes;
  private ConcreteType outputType;

  public ConcreteOperation(ConcreteTypeTuple inputTypes, ConcreteType outputType) {
    this.inputTypes = inputTypes;
    this.outputType = outputType;
  }
  
  @Override
  public ConcreteTypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public ConcreteType getOutputType() {
    return outputType;
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
