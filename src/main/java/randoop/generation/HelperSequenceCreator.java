package randoop.generation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import randoop.BugInRandoopException;
import randoop.main.GenInputsAbstract;
import randoop.operation.ConstructorCall;
import randoop.operation.MethodCall;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ArrayType;
import randoop.types.ConcreteTypes;
import randoop.types.GeneralType;
import randoop.types.GenericClassType;
import randoop.types.JDKTypes;
import randoop.types.ParameterizedType;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeArgument;
import randoop.types.TypeTuple;
import randoop.util.ArrayListSimpleList;
import randoop.util.Randomness;
import randoop.types.Match;
import randoop.util.SimpleList;

public class HelperSequenceCreator {

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
  public static SimpleList<Sequence> createArraySequence(ComponentManager components, GeneralType collectionType) {

    if (!collectionType.isArray()) {
      return new ArrayListSimpleList<Sequence>();
    }

    ArrayType arrayType = (ArrayType)collectionType;
    GeneralType elementType = arrayType.getElementType();

    Sequence s = null;
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

  public static Sequence createCollection(ComponentManager componentManager, ConcreteType inputType) {
   if (!inputType.isParameterized()) {
     throw new IllegalArgumentException("type must be parameterized");
   }
   assert ! inputType.isGeneric() : "type must be instantiated";

   // get the element type
   ParameterizedType collectionType = (ParameterizedType)inputType;
   List<ConcreteType> argumentList = collectionType.getTypeArguments();
   assert argumentList.size() == 1 : "Collection classes should have one type argument";
   ConcreteType elementType = argumentList.get(0);

   int totStatements = 0;
   List<Sequence> inputSequences = new ArrayList<>();
   List<Integer> variableIndices = new ArrayList<>();

   // select implementing Collection type and instantiate
   GenericClassType implementingType = JDKTypes.getImplementingType(collectionType);
   ParameterizedType creationType;
   try {
     creationType = (ParameterizedType)implementingType.instantiate(elementType);
   } catch (RandoopTypeException e) {
     throw new BugInRandoopException("type error instantiating Collection: " + e.getMessage());
   }

   // build sequence to create a Collection object
   ConcreteOperation creationOperation = getCollectionConstructor(creationType);
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
   totStatements += inputSequence.size();

   // call Collections.addAll(c, inputArray)
   ConcreteOperation addOperation = getCollectionAddAllOperation(elementType);

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
 private static Sequence createAnArray(SimpleList<Sequence> candidates, ConcreteType elementType, int length) {
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

   return inputSequence.extend(ConcreteOperation.createArrayCreation(new ConcreteArrayType(elementType), length), inputs);
 }

 public static ConcreteOperation getCollectionConstructor(ParameterizedType creationType) {
    Constructor<?> constructor = null;
    try {
      constructor = creationType.getRuntimeClass().getConstructor();
    } catch (NoSuchMethodException e) {
            throw new BugInRandoopException("Can't find default constructor for Collection " + creationType + ": " + e.getMessage());
    }
    ConstructorCall op = new ConstructorCall(constructor);
    return new ConcreteOperation(op, creationType, new ConcreteTypeTuple(), creationType);
 }

 public static ConcreteOperation getCollectionAddAllOperation(ConcreteType elementType) {
     Class<?> collectionsClass = Collections.class;
     Method method = null;
     try {
       method = collectionsClass.getMethod("addAll", JDKTypes.COLLECTION_TYPE.getRuntimeClass(), (new Object[]{}).getClass());
     } catch (NoSuchMethodException e) {
       throw new BugInRandoopException("Can't find Collections.addAll method: " + e.getMessage());
     }
     MethodCall op = new MethodCall(method);
     assert method.getTypeParameters().length == 1: "method should have one type parameter";
     List<ConcreteType> paramTypes = new ArrayList<>();
     ParameterizedType collectionType;
     try {
       collectionType = (ParameterizedType)JDKTypes.COLLECTION_TYPE.instantiate(elementType);
     } catch (RandoopTypeException e) {
       throw new BugInRandoopException("type error instantiating Collection: " + e.getMessage());
     }
     paramTypes.add(collectionType);
     paramTypes.add(new ConcreteArrayType(elementType));

     ConcreteOperation addAllOperation;
     try {
       addAllOperation = new ConcreteOperation(op, ConcreteType.forClass(collectionsClass), new ConcreteTypeTuple(paramTypes), ConcreteTypes.BOOLEAN_TYPE);
     } catch (RandoopTypeException e) {
       throw new BugInRandoopException("type error constructing Collections.addAll method: " + e.getMessage());
     }
     return addAllOperation;
   }

}
