package randoop.generation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import randoop.BugInRandoopException;
import randoop.main.GenInputsAbstract;
import randoop.operation.ConstructorCall;
import randoop.operation.MethodCall;
import randoop.operation.TypedClassOperation;
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

    private HelperSequenceCreator() {
    }

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

  static Sequence createCollection(ComponentManager componentManager, ParameterizedType collectionType) {
    assert ! collectionType.isGeneric() : "type must be instantiated";

    // get the element type
    List<TypeArgument> argumentList = collectionType.getTypeArguments();
    assert argumentList.size() == 1 : "Collection classes should have one type argument";
    TypeArgument argumentType = argumentList.get(0);
    ReferenceType elementType;
    assert argumentType instanceof ReferenceArgument : "type argument must be reference type";
    elementType = ((ReferenceArgument)argumentType).getReferenceType();

    // select implementing Collection type and instantiate
    GenericClassType implementingType = JDKTypes.getImplementingType(collectionType);
    ParameterizedType creationType;
    creationType = implementingType.instantiate(elementType);



    int totStatements = 0;
    List<Sequence> inputSequences = new ArrayList<>();
    List<Integer> variableIndices = new ArrayList<>();

    // build sequence to create a Collection object
    Sequence creationSequence = new Sequence();
    List<Variable> creationInputs = new ArrayList<>();
    TypedOperation creationOperation;
    if (implementingType.equals(JDKTypes.ENUM_SET_TYPE)) {
      /*
      * Not finishing this for current release, need to be able to handle terms other than nonreceivers
      creationSequence = creationSequence.extend(); // want initialization with value runtimeclassname.class
      creationOperation = getEnumSetCreation(creationType, elementType);
      */
      return null;
    } else {
      creationOperation = getCollectionConstructor(creationType);
    }
    creationSequence = creationSequence.extend(creationOperation, creationInputs);

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

  /**
   * Create the operation needed to create an empty EnumSet of the given type.
   *
   * @param creationType  the EnumSet type
   * @param elementType  the element Enum type
   * @return the empty EnumSet with the given type
   */
  private static TypedOperation getEnumSetCreation(ParameterizedType creationType, ReferenceType elementType) {
    Class<?> enumsetClass = JDKTypes.ENUM_SET_TYPE.getRuntimeClass();
    Method method;
    try {
      method = enumsetClass.getMethod("noneOf", ConcreteTypes.CLASS_TYPE.getRuntimeClass());
    } catch (NoSuchMethodException e) {
      throw new BugInRandoopException("Can't find \"noneOf\" method for EnumSet: " + e.getMessage());
    }
    MethodCall op = new MethodCall(method);
    List<GeneralType> paramTypes = new ArrayList<>();
    paramTypes.add(ConcreteTypes.CLASS_TYPE);
    return new TypedClassOperation(op, JDKTypes.ENUM_SET_TYPE, new TypeTuple(paramTypes), creationType);
  }

  /**
   * Create the constructor call operation for the given type.
   *
   * @param creationType  the class type
   * @return the constructor call for the given class type
   */
  private static TypedOperation getCollectionConstructor(ParameterizedType creationType) {
    Constructor<?> constructor;
    try {
      constructor = creationType.getRuntimeClass().getConstructor();
    } catch (NoSuchMethodException e) {
      throw new BugInRandoopException("Can't find default constructor for Collection " + creationType + ": " + e.getMessage());
    }
    ConstructorCall op = new ConstructorCall(constructor);
    return new TypedClassOperation(op, creationType, new TypeTuple(), creationType);
  }

  /**
   * Create the operation to call {@link java.util.Collections#addAll(Collection, Object[])} that
   * allows initialization of a {@link Collection} object.
   *
   * @param elementType  the element type of the collection
   * @return the operation to initialize a collection from an array.
   */
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

    return new TypedClassOperation(op, ClassOrInterfaceType.forClass(collectionsClass), new TypeTuple(paramTypes), ConcreteTypes.BOOLEAN_TYPE);
  }

}
