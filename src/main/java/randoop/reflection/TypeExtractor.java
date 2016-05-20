package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import randoop.types.ClassOrInterfaceType;
import randoop.types.GeneralType;

/**
 * {@code TypeExtractor} is a {@link ClassVisitor} that extracts both the class type, and concrete
 * types that are used in a class as either a parameter, a return type, or a field type.
 */
class TypeExtractor extends DefaultClassVisitor {

  /** The set of concrete types */
  private Set<GeneralType> inputTypes;

  /**
   * Creates a visitor that adds discovered concrete types to the given set.
   *
   * @param inputTypes  the set of concrete types
   */
  TypeExtractor(Set<GeneralType> inputTypes) {
    this.inputTypes = inputTypes;
  }

  /**
   * {@inheritDoc}
   * Adds all concrete parameter types from the constructor to the input types set of this object.
   */
  @Override
  public void visit(Constructor<?> c) {
    for (Type paramType : c.getGenericParameterTypes()) {
      addIfConcrete(GeneralType.forType(paramType));
    }
  }

  /**
   * {@inheritDoc}
   * Adds any concrete type among parameter and return types to the input types set of this object.
   */
  @Override
  public void visit(Method m) {
    for (Type paramType : m.getGenericParameterTypes()) {
      addIfConcrete(GeneralType.forType(paramType));
    }
    Type returnType = m.getReturnType();
    addIfConcrete(GeneralType.forType(returnType));

  }

  /**
   * {@inheritDoc}
   * Adds a concrete field type to the input types set of this object.
   */
  @Override
  public void visit(Field f) {
    Type fieldType = f.getGenericType();
    addIfConcrete(GeneralType.forType(fieldType));
  }

  /**
   * Determines whether the given general type is not generic, and, if so, adds the concrete type
   * to the input types of this object.
   *
   * @param type  the general type
   */
  private void addIfConcrete(GeneralType type) {
    if (! type.isGeneric() && ! type.isVoid() && ! type.hasWildcard()) {
      if (type.isPrimitive()) {
        type = type.toBoxedPrimitive();
      }
      inputTypes.add(type);
    }
  }

  /**
   * {@inheritDoc}
   * Adds the class if it is concrete.
   */
  @Override
  public void visitBefore(Class<?> c) {
    if (c.getTypeParameters().length == 0) {
      inputTypes.add(ClassOrInterfaceType.forClass(c));
    }
  }

}
