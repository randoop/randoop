package randoop.reflection;

import java.util.LinkedHashSet;
import java.util.Set;
import randoop.types.ClassOrInterfaceType;

/**
 * A {@link ClassVisitor} that simply collects {@link ClassOrInterfaceType} objects for visited
 * {@link Class} objects.
 */
public class DeclarationExtractor extends DefaultClassVisitor {
  private final Set<ClassOrInterfaceType> classDeclarationTypes;
  private ReflectionPredicate reflectionPredicate;

  public DeclarationExtractor(
      Set<ClassOrInterfaceType> classDeclarationTypes, ReflectionPredicate reflectionPredicate) {
    this.classDeclarationTypes = classDeclarationTypes;
    this.reflectionPredicate = reflectionPredicate;
  }

  @Override
  public void visit(Class<?> c, ReflectionManager reflectionManager) {
    if (!reflectionPredicate.test(c)) {
      return;
    }
    classDeclarationTypes.add(ClassOrInterfaceType.forClass(c));
    reflectionManager.apply(this, c);
  }

  @Override
  public void visitBefore(Class<?> c) {
    if (!reflectionPredicate.test(c)) {
      return;
    }
    classDeclarationTypes.add(ClassOrInterfaceType.forClass(c));
  }

  /**
   * Return the classes.
   *
   * @param c the class
   * @param reflectionPredicate the reflection predicate
   * @param accessibilityPredicate the accessibility predicate
   * @return the classes that result from running a visitor
   */
  public static Set<ClassOrInterfaceType> classTypes(
      Class<?> c,
      ReflectionPredicate reflectionPredicate,
      AccessibilityPredicate accessibilityPredicate) {
    ReflectionManager typeManager = new ReflectionManager(accessibilityPredicate);
    Set<ClassOrInterfaceType> result = new LinkedHashSet<>();
    typeManager.apply(new DeclarationExtractor(result, reflectionPredicate), c);
    return result;
  }
}
