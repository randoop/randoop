package randoop.operation;

import randoop.types.GeneralType;
import randoop.types.TypeTuple;

/**
 * Created by bjkeller on 4/28/16.
 */
class TypedTermOperation extends TypedOperation {

  TypedTermOperation(CallableOperation operation, TypeTuple inputTypes, GeneralType outputType) {
    super(operation, inputTypes, outputType);
  }

}
