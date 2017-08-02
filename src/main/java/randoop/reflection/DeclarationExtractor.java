package randoop.reflection;

import java.util.Set;
import randoop.types.ClassOrInterfaceType;

/**
 * A {@link ClassVisitor} that simply collects {@link ClassOrInterfaceType} objects for visited
 * {@link Class} objects.
 */
public class DeclarationExtractor extends DefaultClassVisitor {
  private final Set<ClassOrInterfaceType> classDeclarationTypes;
  private ReflectionPredicate predicate;

  public DeclarationExtractor(
      Set<ClassOrInterfaceType> classDeclarationTypes, ReflectionPredicate predicate) {
    this.classDeclarationTypes = classDeclarationTypes;
    this.predicate = predicate;
  }

  @Override
  public void visit(Class<?> c, ReflectionManager reflectionManager) {
    if (!predicate.test(c)) {
      return;
    }
    classDeclarationTypes.add(ClassOrInterfaceType.forClass(c));
    reflectionManager.apply(this, c);
  }

  @Override
  public void visitBefore(Class<?> c) {
    if (!predicate.test(c)) {
      return;
    }
    classDeclarationTypes.add(ClassOrInterfaceType.forClass(c));
  }
}
