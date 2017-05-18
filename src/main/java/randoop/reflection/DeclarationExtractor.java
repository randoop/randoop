package randoop.reflection;

import java.util.Set;
import randoop.types.ClassOrInterfaceType;

/**
 * A {@link ClassVisitor} that simply collects {@link ClassOrInterfaceType} objects for visited
 * {@link Class} objects.
 */
class DeclarationExtractor extends DefaultClassVisitor {
  private final Set<ClassOrInterfaceType> classDeclarationTypes;
  private ReflectionPredicate predicate;

  DeclarationExtractor(
      Set<ClassOrInterfaceType> classDeclarationTypes, ReflectionPredicate predicate) {
    this.classDeclarationTypes = classDeclarationTypes;
    this.predicate = predicate;
  }

  @Override
  public void visitBefore(Class<?> c) {
    if (!predicate.test(c)) {
      return;
    }
    classDeclarationTypes.add(ClassOrInterfaceType.forClass(c));
  }
}
