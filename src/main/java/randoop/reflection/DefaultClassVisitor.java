package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import randoop.condition.RandoopSpecificationError;

/** Default implementation of the {@code ClassVisitor} class. All methods do nothing. */
public class DefaultClassVisitor implements ClassVisitor {

  /** Creates a DefaultClassVisitor. */
  public DefaultClassVisitor() {}

  @Override
  public void visit(Class<?> c, ReflectionManager reflectionManager)
      throws RandoopSpecificationError {
    // default is to do nothing
  }

  @Override
  public void visit(Constructor<?> c) throws RandoopSpecificationError {
    // default is to do nothing
  }

  @Override
  public void visit(Method m) throws RandoopSpecificationError {
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
