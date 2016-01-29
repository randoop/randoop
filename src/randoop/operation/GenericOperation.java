package randoop.operation;

import randoop.types.GenericType;
import randoop.types.GenericTypeTuple;
import randoop.types.Substitution;

public abstract class GenericOperation extends AbstractOperation {

  private GenericTypeTuple inputTypes;
  private GenericType outputType;

  public GenericOperation(GenericTypeTuple inputTypes, GenericType outputType) {
    this.inputTypes = inputTypes;
    this.outputType = outputType;
  }

  @Override
  public GenericTypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public GenericType getOutputType() {
    return outputType;
  }

  public abstract ConcreteOperation instantiate(Substitution substitution);

}
