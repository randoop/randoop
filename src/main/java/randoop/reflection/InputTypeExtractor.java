package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import randoop.types.ConcreteType;
import randoop.types.GeneralType;
import randoop.types.RandoopTypeException;

/**
 * {@code InputTypeExtractor} is a {@link ClassVisitor} that extracts concrete types that are used
 * in a class as either a parameter, a return type, or a field type.
 */
class InputTypeExtractor implements ClassVisitor {

  /** The set of concrete types */
  private Set<ConcreteType> inputTypes;

  /**
   * Creates a visitor that adds discovered concrete types to the given set.
   *
   * @param inputTypes  the set of concrete types
   */
  InputTypeExtractor(Set<ConcreteType> inputTypes) {
    this.inputTypes = inputTypes;
  }

  /**
   * {@inheritDoc}
   * Adds all concrete parameter types from the constructor to the input types set of this object.
   */
  @Override
  public void visit(Constructor<?> c) {
    for (Type paramType : c.getGenericParameterTypes()) {
      try {
        addIfConcrete(GeneralType.forType(paramType));
      } catch (RandoopTypeException e) {
        // do nothing
      }
    }
  }

  /**
   * {@inheritDoc}
   * Adds any concrete type among parameter and return types to the input types set of this object.
   */
  @Override
  public void visit(Method m) {
    for (Type paramType : m.getGenericParameterTypes()) {
      try {
        addIfConcrete(GeneralType.forType(paramType));
      } catch (RandoopTypeException e) {
        // do nothing
      }
    }
    Type returnType = m.getReturnType();
    try {
      addIfConcrete(GeneralType.forType(returnType));
    } catch (RandoopTypeException e) {
      // do nothing
    }

  }

  /**
   * {@inheritDoc}
   * Adds a concrete field type to the input types set of this object.
   */
  @Override
  public void visit(Field f) {
    Type fieldType = f.getGenericType();
    try {
      addIfConcrete(GeneralType.forType(fieldType));
    } catch (RandoopTypeException e) {
      // do nothing
    }
  }

  /**
   * Determines whether the given general type is not generic, and, if so, adds the concrete type
   * to the input types of this object.
   *
   * @param type  the general type
   */
  private void addIfConcrete(GeneralType type) {
    if (! type.isGeneric()) {
      inputTypes.add((ConcreteType)type);
    }
  }

  @Override
  public void visit(Enum<?> e) {
    // this is the enum constant, so nothing to do here
  }

  @Override
  public void visitBefore(Class<?> c) {
    // do nothing
  }

  @Override
  public void visitAfter(Class<?> c) {
    // do nothing
  }
}
