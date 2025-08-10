package randoop.reflection;

import java.util.ArrayList;
import java.util.Set;
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

  /** Map from a class under test to the literal sequences that appear in it. */
  private MultiMap<ClassOrInterfaceType, Sequence> literalMap;

  /**
   * Creates a visitor that adds discovered literals to the given map.
   *
   * @param literalMap a map from types to sequences in them that yield a constant
   */
  ClassLiteralExtractor(MultiMap<ClassOrInterfaceType, Sequence> literalMap) {
    this.literalMap = literalMap;
  }

  /**
   * {@inheritDoc}
   *
   * <p>For each class, this adds a sequence that creates a value of the class type to the literal
   * map.
   */
  @Override
  public void visitBefore(Class<?> c) {
    ClassOrInterfaceType constantType = ClassOrInterfaceType.forClass(c);
    ClassFileConstants.ConstantSet constantSet = ClassFileConstants.getConstants(c.getName());
    Set<NonreceiverTerm> nonreceiverTerms =
        ClassFileConstants.constantSetToNonreceiverTerms(constantSet);
    for (NonreceiverTerm term : nonreceiverTerms) {
      Sequence seq =
          new Sequence()
              .extend(
                  TypedOperation.createNonreceiverInitialization(term), new ArrayList<Variable>(0));
      literalMap.add(constantType, seq);
    }
  }
}
