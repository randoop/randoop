package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface ClassVisitor {
  void visit(Constructor<?> c);
  void visit(Method m);
  void visit(Field f);
  void visit(Enum<?> e);
  void visitBefore(Class<?> c);
  void visitAfter(Class<?> c);
}
