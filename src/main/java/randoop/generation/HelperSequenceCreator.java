package randoop.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import randoop.SeedSequences;
import randoop.main.GenInputsAbstract;
import randoop.operation.ArrayCreation;
import randoop.operation.ConcreteOperation;
import randoop.operation.NonreceiverTerm;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ConcreteArrayType;
import randoop.types.ConcreteType;
import randoop.util.ArrayListSimpleList;
import randoop.util.Randomness;
import randoop.util.Reflection.Match;
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
   * @param type  the query type
   * @return the singleton list containing the compatible sequence
   */
  public static SimpleList<Sequence> createSequence(ComponentManager components, ConcreteType type) {

    if (!type.isArray()) {
      return new ArrayListSimpleList<Sequence>();
    }

    ConcreteArrayType arrayType = (ConcreteArrayType)type;
    ConcreteType elementType = arrayType.getElementType();

    Sequence s = null;

    if (elementType.isPrimitive()) {
      s = randPrimitiveArray(elementType);
    } else {
      SimpleList<Sequence> candidates =
          components.getSequencesForType(elementType, false);
      if (candidates.isEmpty()) {
        // No sequences that produce appropriate component values found, and
        if (GenInputsAbstract.forbid_null) {
          // use of null is forbidden. So, return the empty array.
          s = new Sequence().extend(ConcreteOperation.createArrayCreation(arrayType, 0));
        } else {
          // null is allowed.
          s = new Sequence();
          List<Variable> ins = new ArrayList<>();
          ConcreteOperation declOp;
          if (Randomness.weighedCoinFlip(0.5)) {
            declOp = ConcreteOperation.createArrayCreation(arrayType, 0);
          } else {
            s = s.extend(ConcreteOperation.createNullOrZeroInitializationForType(elementType));
            ins.add(s.getVariable(0));
            declOp = ConcreteOperation.createArrayCreation(arrayType, 1);
          }
          s = s.extend(declOp, ins);
        }
      } else {
        // Return the array [ x ] where x is the last value in the sequence.
        ConcreteOperation declOp = ConcreteOperation.createArrayCreation(arrayType, 1);
        s = candidates.get(Randomness.nextRandomInt(candidates.size()));
        List<Variable> ins = new ArrayList<>();
        // XXX IS THIS OLD COMMENT TRUE? : this assumes that last statement will
        // have such a var,
        // which I know is currently true because of SequenceCollection
        // implementation.
        ins.add(
            s.randomVariableForTypeLastStatement(elementType, Match.COMPATIBLE_TYPE));
        s = s.extend(declOp, ins);
      }
    }
    assert s != null;
    ArrayListSimpleList<Sequence> l = new ArrayListSimpleList<>();
    l.add(s);
    return l;
  }

  private static Sequence randPrimitiveArray(ConcreteType componentType) {
    assert componentType.isPrimitive();
    Set<Object> potentialElts = SeedSequences.getSeeds(componentType);
    int length = Randomness.nextRandomInt(4);
    Sequence s = new Sequence();
    List<Variable> emptylist = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      Object elt = Randomness.randomSetMember(potentialElts);
      s = s.extend(ConcreteOperation.createPrimitiveInitialization(componentType, elt), emptylist);
    }
    List<Variable> inputs = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      inputs.add(s.getVariable(i));
    }
    s = s.extend(ConcreteOperation.createArrayCreation(new ConcreteArrayType(componentType), length), inputs);
    return s;
  }
}
