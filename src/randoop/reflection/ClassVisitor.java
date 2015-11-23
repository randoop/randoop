package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * ClassVisitor defines the interface for a visitor class that uses
 * reflection to collect information about a class and its members.
 * 
 * @see ReflectionManager
 * @see OperationExtractor
 * 
 * @author bjkeller
 *
 */
public interface ClassVisitor {
  void visit(Constructor<?> c);
  void visit(Method m);
  void visit(Field f);
  void visit(Enum<?> e);
  void visitBefore(Class<?> c);
  void visitAfter(Class<?> c);
}
