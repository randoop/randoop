package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import randoop.operation.NonreceiverTerm;
import randoop.sequence.Sequence;
import randoop.util.ClassFileConstants;
import randoop.util.MultiMap;

/**
 * {@code ClassLiteralExtractor} is a {@link ClassVisitor} that extracts literals from the bytecode
 * of each class visited, adding a sequence for each to a map associating a sequence with a type.
 */
public class ClassLiteralExtractor implements ClassVisitor {

  private MultiMap<Class<?>, Sequence> literalMap;

  public ClassLiteralExtractor(MultiMap<Class<?>, Sequence> literalMap) {
    this.literalMap = literalMap;
  }

  public MultiMap<Class<?>, Sequence> getLiteralMap() {
    return literalMap;
  }

  @Override
  public void visitBefore(Class<?> c) {
    Collection<ClassFileConstants.ConstantSet> constList = new ArrayList<>();
    constList.add(ClassFileConstants.getConstants(c.getName()));
    MultiMap<Class<?>, NonreceiverTerm> constantMap = ClassFileConstants.toMap(constList);
    for (Class<?> constantClass : constantMap.keySet()) {
      for (NonreceiverTerm term : constantMap.getValues(constantClass)) {
        literalMap.add(constantClass, Sequence.create(term));
      }
    }
  }

  @Override
  public void visit(Constructor<?> c) {
    // do nothing
  }

  @Override
  public void visit(Method m) {
    // do nothing
  }

  @Override
  public void visit(Field f) {
    // do nothing
  }

  @Override
  public void visit(Enum<?> e) {
    // do nothing
  }

  @Override
  public void visitAfter(Class<?> c) {
    // do nothing
  }
}
