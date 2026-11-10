package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import randoop.condition.RandoopSpecificationError;

/**
 * ClassVisitor defines the interface for a visitor class that uses reflection to collect
 * information about a class and its members.
 *
 * <p>Note: if the visitor maintains state relative to {@link #visitBefore(Class)} and other methods
 * that could be disrupted by visiting member classes (currently just enums), then a stack should be
 * used to maintain whatever state needs to be remembered. For instance, {@link OperationExtractor}
 * maintains the declaring class type for visited members, and when inner enums are visited the
 * declaring class needs to be remembered and restored by {@link #visitAfter(Class)}.
 *
 * @see ReflectionManager
 * @see OperationExtractor
 */
public interface ClassVisitor {

  /**
   * Perform action on member class.
   *
   * <p>The {@link ReflectionManager} is needed to allow visitors to initiate recursive visits if
   * needed.
   *
   * @param c the member class
   * @param reflectionManager the {@link ReflectionManager} that called this visitor
   * @throws RandoopSpecificationError if a specification for a member is malformed
   */
  void visit(Class<?> c, ReflectionManager reflectionManager) throws RandoopSpecificationError;

  /**
   * Perform action on a constructor.
   *
   * @param c the constructor
   * @throws RandoopSpecificationError if a specification for the constructor is malformed
   */
  void visit(Constructor<?> c) throws RandoopSpecificationError;

  /**
   * Perform an action on a method.
   *
   * @param m the method
   * @throws RandoopSpecificationError if a specification for the method is malformed
   */
  void visit(Method m) throws RandoopSpecificationError;

  /**
   * Perform an action on a field.
   *
   * @param f the field
   */
  void visit(Field f);

  /**
   * Perform an action on an enum value.
   *
   * @param e the enum value
   */
  void visit(Enum<?> e);

  /**
   * Perform an action on a class. Occurs before other visit methods are called.
   *
   * @param c the class to visit
   */
  void visitBefore(Class<?> c);

  /**
   * Perform an action on a class. Called after other visit methods are called.
   *
   * @param c the class to visit
   */
  void visitAfter(Class<?> c);
}
