package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import randoop.field.FinalInstanceField;
import randoop.field.InstanceField;
import randoop.field.StaticField;
import randoop.field.StaticFinalField;
import randoop.operation.ConcreteOperation;
import randoop.operation.ConstructorCall;
import randoop.operation.EnumConstant;
import randoop.operation.FieldGet;
import randoop.operation.FieldSet;
import randoop.operation.GenericOperation;
import randoop.operation.MethodCall;
import randoop.operation.Operation;
import randoop.types.ConcreteSimpleType;
import randoop.types.ConcreteType;
import randoop.types.GeneralType;
import randoop.types.GenericClassType;
import randoop.types.GenericType;
import randoop.util.MultiMap;

/**
 * OperationExtractor is a {@link ClassVisitor} that creates a collection of
 * {@link Operation} objects through its visit methods as called by
 * {@link ReflectionManager#apply(Class)}.
 *
 * @see ReflectionManager
 * @see ClassVisitor
 *
 */
public class OperationExtractor implements ClassVisitor {

  /** The set of concrete class types encountered */
  private final Set<ConcreteType> classTypes;

  /** The map of generic types to operations */
  private final MultiMap<GenericType, GenericOperation> genericClassTypes;

  /** The set of concrete operations encountered */
  private Set<ConcreteOperation> operations;

  /** The current class type */
  private GeneralType classType;

  /**
   * Creates a visitor object that collects Operation objects corresponding to
   * class members visited by {@link ReflectionManager}. Stores
   * {@link Operation} objects in an ordered collection to ensure they are
   * strictly ordered once flattened to a list. This is needed to guarantee
   * determinism between Randoop runs with the same classes and parameters.
   */
  public OperationExtractor(Set<ConcreteType> classTypes, Set<ConcreteOperation> operations, MultiMap<GenericType, GenericOperation> genericClassTypes) {
    this.classTypes = classTypes;
    this.operations = operations;
    this.genericClassTypes = genericClassTypes;
  }

  /**
   * Creates a {@link ConstructorCall} object for the {@link Constructor}.
   *
   * @param c
   *          a {@link Constructor} object to be represented as an
   *          {@link Operation}.
   */
  @Override
  public void visit(Constructor<?> c) {
    assert c.getDeclaringClass().equals(classType.getRuntimeClass()) : "classType and declaring class should be same";

    TypeVariable<?>[] typeParams = c.getTypeParameters();

    if (typeParams.length > 0) {
      // is a generic constructor
    } else {
      // is a constructor possibly with generic arguments
    }

    if (classType.isGeneric()) {
      // goes in classtype->general-op map
    } else {
      // if generic constructor goes in type-params->generic-op map
    }
            
    List<GeneralType> paramTypes = new ArrayList<>();
    for (Type t : c.getGenericParameterTypes()) {
      paramTypes.add(GeneralType.forType(t));
    }

    // new ConstructorCall(c, new GeneralTypeTuple(paramTypes));
  }

  /**
   * Creates a {@link MethodCall} object for the {@link Method}.
   *
   * @param method
   *          a {@link Method} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Method method) {
    assert method.getDeclaringClass().equals(classType.getRuntimeClass()) : "classType and declaring class should be same";

    TypeVariable<?>[] typeParams = method.getTypeParameters();

    GeneralType retType = GeneralType.forType(method.getGenericReturnType());
    List<GeneralType> paramTypes = new ArrayList<>();
    if (! Modifier.isStatic(method.getModifiers() & Modifier.methodModifiers())) {
      paramTypes.add(classType);
    }
    for (Type t : method.getGenericParameterTypes()) {
      paramTypes.add(GeneralType.forType(t));
    }
    // new MethodCall(method, new GeneralTypeTuple(paramTypes), returnType);
  }

  /**
   * Adds the {@link Operation} objects corresponding to getters and setters
   * appropriate to the kind of field.
   *
   * @param field
   *          a {@link Field} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Field field) {
    assert field.getDeclaringClass().equals(classType.getRuntimeClass()) : "classType and declaring class of field should be the same";

    int mods = field.getModifiers();

    if (Modifier.isStatic(mods)) {
      if (Modifier.isFinal(mods)) {
        StaticFinalField s = new StaticFinalField(field);
        operations.add(new FieldGet(s));
      } else {
        StaticField s = new StaticField(field);
        operations.add(new FieldGet(s));
        operations.add(new FieldSet(s));
      }
    } else {
      if (Modifier.isFinal(mods)) {
        FinalInstanceField i = new FinalInstanceField(field);
        operations.add(new FieldGet(i));
      } else {
        InstanceField i = new InstanceField(field);
        operations.add(new FieldGet(i));
        operations.add(new FieldSet(i));
      }
    }
  }

  /**
   * Creates a {@link EnumConstant} object for the {@link Enum}.
   *
   * @param e
   *          an {@link Enum} object to be represented as an {@link Operation}.
   */
  @Override
  public void visit(Enum<?> e) {
    assert e.getDeclaringClass().equals(classType.getRuntimeClass()) : "classType and enum declaring class should be same";
    assert ! classType.isGeneric() : "type of enum class cannot be generic";
    operations.add(new EnumConstant(e, (ConcreteType)classType));
  }

  @Override
  public void visitBefore(Class<?> c) {
    if (c.getTypeParameters().length > 0) { // c is generic
      classType = new GenericClassType(c);
    } else {
      ConcreteType type = new ConcreteSimpleType(c);
      classTypes.add(type);
      classType = type;
    }

  }

  @Override
  public void visitAfter(Class<?> c) {
    // nothing to do here
  }
}
