package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Default implementation of the {@code ClassVisitor} class. All methods do nothing. */
public class DefaultClassVisitor implements ClassVisitor {

  @Override
  public void visit(Class<?> c, ReflectionManager reflectionManager) {
    // default is to do nothing
  }

  @Override
  public void visit(Constructor<?> c) {
    // default is to do nothing
  }

  @Override
  public void visit(Method m) {
    // default is to do nothing
  }

  @Override
  public void visit(Field f) {
    // default is to do nothing
  }

  @Override
  public void visit(Enum<?> e) {
    // default is to do nothing
  }

  @Override
  public void visitBefore(Class<?> c) {
    // default is to do nothing
  }

  @Override
  public void visitAfter(Class<?> c) {
    // default is to do nothing
  }

  @Override
  public String toString() {
    return getClass().toString();
  }
}
