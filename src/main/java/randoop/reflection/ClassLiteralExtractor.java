package randoop.reflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

  private MultiMap<ClassOrInterfaceType, Sequence> literalMap;

  ClassLiteralExtractor(MultiMap<ClassOrInterfaceType, Sequence> literalMap) {
    this.literalMap = literalMap;
  }

  @Override
  public void visitBefore(Class<?> c) {
    ClassFileConstants.ConstantSet constantSet = ClassFileConstants.getConstants(c.getName());
    MultiMap<Class<?>, NonreceiverTerm> constantMap = new MultiMap<>();
    ClassFileConstants.addToConstantMap(constantSet, constantMap);
    for (Class<?> constantClass : constantMap.keySet()) {
      ClassOrInterfaceType constantType = ClassOrInterfaceType.forClass(constantClass);
      for (NonreceiverTerm term : constantMap.getValues(constantClass)) {
        Sequence seq =
            new Sequence()
                .extend(
                    TypedOperation.createNonreceiverInitialization(term),
                    new ArrayList<Variable>(0));
        literalMap.add(constantType, seq);
      }
    }
  }

  public static void main(String[] args) {
    ClassLiteralExtractor extractor = new ClassLiteralExtractor(new MultiMap<>());
    extractor.visitBefore(randoop.reflection.test.ClassOne.class);
    extractor.visitBefore(randoop.reflection.test.ClassTwo.class);
    System.out.println(extractor.literalMap);
  }
}
