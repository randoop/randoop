package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ParameterizedType;
import randoop.types.PrimitiveType;
import randoop.types.Type;
import randoop.util.Log;

/**
 * {@code TypeExtractor} is a {@link ClassVisitor} that extracts both the class type, and concrete
 * types that are used in a class as either a parameter, a return type, or a field type.
 */
class TypeExtractor extends DefaultClassVisitor {

  /** The set of concrete types. */
  private Set<Type> inputTypes;

  /** The visibility predicate for checking whether a type is visible in generated tests. */
  private final VisibilityPredicate predicate;

  /**
   * Creates a visitor that adds discovered concrete types to the given set if they satisfy the
   * visibility predicate.
   *
   * @param inputTypes the set of concrete types
   * @param predicate the visibility predicate
   */
  TypeExtractor(Set<Type> inputTypes, VisibilityPredicate predicate) {
    this.inputTypes = inputTypes;
    this.predicate = predicate;
  }

  @Override
  public void visit(Class<?> c, ReflectionManager reflectionManager) {
    addIfConcrete(ClassOrInterfaceType.forClass(c));
    reflectionManager.apply(this, c);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Adds all concrete parameter types from the constructor to the input types set of this
   * object.
   */
  @Override
  public void visit(Constructor<?> c) {
    for (java.lang.reflect.Type paramType : c.getGenericParameterTypes()) {
      addIfConcrete(Type.forType(paramType));
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Adds any concrete type among parameter and return types to the input types set of this
   * object. Avoids bridge methods, because may have rawtypes not useful in building tests.
   */
  @Override
  public void visit(Method m) {
    if (m.isBridge()) {
      return;
    }
    for (java.lang.reflect.Type paramType : m.getGenericParameterTypes()) {
      addIfConcrete(Type.forType(paramType));
    }
    java.lang.reflect.Type returnType = m.getGenericReturnType();
    addIfConcrete(Type.forType(returnType));
  }

  /**
   * {@inheritDoc}
   *
   * <p>Adds a concrete field type to the input types set of this object.
   */
  @Override
  public void visit(Field f) {
    java.lang.reflect.Type fieldType = f.getGenericType();
    addIfConcrete(Type.forType(fieldType));
  }

  /**
   * Determines whether the given general type is not generic, and, if so, adds the concrete type to
   * the input types of this object.
   *
   * @param type the general type
   */
  private void addIfConcrete(Type type) {
    if (!type.isVoid()
        && !type.isGeneric()
        && !(type.isParameterized() && ((ParameterizedType) type).hasWildcard())) {
      if (!predicate.isVisible(type.getRuntimeClass())) {
        return;
      }
      if (type.isPrimitive()) {
        type = ((PrimitiveType) type).toBoxedPrimitive();
      }
      Log.logPrintf("Adding %s as candidate parameter type%n", type);
      inputTypes.add(type);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Adds the class if it is concrete.
   */
  @Override
  public void visitBefore(Class<?> c) {
    if (c.getTypeParameters().length == 0) {
      inputTypes.add(ClassOrInterfaceType.forClass(c));
    }
  }
}
