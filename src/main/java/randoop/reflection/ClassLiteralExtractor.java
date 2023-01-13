package randoop.reflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.plumelib.util.CollectionsPlume;
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.util.ClassFileConstants;
import randoop.util.MultiMap;

/**
 * {@code ClassLiteralExtractor} is a {@link ClassVisitor} that extracts literals from the bytecode
 * of each class visited, adding a sequence for each to a map associating a sequence with a type.
 *
 * @see OperationModel
 */
class ClassLiteralExtractor extends DefaultClassVisitor {

  /**
   * A map from a class under test to the set of literals, represented by {@link Sequence}s, that
   * occur within the class.
   */
  private MultiMap<ClassOrInterfaceType, Sequence> literalMap;

  /**
   * The map of literals to their term frequency: tf(t,d), where t is a literal and d is all classes
   * under test. Note that this is the raw frequency, just the number of times they occur within all
   * classes under test.
   */
  private final Map<Sequence, Integer> literalsTermFrequency;

  /**
   * Initializes the ClassLiteralExtractor with the given literal map and literal terms map.
   *
   * @param literalMap a map from a class under test to the set of literals that appear in that
   *     class
   * @param literalsTermFrequency a map of literals to their term frequency. A literal is
   *     represented by a {@link Sequence}.
   */
  ClassLiteralExtractor(
      MultiMap<ClassOrInterfaceType, Sequence> literalMap,
      Map<Sequence, Integer> literalsTermFrequency) {
    this.literalMap = literalMap;
    this.literalsTermFrequency = literalsTermFrequency;
  }

  @Override
  public void visitBefore(Class<?> c) {
    Collection<ClassFileConstants.ConstantSet> constList =
        Collections.singletonList(ClassFileConstants.getConstants(c.getName()));
    MultiMap<Class<?>, NonreceiverTerm> constantMap = ClassFileConstants.toMap(constList);
    for (Class<?> constantClass : constantMap.keySet()) {
      ClassOrInterfaceType constantType = ClassOrInterfaceType.forClass(constantClass);
      for (NonreceiverTerm term : constantMap.getValues(constantClass)) {
        Sequence seq =
            new Sequence()
                .extend(
                    TypedOperation.createNonreceiverInitialization(term),
                    new ArrayList<Variable>(0));
        literalMap.add(constantType, seq);
        CollectionsPlume.incrementMap(literalsTermFrequency, seq, term.getFrequency());
      }
    }
  }
}
