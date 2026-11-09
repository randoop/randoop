package randoop.reflection;

import java.util.LinkedHashSet;
import java.util.Set;
import randoop.condition.RandoopSpecificationError;
import randoop.types.ClassOrInterfaceType;

/**
 * A {@link ClassVisitor} that simply collects {@link ClassOrInterfaceType} objects for visited
 * {@link Class} objects.
 */
public class DeclarationExtractor extends DefaultClassVisitor {
  /** The set to which this visitor adds the types of visited classes. */
  private final Set<ClassOrInterfaceType> classDeclarationTypes;

  /** Determines which classes are collected; a class that does not satisfy it is ignored. */
  private ReflectionPredicate reflectionPredicate;

  /**
   * Creates a visitor that adds the type of each visited class to {@code classDeclarationTypes}.
   *
   * @param classDeclarationTypes the set to which to add the types of visited classes; is
   *     side-effected
   * @param reflectionPredicate determines which classes are collected
   */
  public DeclarationExtractor(
      Set<ClassOrInterfaceType> classDeclarationTypes, ReflectionPredicate reflectionPredicate) {
    super();
    this.classDeclarationTypes = classDeclarationTypes;
    this.reflectionPredicate = reflectionPredicate;
  }

  @Override
  public void visit(Class<?> c, ReflectionManager reflectionManager)
      throws RandoopSpecificationError {
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
   * Returns the classes.
   *
   * @param c the class
   * @param reflectionPredicate the reflection predicate
   * @param accessibilityPredicate the accessibility predicate
   * @return the classes that result from running a visitor
   * @throws RandoopSpecificationError if a specification for a member is malformed
   */
  public static Set<ClassOrInterfaceType> classTypes(
      Class<?> c,
      ReflectionPredicate reflectionPredicate,
      AccessibilityPredicate accessibilityPredicate)
      throws RandoopSpecificationError {
    ReflectionManager typeManager = new ReflectionManager(accessibilityPredicate);
    Set<ClassOrInterfaceType> result = new LinkedHashSet<>();
    typeManager.apply(new DeclarationExtractor(result, reflectionPredicate), c);
    return result;
  }
}
