package randoop.generation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import randoop.BugInRandoopException;
import randoop.main.GenInputsAbstract;
import randoop.operation.ConstructorCall;
import randoop.operation.MethodCall;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;
import randoop.types.GenericClassType;
import randoop.types.JDKTypes;
import randoop.types.ParameterizedType;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.TypeArgument;
import randoop.types.TypeTuple;
import randoop.util.ArrayListSimpleList;
import randoop.util.Randomness;
import randoop.util.SimpleList;

class HelperSequenceCreator {

  /**
   * Returns a sequence that creates an object of type compatible with the given
   * class. Wraps the object in a list, and returns the list.
   *
   * CURRENTLY, will return a sequence (i.e. a non-empty list) only if cls is an
   * array.
   *
   * @param components  the component manager with existing sequences
   * @param collectionType  the query type
   * @return the singleton list containing the compatible sequence
   */
  static SimpleList<Sequence> createArraySequence(ComponentManager components, GeneralType collectionType) {

    final int MAX_LENGTH = 7;

    if (!collectionType.isArray()) {
      return new ArrayListSimpleList<>();
    }

    ArrayType arrayType = (ArrayType)collectionType;
    GeneralType elementType = arrayType.getElementType();

    Sequence s;
    SimpleList<Sequence> candidates =
          components.getSequencesForType(elementType, false);
    if (candidates.isEmpty()) {
        // No sequences that produce appropriate component values found, and
      if (GenInputsAbstract.forbid_null) {
          // use of null is forbidden. So, return the empty array.
          s = new Sequence().extend(TypedOperation.createArrayCreation(arrayType, 0));
        } else {
          // null is allowed.
          s = new Sequence();
          List<Variable> ins = new ArrayList<>();
          TypedOperation declOp;
          if (Randomness.weighedCoinFlip(0.5)) {
            declOp = TypedOperation.createArrayCreation(arrayType, 0);
          } else {
            s = s.extend(TypedOperation.createNullOrZeroInitializationForType(elementType));
            ins.add(s.getVariable(0));
            declOp = TypedOperation.createArrayCreation(arrayType, 1);
          }
          s = s.extend(declOp, ins);
      }
    } else {
        int length = Randomness.nextRandomInt(MAX_LENGTH);
        s = createAnArray(candidates, elementType, length);
    }
    assert s != null;
    ArrayListSimpleList<Sequence> l = new ArrayListSimpleList<>();
    l.add(s);
    return l;
  }

  private static Sequence createCollection(ComponentManager componentManager, ReferenceType inputType) {
    if (!inputType.isParameterized()) {
      throw new IllegalArgumentException("type must be parameterized");
    }
    assert ! inputType.isGeneric() : "type must be instantiated";

    // get the element type
    ParameterizedType collectionType = (ParameterizedType)inputType;
    List<TypeArgument> argumentList = collectionType.getTypeArguments();
    assert argumentList.size() == 1 : "Collection classes should have one type argument";
    TypeArgument argumentType = argumentList.get(0);
    ReferenceType elementType;
    assert argumentType instanceof ReferenceArgument : "type argument must be reference type";
    elementType = ((ReferenceArgument)argumentType).getReferenceType();

    int totStatements = 0;
    List<Sequence> inputSequences = new ArrayList<>();
    List<Integer> variableIndices = new ArrayList<>();

    // select implementing Collection type and instantiate
    GenericClassType implementingType = JDKTypes.getImplementingType(collectionType);
    ParameterizedType creationType;
    creationType = implementingType.instantiate(elementType);

    // build sequence to create a Collection object
    TypedOperation creationOperation = getCollectionConstructor(creationType);
    Sequence creationSequence = new Sequence();
    creationSequence = creationSequence.extend(creationOperation, new ArrayList<Variable>());

    inputSequences.add(creationSequence);
    variableIndices.add(totStatements + creationSequence.getLastVariable().index);
    totStatements += creationSequence.size();

    // build sequence to create array of element type
    SimpleList<Sequence> candidates = componentManager.getSequencesForType(elementType, false);
    int length = Randomness.nextRandomInt(candidates.size()) + 1;
    Sequence inputSequence = createAnArray(candidates, elementType, length);

    inputSequences.add(inputSequence);
    variableIndices.add(totStatements + inputSequence.getLastVariable().index);

    // call Collections.addAll(c, inputArray)
    TypedOperation addOperation = getCollectionAddAllOperation(elementType);

    Sequence helperSequence = Sequence.concatenate(inputSequences);
    List<Variable> inputs = new ArrayList<>();
    for (int index : variableIndices) {
      inputs.add(helperSequence.getVariable(index));
    }
    return helperSequence.extend(addOperation, inputs);
  }



 /**
  * Creates a sequence that builds an array of the given element type using sequences from the
  * given list of candidates.
  *
  * @param candidates  the list of candidate elements
  * @param elementType  the type of elements for the array
  * @return a sequence that creates an array with the given element type
  */
 private static Sequence createAnArray(SimpleList<Sequence> candidates, GeneralType elementType, int length) {
   int totStatements = 0;
   List<Sequence> inputSequences = new ArrayList<>();
   List<Integer> variables = new ArrayList<>();
   for (int i = 0; i < length; i++) {
     Sequence inputSeq = candidates.get(Randomness.nextRandomInt(candidates.size()));
     Variable inputVar = inputSeq.randomVariableForTypeLastStatement(elementType);

     assert inputVar != null;
     variables.add(totStatements + inputVar.index);
     inputSequences.add(inputSeq);
     totStatements += inputSeq.size();
   }
   Sequence inputSequence = Sequence.concatenate(inputSequences);
   List<Variable> inputs = new ArrayList<>();
   for (Integer inputIndex : variables) {
     Variable v = inputSequence.getVariable(inputIndex);
     inputs.add(v);
   }

   return inputSequence.extend(TypedOperation.createArrayCreation(ArrayType.ofElementType(elementType), length), inputs);
 }

  private static TypedOperation getCollectionConstructor(ParameterizedType creationType) {
    Constructor<?> constructor;
    try {
      constructor = creationType.getRuntimeClass().getConstructor();
    } catch (NoSuchMethodException e) {
      throw new BugInRandoopException("Can't find default constructor for Collection " + creationType + ": " + e.getMessage());
    }
    ConstructorCall op = new ConstructorCall(constructor);
    return new TypedOperation(op, creationType, new TypeTuple(), creationType);
  }

  private static TypedOperation getCollectionAddAllOperation(ReferenceType elementType) {
    Class<?> collectionsClass = Collections.class;
    Method method;
    try {
      method = collectionsClass.getMethod("addAll", JDKTypes.COLLECTION_TYPE.getRuntimeClass(), (new Object[]{}).getClass());
    } catch (NoSuchMethodException e) {
      throw new BugInRandoopException("Can't find Collections.addAll method: " + e.getMessage());
    }
    MethodCall op = new MethodCall(method);
    assert method.getTypeParameters().length == 1: "method should have one type parameter";
    List<GeneralType> paramTypes = new ArrayList<>();
    ParameterizedType collectionType;
    collectionType = JDKTypes.COLLECTION_TYPE.instantiate(elementType);

    paramTypes.add(collectionType);
    paramTypes.add(ArrayType.ofElementType(elementType));

    return new TypedOperation(op, ClassOrInterfaceType.forClass(collectionsClass), new TypeTuple(paramTypes), ConcreteTypes.BOOLEAN_TYPE);
  }

}
