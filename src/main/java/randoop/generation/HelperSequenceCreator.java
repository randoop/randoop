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
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.JDKTypes;
import randoop.types.ParameterizedType;
import randoop.types.ReferenceArgument;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeArgument;
import randoop.types.TypeTuple;
import randoop.util.ArrayListSimpleList;
import randoop.util.Log;
import randoop.util.Randomness;
import randoop.util.SimpleList;

class HelperSequenceCreator {

  private HelperSequenceCreator() {
    throw new Error("Do not instantiate");
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
  static SimpleList<Sequence> createArraySequence(
      ComponentManager components, Type collectionType) {

    final int MAX_LENGTH = 7;

    if (!collectionType.isArray()) {
      return new ArrayListSimpleList<>();
    }

    ArrayType arrayType = (ArrayType) collectionType;
    Type elementType = arrayType.getElementType();

    if (elementType.isParameterized()) {
      // XXX build elementType default construction sequence here, if cannot build one then stop
      InstantiatedType creationType = getImplementingType((InstantiatedType) elementType);
      if (getConstructor(creationType) == null) {
        /* If has no visible default constructor, */
        // XXX Note: OK to have any constructor that is easy to call, just need a canonical object
        /* If element type is C<T extends C<T>, so use T */
        if (creationType.isRecursiveType()) {
          // XXX being incautious, argument type might be parameterized
          elementType =
              ((ReferenceArgument) creationType.getTypeArguments().get(0)).getReferenceType();
        } else {
          /*
              - should look for static creation method c : () -> C<T> and use that
              - otherwise/for now, return nothing
          */
          if (Log.isLoggingOn()) {
            Log.logLine(
                "creating array of "
                    + elementType
                    + " failed because cannot create "
                    + creationType);
          }

          return new ArrayListSimpleList<>();
        }
      }
    }

    SimpleList<Sequence> candidates = components.getSequencesForType(elementType);
    int length;
    if (candidates.isEmpty()) {
      // No sequences that produce appropriate component values found,
      // if null allowed, create an array containing null, otherwise create empty array
      ArrayListSimpleList<Sequence> seqList = new ArrayListSimpleList<>();
      if (!GenInputsAbstract.forbid_null) {
        if (!Randomness.weighedCoinFlip(0.5)) {
          seqList.add(
              new Sequence()
                  .extend(TypedOperation.createNullOrZeroInitializationForType(elementType)));
        }
      }
      length = seqList.size();
      candidates = seqList;
    } else {
      length = Randomness.nextRandomInt(MAX_LENGTH);
    }
    Sequence s = createAnArray(candidates, elementType, length);
    assert s != null;
    ArrayListSimpleList<Sequence> l = new ArrayListSimpleList<>();
    l.add(s);
    return l;
  }

  /**
   * Generates a sequence that creates a Collection.
   *
   * @param componentManager  the component manager for selecting values
   * @param collectionType  the type for collection
   * @return a collection of the given type
   */
  static Sequence createCollection(
      ComponentManager componentManager, ParameterizedType collectionType) {
    assert !collectionType.isGeneric() : "type must be instantiated";

    // get the element type
    List<TypeArgument> argumentList = collectionType.getTypeArguments();
    assert argumentList.size() == 1 : "Collection classes should have one type argument";
    TypeArgument argumentType = argumentList.get(0);
    ReferenceType elementType;
    assert argumentType instanceof ReferenceArgument : "type argument must be reference type";
    elementType = ((ReferenceArgument) argumentType).getReferenceType();

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
      NonreceiverTerm classLiteral =
          new NonreceiverTerm(JavaTypes.CLASS_TYPE, elementType.getRuntimeClass());
      creationSequence =
          creationSequence.extend(TypedOperation.createNonreceiverInitialization(classLiteral));
      creationInputs.add(creationSequence.getLastVariable());
      creationOperation = getEnumSetCreation(creationType);
    } else {
      creationOperation = getCollectionConstructor(creationType);
    }
    creationSequence = creationSequence.extend(creationOperation, creationInputs);
    inputSequences.add(creationSequence);
    int creationIndex = totStatements + creationSequence.getLastVariable().index;
    variableIndices.add(creationIndex);
    totStatements += creationSequence.size();

    SimpleList<Sequence> candidates = componentManager.getSequencesForType(elementType);
    int length = 0;
    if (!candidates.isEmpty()) {
      length = Randomness.nextRandomInt(candidates.size()) + 1;
    }
    assert !candidates.isEmpty() || length == 0 : "if there are no candidates, length must be zero";

    if (!elementType.isParameterized()) {
      // build sequence to create array of element type
      Sequence inputSequence = createAnArray(candidates, elementType, length);
      inputSequences.add(inputSequence);
      variableIndices.add(totStatements + inputSequence.getLastVariable().index);
      // call Collections.addAll(c, inputArray)
      TypedOperation addOperation = getCollectionAddAllOperation(elementType);
      return Sequence.createSequence(addOperation, inputSequences, variableIndices);
    } else {
      // build sequence creating selected values
      List<Integer> variables = new ArrayList<>();
      createElementSequences(
          candidates, length, elementType, inputSequences, totStatements, variables);
      Sequence addSequence = Sequence.concatenate(inputSequences);
      // add each value to the collection
      for (Integer index : variables) {
        List<Variable> inputs = new ArrayList<>();
        inputs.add(addSequence.getVariable(creationIndex));
        inputs.add(addSequence.getVariable(index));
        addSequence = addSequence.extend(getAddOperation(collectionType, elementType), inputs);
      }
      return addSequence;
    }
  }

  /**
   * Creates a sequence that builds an array of the given element type using sequences from the
   * given list of candidates.
   *
   * @param candidates  the list of candidate elements
   * @param elementType  the type of elements for the array
   * @param length  the length of the array
   * @return a sequence that creates an array with the given element type
   */
  private static Sequence createAnArray(
      SimpleList<Sequence> candidates, Type elementType, int length) {
    assert !candidates.isEmpty() || length == 0 : "if there are no candidates, length must be zero";
    List<Sequence> inputSequences = new ArrayList<>();
    List<Integer> variables = new ArrayList<>();
    createElementSequences(candidates, length, elementType, inputSequences, 0, variables);

    ArrayType arrayType = ArrayType.ofElementType(elementType);
    if (!elementType.isParameterized()) {
      TypedOperation creationOperation = TypedOperation.createArrayCreation(arrayType, length);
      return Sequence.createSequence(creationOperation, inputSequences, variables);
    } else {
      Sequence creationSequence =
          createGenericArrayCreationSequence((InstantiatedType) elementType, length);
      inputSequences.add(creationSequence);

      TypedOperation arrayElementAssignment =
          TypedOperation.createArrayElementAssignment(arrayType);
      Sequence addSequence = Sequence.concatenate(inputSequences);
      int creationIndex = addSequence.getLastVariable().index;
      int i = 0;
      for (Integer index : variables) {
        addSequence =
            addSequence.extend(TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, i));
        List<Variable> inputs = new ArrayList<>();
        inputs.add(addSequence.getVariable(creationIndex));
        inputs.add(addSequence.getLastVariable());
        inputs.add(addSequence.getVariable(index));
        addSequence = addSequence.extend(arrayElementAssignment, inputs);
        i++;
      }
      return addSequence;
    }
  }

  /**
   * Creates a {@link Sequence} creates an array using reflection.
   * This is necessary for creating an array with parameterized type.
   * For instance, to create a <code>List&lt;String&gt;[]</code>, the code is
   * <pre><code>
   *   Class&lt;?&gt; componentType = (new ArrayList&lt;String&gt;()).getClass();
   *   &#64;SuppressWarnings("unchecked")
   *   List&lt;String&gt;[] a = (List&lt;String&gt;[])(Array.newInstance(componentType, length));
   * </code></pre>
   * Note that the {@code SuppressWarnings} annotation is added when the assignment with the cast
   * is output.
   *
   * @param elementType  the type of the array element
   * @param length  the length of the array to be created
   * @return the sequence to create an array with the given element type and length
   */
  private static Sequence createGenericArrayCreationSequence(
      InstantiatedType elementType, int length) {
    InstantiatedType creationType = getImplementingType(elementType);
    Substitution<ReferenceType> substitution = creationType.getTypeSubstitution();

    Sequence creationSequence = new Sequence();

    // get constructor for element type, create object
    Constructor<?> constructor = getConstructor(creationType);
    assert constructor != null : "cannot procede if not able to build default object";
    TypedOperation elementTypeConstructor =
        TypedOperation.forConstructor(constructor).apply(substitution);
    // elementTypeObject = new ElementType();
    creationSequence = creationSequence.extend(elementTypeConstructor, new ArrayList<Variable>());

    Method getClassMethod;
    try {
      getClassMethod = (Object.class).getMethod("getClass");
    } catch (NoSuchMethodException e) {
      throw new BugInRandoopException("Cannot find Object.getClass(): " + e.getMessage());
    }
    TypedOperation getClassMethodOp = TypedOperation.forMethod(getClassMethod);
    // componentType = elementTypeObject.getClass()
    List<Variable> input = new ArrayList<>();
    input.add(creationSequence.getLastVariable());
    creationSequence = creationSequence.extend(getClassMethodOp, input);
    int typeIndex = creationSequence.getLastVariable().index;

    // Array.newInstance(componentType, length)
    TypedOperation lengthTerm =
        TypedOperation.createNonreceiverInitialization(
            new NonreceiverTerm(JavaTypes.INT_TYPE, length));
    creationSequence = creationSequence.extend(lengthTerm, new ArrayList<Variable>());
    input = new ArrayList<>();
    input.add(creationSequence.getVariable(typeIndex));
    input.add(creationSequence.getLastVariable());

    Method newInstanceMethod;
    try {
      newInstanceMethod =
          (java.lang.reflect.Array.class).getMethod("newInstance", Class.class, int.class);
    } catch (NoSuchMethodException e) {
      throw new BugInRandoopException("Cannot find Array.newInstance(): " + e.getMessage());
    }
    TypedOperation creationOperation = TypedOperation.forMethod(newInstanceMethod);
    creationSequence = creationSequence.extend(creationOperation, input);

    TypedOperation castOperation =
        TypedOperation.createCast(JavaTypes.OBJECT_TYPE, ArrayType.ofElementType(elementType));
    input = new ArrayList<>();
    input.add(creationSequence.getLastVariable());
    creationSequence = creationSequence.extend(castOperation, input);
    return creationSequence;
  }

  private static Constructor<?> getConstructor(ClassOrInterfaceType creationType) {
    Constructor<?> constructor;
    try {
      constructor = creationType.getRuntimeClass().getConstructor();
    } catch (NoSuchMethodException e) {
      return null;
    }
    return constructor;
  }

  private static InstantiatedType getImplementingType(InstantiatedType elementType) {
    InstantiatedType creationType = elementType;
    if (elementType.getGenericClassType().isSubtypeOf(JDKTypes.COLLECTION_TYPE)
        && elementType.getPackage().equals(JDKTypes.COLLECTION_TYPE.getPackage())) {
      GenericClassType implementingType = JDKTypes.getImplementingType(elementType);
      List<ReferenceType> typeArgumentList = new ArrayList<>();
      for (TypeArgument argument : elementType.getTypeArguments()) {
        assert (argument instanceof ReferenceArgument)
            : "all arguments should be ReferenceArgument";
        typeArgumentList.add(((ReferenceArgument) argument).getReferenceType());
      }
      creationType = implementingType.instantiate(typeArgumentList);
    }
    return creationType;
  }

  /**
   * Selects sequences as values for creating a collection.
   *
   * @param candidates  the sequences from which to select
   * @param length  the number of values to select
   * @param elementType  the type of elements
   * @param inputSequences  the prior sequences in the construction
   * @param totStatements  the number of previous statements
   * @param variables  the list of variable indicies
   */
  private static void createElementSequences(
      SimpleList<Sequence> candidates,
      int length,
      Type elementType,
      List<Sequence> inputSequences,
      int totStatements,
      List<Integer> variables) {
    for (int i = 0; i < length; i++) {
      Sequence inputSeq = candidates.get(Randomness.nextRandomInt(candidates.size()));
      inputSequences.add(inputSeq);
      Variable inputVar = inputSeq.randomVariableForTypeLastStatement(elementType);
      assert inputVar != null;
      variables.add(totStatements + inputVar.index);
      totStatements += inputSeq.size();
    }
  }

  /**
   * Create the operation needed to create an empty EnumSet of the given type.
   *
   * @param creationType  the EnumSet type
   * @return the empty EnumSet with the given type
   */
  private static TypedOperation getEnumSetCreation(ParameterizedType creationType) {
    Class<?> enumsetClass = JDKTypes.ENUM_SET_TYPE.getRuntimeClass();
    Method method;
    try {
      method = enumsetClass.getMethod("noneOf", JavaTypes.CLASS_TYPE.getRuntimeClass());
    } catch (NoSuchMethodException e) {
      throw new BugInRandoopException(
          "Can't find \"noneOf\" method for EnumSet: " + e.getMessage());
    }
    MethodCall op = new MethodCall(method);
    List<Type> paramTypes = new ArrayList<>();
    paramTypes.add(JavaTypes.CLASS_TYPE);
    return new TypedClassOperation(op, creationType, new TypeTuple(paramTypes), creationType);
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
      throw new BugInRandoopException(
          "Can't find default constructor for Collection " + creationType + ": " + e.getMessage());
    }
    ConstructorCall op = new ConstructorCall(constructor);
    return new TypedClassOperation(op, creationType, new TypeTuple(), creationType);
  }

  /**
   * Create a method call operation for the <code>add()</code> method of the given collection type.
   *
   * @param collectionType  the collection type
   * @param elementType  the element type of the collection
   * @return return an operation to add elements to the collection type
   */
  private static TypedOperation getAddOperation(
      ParameterizedType collectionType, ReferenceType elementType) {
    Method addMethod;
    try {
      addMethod = collectionType.getRuntimeClass().getMethod("add", Object.class);
    } catch (NoSuchMethodException e) {
      throw new BugInRandoopException(
          "Can't find add() method for " + collectionType + ": " + e.getMessage());
    }
    MethodCall op = new MethodCall(addMethod);
    List<Type> arguments = new ArrayList<>();
    arguments.add(collectionType);
    arguments.add(elementType);
    return new TypedClassOperation(
        op, collectionType, new TypeTuple(arguments), JavaTypes.BOOLEAN_TYPE);
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
      method =
          collectionsClass.getMethod(
              "addAll", JDKTypes.COLLECTION_TYPE.getRuntimeClass(), (new Object[] {}).getClass());
    } catch (NoSuchMethodException e) {
      throw new BugInRandoopException("Can't find Collections.addAll method: " + e.getMessage());
    }
    MethodCall op = new MethodCall(method);
    assert method.getTypeParameters().length == 1 : "method should have one type parameter";
    List<Type> paramTypes = new ArrayList<>();
    ParameterizedType collectionType;
    collectionType = JDKTypes.COLLECTION_TYPE.instantiate(elementType);

    paramTypes.add(collectionType);
    paramTypes.add(ArrayType.ofElementType(elementType));

    return new TypedClassOperation(
        op,
        ClassOrInterfaceType.forClass(collectionsClass),
        new TypeTuple(paramTypes),
        JavaTypes.BOOLEAN_TYPE);
  }
}
